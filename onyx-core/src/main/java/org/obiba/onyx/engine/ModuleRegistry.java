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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry of all {@link Module} instances deployed in Onyx. {@link Stage} instances can also be obtained through
 * this class.
 */
public class ModuleRegistry {

  private static final Logger log = LoggerFactory.getLogger(ModuleRegistry.class);

  private Map<String, Module> modules = Collections.synchronizedMap(new HashMap<String, Module>());

  private Map<String, Stage> stages = Collections.synchronizedMap(new HashMap<String, Stage>());

  private Comparator<Stage> stageOrderingStrategy;

  public void registerModule(Module module) {
    log.info("Registering module '{}' of type {}", module.getName(), module.getClass().getName());
    modules.put(module.getName(), module);
    List<Stage> moduleStages = module.getStages();
    for(Stage stage : moduleStages) {
      log.info("Registering stage '{}' from module '{}'", stage.getName(), module.getName());
      Stage existingStage = this.stages.get(stage.getName());
      if(existingStage != null) {
        log.error("Unable to register stage '{}' from module '{}': a stage with the same name was already registered by module {}.", new Object[] { stage.getName(), module.getName(), existingStage.getModule() });
        throw new IllegalStateException("A stage already exists with the name " + stage.getName());
      }
      this.stages.put(stage.getName(), stage);
    }
  }

  public void unregisterModule(String name) {
    Module module = modules.remove(name);
    if(module != null) {
      List<Stage> moduleStages = module.getStages();
      for(Stage stage : moduleStages) {
        this.stages.remove(stage.getName());
      }
    }
  }

  public Module getModule(String name) {
    return modules.get(name);
  }

  public Collection<Module> getModules() {
    return modules.values();
  }

  public Stage getStage(String name) {
    return stages.get(name);
  }

  public void setStageOrderingStrategy(Comparator<Stage> stageOrderingStrategy) {
    this.stageOrderingStrategy = stageOrderingStrategy;
  }

  /**
   * Returns a list of registered {@link Stage} instance in order specified by the <code>stageOrderingStrategy</code>.
   * If no strategy is defined, ordering is unspecified.
   * @return an ordered List of Stage.
   */
  public List<Stage> listStages() {
    if(this.stageOrderingStrategy != null) {
      Set<Stage> set = new TreeSet<Stage>(this.stageOrderingStrategy);
      set.addAll(this.stages.values());
      return new ArrayList<Stage>(set);
    }
    return new ArrayList<Stage>(this.stages.values());
  }
}
