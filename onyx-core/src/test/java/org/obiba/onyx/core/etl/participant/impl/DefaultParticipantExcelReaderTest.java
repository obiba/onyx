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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantAttributeReader;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Unit tests for <code>DefaultParticipantExcelReader</code>.
 */
public class DefaultParticipantExcelReaderTest {
  //
  // Constants
  //

  private static final String TEST_RESOURCES_DIR = DefaultParticipantExcelReaderTest.class.getSimpleName();

  //
  // Instance Variables
  //

  private ParticipantMetadata participantMetadata;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() throws Exception {
    initParticipantMetadata();
  }

  //
  // Test Methods
  //

  /**
   * Tests processing of an appointment list that contains no configured attributes (i.e., essential attributes only).
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithNoConfiguredAttributes() throws IOException {
    DefaultParticipantExcelReader reader = createParticipantExcelReader(2, 3);

    reader.process(DefaultParticipantExcelReaderTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCES_DIR + "/appointmentList_noConfiguredAttributes.xls"));
  }

  /**
   * Tests processing of an appointment list with columns in reverse order (i.e., reverse of order used in the sheet
   * processed by <code>testProcessWithNoConfiguredAttributes</code>).
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithColumnsInReverseOrder() throws IOException {
    DefaultParticipantExcelReader reader = createParticipantExcelReader(2, 3);

    reader.process(DefaultParticipantExcelReaderTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCES_DIR + "/appointmentList_columnsInReverseOrder.xls"));
  }

  /**
   * Tests processing of an appointment list with the header row and first data row configured differently.
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithDifferentHeaderRowAndFirstDataRow() throws IOException {
    DefaultParticipantExcelReader reader = createParticipantExcelReader(3, 5);

    reader.process(DefaultParticipantExcelReaderTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCES_DIR + "/appointmentList_differentHeaderRowAndFirstDataRow.xls"));
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
    DefaultParticipantExcelReader reader = createParticipantExcelReader(2, 3);

    Map<String, String> columnNameToAttributeNameMap = new HashMap<String, String>();
    columnNameToAttributeNameMap.put("enrollmentId", "Enrollment ID");
    columnNameToAttributeNameMap.put("firstName", "First Name");
    columnNameToAttributeNameMap.put("lastName", "Last Name");
    columnNameToAttributeNameMap.put("birthDate", "Birth Date");
    columnNameToAttributeNameMap.put("gender", "Gender");
    columnNameToAttributeNameMap.put("assessmentCenterId", "Assessment Center ID");
    columnNameToAttributeNameMap.put("appointmentTime", "Appointment Time");

    reader.setColumnNameToAttributeNameMap(columnNameToAttributeNameMap);

    reader.process(DefaultParticipantExcelReaderTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCES_DIR + "/appointmentList_differentColumnNames.xls"));
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
    DefaultParticipantExcelReader reader = createParticipantExcelReader(2, 3);

    Map<String, String> columnNameToAttributeNameMap = new HashMap<String, String>();
    columnNameToAttributeNameMap.put("DOB", "Birth Date");
    columnNameToAttributeNameMap.put("Sex", "Gender");

    reader.setColumnNameToAttributeNameMap(columnNameToAttributeNameMap);

    reader.process(DefaultParticipantExcelReaderTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCES_DIR + "/appointmentList_someDifferentColumnNames.xls"));
  }

  /**
   * Tests processing of an appointment list when an empty column name / attribute name map is used.
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithEmptyColumnNameAttributeNameMap() throws IOException {
    DefaultParticipantExcelReader reader = createParticipantExcelReader(2, 3);

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
    DefaultParticipantExcelReader reader = createParticipantExcelReader(2, 3);

    try {
      reader.process(DefaultParticipantExcelReaderTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCES_DIR + "/appointmentList_missingMandatoryAttributeColumn.xls"));
    } catch(IllegalArgumentException ex) {
      String errorMessage = ex.getMessage();
      Assert.assertEquals("Invalid worksheet; no column exists for mandatory field 'Birth Date'", errorMessage);
      return;
    }

    Assert.fail("Should have thrown an IllegalArgumentException");
  }

  /**
   * Tests processing of an appointment list where a mandatory attribute (Enrollment ID, at line 4) has not been
   * assigned a value.
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithMissingMandatoryAttributeValue() throws IOException {
    DefaultParticipantExcelReader reader = createParticipantExcelReader(2, 3);

    try {
      reader.process(DefaultParticipantExcelReaderTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCES_DIR + "/appointmentList_missingMandatoryAttributeValue.xls"));
    } catch(IllegalArgumentException ex) {
      String errorMessage = ex.getMessage();
      Assert.assertEquals("Error at line 4: No value for mandatory field: Enrollment ID", errorMessage);
      return;
    }

    Assert.fail("Should have thrown an IllegalArgumentException");
  }

  /**
   * Tests processing of an appointment list where an attribute (Appointment Time, line 5) has been assigned a value of
   * the wrong type.
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithWrongAttributeValueType() throws IOException {
    DefaultParticipantExcelReader reader = createParticipantExcelReader(2, 3);

    try {
      reader.process(DefaultParticipantExcelReaderTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCES_DIR + "/appointmentList_wrongAttributeValueType.xls"));
    } catch(IllegalArgumentException ex) {
      String errorMessage = ex.getMessage();
      Assert.assertEquals("Error at line 5: Wrong data type value for field 'Appointment Time': TEST", errorMessage);
      return;
    }

    Assert.fail("Should have thrown an IllegalArgumentException");
  }

  /**
   * Tests processing of an appointment list where an attribute (Gender, line 3) has been assigned value that is not
   * allowed (i.e., is not with the attribute's "allowed value" list).
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithNotAllowedAttributeValue() throws IOException {
    DefaultParticipantExcelReader reader = createParticipantExcelReader(2, 3);

    try {
      reader.process(DefaultParticipantExcelReaderTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCES_DIR + "/appointmentList_notAllowedAttributeValue.xls"));
    } catch(IllegalArgumentException ex) {
      String errorMessage = ex.getMessage();
      Assert.assertEquals("Error at line 3: Value not allowed for field 'Gender': TEST", errorMessage);
      return;
    }

    Assert.fail("Should have thrown an IllegalArgumentException");
  }

  //
  // Helper Methods
  //

  private DefaultParticipantExcelReader createParticipantExcelReader(int headerRowNumber, int firstDataRowNumber) {
    DefaultParticipantExcelReader reader = new DefaultParticipantExcelReader();

    reader.setHeaderRowNumber(headerRowNumber);
    reader.setFirstDataRowNumber(firstDataRowNumber);
    reader.setParticipantMetadata(participantMetadata);

    return reader;
  }

  private void initParticipantMetadata() throws IOException {
    participantMetadata = new ParticipantMetadata();

    ParticipantAttributeReader attributeReader = new ParticipantAttributeReader();
    attributeReader.setResources(new Resource[] { new ClassPathResource(TEST_RESOURCES_DIR + "/essential-attributes.xml") });

    List<ParticipantAttribute> essentialAttributes = attributeReader.read();
    participantMetadata.setEssentialAttributes(essentialAttributes);
  }
}
