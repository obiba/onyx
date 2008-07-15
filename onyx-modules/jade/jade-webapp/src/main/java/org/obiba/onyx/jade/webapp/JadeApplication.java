package org.obiba.onyx.jade.webapp;

import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.obiba.onyx.jade.webapp.pages.home.HomePage;
import org.obiba.wicket.application.WebApplicationStartupListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JadeApplication extends SpringWebApplication {

  private final Logger log = LoggerFactory.getLogger(JadeApplication.class);

  @SuppressWarnings("unchecked")
  protected void init() {
    log.info("Initializing Jade Web Application ");
    super.init();

    super.addComponentInstantiationListener(new SpringComponentInjector(this, super.getSpringContextLocator().getSpringContext()));

    Map<String, WebApplicationStartupListener> startups = super.getSpringContextLocator().getSpringContext().getBeansOfType(WebApplicationStartupListener.class);
    if(startups != null) {
      for(Map.Entry<String, WebApplicationStartupListener> entry : startups.entrySet()) {
        log.info("Executing WebApplicationStartupListener named {} of type {}", entry.getKey(), entry.getValue().getClass().getSimpleName());
        try {
          entry.getValue().startup(this);
        } catch(RuntimeException e) {
          log.error("Error executing WebApplicationStartupListener named {} of type {}", entry.getKey(), entry.getValue().getClass().getSimpleName());
          log.error("Reported error : ", e);
        }
      }
    }
  }

  @Override
  public Class<?> getHomePage() {
    return HomePage.class;
  }

  public boolean isDevelopmentMode() {
    return Application.DEVELOPMENT.equalsIgnoreCase(getConfigurationType());
  }

}
