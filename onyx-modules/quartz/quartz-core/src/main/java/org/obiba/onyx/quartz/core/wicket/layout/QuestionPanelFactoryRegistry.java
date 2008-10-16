package org.obiba.onyx.quartz.core.wicket.layout;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestionPanelFactoryRegistry {

  private static final Logger log = LoggerFactory.getLogger(QuestionPanelFactoryRegistry.class);
  
  private Map<String, IQuestionPanelFactory> factories = Collections.synchronizedMap(new HashMap<String, IQuestionPanelFactory>());

  public void registerFactory(IQuestionPanelFactory factory) {
    log.info("Registering IQuestionPanelFactory {}", factory.getName());
    factories.put(factory.getName(), factory);
  }

  public void unregisterFactory(String name) {
    factories.remove(name);
  }
  
  public IQuestionPanelFactory getFactory(String name) {
    return factories.get(name);
  }

}
