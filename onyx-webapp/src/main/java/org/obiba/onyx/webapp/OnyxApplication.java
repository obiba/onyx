package org.obiba.onyx.webapp;

import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.markup.html.pages.AccessDeniedPage;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.lang.PackageName;
import org.obiba.onyx.core.service.UserService;
import org.obiba.onyx.webapp.authentication.UserRolesAuthorizer;
import org.obiba.onyx.webapp.config.page.ApplicationConfigurationPage;
import org.obiba.onyx.webapp.home.page.HomePage;
import org.obiba.onyx.webapp.login.page.LoginPage;
import org.obiba.onyx.webapp.participant.page.ParticipantSearchPage;
import org.obiba.onyx.webapp.stage.page.StagePage;
import org.obiba.wicket.application.WebApplicationStartupListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnyxApplication extends SpringWebApplication implements IUnauthorizedComponentInstantiationListener {

  private final Logger log = LoggerFactory.getLogger(OnyxApplication.class);

  private UserService userService;

  public UserService getUserService() {
    return userService;
  }

  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  protected void init() {
    log.info("Onyx Web Application is starting");
    super.init();

    super.addComponentInstantiationListener(new SpringComponentInjector(this, super.internalGetApplicationContext()));

    forEachListeners(new IListenerCallback() {
      public void handleListener(String beanName, WebApplicationStartupListener listener) {
        listener.startup(OnyxApplication.this);
      }

      public boolean terminateOnException() {
        return true;
      }
    });

    // nice urls
    mount("participant", PackageName.forClass(ParticipantSearchPage.class));
    mount("stage", PackageName.forClass(StagePage.class));

    getSecuritySettings().setAuthorizationStrategy(new RoleAuthorizationStrategy(new UserRolesAuthorizer()));
    getSecuritySettings().setUnauthorizedComponentInstantiationListener(this);

    log.info("Onyx Web Application has been started");
  }

  @Override
  protected void onDestroy() {
    log.info("Onyx Web Application is stoping");
    forEachListeners(new IListenerCallback() {
      public void handleListener(String beanName, WebApplicationStartupListener listener) {
        listener.shutdown(OnyxApplication.this);
      }

      public boolean terminateOnException() {
        return false;
      }
    });
    log.info("Onyx Web Application has been stoped");
    super.onDestroy();
  }

  @Override
  public Class<?> getHomePage() {
    if(userService.getUserCount() > 0) {
      if(OnyxAuthenticatedSession.get().isSignedIn()) {
        return HomePage.class;
      } else {
        return LoginPage.class;
      }
    } else {
      return ApplicationConfigurationPage.class;
    }
  }

  public boolean isDevelopmentMode() {
    return Application.DEVELOPMENT.equalsIgnoreCase(getConfigurationType());
  }

  /**
   * Finds implementations of {@link WebApplicationStartupListener} in Spring's Application Context and executes the
   * specified callback for each instance found.
   * @param callback the callback implementation to call for each listener instance
   */
  @SuppressWarnings("unchecked")
  private void forEachListeners(IListenerCallback callback) {
    Map<String, WebApplicationStartupListener> listeners = super.internalGetApplicationContext().getBeansOfType(WebApplicationStartupListener.class);
    if(listeners != null) {
      for(Map.Entry<String, WebApplicationStartupListener> entry : listeners.entrySet()) {
        log.info("Executing WebApplicationStartupListener named {} of type {}", entry.getKey(), entry.getValue().getClass().getSimpleName());
        try {
          callback.handleListener(entry.getKey(), entry.getValue());
        } catch(RuntimeException e) {
          log.error("Error executing WebApplicationStartupListener named {} of type {}", entry.getKey(), entry.getValue().getClass().getSimpleName());
          log.error("Reported error : ", e);
          if(callback.terminateOnException()) throw e;
        }
      }
    }
  }

  /**
   * Called for each WebApplicationStartupListener found in Spring's application context. Implementations should either
   * call startup or shutdown.
   */
  private interface IListenerCallback {
    public void handleListener(String beanName, WebApplicationStartupListener listener);

    public boolean terminateOnException();
  }

  @Override
  public Session newSession(Request request, Response response) {
    return new OnyxAuthenticatedSession(this, request);
  }

  public void onUnauthorizedInstantiation(Component component) {
    // If there is a sign in page class declared, and the unauthorized component is a page, but it's not the sign in
    // page
    if(component instanceof Page) {
      if(!OnyxAuthenticatedSession.get().isSignedIn()) {
        // Redirect to intercept page to let the user sign in
        throw new RestartResponseAtInterceptPageException(LoginPage.class);
      } else {
        // User is signed in but doesn't have the proper access rights. Display error and redirect accordingly.
        throw new RestartResponseAtInterceptPageException(AccessDeniedPage.class);
      }
    } else {
      // The component was not a page, so show an error message in the FeedbackPanel of the page
      component.error("You do not have sufficient privileges to see this component.");
      throw new UnauthorizedInstantiationException(component.getClass());
    }
  }

}
