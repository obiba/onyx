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

import static org.easymock.EasyMock.createMock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import org.obiba.onyx.core.service.ParticipantService;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Unit tests for <code>DefaultParticipantExcelReader</code>.
 */
public class ParticipantReaderTest {
  //
  // Constants
  //

  private static final String TEST_RESOURCES_DIR = ParticipantReaderTest.class.getSimpleName();

  private static final String APPOINTMENTS_RESOURCES_DIR = "appointments";

  //
  // Instance Variables
  //

  private ParticipantMetadata participantMetadata;

  // private List<IParticipantReadListener> listeners = new ArrayList<IParticipantReadListener>();

  private ParticipantService participantServiceMock;

  @Autowired(required = true)
  ExecutionContext context;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() throws Exception {
    participantServiceMock = createMock(ParticipantService.class);
    // listeners.add(new UpdateParticipantListener("cag001", getUser(), participantServiceMock));
    initParticipantMetadata();
  }

  //
  // Test Methods
  //

  @Test
  public void testIsUpdateAvailableFalse() {
    ParticipantReader reader = createParticipantReader(1, 2, 3, false, APPOINTMENTS_RESOURCES_DIR + "/inNoData");

    try {
      Assert.assertEquals(false, reader.isUpdateAvailable());
    } catch(IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testIsUpdateAvailableTrue() {
    ParticipantReader reader = createParticipantReader(1, 2, 3, false, APPOINTMENTS_RESOURCES_DIR + "/in");

    try {
      Assert.assertEquals(true, reader.isUpdateAvailable());
    } catch(IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testSortFilesOnDateAsc() {
    ParticipantReader reader = createParticipantReader(1, 2, 3, false, APPOINTMENTS_RESOURCES_DIR + "/in");

    try {
      File[] appointmentFiles = reader.getInputDirectory().getFile().listFiles(reader.getFilter());
      long now = System.currentTimeMillis();

      // Set the file timestamps of the appointment files, spaced at one-minute intervals.
      // Explicitly setting the timestamps prevents the test from failing if the files'
      // timestamps are modified when checked into source control.
      for(int i = 0; i < appointmentFiles.length; i++) {
        appointmentFiles[i].setLastModified(now - 1000 * 60 * i);
      }

      File[] sortedAppointmentFiles = new File[appointmentFiles.length];
      System.arraycopy(appointmentFiles, 0, sortedAppointmentFiles, 0, appointmentFiles.length);
      reader.sortFilesOnDateAsc(sortedAppointmentFiles);

      // Verify that the appointment files are sorted by date, in ascending order (earliest first).
      for(int i = 0; i < appointmentFiles.length; i++) {
        Assert.assertEquals(appointmentFiles[appointmentFiles.length - i - 1].getName(), sortedAppointmentFiles[i].getName());
      }
    } catch(IOException e) {
      Assert.fail(e.getMessage());
    }

  }

  /**
   * Tests processing of an appointment list that contains no configured attributes (i.e., essential attributes only).
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithNoConfiguredAttributes() throws IOException {
    participantMetadata.setConfiguredAttributes(null);
    ParticipantReaderForTest reader = createParticipantReaderForTest(1, 2, 3, false, TEST_RESOURCES_DIR + "/appointmentList_noConfiguredAttributes.xls");
    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();

    try {
      while(reader.getRow() != null) {
        participants.add(reader.read());
      }
    } catch(Exception e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertEquals(3, participants.size());
    Assert.assertEquals("Tremblay", participants.get(0).getLastName());
    Assert.assertEquals("Casserly", participants.get(2).getLastName());
  }

  /**
   * Tests processing of an appointment list with columns in reverse order (i.e., reverse of order used in the sheet
   * processed by <code>testProcessWithNoConfiguredAttributes</code>).
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithColumnsInReverseOrder() throws IOException {
    ParticipantReaderForTest reader = createParticipantReaderForTest(1, 2, 3, false, TEST_RESOURCES_DIR + "/appointmentList_columnsInReverseOrder.xls");
    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();

    try {
      while(reader.getRow() != null) {
        participants.add(reader.read());
      }
    } catch(Exception e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertEquals(3, participants.size());
    Assert.assertEquals("Tremblay", participants.get(0).getLastName());
    Assert.assertEquals("100001", participants.get(0).getEnrollmentId());
    Assert.assertEquals("Casserly", participants.get(2).getLastName());
    Assert.assertEquals("100003", participants.get(2).getEnrollmentId());

  }

  /**
   * Tests processing of an appointment list with the sheet, header row and first data row configured differently.
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithDifferentHeaderRowAndFirstDataRow() throws IOException {
    ParticipantReaderForTest reader = createParticipantReaderForTest(2, 3, 5, false, TEST_RESOURCES_DIR + "/appointmentList_differentSheetAndHeaderRowAndFirstDataRow.xls");
    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();

    try {
      while(reader.getRow() != null) {
        participants.add(reader.read());
      }
    } catch(Exception e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertEquals(3, participants.size());
    Assert.assertEquals("Tremblay", participants.get(0).getLastName());
    Assert.assertEquals("100001", participants.get(0).getEnrollmentId());
    Assert.assertEquals("Casserly", participants.get(2).getLastName());
    Assert.assertEquals("100003", participants.get(2).getEnrollmentId());
  }

  /**
   * Tests processing of an appointment list where the column names do not match the corresponding attribute names
   * specified in the participant meta-data.
   * 
   * The meta-data include the necessary column name / attribute name map.
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithDifferentColumnNames() throws IOException {
    ParticipantReaderForTest reader = createParticipantReaderForTest(1, 2, 3, false, TEST_RESOURCES_DIR + "/appointmentList_differentColumnNames.xls");

    Map<String, String> columnNameToAttributeNameMap = new HashMap<String, String>();
    columnNameToAttributeNameMap.put("enrollmentId", "Enrollment ID");
    columnNameToAttributeNameMap.put("firstName", "First Name");
    columnNameToAttributeNameMap.put("lastName", "Last Name");
    columnNameToAttributeNameMap.put("birthDate", "Birth Date");
    columnNameToAttributeNameMap.put("gender", "Gender");
    columnNameToAttributeNameMap.put("assessmentCenterId", "Assessment Center ID");
    columnNameToAttributeNameMap.put("appointmentTime", "Appointment Time");

    reader.setColumnNameToAttributeNameMap(columnNameToAttributeNameMap);

    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();

    try {
      while(reader.getRow() != null) {
        participants.add(reader.read());
      }
    } catch(Exception e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertEquals(3, participants.size());
    Assert.assertEquals("Tremblay", participants.get(0).getLastName());
    Assert.assertEquals("100001", participants.get(0).getEnrollmentId());
    Assert.assertEquals("Casserly", participants.get(2).getLastName());
    Assert.assertEquals("100003", participants.get(2).getEnrollmentId());
  }

  /**
   * Tests processing of an appointment list when some (but not ALL) column names do not match the corresponding
   * attribute names specified in the participant meta-data).
   * 
   * The meta-data include the (partial) column name / attribute name map.
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithSomeDifferentColumnNames() throws IOException {
    ParticipantReaderForTest reader = createParticipantReaderForTest(1, 2, 3, false, TEST_RESOURCES_DIR + "/appointmentList_someDifferentColumnNames.xls");

    Map<String, String> columnNameToAttributeNameMap = new HashMap<String, String>();
    columnNameToAttributeNameMap.put("DOB", "Birth Date");
    columnNameToAttributeNameMap.put("Sex", "Gender");

    reader.setColumnNameToAttributeNameMap(columnNameToAttributeNameMap);

    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();

    try {
      while(reader.getRow() != null) {
        participants.add(reader.read());
      }
    } catch(Exception e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertEquals(3, participants.size());
    Assert.assertEquals("Tremblay", participants.get(0).getLastName());
    Assert.assertEquals(Gender.MALE, participants.get(1).getGender());
    Assert.assertEquals("Casserly", participants.get(2).getLastName());
    Assert.assertEquals(Gender.FEMALE, participants.get(2).getGender());
  }

  /**
   * Tests processing of an appointment list when an empty column name / attribute name map is used.
   * 
   * @throws IOException if the appointment list could not be read
   */
  // @Test
  public void testProcessWithEmptyColumnNameAttributeNameMap() throws IOException {
    ParticipantReader reader = createParticipantReader(1, 2, 3, false, "");

    Map<String, String> columnNameToAttributeNameMap = new HashMap<String, String>();

    reader.setColumnNameToAttributeNameMap(columnNameToAttributeNameMap);
  }

  /**
   * Tests processing of an appointment list where a column is missing for a mandatory attribute (Birth Date).
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithMissingMandatoryAttributeColumn() throws IOException {
    ParticipantReaderForTest reader = createParticipantReaderForTest(1, 2, 3, false, TEST_RESOURCES_DIR + "/appointmentList_missingMandatoryAttributeColumn.xls");

    try {
      reader.open(context);
      List<Participant> participants = new ArrayList<Participant>();

      while(reader.getRow() != null) {
        participants.add(reader.read());
      }
    } catch(Exception e) {
      String errorMessage = e.getMessage();
      Assert.assertEquals("Invalid worksheet; no column exists for mandatory field 'Birth Date'", errorMessage);
      return;
    }

    Assert.fail("Should have thrown an IllegalArgumentException");
  }

  /**
   * Tests processing of an appointment list where a mandatory attribute (Enrollment ID, at line 4) has not been
   * assigned a value. Reader should not return an error as it is a validation done in the ParticipantProcessor.
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithMissingMandatoryAttributeValue() throws IOException {
    ParticipantReaderForTest reader = createParticipantReaderForTest(1, 2, 3, false, TEST_RESOURCES_DIR + "/appointmentList_missingMandatoryAttributeValue.xls");
    reader.open(context);
    List<Participant> participants = new ArrayList<Participant>();

    try {
      while(reader.getRow() != null) {
        participants.add(reader.read());
      }
    } catch(Exception e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertEquals(3, participants.size());
    Assert.assertEquals("Tremblay", participants.get(0).getLastName());
    Assert.assertEquals("100001", participants.get(0).getEnrollmentId());
    Assert.assertEquals("Smith", participants.get(1).getLastName());
    Assert.assertNull(participants.get(1).getEnrollmentId());
    Assert.assertEquals("Casserly", participants.get(2).getLastName());
    Assert.assertEquals("100003", participants.get(2).getEnrollmentId());
  }

  /**
   * Tests processing of an appointment list where an attribute (Appointment Time, line 5) has been assigned a value of
   * the wrong type.
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithWrongAttributeValueType() throws IOException {
    ParticipantReaderForTest reader = createParticipantReaderForTest(1, 2, 3, false, TEST_RESOURCES_DIR + "/appointmentList_wrongAttributeValueType.xls");

    try {
      reader.open(context);
      List<Participant> participants = new ArrayList<Participant>();

      while(reader.getRow() != null) {
        participants.add(reader.read());
      }
    } catch(Exception e) {
      String errorMessage = e.getMessage();
      Assert.assertEquals("Wrong data type value for field 'Appointment Time': TEST", errorMessage);
      return;
    }

    Assert.fail("Should have thrown an IllegalArgumentException");
  }

  // /**
  // * Tests processing of an appointment list where an attribute (Gender, line 3) has been assigned value that is not
  // * allowed (i.e., is not with the attribute's "allowed value" list).
  // *
  // * @throws IOException if the appointment list could not be read
  // */
  // @Test
  // public void testProcessWithNotAllowedAttributeValue() throws IOException {
  // expect(participantServiceMock.getParticipant((Participant) EasyMock.anyObject())).andReturn(null).times(3);
  // participantServiceMock.updateParticipant((Participant) EasyMock.anyObject());
  // expectLastCall().times(3);
  // replay(participantServiceMock);
  //
  // ParticipantReader reader = createParticipantExcelReader(1, 2, 3, false);
  //
  // try {
  // // reader.process(ParticipantReaderTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCES_DIR +
  // // "/appointmentList_notAllowedAttributeValue.xls"), listeners);
  // } catch(IllegalArgumentException ex) {
  // String errorMessage = ex.getMessage();
  // Assert.assertEquals("Line 3: Value not allowed for field 'Gender': TEST", errorMessage);
  // return;
  // }
  //
  // verify(participantServiceMock);
  // Assert.fail("Should have thrown an IllegalArgumentException");
  // }
  //
  // /**
  // * Tests processing of an appointment list that contains a duplicate attribute column (Last Name).
  // *
  // * @throws IOException if the appointment list could not be read
  // */
  // @Test
  // public void testProcessWithDuplicateAttributeColumn() throws IOException {
  // expect(participantServiceMock.getParticipant((Participant) EasyMock.anyObject())).andReturn(null).times(3);
  // participantServiceMock.updateParticipant((Participant) EasyMock.anyObject());
  // expectLastCall().times(3);
  // replay(participantServiceMock);
  //
  // ParticipantReader reader = createParticipantExcelReader(1, 2, 3, false);
  //
  // try {
  // // reader.process(ParticipantReaderTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCES_DIR +
  // // "/appointmentList_duplicateAttributeColumn.xls"), listeners);
  // } catch(IllegalArgumentException ex) {
  // String errorMessage = ex.getMessage();
  // Assert.assertEquals("Duplicate column for field: Last Name", errorMessage);
  // return;
  // }
  //
  // verify(participantServiceMock);
  // Assert.fail("Should have thrown an IllegalArgumentException");
  // }
  //
  // @Test
  // public void testProcessWithConfiguredAttributes() throws IOException {
  // Calendar cal = Calendar.getInstance();
  //
  // cal.clear();
  // cal.set(2008, 9 - 1, 1, 9, 0);
  // final Date expectedAppointmentTime = cal.getTime();
  //
  // cal.clear();
  // cal.set(1964, 10 - 1, 1);
  // final Date expectedBirthDate = cal.getTime();
  //
  // expect(participantServiceMock.getParticipant((Participant) EasyMock.anyObject())).andReturn(null);
  // participantServiceMock.updateParticipant((Participant) EasyMock.anyObject());
  // replay(participantServiceMock);
  //
  // ParticipantReader reader = createParticipantExcelReader(1, 2, 3, true);
  //
  // // listeners.add(new IParticipantReadListener() {
  // // public void onParticipantRead(int line, Participant participant) {
  // // // Verify that the participant's essential attributes have been assigned the correct values.
  // // Assert.assertEquals(expectedAppointmentTime, participant.getAppointment().getDate());
  // // Assert.assertEquals("cag001", participant.getSiteNo());
  // // Assert.assertEquals("100001", participant.getEnrollmentId());
  // // Assert.assertEquals("Tremblay", participant.getLastName());
  // // Assert.assertEquals("Chantal", participant.getFirstName());
  // // Assert.assertEquals("F", (participant.getGender().equals(Gender.FEMALE) ? "F" : "M"));
  // // Assert.assertEquals(expectedBirthDate, participant.getBirthDate());
  // //
  // // // Verify that the participant's configured attributes have been assigned the correct values.
  // // Assert.assertEquals("299, Avenue des Pins Ouest", participant.getConfiguredAttributeValue("Street").getValue());
  // // Assert.assertEquals("Montr\u00e9al", participant.getConfiguredAttributeValue("City").getValue());
  // // Assert.assertEquals("QC", participant.getConfiguredAttributeValue("Province").getValue());
  // // Assert.assertEquals("Canada", participant.getConfiguredAttributeValue("Country").getValue());
  // // Assert.assertEquals("H1T 2M4", participant.getConfiguredAttributeValue("Postal Code").getValue());
  // // Assert.assertEquals("514-343-9898 ext 9494", participant.getConfiguredAttributeValue("Phone").getValue());
  // // }
  // //
  // // public void onParticipantReadEnd() {
  // // }
  // // });
  //
  // // reader.process(ParticipantReaderTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCES_DIR +
  // // "/appointmentList_includesConfiguredAttributes.xls"), listeners);
  // verify(participantServiceMock);
  // }
  //
  // @Test
  // public void testProcessWithRowContainingWhitespaceOnly() throws IOException {
  // expect(participantServiceMock.getParticipant((Participant) EasyMock.anyObject())).andReturn(null).times(3);
  // participantServiceMock.updateParticipant((Participant) EasyMock.anyObject());
  // expectLastCall().times(3);
  // replay(participantServiceMock);
  //
  // ParticipantReader reader = createParticipantExcelReader(1, 2, 3, false);
  // // reader.process(ParticipantReaderTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCES_DIR +
  // // "/appointmentList_rowContainingWhitespaceOnly.xls"), listeners);
  // verify(participantServiceMock);
  // }
  //
  // /**
  // * Tests processing of an appointment list containing a duplicate enrollment id.
  // *
  // * @throws IOException if the appointment list could not be read
  // */
  // @Test
  // public void testProcessWithDuplicateDuplicateEnrollmentId() throws IOException {
  // expect(participantServiceMock.getParticipant((Participant) EasyMock.anyObject())).andReturn(null).times(3);
  // participantServiceMock.updateParticipant((Participant) EasyMock.anyObject());
  // expectLastCall().times(3);
  // replay(participantServiceMock);
  //
  // ParticipantReader reader = createParticipantExcelReader(1, 2, 3, false);
  //
  // try {
  // // reader.process(ParticipantReaderTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCES_DIR +
  // // "/appointmentList_duplicateEnrollmentId.xls"), listeners);
  // } catch(IllegalArgumentException ex) {
  // String errorMessage = ex.getMessage();
  // Assert.assertEquals("Line 5: Duplicate Enrollment ID", errorMessage);
  // return;
  // }
  //
  // verify(participantServiceMock);
  // Assert.fail("Should have thrown an IllegalArgumentException");
  // }
  //
  // //
  // // Helper Methods
  // //

  private ParticipantReader createParticipantReader(int sheetNumber, int headerRowNumber, int firstDataRowNumber, boolean includeConfiguredAttributes, String resourcePath) {
    if(!includeConfiguredAttributes) {
      participantMetadata.setConfiguredAttributes(null);
    }

    ParticipantReader reader = new ParticipantReader();

    reader.setSheetNumber(sheetNumber);
    reader.setHeaderRowNumber(headerRowNumber);
    reader.setFirstDataRowNumber(firstDataRowNumber);
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

  // private User getUser() {
  // User user = new User();
  // user.setFirstName("Admin");
  // user.setLastName("Onyx");
  // return user;
  // }

  private class ParticipantReaderForTest extends ParticipantReader {

    Resource resource;

    public ParticipantReaderForTest(Resource resource) {
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

  private ParticipantReaderForTest createParticipantReaderForTest(int sheetNumber, int headerRowNumber, int firstDataRowNumber, boolean includeConfiguredAttributes, String resourcePath) {
    if(!includeConfiguredAttributes) {
      participantMetadata.setConfiguredAttributes(null);
    }

    Resource[] resources = new Resource[] { new ClassPathResource(resourcePath) };
    ParticipantReaderForTest reader = new ParticipantReaderForTest(resources[0]);

    reader.setSheetNumber(sheetNumber);
    reader.setHeaderRowNumber(headerRowNumber);
    reader.setFirstDataRowNumber(firstDataRowNumber);
    reader.setParticipantMetadata(participantMetadata);

    return reader;
  }
}
