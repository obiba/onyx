/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp;

import java.util.Map;
import java.util.Properties;

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
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.ISpringContextLocator;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.lang.PackageName;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.UserService;
import org.obiba.onyx.webapp.authentication.UserRolesAuthorizer;
import org.obiba.onyx.webapp.config.page.ApplicationConfigurationPage;
import org.obiba.onyx.webapp.home.page.HomePage;
import org.obiba.onyx.webapp.login.page.LoginPage;
import org.obiba.onyx.webapp.participant.page.ParticipantSearchPage;
import org.obiba.onyx.webapp.stage.page.StagePage;
import org.obiba.onyx.webapp.user.page.UserSearchPage;
import org.obiba.runtime.Version;
import org.obiba.wicket.application.ISpringWebApplication;
import org.obiba.wicket.application.WebApplicationStartupListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class OnyxApplication extends WebApplication implements ISpringWebApplication, IUnauthorizedComponentInstantiationListener {

  private final Logger log = LoggerFactory.getLogger(OnyxApplication.class);

  /**
   * Singleton instance of spring application context locator
   */
  private final static ISpringContextLocator contextLocator = new ISpringContextLocator() {

    private static final long serialVersionUID = 1L;

    public ApplicationContext getSpringContext() {
      Application app = Application.get();
      return ((OnyxApplication) app).internalGetApplicationContext();
    }
  };

  private XmlWebApplicationContext applicationContext;

  private UserService userService;

  private Version version;

  public UserService getUserService() {
    return userService;
  }

  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
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

  public ISpringContextLocator getSpringContextLocator() {
    return contextLocator;
  }

  @Override
  public Class<?> getHomePage() {
    User template = new User();
    template.setDeleted(false);

    if(userService.getUserCount(template) > 0) {
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

  protected final ApplicationContext internalGetApplicationContext() {
    return applicationContext;
  }

  protected void createApplicationContext() {
    try {
      PropertiesFactoryBean pfb = new PropertiesFactoryBean();
      pfb.setLocation(new ServletContextResource(getServletContext(), "WEB-INF/onyx.properties"));
      pfb.setSingleton(false);
      Properties onyxProperties = (Properties) pfb.getObject();

      String configPath = onyxProperties.getProperty("org.obiba.onyx.config.path");
      if(configPath == null) {
        throw new IllegalStateException("Onyx config path not set.");
      }

      PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
      configurer.setProperties(onyxProperties);
      // This must be set to true in order to let another PropertyPlaceholderConfigurer replace the unresolved entries.
      configurer.setIgnoreUnresolvablePlaceholders(true);

      applicationContext = new XmlWebApplicationContext();
      applicationContext.setServletContext(getServletContext());
      applicationContext.addBeanFactoryPostProcessor(configurer);
      applicationContext.setConfigLocation("WEB-INF/spring/context.xml," + configPath + "/*/module-context.xml");
      applicationContext.refresh();

      getServletContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);

      applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);

    } catch(Exception e) {
      throw new RuntimeException(e);
    }

  }

  protected void init() {
    log.info("Onyx Web Application is starting");
    super.init();

    createApplicationContext();

    super.addComponentInstantiationListener(new SpringComponentInjector(this, applicationContext));

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
    mount("user", PackageName.forClass(UserSearchPage.class));

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
    log.info("Destroying Spring ApplicationContext");
    applicationContext.destroy();
    log.info("Destroying Web Application sessions");
    getSessionStore().destroy();
    log.info("Onyx Web Application has been stopped");
    super.onDestroy();
  }

  /**
   * Finds implementations of {@link WebApplicationStartupListener} in Spring's Application Context and executes the
   * specified callback for each instance found.
   * @param callback the callback implementation to call for each listener instance
   */
  @SuppressWarnings("unchecked")
  private void forEachListeners(IListenerCallback callback) {
    Map<String, WebApplicationStartupListener> listeners = applicationContext.getBeansOfType(WebApplicationStartupListener.class);
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

}
