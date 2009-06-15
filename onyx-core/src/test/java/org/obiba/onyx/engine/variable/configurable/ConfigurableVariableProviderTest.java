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

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.impl.DefaultVariablePathNamingStrategy;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

public class ConfigurableVariableProviderTest {

  private ConfigurableVariableProvider configurableVariableProvider;

  @Before
  public void setUp() throws Exception {
    IDataSource postalCodeDataSource = new FixedDataSource(new Data(DataType.TEXT, "M5G 2C1"));
    DataSourceVariable dataSourceVariable = new DataSourceVariable("PostalCode", DataType.TEXT, postalCodeDataSource);
    configurableVariableProvider = ConfigurableVariableProvider.getConfigurableVariableProvider(Arrays.asList(new DataSourceVariable[] { dataSourceVariable }));
  }

  @Test
  public void getVariablesTest() {
    List<Variable> variables = configurableVariableProvider.getVariables();
    Assert.assertEquals("Configured", variables.get(0).getName());
    Assert.assertEquals("PostalCode", variables.get(0).getVariables().get(0).getName());
  }

  @Test
  public void getVariableDataTest() {
    List<Variable> variables = configurableVariableProvider.getVariables();
    VariableData variableData = configurableVariableProvider.getVariableData(createParticipant(), variables.get(0).getVariables().get(0), new DefaultVariablePathNamingStrategy());
    List<Data> datas = variableData.getDatas();
    Assert.assertEquals("M5G 2C1", datas.get(0).getValue());
  }

  private Participant createParticipant() {
    Participant p = new Participant();
    p.setBarcode("1187432");
    p.setLastName("Tremblay");
    p.setFirstName("Patricia");
    p.setGender(Gender.FEMALE);
    Calendar c = Calendar.getInstance();
    c.set(1973, 5, 15);
    p.setBirthDate(c.getTime());
    return p;
  }
}
