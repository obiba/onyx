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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.Attribute;
import org.obiba.onyx.core.domain.participant.Group;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLog;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.util.data.DataType;

import com.thoughtworks.xstream.XStream;

/**
 * Test XStream configuration needed to produce the experimental-conditions.xml file.
 */
public class ExperimentalConditionLogWriterTest {

  XStream xstream;

  InstrumentCalibration tanitaInstrumentCalibration;

  Attribute doctorFirstName;

  Attribute doctorLastName;

  Attribute specialNeeds;

  Attribute maritalStatus;

  Attribute familyMemberFirstName;

  Attribute familyMemberLastName;

  Group doctorGroup;

  Group familyMemberGroup;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    xstream = new XStream();
    xstream.alias("attribute", Attribute.class);

    tanitaInstrumentCalibration = new InstrumentCalibration();
    tanitaInstrumentCalibration.setName("TanitaCalibration");

    List<String> instructions = new ArrayList<String>();
    instructions.add("TanitaCalibration.Instruction1");
    instructions.add("TanitaCalibration.Instruction2");
    instructions.add("TanitaCalibration.Instruction3");
    instructions.add("TanitaCalibration.Instruction4");

    tanitaInstrumentCalibration.setInstructions(instructions);

    tanitaInstrumentCalibration.setInstrumentType("Impedance418");

    Attribute twentyKilo = new Attribute();
    twentyKilo.setName("20kg");
    twentyKilo.setType(DataType.INTEGER);
    twentyKilo.setUnit("kg");

    Attribute fourtyKilo = new Attribute();
    fourtyKilo.setName("40kg");
    fourtyKilo.setType(DataType.INTEGER);
    fourtyKilo.setUnit("kg");

    List<Attribute> attributes = new ArrayList<Attribute>(1);
    attributes.add(twentyKilo);
    attributes.add(fourtyKilo);

    tanitaInstrumentCalibration.setAttributes(attributes);

  }

  @Test
  public void testWriteXmlGroupsAndAttributesMixed() {
    List<ExperimentalConditionLog> attributeList = new ArrayList<ExperimentalConditionLog>();

    attributeList.add(tanitaInstrumentCalibration);

    xstream.processAnnotations(ExperimentalConditionLog.class);
    xstream.processAnnotations(InstrumentCalibration.class);

    String xml = xstream.toXML(attributeList);
    System.out.println(xml);
  }

  private Attribute createAttribute(String name) {
    Attribute attribute = new Attribute();
    attribute.setName(name);
    attribute.setType(DataType.TEXT);
    return attribute;
  }

}
