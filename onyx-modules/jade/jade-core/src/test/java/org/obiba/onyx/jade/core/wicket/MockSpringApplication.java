package org.obiba.onyx.jade.core.wicket;

import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

/**
 * A configurable Wicket {@code WebApplication} implementation that can inject member variables annotated with
 * {@code @SpringBean}.
 * 
 */
public class MockSpringApplication extends SpringWebApplication {

  Class<?> homePage;

  @Override
  protected void init() {
    super.init();
    super.addComponentInstantiationListener(new SpringComponentInjector(this, internalGetApplicationContext()));
  }

  public void setHomePage(Class<?> homePage) {
    this.homePage = homePage;
  }

  @Override
  public Class<?> getHomePage() {
    return null;
  }

}
