/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.data;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDirectory;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class VariableDataSourceTest {

  private VariableDirectoryMock mockDirectory;

  private Map<String, Data> variableDatas;

  private Map<String, String> variableUnits;

  @Before
  public void setUp() {
    mockDirectory = new VariableDirectoryMock();

    variableDatas = new HashMap<String, Data>();
    variableDatas.put("/PARTICIPANT/FIRST_NAME", null);
    variableDatas.put("/PARTICIPANT/LAST_NAME", DataBuilder.buildText("Tremblay"));
    variableDatas.put("/PARTICIPANT/AGE", DataBuilder.buildInteger(46));
    variableDatas.put("/PARTICIPANT/WEIGHT_0", DataBuilder.buildDecimal(56.5));
    variableDatas.put("/PARTICIPANT/WEIGHT_1", DataBuilder.buildDecimal(65.8));

    variableUnits = new HashMap<String, String>();
    variableUnits.put("/PARTICIPANT/AGE", "years");
    variableUnits.put("/PARTICIPANT/WEIGHT", "kg");
  }

  @Test
  @Ignore
  public void testVariableDataSourceNoVariable() {
    Participant participant = createParticipant();
    VariableDataSource variableDataSource = new VariableDataSource("/PARTICIPANT/FULL_NAME");
    Data data = variableDataSource.getData(participant);
    Assert.assertNull(data);
  }

  @Test
  @Ignore
  public void testVariableDataSourceNoData() {
    Participant participant = createParticipant();
    VariableDataSource variableDataSource = new VariableDataSource("/PARTICIPANT/FIRST_NAME");
    Data data = variableDataSource.getData(participant);
    Assert.assertNull(data);
  }

  @Test
  @Ignore
  public void testVariableDataSourceDataNoUnit() {
    Participant participant = createParticipant();
    VariableDataSource variableDataSource = new VariableDataSource("/PARTICIPANT/LAST_NAME");

    Data data = variableDataSource.getData(participant);
    String unit = variableDataSource.getUnit();

    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals("Tremblay", data.getValue());
    Assert.assertNull(unit);
  }

  @Test
  @Ignore
  public void testVariableDataSourceDataWithUnit() {
    Participant participant = createParticipant();
    VariableDataSource variableDataSource = new VariableDataSource("/PARTICIPANT/AGE");

    Data data = variableDataSource.getData(participant);
    String unit = variableDataSource.getUnit();

    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals("46", data.getValueAsString());
    Assert.assertEquals("years", unit);
  }

  @Test
  @Ignore
  public void testVariableDataSourceMultipleData() {
    Participant participant = createParticipant();
    VariableDataSource variableDataSource = new VariableDataSource("/PARTICIPANT/WEIGHT");

    Data data = variableDataSource.getData(participant);
    String unit = variableDataSource.getUnit();

    VariableData var = mockDirectory.getVariableData(participant, "/PARTICIPANT/WEIGHT");
    Data example = var.getDatas().get(0);

    Assert.assertEquals(DataType.DECIMAL, data.getType());
    Assert.assertEquals(example.getValue(), data.getValue());
    Assert.assertEquals("kg", unit);
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

  private class VariableDirectoryMock extends VariableDirectory {

    @Override
    public Variable getVariable(String path) {
      for(String key : variableDatas.keySet()) {
        if(!key.contains(path) || variableDatas.get(key) == null) continue;
        Variable variable = new Variable(path);
        variable.setUnit(variableUnits.get(path));
        variable.setDataType(variableDatas.get(key).getType());
        return variable;
      }
      return null;
    }

    @Override
    public VariableData getVariableData(Participant participant, String path) {
      VariableData variableData = new VariableData(path);
      for(Map.Entry<String, Data> entry : variableDatas.entrySet()) {
        if(!entry.getKey().contains(path)) continue;
        variableData.addData(entry.getValue());
      }
      return variableData;
    }
  }
}
