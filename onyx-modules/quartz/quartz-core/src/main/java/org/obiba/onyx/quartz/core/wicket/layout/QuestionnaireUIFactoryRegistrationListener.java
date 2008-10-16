package org.obiba.onyx.quartz.core.wicket.layout;

import java.util.Map;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.wicket.application.WebApplicationStartupListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class QuestionnaireUIFactoryRegistrationListener implements WebApplicationStartupListener, ApplicationContextAware {

  private static final Logger log = LoggerFactory.getLogger(QuestionnaireUIFactoryRegistrationListener.class);

  private ApplicationContext applicationContext;

  private PageLayoutFactoryRegistry pageLayoutFactoryRegistry;

  private QuestionPanelFactoryRegistry questionPanelFactoryRegistry;

  @SuppressWarnings("unchecked")
  public void shutdown(WebApplication application) {
    Map<String, IPageLayoutFactory> pageLayoutFactories = applicationContext.getBeansOfType(IPageLayoutFactory.class);
    if(pageLayoutFactories != null) {
      for(IPageLayoutFactory factory : pageLayoutFactories.values()) {
        log.info("Unregistering IPageLayoutFactory '{}' of type {}", factory.getName(), factory.getClass().getSimpleName());
        pageLayoutFactoryRegistry.unregisterFactory(factory.getName());
      }
    }

    Map<String, IQuestionPanelFactory> questionPanelFactories = applicationContext.getBeansOfType(IQuestionPanelFactory.class);
    if(questionPanelFactories != null) {
      for(IQuestionPanelFactory factory : questionPanelFactories.values()) {
        log.info("Unregistering IQuestionPanelFactory '{}' of type {}", factory.getName(), factory.getClass().getSimpleName());
        questionPanelFactoryRegistry.unregisterFactory(factory.getName());
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void startup(WebApplication application) {
    Map<String, IPageLayoutFactory> pageLayoutFactories = applicationContext.getBeansOfType(IPageLayoutFactory.class);
    if(pageLayoutFactories != null) {
      for(IPageLayoutFactory factory : pageLayoutFactories.values()) {
        log.info("Registering IPageLayoutFactory '{}' of type {}", factory.getName(), factory.getClass().getSimpleName());
        pageLayoutFactoryRegistry.registerFactory(factory);
      }
    }

    Map<String, IQuestionPanelFactory> questionPanelFactories = applicationContext.getBeansOfType(IQuestionPanelFactory.class);
    if(questionPanelFactories != null) {
      for(IQuestionPanelFactory factory : questionPanelFactories.values()) {
        log.info("Registering IQuestionPanelFactory '{}' of type {}", factory.getName(), factory.getClass().getSimpleName());
        questionPanelFactoryRegistry.registerFactory(factory);
      }
    }

  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setPageLayoutFactoryRegistry(PageLayoutFactoryRegistry pageLayoutFactoryRegistry) {
    this.pageLayoutFactoryRegistry = pageLayoutFactoryRegistry;
  }

  public void setQuestionPanelFactoryRegistry(QuestionPanelFactoryRegistry questionPanelFactoryRegistry) {
    this.questionPanelFactoryRegistry = questionPanelFactoryRegistry;
  }

}
