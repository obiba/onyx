package org.obiba.onyx.quartz.core.wicket.layout;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageLayoutFactoryRegistry {

  private static final Logger log = LoggerFactory.getLogger(PageLayoutFactoryRegistry.class);
  
  private Map<String, IPageLayoutFactory> factories = Collections.synchronizedMap(new HashMap<String, IPageLayoutFactory>());

  public void registerFactory(IPageLayoutFactory factory) {
    log.info("Registering IPageLayoutFactory {}", factory.getName());
    factories.put(factory.getName(), factory);
  }

  public void unregisterFactory(String name) {
    factories.remove(name);
  }
  
  public IPageLayoutFactory getFactory(String name) {
    return factories.get(name);
  }

}
