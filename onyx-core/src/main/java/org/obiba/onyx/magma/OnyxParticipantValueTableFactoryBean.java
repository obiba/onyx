/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import java.util.HashSet;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.beans.BeanValueTable;
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.magma.spring.ValueTableFactoryBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class OnyxParticipantValueTableFactoryBean implements ValueTableFactoryBean {

  private String valueTableName;

  private VariableEntityProvider variableEntityProvider;

  @Autowired
  private Set<VariableValueSourceFactory> factories;

  @Autowired
  private Set<ValueSetBeanResolver> resolvers;

  @Autowired
  private CustomVariablesRegistry customVariablesRegistry;

  public void setValueTableName(String valueTableName) {
    this.valueTableName = valueTableName;
  }

  public String getValueTableName() {
    return valueTableName;
  }

  public void setVariableEntityProvider(VariableEntityProvider variableEntityProvider) {
    this.variableEntityProvider = variableEntityProvider;
  }

  public ValueTable buildValueTable(Datasource datasource) {
    BeanValueTable bvt = new BeanValueTable(datasource, getValueTableName(), variableEntityProvider);
    for(ValueSetBeanResolver resolver : resolvers) {
      bvt.addResolver(resolver);
    }

    // Construct a set including all factories AND an additional factory holding
    // custom variables in the "Participants" table.
    Set<VariableValueSourceFactory> factoriesWithCustomParticipantVariables = new HashSet<VariableValueSourceFactory>();
    factoriesWithCustomParticipantVariables.addAll(factories);
    factoriesWithCustomParticipantVariables.add((new PrebuiltVariableValueSourceFactory()).addVariableValueSources(customVariablesRegistry.getVariables("Participants")));

    for(VariableValueSourceFactory factory : factoriesWithCustomParticipantVariables) {
      bvt.addVariableValueSources(factory);
    }
    return bvt;
  }
}
