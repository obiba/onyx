/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantAttributeReader;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Unit tests for <code>DefaultParticipantExcelReader</code>.
 */
public class XmlParticipantReaderTest {
  //
  // Constants
  //

  private static final String TEST_RESOURCES_DIR = XmlParticipantReaderTest.class.getSimpleName();

  private static final String APPOINTMENTS_RESOURCES_DIR = "appointments";

  //
  // Instance Variables
  //

  private ParticipantMetadata participantMetadata;

  @Autowired(required = true)
  ExecutionContext context;

  private Map<String, String> columnNameToAttributeNameMap;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() throws Exception {
    initParticipantMetadata();
    initAttributeDefinition();
  }

  //
  // Test Methods
  //

  @Test
  public void testIsUpdateAvailableFalse() {

    try {
      XmlParticipantReader reader = createXmlParticipantReader(false, APPOINTMENTS_RESOURCES_DIR + "/inNoData");
      Assert.assertEquals(false, reader.isUpdateAvailable());
      reader = createXmlParticipantReader(false, APPOINTMENTS_RESOURCES_DIR + "/in");
      Assert.assertEquals(false, reader.isUpdateAvailable());

    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testIsUpdateAvailableTrue() {
    XmlParticipantReader reader = createXmlParticipantReader(false, APPOINTMENTS_RESOURCES_DIR + "/inXml");

    try {
      Assert.assertEquals(true, reader.isUpdateAvailable());
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Tests processing of an appointment list that contains no configured attributes (i.e., essential attributes only).
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithNoConfiguredAttributes() throws IOException {
    XmlParticipantReaderForTest reader = createXmlParticipantReaderForTest(false, TEST_RESOURCES_DIR + "/appointmentList_noConfiguredAttributes.xml");
    reader.setColumnNameToAttributeNameMap(columnNameToAttributeNameMap);
    reader.setParticipantMetadata(participantMetadata);

    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();

    try {
      while(reader.getIterator().hasNext()) {
        Participant participant = reader.read();
        if(participant != null) participants.add(participant);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    Assert.assertEquals(3, participants.size());
    Assert.assertEquals("Tremblay", participants.get(0).getLastName());
    Assert.assertEquals("Casserly", participants.get(2).getLastName());
  }

  /**
   * Tests processing of an appointment list where a tag is missing for a mandatory attribute (Birth Date). XmlReader
   * should not return an error as it is a validation done in the ParticipantProcessor (Management is different here
   * from the default ParticipantReader class).
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithMissingMandatoryAttributeTag() throws IOException {
    XmlParticipantReaderForTest reader = createXmlParticipantReaderForTest(false, TEST_RESOURCES_DIR + "/appointmentList_missingMandatoryAttributeTag.xml");
    reader.setColumnNameToAttributeNameMap(columnNameToAttributeNameMap);
    reader.setParticipantMetadata(participantMetadata);

    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();

    try {
      while(reader.getIterator().hasNext()) {
        Participant participant = reader.read();
        if(participant != null) participants.add(participant);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    Assert.assertEquals(3, participants.size());
    Assert.assertEquals("Tremblay", participants.get(0).getLastName());
    Assert.assertNull(participants.get(0).getBirthDate());
    Assert.assertEquals("Smith", participants.get(1).getLastName());
    Assert.assertNull(participants.get(1).getBirthDate());
    Assert.assertEquals("Casserly", participants.get(2).getLastName());
    Assert.assertNull(participants.get(2).getBirthDate());
  }

  /**
   * Tests processing of an appointment list where a mandatory attribute (Enrollment ID, at line 4) has not been
   * assigned a value. Reader should not return an error as it is a validation done in the ParticipantProcessor.
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithMissingMandatoryAttributeValue() throws IOException {
    XmlParticipantReaderForTest reader = createXmlParticipantReaderForTest(false, TEST_RESOURCES_DIR + "/appointmentList_missingMandatoryAttributeValue.xml");
    reader.setColumnNameToAttributeNameMap(columnNameToAttributeNameMap);
    reader.setParticipantMetadata(participantMetadata);

    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();

    try {
      while(reader.getIterator().hasNext()) {
        Participant participant = reader.read();
        if(participant != null) participants.add(participant);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    Assert.assertEquals(3, participants.size());
    Assert.assertEquals("Tremblay", participants.get(0).getLastName());
    Assert.assertEquals("100001", participants.get(0).getEnrollmentId());
    Assert.assertEquals("Smith", participants.get(1).getLastName());
    Assert.assertNull(participants.get(1).getEnrollmentId());
    Assert.assertEquals("Casserly", participants.get(2).getLastName());
    Assert.assertNull(participants.get(2).getEnrollmentId());
  }

  /**
   * Tests processing of an appointment list where an attribute (Appointment Time, line 5) has been assigned a value of
   * the wrong type.
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithWrongAttributeValueType() throws IOException {
    XmlParticipantReaderForTest reader = createXmlParticipantReaderForTest(false, TEST_RESOURCES_DIR + "/appointmentList_wrongAttributeValueType.xml");
    reader.setColumnNameToAttributeNameMap(columnNameToAttributeNameMap);
    reader.setParticipantMetadata(participantMetadata);

    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();

    try {
      while(reader.getIterator().hasNext()) {
        Participant participant = reader.read();
        if(participant != null) participants.add(participant);
      }
    } catch(Exception e) {
      String errorMessage = e.getMessage();
      Assert.assertEquals("Wrong data type value for field 'Appointment Time': TEST", errorMessage);
      return;
    }

    Assert.fail("Should have thrown an IllegalArgumentException");
  }

  /**
   * Tests processing of an appointment list where an attribute (Gender, line 3) has been assigned value that is not
   * allowed (i.e., is not with the attribute's "allowed value" list). Reader should not return an error as it is a
   * validation done in the ParticipantProcessor.
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithNotAllowedAttributeValue() throws IOException {
    XmlParticipantReaderForTest reader = createXmlParticipantReaderForTest(false, TEST_RESOURCES_DIR + "/appointmentList_notAllowedAttributeValue.xml");
    reader.setColumnNameToAttributeNameMap(columnNameToAttributeNameMap);
    reader.setParticipantMetadata(participantMetadata);

    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();

    try {
      while(reader.getIterator().hasNext()) {
        Participant participant = reader.read();
        if(participant != null) participants.add(participant);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    Assert.assertEquals(3, participants.size());
    Assert.assertEquals("Tremblay", participants.get(0).getLastName());
    Assert.assertNull(participants.get(0).getGender());
    Assert.assertEquals(Gender.MALE, participants.get(1).getGender());
    Assert.assertEquals("Casserly", participants.get(2).getLastName());
    Assert.assertEquals(Gender.FEMALE, participants.get(2).getGender());
  }

  /**
   * Tests processing of an appointment list that contains a duplicate attribute column (Last Name).
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithDuplicateAttributeTag() throws IOException {
    XmlParticipantReaderForTest reader = createXmlParticipantReaderForTest(false, TEST_RESOURCES_DIR + "/appointmentList_duplicateAttributeTag.xml");
    reader.setColumnNameToAttributeNameMap(columnNameToAttributeNameMap);
    reader.setParticipantMetadata(participantMetadata);

    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();

    try {
      while(reader.getIterator().hasNext()) {
        Participant participant = reader.read();
        if(participant != null) participants.add(participant);
      }
    } catch(Exception e) {
      String errorMessage = e.getMessage();
      Assert.assertEquals("Duplicate tag for field: NOM", errorMessage);
      return;
    }

    Assert.fail("Should have thrown an IllegalArgumentException");
  }

  @Test
  public void testProcessWithConfiguredAttributes() throws IOException {
    Calendar cal = Calendar.getInstance();

    cal.clear();
    cal.set(2008, 9 - 1, 1, 9, 0);
    final Date expectedAppointmentTime = cal.getTime();

    cal.clear();
    cal.set(1964, 10 - 1, 1);
    final Date expectedBirthDate = cal.getTime();

    initConfiguredAttributeDefinition();
    XmlParticipantReaderForTest reader = createXmlParticipantReaderForTest(true, TEST_RESOURCES_DIR + "/appointmentList_includesConfiguredAttributes.xml");
    reader.setColumnNameToAttributeNameMap(columnNameToAttributeNameMap);
    reader.setParticipantMetadata(participantMetadata);

    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();
    try {
      while(reader.getIterator().hasNext()) {
        Participant participant = reader.read();
        if(participant != null) participants.add(participant);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    Assert.assertEquals(1, participants.size());
    Assert.assertEquals(expectedAppointmentTime, participants.get(0).getAppointment().getDate());
    Assert.assertEquals("cag001", participants.get(0).getSiteNo());
    Assert.assertEquals("100001", participants.get(0).getEnrollmentId());
    Assert.assertEquals("Tremblay", participants.get(0).getLastName());
    Assert.assertEquals("Chantal", participants.get(0).getFirstName());
    Assert.assertEquals("F", (participants.get(0).getGender().equals(Gender.FEMALE) ? "F" : "M"));
    Assert.assertEquals(expectedBirthDate, participants.get(0).getBirthDate());

    // Verify that the participant's configured attributes have been assigned the correct values.
    Assert.assertEquals("299, Avenue des Pins Ouest", participants.get(0).getConfiguredAttributeValue("Street").getValue());
    Assert.assertEquals("Montr\u00e9al", participants.get(0).getConfiguredAttributeValue("City").getValue());
    Assert.assertEquals("QC", participants.get(0).getConfiguredAttributeValue("Province").getValue());
    Assert.assertEquals("Canada", participants.get(0).getConfiguredAttributeValue("Country").getValue());
    Assert.assertEquals("H1T 2M4", participants.get(0).getConfiguredAttributeValue("Postal Code").getValue());
    Assert.assertEquals("514-343-9898 ext 9494", participants.get(0).getConfiguredAttributeValue("Phone").getValue());
  }

  @Test
  public void testProcessWithRowContainingWhitespaceOnly() throws IOException {
    XmlParticipantReaderForTest reader = createXmlParticipantReaderForTest(false, TEST_RESOURCES_DIR + "/appointmentList_rowContainingWhitespaceOnly.xml");
    reader.setColumnNameToAttributeNameMap(columnNameToAttributeNameMap);
    reader.setParticipantMetadata(participantMetadata);

    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();

    try {
      while(reader.getIterator().hasNext()) {
        Participant participant = reader.read();
        if(participant != null) participants.add(participant);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    Assert.assertEquals(3, participants.size());
  }

  /**
   * Tests processing of an appointment list containing a duplicate enrollment id. Reader should not return an error as
   * it is a validation done in the ParticipantProcessor.
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithDuplicateDuplicateEnrollmentId() throws IOException {
    XmlParticipantReaderForTest reader = createXmlParticipantReaderForTest(false, TEST_RESOURCES_DIR + "/appointmentList_duplicateEnrollmentId.xml");
    reader.setColumnNameToAttributeNameMap(columnNameToAttributeNameMap);
    reader.setParticipantMetadata(participantMetadata);

    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();

    try {
      while(reader.getIterator().hasNext()) {
        Participant participant = reader.read();
        if(participant != null) participants.add(participant);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    Assert.assertEquals(3, participants.size());
    Assert.assertEquals("Tremblay", participants.get(0).getLastName());
    Assert.assertEquals("100001", participants.get(0).getEnrollmentId());
    Assert.assertEquals("Smith", participants.get(1).getLastName());
    Assert.assertEquals("100002", participants.get(1).getEnrollmentId());
    Assert.assertEquals("Casserly", participants.get(2).getLastName());
    Assert.assertEquals("100001", participants.get(2).getEnrollmentId());
  }

  //
  // Helper Methods
  //
  private XmlParticipantReader createXmlParticipantReader(boolean includeConfiguredAttributes, String resourcePath) {
    if(!includeConfiguredAttributes) {
      participantMetadata.setConfiguredAttributes(null);
    }

    XmlParticipantReader reader = new XmlParticipantReader();
    reader.setParticipantMetadata(participantMetadata);
    Resource[] resources = new Resource[] { new ClassPathResource(resourcePath) };
    reader.setInputDirectory(resources[0]);

    return reader;
  }

  private void initParticipantMetadata() throws IOException {
    participantMetadata = new ParticipantMetadata();

    ParticipantAttributeReader attributeReader = new ParticipantAttributeReader();

    attributeReader.setResources(new Resource[] { new ClassPathResource(TEST_RESOURCES_DIR + "/essential-attributes.xml") });
    List<ParticipantAttribute> essentialAttributes = attributeReader.read();
    participantMetadata.setEssentialAttributes(essentialAttributes);

    attributeReader.setResources(new Resource[] { new ClassPathResource(TEST_RESOURCES_DIR + "/configured-attributes.xml") });
    List<ParticipantAttribute> configuredAttributes = attributeReader.read();
    participantMetadata.setConfiguredAttributes(configuredAttributes);
  }

  private class XmlParticipantReaderForTest extends XmlParticipantReader {

    Resource resource;

    public XmlParticipantReaderForTest(Resource resource) {
      this.resource = resource;
    }

    @Override
    public void open(ExecutionContext context) throws ItemStreamException {
      try {
        setFileInputStream(new FileInputStream(resource.getFile()));
      } catch(IOException e) {

      }

      super.open(context);
    }
  }

  private XmlParticipantReaderForTest createXmlParticipantReaderForTest(boolean includeConfiguredAttributes, String resourcePath) {
    if(!includeConfiguredAttributes) {
      participantMetadata.setConfiguredAttributes(null);
    }
    Resource[] resources = new Resource[] { new ClassPathResource(resourcePath) };
    XmlParticipantReaderForTest reader = new XmlParticipantReaderForTest(resources[0]);

    return reader;
  }

  private void initAttributeDefinition() {
    columnNameToAttributeNameMap = new HashMap<String, String>();
    columnNameToAttributeNameMap.put("Code_participant", "Enrollment ID");
    columnNameToAttributeNameMap.put("Prenom", "First Name");
    columnNameToAttributeNameMap.put("Nom", "Last Name");
    columnNameToAttributeNameMap.put("Date_naissance", "Birth Date");
    columnNameToAttributeNameMap.put("Sexe", "Gender");
    columnNameToAttributeNameMap.put("Site", "Assessment Center ID");
    columnNameToAttributeNameMap.put("Date_heure_RDV", "Appointment Time");
  }

  private void initConfiguredAttributeDefinition() {
    if(columnNameToAttributeNameMap == null) columnNameToAttributeNameMap = new HashMap<String, String>();

    columnNameToAttributeNameMap.put("Rue", "Street");
    columnNameToAttributeNameMap.put("Ville", "City");
    columnNameToAttributeNameMap.put("Province", "Province");
    columnNameToAttributeNameMap.put("Pays", "Country");
    columnNameToAttributeNameMap.put("Code_postal", "Postal Code");
    columnNameToAttributeNameMap.put("Telephone", "Phone");
  }
}