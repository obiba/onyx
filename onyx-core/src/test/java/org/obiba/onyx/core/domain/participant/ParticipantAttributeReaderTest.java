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
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.etl.participant.impl.DefaultParticipantExcelReaderTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

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
    Assert.assertEquals(6, participantAttributes.size());
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
    Assert.assertEquals("Doctor First Name", participantAttributes.get(0).getName());
    Assert.assertEquals("Doctor Last Name", participantAttributes.get(1).getName());
    Assert.assertEquals("Special Needs", participantAttributes.get(2).getName());
    Assert.assertEquals("Family Member First Name", participantAttributes.get(3).getName());
    Assert.assertEquals("Family Member Last Name", participantAttributes.get(4).getName());
    Assert.assertEquals("Marital Status", participantAttributes.get(5).getName());
  }

  @Test
  public void testDefaultGroupContainsAllDefaultAttributes() {
    List<ParticipantAttribute> defaultGroupAttributes = participantAttributes.get(2).getGroup().getParticipantAttributes();
    Assert.assertEquals(2, defaultGroupAttributes.size());
    Assert.assertEquals("Special Needs", defaultGroupAttributes.get(0).getName());
    Assert.assertEquals("Marital Status", defaultGroupAttributes.get(1).getName());
  }

  @Test
  public void testAttributesInFamilyMemberGroupAreContiguous() {
    List<ParticipantAttribute> familyMemberGroupAttributes = participantAttributes.get(3).getGroup().getParticipantAttributes();
    Assert.assertEquals(2, familyMemberGroupAttributes.size());
    Assert.assertEquals(participantAttributes.get(3), familyMemberGroupAttributes.get(0));
    Assert.assertEquals(participantAttributes.get(4), familyMemberGroupAttributes.get(1));
  }

  @Test
  public void testAttributesInDoctorGroupAreContiguous() {
    List<ParticipantAttribute> doctorGroupAttributes = participantAttributes.get(0).getGroup().getParticipantAttributes();
    Assert.assertEquals(2, doctorGroupAttributes.size());
    Assert.assertEquals(participantAttributes.get(0), doctorGroupAttributes.get(0));
    Assert.assertEquals(participantAttributes.get(1), doctorGroupAttributes.get(1));
  }

  @Test
  public void testOldXmlFileWithoutGroups() throws IOException {
    String oldXmlFileDirectory = DefaultParticipantExcelReaderTest.class.getSimpleName();
    ParticipantAttributeReader attributeReader = new ParticipantAttributeReader();
    attributeReader.setResources(new Resource[] { new ClassPathResource(oldXmlFileDirectory + "/configured-attributes.xml") });
    List<ParticipantAttribute> participantAttributes = attributeReader.read();

    Group defaultGroup = participantAttributes.get(0).getGroup();
    int numberOfAttributesInDefaultGroup = defaultGroup.getParticipantAttributes().size();

    Assert.assertEquals("Expect all attributes to be part of the default group.", Group.DEFAULT_GROUP_NAME, defaultGroup.getName());
    Assert.assertEquals(participantAttributes.size(), numberOfAttributesInDefaultGroup);
  }

  private List<ParticipantAttribute> getParticipantAttributes() throws IOException {
    ParticipantAttributeReader attributeReader = new ParticipantAttributeReader();

    attributeReader.setResources(new Resource[] { new ClassPathResource(TEST_RESOURCES_DIR + "/participant-attributes.xml") });

    return attributeReader.read();
  }
}
