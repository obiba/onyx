package org.obiba.onyx.jade.webapp;

import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.obiba.onyx.jade.webapp.pages.home.HomePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JadeApplication extends SpringWebApplication {
  private final Logger log = LoggerFactory.getLogger(JadeApplication.class);
  protected void init() {
    log.info("Initializing Jade Web Application ");
    super.init();

    super.addComponentInstantiationListener(new SpringComponentInjector(this, super.getSpringContextLocator().getSpringContext()));
  }
  
  @Override
  public Class<?> getHomePage() {
    return HomePage.class;
  }

}
