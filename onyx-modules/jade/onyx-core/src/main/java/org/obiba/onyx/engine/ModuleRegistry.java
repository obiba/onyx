package org.obiba.onyx.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModuleRegistry {

  private Map<String, Module> modules = Collections.synchronizedMap(new HashMap<String, Module>());

  public void registerModule(Module module) {
    modules.put(module.getName(), module);
  }

  public void unregisterModule(String name) {
    modules.remove(name);
  }

  public Module getModule(String name) {
    return modules.get(name);
  }
  
  public Collection<Module> getModules() {
    return modules.values();
  }
}
