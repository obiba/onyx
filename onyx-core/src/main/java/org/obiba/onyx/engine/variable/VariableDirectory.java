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

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.util.VariableFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Directory of {@link Variable} and {@link IVariableProvider} for the whole system.
 */
public class VariableDirectory implements IVariableProvider {

  private static final Logger log = LoggerFactory.getLogger(VariableDirectory.class);

  private Map<String, IVariableProvider> variablePathToProvidersMap = Collections.synchronizedMap(new HashMap<String, IVariableProvider>());

  private Map<IVariableProvider, List<String>> providerToVariablePathsMap = Collections.synchronizedMap(new HashMap<IVariableProvider, List<String>>());

  private IVariablePathNamingStrategy variablePathNamingStrategy;

  private Variable root;

  public void setVariablePathNamingStrategy(IVariablePathNamingStrategy variablePathNamingStrategy) {
    this.variablePathNamingStrategy = variablePathNamingStrategy;
  }

  /**
   * Register all the variables from the provider and the provider itself.
   * @param provider
   */
  public void registerVariables(IVariableProvider provider) {
    if(provider == this) return;

    if(!providerToVariablePathsMap.containsKey(provider)) {
      log.info("Registering variables from provider {}", provider.getClass().getSimpleName());
      List<Variable> entities = provider.getVariables();
      if(entities != null) {
        for(Variable entity : entities) {
          getVariableRoot().addVariable(entity);
          for(Variable variable : getVariables(entity, new ArrayList<Variable>())) {
            if(variable.getDataType() != null) {
              String path = variablePathNamingStrategy.getPath(variable);
              if(variablePathToProvidersMap.containsKey(path)) {
                throw new IllegalArgumentException("Variable path " + path + " already registered by " + variablePathToProvidersMap.get(path).getClass().getSimpleName());
              }
              log.debug("Registering variable {} from provider {}", path, provider.getClass().getSimpleName());

              variablePathToProvidersMap.put(path, provider);

              if(!providerToVariablePathsMap.containsKey(provider)) {
                providerToVariablePathsMap.put(provider, new ArrayList<String>());
              }
              providerToVariablePathsMap.get(provider).add(path);

            }
          }
        }
      }
      log.info("Provider {} registered {} variables", provider.getClass().getSimpleName(), variablePathToProvidersMap.keySet().size());
    }
  }

  /**
   * Unregister a previously declared variable.
   * @param variablePath
   */
  public void unregisterVariable(String variablePath) {
    IVariableProvider provider = variablePathToProvidersMap.get(variablePath);
    if(provider != null) {
      variablePathToProvidersMap.remove(variablePath);
      providerToVariablePathsMap.get(provider).remove(variablePath);
      if(providerToVariablePathsMap.get(provider).size() == 0) {
        providerToVariablePathsMap.remove(provider);
      }
    }
  }

  /**
   * Get the root of the whole set of variables.
   * @return
   */
  public Variable getVariableRoot() {
    if(root == null) {
      root = new Variable(variablePathNamingStrategy.getRootName());
    }
    return root;
  }

  /**
   * Get data set from the given participant.
   * @param participant
   * @param filter
   * @return
   */
  public VariableDataSet getParticipantData(Participant participant, IVariableFilter filter) {
    VariableDataSet dataSet = new VariableDataSet();

    log.info("START participant.name={}", participant.getFullName());

    for(IVariableProvider provider : providerToVariablePathsMap.keySet()) {
      for(String path : providerToVariablePathsMap.get(provider)) {
        if(filter == null || filter.includeVariable(path)) {
          VariableData varData = getVariableData(participant, path);
          if(varData != null && (varData.getDatas().size() > 0 || varData.getVariableDatas().size() > 0)) {
            dataSet.addVariableData(varData);
          }
        }
      }
    }
    log.info("END participant.name={}", participant.getFullName());

    return dataSet;
  }

  /**
   * Get the participant's variable data for the variable at the given path.
   * @param participant
   * @param path
   * @return null if variable or variable provider not found
   */
  public VariableData getVariableData(Participant participant, String path) {
    Variable variable = VariableFinder.getInstance(getVariableRoot(), variablePathNamingStrategy).findVariable(path);
    if(variable == null) return null;

    IVariableProvider provider = variablePathToProvidersMap.get(path);
    if(provider == null) return null;

    return provider.getVariableData(participant, variable, variablePathNamingStrategy);
  }

  public List<Variable> getVariables() {
    List<Variable> entities = new ArrayList<Variable>();
    VariableFinder finder = VariableFinder.getInstance(getVariableRoot(), variablePathNamingStrategy);

    for(IVariableProvider provider : providerToVariablePathsMap.keySet()) {
      for(String path : providerToVariablePathsMap.get(provider)) {
        Variable variable = finder.findVariable(path);
        if(variable != null) {
          entities.add(variable);
        }
      }
    }

    return entities;
  }

  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    IVariableProvider provider = variablePathToProvidersMap.get(variablePathNamingStrategy.getPath(variable));
    if(provider == null) return null;

    return provider.getVariableData(participant, variable, variablePathNamingStrategy);
  }

  /**
   * Get recursively the children variable and it self, in a flat list.
   * @param parent
   * @param variables
   * @return
   */
  private List<Variable> getVariables(Variable parent, List<Variable> variables) {
    variables.add((Variable) parent);

    for(Variable child : parent.getVariables()) {
      getVariables(child, variables);
    }

    return variables;
  }
}
