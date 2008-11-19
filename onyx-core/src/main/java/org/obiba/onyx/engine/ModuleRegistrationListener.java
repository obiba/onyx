/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
 * Finds all {@link Module} instances in the Spring {@code ApplicationContext} and registers them in the
 * {@link ModuleRegistry}. The method {@link Module#initialize()} is called before registration. The method
 * {@link Module#shutdown()} is called after un-registration.
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

        // Unregister the module from the registry.
        try {
          registry.unregisterModule(module.getName());
        } catch(RuntimeException e) {
          // Report the problem, but keep going in order to unregister the other modules if possible
          log.error("An error occurred during module unregistration.", e);
        }

        // Shutdown the module
        try {
          log.info("Shuting down module '{}'", module.getName());
          module.shutdown();
        } catch(RuntimeException e) {
          log.error("Could not shutdown module '{}'", module.getName());
          log.error("Module shutdown failed due to the following exception.", e);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void startup(WebApplication application) {
    Map<String, Module> modules = applicationContext.getBeansOfType(Module.class);
    if(modules != null) {
      for(Module module : modules.values()) {

        try {
          log.info("Initializing module '{}' of type {}", module.getName(), module.getClass().getSimpleName());
          module.initialize();
        } catch(RuntimeException e) {
          log.error("Could not initialize module '{}'", module.getName());
          log.error("Module initialisation failed due to the following exception.", e);

          // Try to register the other modules anyway
          continue;
        }

        try {
          registry.registerModule(module);
        } catch(RuntimeException e) {
          try {
            module.shutdown();
          } catch(RuntimeException ignored) {
          }
        }
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
