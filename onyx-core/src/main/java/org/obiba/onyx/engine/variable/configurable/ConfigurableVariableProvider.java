/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.configurable;

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.IVariableProvider;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableHelper;

/**
 * Provides cohort specified variables. The variables provided are specified in an XML file and are based on existing
 * variables. Custom {@link IDataSource} object are used to extract or modify values from the existing variable to
 * create the new cohort specified variables.
 */
public class ConfigurableVariableProvider implements IVariableProvider {

  /** Name of parent variable containing all the configured variables. */
  public static final String CONFIGURED = "Configured";

  private final List<DataSourceVariable> dataSourceVariables;

  public ConfigurableVariableProvider(List<DataSourceVariable> dataSourceVariables) {
    this.dataSourceVariables = dataSourceVariables;
  }

  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    for(DataSourceVariable dataSourceVariable : dataSourceVariables) {
      if(dataSourceVariable.getVariable().getName().equals(variable.getName()) && dataSourceVariable.getParentPath().equals(variablePathNamingStrategy.getPath(variable.getParent()))) {
        VariableData variableData = new VariableData(variablePathNamingStrategy.getPath(variable), dataSourceVariable.getDataSource().getData(participant));
        return variableData;
      }
    }
    return null;
  }

  public List<Variable> getContributedVariables(Variable root, IVariablePathNamingStrategy variablePathNamingStrategy) {
    List<Variable> variables = new ArrayList<Variable>();

    for(DataSourceVariable dataSourceVariable : dataSourceVariables) {
      if(dataSourceVariable.getParentPath() != null) {
        List<String> names = variablePathNamingStrategy.getNormalizedNames(dataSourceVariable.getParentPath());
        if(names.size() < 2) {
          throw new IllegalArgumentException("At least 2 variable levels expected: " + dataSourceVariable.getParentPath());
        }
        String rootName = names.get(0);
        if(!root.getName().equals(rootName)) {
          throw new IllegalArgumentException("Wrong variable root name: '" + rootName + "' found, '" + root.getName() + "' expected.");
        }

        // find the parent from variable root
        Variable parent = root;
        for(int i = 1; i < names.size(); i++) {
          String childName = names.get(i);
          Variable child = parent.getVariable(childName);
          if(child == null) {
            child = parent.addVariable(new Variable(childName));
          }
          parent = child;
        }
        Variable variable = dataSourceVariable.getVariable();
        parent.addVariable(variable);
        VariableHelper.addSourceAttribute(variable, dataSourceVariable.getDataSource().toString());
        variables.add(variable);
      }
    }
    return variables;

  }

  public List<Variable> getVariables() {
    return null;
  }

}
