/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class VariableDirectory implements IVariableProvider {

  private static final Logger log = LoggerFactory.getLogger(VariableDirectory.class);

  private Map<String, IVariableProvider> variablePathToProvidersMap = Collections.synchronizedMap(new HashMap<String, IVariableProvider>());

  private Map<String, Variable> variablePathToVariableMap = Collections.synchronizedMap(new HashMap<String, Variable>());

  private List<IVariableProvider> providers = new ArrayList<IVariableProvider>();

  private IEntityPathNamingStrategy entityPathNamingStrategy;

  private Entity root;

  public void setEntityPathNamingStrategy(IEntityPathNamingStrategy entityPathNamingStrategy) {
    this.entityPathNamingStrategy = entityPathNamingStrategy;
  }

  public void registerVariables(IVariableProvider provider) {
    if(provider == this) return;

    if(root == null) {
      root = new Entity(entityPathNamingStrategy.getRootName());
    }

    if(!providers.contains(provider)) {
      log.info("registerVariables({})", provider.getClass().getSimpleName());
      providers.add(provider);
      for(Entity entity : provider.getVariables()) {
        root.addEntity(entity);
        for(Variable variable : getVariables(entity, new ArrayList<Variable>())) {
          String path = entityPathNamingStrategy.getPath(variable);
          if(variablePathToProvidersMap.containsKey(path)) {
            throw new IllegalArgumentException("Variable path " + path + "already registered by " + variablePathToProvidersMap.get(path).getClass().getSimpleName());
          }
          log.info("Registering variable {} from provider {}", path, provider.getClass().getSimpleName());
          variablePathToProvidersMap.put(path, provider);
          variablePathToVariableMap.put(path, variable);
        }
      }
    }
  }

  public void unregisterVariable(String variablePath) {
    variablePathToProvidersMap.remove(variablePath);
    variablePathToVariableMap.remove(variablePath);
  }

  private List<Variable> getVariables(Entity parent, List<Variable> variables) {
    if(parent instanceof Variable) {
      variables.add((Variable) parent);
    }
    for(Entity child : parent.getEntities()) {
      getVariables(child, variables);
    }
    return variables;
  }

  public List<Entity> getVariables() {
    List<Entity> entities = new ArrayList<Entity>();
    for(IVariableProvider provider : providers) {
      entities.addAll(provider.getVariables());
    }
    return entities;
  }

}
