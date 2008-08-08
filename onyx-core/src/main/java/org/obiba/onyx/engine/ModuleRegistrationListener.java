package org.obiba.onyx.engine;

import java.util.Map;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.wicket.application.WebApplicationStartupListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Get the {@link Module} from the Spring application context and (un)registrate them. 
 * @author Yannick Marcon
 *
 */
public class ModuleRegistrationListener implements WebApplicationStartupListener, ApplicationContextAware {

  private static final Logger log = LoggerFactory.getLogger(ModuleRegistrationListener.class);

  private ApplicationContext applicationContext;

  private ModuleRegistry registry;

  @SuppressWarnings("unchecked")
  public void shutdown(WebApplication application) {
    Map<String, Module> modules = applicationContext.getBeansOfType(Module.class);
    if(modules != null) {
      for(Module module : modules.values()) {
        log.info("Unresgitering module '{}' of type {}", module.getName(), module.getClass().getSimpleName());
        module.shutdown();
        registry.unregisterModule(module.getName());
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void startup(WebApplication application) {
    Map<String, Module> modules = applicationContext.getBeansOfType(Module.class);
    if(modules != null) {
      for(Module module : modules.values()) {
        log.info("Resgitering module '{}' of type {}", module.getName(), module.getClass().getSimpleName());
        module.initialize();
        registry.registerModule(module);
      }
    }
  }

  public void setModuleRegistry(ModuleRegistry registry) {
    this.registry = registry;
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}
