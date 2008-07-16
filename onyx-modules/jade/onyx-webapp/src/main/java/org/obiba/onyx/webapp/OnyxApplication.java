package org.obiba.onyx.webapp;

import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.obiba.onyx.webapp.page.home.HomePage;
import org.obiba.wicket.application.WebApplicationStartupListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnyxApplication extends SpringWebApplication {

  private final Logger log = LoggerFactory.getLogger(OnyxApplication.class);

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
    return HomePage.class;
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
}
