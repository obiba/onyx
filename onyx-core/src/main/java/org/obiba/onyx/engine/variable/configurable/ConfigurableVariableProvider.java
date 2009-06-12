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

/**
 * Provides cohort specified variables. The variables provided are specified in an XML file and are based on existing
 * variables. Custom {@link IDataSource} object are used to extract or modify values from the existing variable to
 * create the new cohort specified variables.
 */
public class ConfigurableVariableProvider implements IVariableProvider {

  private final List<DataSourceVariable> dataSourceVariables;

  private static ConfigurableVariableProvider configurableVariableProvider;

  private List<Variable> variables;

  private ConfigurableVariableProvider(List<DataSourceVariable> dataSourceVariables) {
    this.dataSourceVariables = dataSourceVariables;
  }

  /**
   * Obtain singleton instance of the ConfigurableVariableProvider.
   * @param dataSourceVariables Variables
   * @return A singleton instance of ConfigurableVariableProvider.
   */
  public static ConfigurableVariableProvider getConfigurableVariableProvider(List<DataSourceVariable> dataSourceVariables) {
    if(configurableVariableProvider == null) {
      configurableVariableProvider = new ConfigurableVariableProvider(dataSourceVariables);
    }
    return configurableVariableProvider;
  }

  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    for(DataSourceVariable dataSourceVariable : dataSourceVariables) {
      if(dataSourceVariable.getPath().equals(variable.getName())) {
        return dataSourceVariable.getVariableData(participant);
      }
    }
    return null;
  }

  public List<Variable> getVariables() {
    if(variables == null) {
      variables = new ArrayList<Variable>(dataSourceVariables.size());
      for(DataSourceVariable dataSourceVariable : dataSourceVariables) {
        Variable variable = new Variable(dataSourceVariable.getPath());

        variable.setDataType(dataSourceVariable.getDataType());
        variables.add(variable);
      }
    }
    return variables;
  }

}
