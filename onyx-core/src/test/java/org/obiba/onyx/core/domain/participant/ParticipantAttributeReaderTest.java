/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.participant;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.etl.participant.impl.ParticipantReaderTest;
import org.obiba.onyx.util.data.Data;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static junit.framework.Assert.assertEquals;

/**
 * Tests reading and and flattening a participant attribute xml file.
 */
public class ParticipantAttributeReaderTest {
  private static final String TEST_RESOURCES_DIR = ParticipantAttributeReaderTest.class.getSimpleName();

  private List<ParticipantAttribute> participantAttributes;

  @Before
  public void setUp() throws Exception {
    participantAttributes = getParticipantAttributes();
  }

  @Test
  public void testSizeOfAttributeList() {
    assertEquals(7, participantAttributes.size());
  }

  @Test
  public void testGroupExistsForEveryAttribute() {
    for(ParticipantAttribute participantAttribute : participantAttributes) {
      Assert.assertNotNull("Group is null.", participantAttribute.getGroup());
    }
  }

  @Test
  public void testThatDefaultEmptyGroupExists() {
    for(ParticipantAttribute participantAttribute : participantAttributes) {
      if(participantAttribute.getGroup().getName().equals(Group.DEFAULT_GROUP_NAME)) {
        Assert.assertTrue(true);
        return;
      }
    }
    Assert.assertFalse("At least one group was not a default group.", true);
  }

  @Test
  public void testAttributesInSameOrderAsXmlFile() {
    assertEquals("Doctor First Name", participantAttributes.get(0).getName());
    assertEquals("Doctor Last Name", participantAttributes.get(1).getName());
    assertEquals("Special Needs", participantAttributes.get(2).getName());
    assertEquals("Family Member First Name", participantAttributes.get(3).getName());
    assertEquals("Family Member Last Name", participantAttributes.get(4).getName());
    assertEquals("Marital Status", participantAttributes.get(5).getName());
  }

  @Test
  public void testDefaultGroupContainsAllDefaultAttributes() {
    List<ParticipantAttribute> defaultGroupAttributes = participantAttributes.get(2).getGroup().getParticipantAttributes();
    assertEquals(3, defaultGroupAttributes.size());
    assertEquals("Special Needs", defaultGroupAttributes.get(0).getName());
    assertEquals("Marital Status", defaultGroupAttributes.get(1).getName());
    assertEquals("Birth Date", defaultGroupAttributes.get(2).getName());
  }

  @Test
  public void testAttributesInFamilyMemberGroupAreContiguous() {
    List<ParticipantAttribute> familyMemberGroupAttributes = participantAttributes.get(3).getGroup().getParticipantAttributes();
    assertEquals(2, familyMemberGroupAttributes.size());
    assertEquals(participantAttributes.get(3), familyMemberGroupAttributes.get(0));
    assertEquals(participantAttributes.get(4), familyMemberGroupAttributes.get(1));
  }

  @Test
  public void testAttributesInDoctorGroupAreContiguous() {
    List<ParticipantAttribute> doctorGroupAttributes = participantAttributes.get(0).getGroup().getParticipantAttributes();
    assertEquals(2, doctorGroupAttributes.size());
    assertEquals(participantAttributes.get(0), doctorGroupAttributes.get(0));
    assertEquals(participantAttributes.get(1), doctorGroupAttributes.get(1));
  }

  @Test
  public void testOldXmlFileWithoutGroups() throws IOException {
    String oldXmlFileDirectory = ParticipantReaderTest.class.getSimpleName();
    ParticipantAttributeReader attributeReader = new ParticipantAttributeReader();
    attributeReader.setResources(new Resource[] { new ClassPathResource(oldXmlFileDirectory + "/configured-attributes.xml") });
    List<ParticipantAttribute> participantAttributes1 = attributeReader.read();

    Group defaultGroup = participantAttributes1.get(0).getGroup();
    int numberOfAttributesInDefaultGroup = defaultGroup.getParticipantAttributes().size();

    assertEquals("Expect all attributes to be part of the default group.", Group.DEFAULT_GROUP_NAME,
        defaultGroup.getName());
    assertEquals(participantAttributes1.size(), numberOfAttributesInDefaultGroup);
  }

  @Test
  public void testDefaultValues() {
    assertEquals("Default Doctor First Name", participantAttributes.get(0).getDefaultValues().get(0).getValue());
    Date date = participantAttributes.get(6).getDefaultValues().get(0).getValue();
    assertEquals("2010-01-01 00:00:00", Data.DATE_FORMAT.format(date));
  }

  private List<ParticipantAttribute> getParticipantAttributes() throws IOException {
    ParticipantAttributeReader attributeReader = new ParticipantAttributeReader();

    attributeReader.setResources(new Resource[] { new ClassPathResource(TEST_RESOURCES_DIR + "/participant-attributes.xml") });

    return attributeReader.read();
  }
}
