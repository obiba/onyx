package org.obiba.onyx.webapp.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.util.Base64;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWebServicePage extends WebPage {

  private static Logger log = LoggerFactory.getLogger(AbstractWebServicePage.class);

  private static final String BASIC_AUTH = "Basic";

  private static final String AUTH_HEADER = "Authorization";

  public static final String POST = "POST";

  public static final String PUT = "PUT";

  public static final String GET = "GET";

  public static final String DELETE = "DELETE";

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private int status = 400;

  public AbstractWebServicePage(PageParameters params) {
    super(params);
    setStatelessHint(true);
    signIn();
  }

  protected void signIn() {
    String authHeader = ((WebRequest) getRequest()).getHttpServletRequest().getHeader(AUTH_HEADER);
    if(authHeader == null) {
      setStatus(403);
    } else {
      log.trace("Authorization=" + authHeader);
      DecodedCredentials credentials = new DecodedCredentials(BASIC_AUTH, authHeader);
      OnyxAuthenticatedSession session = (OnyxAuthenticatedSession) getSession();

      Object errCode = session.authenticate(credentials.getUsername(), credentials.getPassword());
      if(errCode != null || !isAuthorized()) {
        setStatus(403);
      } else {
        setSessionTimeout();
      }
    }
  }

  private void setSessionTimeout() {
    ApplicationConfiguration appConfig = queryService.matchOne(new ApplicationConfiguration());

    if(appConfig != null) {
      Integer sessionTimeoutInMinutes = appConfig.getSessionTimeout();

      if(sessionTimeoutInMinutes != null) {
        getHttpServletRequest().getSession().setMaxInactiveInterval(sessionTimeoutInMinutes * 60);
      }
    }
  }

  /**
   * To be overridden for specific access restrictions.
   *
   * @return
   */
  protected boolean isAuthorized() {
    return OnyxAuthenticatedSession.get().isSignedIn();
  }

  protected boolean hasRole(Role role) {
    return userSessionService.getRoles().contains(role);
  }

  @Override
  protected void onBeforeRender() {
    super.onBeforeRender();
    if(status == 403) {
      doDefault();
    } else {
      log.trace("QueryString: {}", getHttpServletRequest().getQueryString());
      String method = getHttpServletRequest().getMethod();
      try {
        processRequest(method);
      } catch(Exception e) {
        processError(e);
      }
    }
  }

  private HttpServletRequest getHttpServletRequest() {
    return ((WebRequest) getRequest()).getHttpServletRequest();
  }

  private void processRequest(String method) {
    if(POST.equals(method)) {
      setModelFromBody(getRequestBody());
      doPost(getPageParameters());
    } else if(GET.equals(method)) {
      doGet(getPageParameters());
    } else if(PUT.equals(method)) {
      setModelFromBody(getRequestBody());
      doPut(getPageParameters());
    } else if(DELETE.equals(method)) {
      doDelete(getPageParameters());
    }
  }

  protected void processError(Exception e) {
    HttpServletRequest rq = getHttpServletRequest();
    log.error(rq.getMethod() + " " + rq.getRequestURI(), e);
    setStatus(500);
    setDefaultModel(new Model<ErrorMessage>(new ErrorMessage(e)));
  }

  @Override
  public final boolean hasAssociatedMarkup() {
    return false;
  }

  protected int getStatus() {
    return status;
  }

  protected void setStatus(int status) {
    this.status = status;
  }

  /**
   * Use this method for lookups in the service implementation class
   *
   * @param params
   */
  public void doGet(PageParameters params) {
    doDefault();
  }

  /**
   * Use this method for updates in the service implementation class
   *
   * @param params
   */
  public void doPost(PageParameters params) {
    doDefault();
  }

  /**
   * Use this method for creates in the service implementation class
   *
   * @param params
   */
  public void doPut(PageParameters params) {
    doDefault();
  }

  /**
   * Use this method for deletes in the service implementation class
   *
   * @param params
   */
  public void doDelete(PageParameters params) {
    doDefault();
  }

  /**
   * @param body
   */
  protected abstract void setModelFromBody(String body);

  private void doDefault() {
    if(status == 403) {
      setDefaultModel(new Model<ErrorMessage>(new ErrorMessage("Forbidden")));
    } else {
      setDefaultModel(new Model<ErrorMessage>(new ErrorMessage("Unsupported operation")));
    }
  }

  private String getRequestBody() {
    HttpServletRequest request = ((WebRequest) getRequest()).getHttpServletRequest();
    StringBuilder stringBuilder = new StringBuilder();
    BufferedReader bufferedReader = null;
    try {
      InputStream inputStream = request.getInputStream();
      if(inputStream != null) {
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        char[] charBuffer = new char[128];
        int bytesRead = -1;
        while((bytesRead = bufferedReader.read(charBuffer)) > 0) {
          stringBuilder.append(charBuffer, 0, bytesRead);
        }
      } else {
        stringBuilder.append("");
      }
    } catch(IOException ex) {
      setDefaultModelObject(ex.getMessage());
    } finally {
      if(bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch(IOException ex) {
          setDefaultModelObject(ex.getMessage());
        }
      }
    }
    String body = stringBuilder.toString();
    log.debug("Message Body:\n---------\n" + body);
    return body;

  }

  @Override
  public Component add(IBehavior... arg0) {
    throw new UnsupportedOperationException("WebServicePage does not support IBehaviours");
  }

  public static class ErrorMessage implements Serializable {
    private String exception;

    private String message;

    public ErrorMessage(Exception e) {
      exception = e.getClass().getName();
      message = e.getMessage();
      Throwable cause = e.getCause();
      while (cause != null) {
        exception = cause.getClass().getName();
        message = cause.getMessage();
        cause = cause.getCause();
      }
    }

    public ErrorMessage(String message) {
      this.message = message;
    }
  }

  private static class DecodedCredentials {

    private final String username;

    private final String password;

    DecodedCredentials(String scheme, String authorization) {
      // Scheme <token>
      String schemeAndToken[] = authorization.split(" ", 2);

      if(!scheme.equals(schemeAndToken[0])) {
        throw new IllegalArgumentException("Unsupported Authorization scheme: " + schemeAndToken[0]);
      }

      String decoded[] = new String(Base64.decode(schemeAndToken[1])).split(":", 2);
      username = decoded[0];
      password = decoded[1];
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }
  }

}