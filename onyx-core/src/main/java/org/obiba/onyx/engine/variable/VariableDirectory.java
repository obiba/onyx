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

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Participant;
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

  private IVariablePathNamingStrategy variablePathNamingStrategy;

  private EntityQueryService queryService;

  private Variable root;

  public void setVariablePathNamingStrategy(IVariablePathNamingStrategy variablePathNamingStrategy) {
    this.variablePathNamingStrategy = variablePathNamingStrategy;
  }

  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }

  public void registerVariables(IVariableProvider provider) {
    if(provider == this) return;

    if(root == null) {
      root = new Variable(variablePathNamingStrategy.getRootName());
    }

    if(!providers.contains(provider)) {
      log.info("Registering variables from provider {}", provider.getClass().getSimpleName());
      providers.add(provider);
      int variableCount = 0;
      List<Variable> entities = provider.getVariables();
      if(entities != null) {
        for(Variable entity : entities) {
          root.addVariable(entity);
          for(Variable variable : getVariables(entity, new ArrayList<Variable>())) {
            if(variable.getDataType() != null) {
              String path = variablePathNamingStrategy.getPath(variable);
              if(variablePathToProvidersMap.containsKey(path)) {
                throw new IllegalArgumentException("Variable path " + path + " already registered by " + variablePathToProvidersMap.get(path).getClass().getSimpleName());
              }
              log.debug("Registering variable {} from provider {}", path, provider.getClass().getSimpleName());
              variableCount++;
              variablePathToProvidersMap.put(path, provider);
              variablePathToVariableMap.put(path, variable);
            }
          }
        }
      }
      log.info("Provider {} registered {} variables", provider.getClass().getSimpleName(), variableCount);
    }
  }

  public void unregisterVariable(String variablePath) {
    variablePathToProvidersMap.remove(variablePath);
    variablePathToVariableMap.remove(variablePath);
  }

  /**
   * Get the root of the whole set of variables.
   * @return
   */
  public Variable getVariableRoot() {
    return root;
  }

  public List<VariableDataSet> getParticipantData() {
    List<VariableDataSet> dataSets = new ArrayList<VariableDataSet>();

    Participant template = new Participant();
    template.setExported(false);
    for(Participant participant : queryService.match(template)) {
      log.info("participant.name={}", participant.getFullName());
      VariableDataSet dataSet = new VariableDataSet();
      for(String path : variablePathToVariableMap.keySet()) {
        // log.info("path={}", path);
        VariableData varData = getVariableData(participant, path);
        if(varData != null && varData.getDatas().size() > 0) {
          dataSet.addVariableData(varData);
        }
      }
      if(dataSet.getVariableDatas().size() > 0) {
        dataSets.add(dataSet);
      }
    }

    return dataSets;
  }

  private List<Variable> getVariables(Variable parent, List<Variable> variables) {
    if(parent instanceof Variable) {
      variables.add((Variable) parent);
    }
    for(Variable child : parent.getVariables()) {
      getVariables(child, variables);
    }
    return variables;
  }

  public List<Variable> getVariables() {
    List<Variable> entities = new ArrayList<Variable>();
    for(IVariableProvider provider : providers) {
      entities.addAll(provider.getVariables());
    }
    return entities;
  }

  public Variable getVariable(String path) {
    return variablePathToVariableMap.get(path);
  }

  public VariableData getVariableData(Participant participant, String path) {
    Variable variable = getVariable(path);
    IVariableProvider provider = variablePathToProvidersMap.get(path);
    if(provider == null) return null;

    return provider.getVariableData(participant, variable, variablePathNamingStrategy);
  }

  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    IVariableProvider provider = variablePathToProvidersMap.get(variablePathNamingStrategy.getPath(variable));
    if(provider == null) return null;

    return provider.getVariableData(participant, variable, variablePathNamingStrategy);
  }

}
