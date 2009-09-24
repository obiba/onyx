/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.workstation;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.Attribute;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLog;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLogReader;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Tests reading
 */
public class ExperimentalConditionLogReaderTest {
  private static final String TEST_RESOURCES_DIR = ExperimentalConditionLogReaderTest.class.getSimpleName();

  private List<ExperimentalConditionLog> experimentalConditionLogs;

  @Before
  public void setUp() throws Exception {
    experimentalConditionLogs = getAttributes();
  }

  @Test
  public void testNumberOfExperimentalConditionLogs() {
    Assert.assertEquals(2, experimentalConditionLogs.size());
  }

  @Test
  public void testRoomCharacteristicsExist() throws Exception {
    for(ExperimentalConditionLog log : experimentalConditionLogs) {
      if(log.getName().equals("RoomCharacteristics")) {
        Assert.assertTrue(true);
        return;
      }
    }
    Assert.assertFalse("RoomCharacteristics not found", true);
  }

  @Test
  public void testRoomCharacteristicsHasTwoAttributes() throws Exception {
    ExperimentalConditionLog log = getExperimentalConditionLog("RoomCharacteristics");
    Assert.assertEquals(2, log.getAttributes().size());
  }

  @Test
  public void testRoomCharacteristicsTemperatureAttributeExists() throws Exception {
    Assert.assertNotNull(getAttribute("RoomCharacteristics", "Temperature"));
  }

  @Test
  public void testTanitaCalibrationInstrumentName() throws Exception {
    InstrumentCalibration instrumentCalibration = getInstrumentCalibration("TanitaCalibration");
    Assert.assertEquals("Impedance418", instrumentCalibration.getInstrumentType());
  }

  @Test
  public void testNumberOfInstructionsForTanitaCalibration() throws Exception {
    InstrumentCalibration instrumentCalibration = getInstrumentCalibration("TanitaCalibration");
    Assert.assertEquals(4, instrumentCalibration.getInstructions().size());

  }

  @Test
  public void testTanitaCalibrationHasTwoAttributes() throws Exception {
    InstrumentCalibration instrumentCalibration = getInstrumentCalibration("TanitaCalibration");
    Assert.assertEquals(3, instrumentCalibration.getAttributes().size());
  }

  @Test
  public void testTanitaCalibrationHasAllowedValues() throws Exception {
    Attribute attribute = getAttribute("TanitaCalibration", "ChoiceExample");
    Assert.assertEquals(3, attribute.getAllowedValues().size());
  }

  private ExperimentalConditionLog getExperimentalConditionLog(String experimentalConditionLogName) {
    for(ExperimentalConditionLog log : experimentalConditionLogs) {
      if(log.getName().equals(experimentalConditionLogName)) {
        return log;
      }
    }
    throw new IllegalStateException("The ExperimentalConditionLog [" + experimentalConditionLogName + "] does not exist.");
  }

  private Attribute getAttribute(String experimentalConditionLogName, String attributeName) {
    ExperimentalConditionLog log = getExperimentalConditionLog(experimentalConditionLogName);
    for(Attribute attribute : log.getAttributes()) {
      if(attribute.getName().equals(attributeName)) {
        return attribute;
      }
    }
    throw new IllegalStateException("The Attribute [" + attributeName + "] does not exist.");
  }

  private InstrumentCalibration getInstrumentCalibration(String instrumentCalibrationName) {
    ExperimentalConditionLog log = getExperimentalConditionLog(instrumentCalibrationName);
    if(log instanceof InstrumentCalibration) {
      return (InstrumentCalibration) log;
    }
    throw new IllegalStateException("The InstrumentCalibration [" + instrumentCalibrationName + "] does not exist.");
  }

  private List<ExperimentalConditionLog> getAttributes() throws IOException {
    ExperimentalConditionLogReader experimentalConditionLogReader = new ExperimentalConditionLogReader();

    System.out.println(TEST_RESOURCES_DIR + "/experimental-conditions.xml");
    experimentalConditionLogReader.setResources(new Resource[] { new ClassPathResource(TEST_RESOURCES_DIR + "/experimental-conditions.xml") });

    return experimentalConditionLogReader.read();

  }
}
