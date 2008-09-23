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

  public void registerModule(Module module) {
    log.info("Registeting module {}", module.getName());
    modules.put(module.getName(), module);
    List<Stage> moduleStages = module.getStages();
    for(Stage stage : moduleStages) {
      log.info("Registeting stage {} from module {}", stage.getName(), module.getName());
      Stage existingStage = this.stages.get(stage.getName());
      if(existingStage != null) {
        log.error("Unable to register stage {} from module {}: a stage with the same name was already registered by module {}.", new Object[] { stage.getName(), module.getName(), existingStage.getModule() });
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

  /**
   * Returns a list of registered {@link Stage} instance in order of their <code>displayOrder</code> property.
   * @return an ordered List of Stage.
   */
  public List<Stage> listStages() {
    Set<Stage> set = new TreeSet<Stage>(new Comparator<Stage>() {
      public int compare(Stage o1, Stage o2) {
        return o1.getDisplayOrder().compareTo(o2.getDisplayOrder());
      }
    });
    set.addAll(stages.values());
    return new ArrayList<Stage>(set);
  }
}
