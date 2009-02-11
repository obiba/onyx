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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.core.etl.participant.IParticipantReadListener;
import org.obiba.onyx.core.etl.participant.IParticipantReader;
import org.obiba.onyx.core.io.support.ExcelReaderSupport;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultParticipantExcelReader implements IParticipantReader {
  //
  // Constants
  //

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultParticipantExcelReader.class);

  private static final String ENROLLMENT_ID_ATTRIBUTE_NAME = "Enrollment ID";

  private static final String ASSESSMENT_CENTER_ID_ATTRIBUTE_NAME = "Assessment Center ID";

  private static final String FIRST_NAME_ATTRIBUTE_NAME = "First Name";

  private static final String LAST_NAME_ATTRIBUTE_NAME = "Last Name";

  private static final String BIRTH_DATE_ATTRIBUTE_NAME = "Birth Date";

  private static final String GENDER_ATTRIBUTE_NAME = "Gender";

  private static final String APPOINTMENT_TIME_ATTRIBUTE_NAME = "Appointment Time";

  //
  // Instance Variables
  //

  /**
   * Participant meta-data.
   */
  private ParticipantMetadata participantMetadata;

  /**
   * Maps Excel column names to attribute names.
   */
  private Map<String, String> columnNameToAttributeNameMap;

  /**
   * Maps attribute names to column indices.
   */
  private Map<String, Integer> attributeNameToColumnIndexMap;

  /**
   * Sheet number.
   */
  private int sheetNumber;

  /**
   * Header row number.
   */
  private int headerRowNumber;

  /**
   * First data row number.
   */
  private int firstDataRowNumber;

  /**
   * Read listeners.
   */
  private List<IParticipantReadListener> listeners = new ArrayList<IParticipantReadListener>();

  //
  // Constructors
  //

  public DefaultParticipantExcelReader() {
    columnNameToAttributeNameMap = new HashMap<String, String>();
  }

  //
  // IParticipantReader Methods
  //

  public void addParticipantReadListener(IParticipantReadListener listener) {
    listeners.add(listener);
  }

  public void removeParticipantReadListener(IParticipantReadListener listener) {
    listeners.remove(listener);
  }

  @SuppressWarnings("unchecked")
  public void process(InputStream input) throws IOException, IllegalArgumentException {
    HSSFWorkbook wb = new HSSFWorkbook(input);
    HSSFSheet sheet = wb.getSheetAt(sheetNumber - 1);
    HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);

    // Keep track of enrollment ids -- duplicates not allowed!
    Set<String> enrollmentIds = new HashSet<String>();

    initAttributeNameToColumnIndexMap(sheet.getRow(headerRowNumber - 1));

    Iterator<HSSFRow> rowIter = (Iterator<HSSFRow>) sheet.rowIterator();

    // Skip ahead to the first data row.
    HSSFRow row = skipToFirstDataRow(rowIter);

    // Go ahead and process all the data rows.
    int line = 0;

    while(row != null) {
      // Need this check because even though the row iterator only returns "physical" rows, rows containing
      // cells with whitespace only are also returned. We want to ignore those rows.
      if(!rowContainsWhitespaceOnly(evaluator, row)) {
        line = row.getRowNum() + 1;

        Participant participant = null;

        try {
          participant = processParticipant(row, evaluator);
          participant.setAppointment(processAppointment(row, evaluator));

          checkUniqueEnrollmentId(enrollmentIds, participant.getEnrollmentId());
        } catch(IllegalArgumentException ex) {
          throw new IllegalArgumentException("Line " + line + ": " + ex.getMessage());
        }

        // Notify listeners that a participant has been processed.
        for(IParticipantReadListener listener : listeners) {
          listener.onParticipantRead(line, participant);
        }
      }

      if(rowIter.hasNext()) {
        row = rowIter.next();
      } else {
        row = null;
      }
    }

    // Notify listeners that the last participant has been processed.
    for(IParticipantReadListener listener : listeners) {
      listener.onParticipantReadEnd(line);
    }
  }

  //
  // Methods
  //

  public void setParticipantMetadata(ParticipantMetadata participantMetadata) {
    this.participantMetadata = participantMetadata;

    if(participantMetadata != null) {
      addDefaultColumnNameToAttributeNameMapEntries();
    }
  }

  public void setColumnNameToAttributeNameMap(Map<String, String> columnNameToAttributeNameMap) {
    if(columnNameToAttributeNameMap != null) {
      // Add map entries to columnNameToAttributeNameMap. Convert all keys to UPPERCASE.
      Iterator<Map.Entry<String, String>> mapIter = columnNameToAttributeNameMap.entrySet().iterator();
      while(mapIter.hasNext()) {
        Map.Entry<String, String> mapEntry = mapIter.next();
        this.columnNameToAttributeNameMap.put(mapEntry.getKey().toUpperCase(), mapEntry.getValue());
      }
    }
  }

  /**
   * Set he column name to attribute name map with a configuration string.
   * 
   * @param keyValuePairs list of key/value pairs separated by a comma. For example, "<code>param1=foo,param2=bar</code>".
   */
  public void setColumnToAttribute(String keyValuePairs) {
    if(columnNameToAttributeNameMap != null) {
      // Get list of strings separated by the delimiter
      StringTokenizer tokenizer = new StringTokenizer(keyValuePairs, ",");
      while(tokenizer.hasMoreElements()) {
        String token = tokenizer.nextToken();
        String[] entry = token.split("=");
        if(entry.length == 2) {
          columnNameToAttributeNameMap.put(entry[0].toUpperCase().trim(), entry[1].trim());
        } else {
          log.error("Could not identify Participant column to attribute mapping: " + token);
        }
      }
    }
  }

  public void setSheetNumber(int sheetNumber) {
    this.sheetNumber = sheetNumber;
  }

  public void setHeaderRowNumber(int headerRowNumber) {
    this.headerRowNumber = headerRowNumber;
  }

  public void setFirstDataRowNumber(int firstDataRowNumber) {
    this.firstDataRowNumber = firstDataRowNumber;
  }

  public int getFirstDataRowNumber() {
    return firstDataRowNumber;
  }

  private void initAttributeNameToColumnIndexMap(HSSFRow headerRow) {
    if(headerRow == null) {
      throw new IllegalArgumentException("Null headerRow");
    }

    attributeNameToColumnIndexMap = new HashMap<String, Integer>();

    Iterator cellIter = headerRow.cellIterator();

    while(cellIter.hasNext()) {
      HSSFCell cell = (HSSFCell) cellIter.next();

      if(cell != null) {
        if(cell.getCellType() != HSSFCell.CELL_TYPE_STRING) {
          throw new IllegalArgumentException("Header row contains unexpected cell type");
        }

        String columnName = cell.getRichStringCellValue().getString();

        if(columnName != null) {
          String attributeName = (String) columnNameToAttributeNameMap.get(columnName.toUpperCase());

          if(attributeName != null) {
            if(!attributeNameToColumnIndexMap.containsKey(attributeName.toUpperCase())) {
              attributeNameToColumnIndexMap.put(attributeName.toUpperCase(), cell.getColumnIndex());
            } else {
              throw new IllegalArgumentException("Duplicate column for field: " + attributeName);
            }
          }
        }
      }
    }

    checkColumnsForMandatoryAttributesPresent();
  }

  private HSSFRow skipToFirstDataRow(Iterator<HSSFRow> rowIter) {
    HSSFRow row = null;

    while(true) {
      row = rowIter.next();

      if(row.getRowNum() >= (getFirstDataRowNumber() - 1)) {
        break;
      }
    }

    return row;
  }

  private Participant processParticipant(HSSFRow row, HSSFFormulaEvaluator evaluator) {
    Participant participant = new Participant();

    setParticipantEssentialAttributes(participant, row, evaluator);
    setParticipantConfiguredAttributes(participant, row, evaluator);

    return participant;
  }

  private Appointment processAppointment(HSSFRow row, HSSFFormulaEvaluator evaluator) {
    Appointment appointment = new Appointment();
    Data data = null;

    data = getEssentialAttributeValue(ENROLLMENT_ID_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(ENROLLMENT_ID_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    String enrollmentId = data.getValue();
    appointment.setAppointmentCode(enrollmentId);

    data = getEssentialAttributeValue(APPOINTMENT_TIME_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(APPOINTMENT_TIME_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    Date appointmentTime = data.getValue();
    appointment.setDate(appointmentTime);

    return appointment;
  }

  protected void setParticipantEssentialAttributes(Participant participant, HSSFRow row, HSSFFormulaEvaluator evaluator) {
    participant.setRecruitmentType(RecruitmentType.ENROLLED);

    Data data = null;

    data = getEssentialAttributeValue(ENROLLMENT_ID_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(ENROLLMENT_ID_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    String enrollmentId = data.getValue();
    participant.setEnrollmentId(enrollmentId);

    data = getEssentialAttributeValue(ASSESSMENT_CENTER_ID_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(ASSESSMENT_CENTER_ID_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    String assessmentCenterId = data.getValue();
    participant.setSiteNo(assessmentCenterId);

    data = getEssentialAttributeValue(FIRST_NAME_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(FIRST_NAME_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    String firstName = data.getValue();
    participant.setFirstName(firstName);

    data = getEssentialAttributeValue(LAST_NAME_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(LAST_NAME_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    String lastName = data.getValue();
    participant.setLastName(lastName);

    data = getEssentialAttributeValue(BIRTH_DATE_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(BIRTH_DATE_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    Date birthDate = data.getValue();
    participant.setBirthDate(birthDate);

    data = getEssentialAttributeValue(GENDER_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(GENDER_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    String gender = data.getValue();
    if(gender.equals("M")) {
      participant.setGender(Gender.MALE);
    } else if(gender.equals("F")) {
      participant.setGender(Gender.FEMALE);
    }
  }

  protected void setParticipantConfiguredAttributes(Participant participant, HSSFRow row, HSSFFormulaEvaluator evaluator) {
    for(ParticipantAttribute configuredAttribute : participantMetadata.getConfiguredAttributes()) {
      if(configuredAttribute.isAssignableAtEnrollment()) {
        HSSFCell cell = row.getCell(attributeNameToColumnIndexMap.get(configuredAttribute.getName().toUpperCase()));
        setConfiguredAttribute(participant, configuredAttribute, cell, evaluator);
      }
    }
  }

  private void addDefaultColumnNameToAttributeNameMapEntries() {
    if(columnNameToAttributeNameMap == null) {
      columnNameToAttributeNameMap = new HashMap<String, String>();
    }

    // Set default mappings for essential attributes.
    for(ParticipantAttribute essentialAttribute : participantMetadata.getEssentialAttributes()) {
      if(!essentialAttribute.isAssignableAtEnrollment()) {
        continue;
      }

      String essentialAttributeName = essentialAttribute.getName();

      if(!columnNameToAttributeNameMap.containsValue(essentialAttributeName)) {
        columnNameToAttributeNameMap.put(essentialAttributeName.toUpperCase(), essentialAttributeName);
      }
    }

    // Set default mappings for configured attributes.
    for(ParticipantAttribute configuredAttribute : participantMetadata.getConfiguredAttributes()) {
      String configuredAttributeName = configuredAttribute.getName();

      if(!columnNameToAttributeNameMap.containsValue(configuredAttributeName)) {
        columnNameToAttributeNameMap.put(configuredAttributeName.toUpperCase(), configuredAttributeName);
      }
    }
  }

  private void setConfiguredAttribute(Participant participant, ParticipantAttribute attribute, HSSFCell cell, HSSFFormulaEvaluator evaluator) {
    Data data = getAttributeValue(attribute, cell, evaluator);

    checkMandatoryCondition(attribute, data);

    participant.setConfiguredAttributeValue(attribute.getName(), data);
  }

  private Data getEssentialAttributeValue(String attributeName, HSSFCell cell, HSSFFormulaEvaluator evaluator) {
    ParticipantAttribute attribute = participantMetadata.getEssentialAttribute(attributeName);
    Data data = getAttributeValue(attribute, cell, evaluator);

    return data;
  }

  /**
   * Returns the value of the participant attribute stored in the specified data cell.
   * 
   * @param attribute participant attribute
   * @param cell data cell
   * @param evaluator cell evaluator
   * @return attribute value (or <code>null</code> if none)
   * @throws IllegalArgumentException if the cell type is not compatible with the attribute type, or if the attribute is
   * mandatory but its value is <code>null</code>
   */
  private Data getAttributeValue(ParticipantAttribute attribute, HSSFCell cell, HSSFFormulaEvaluator evaluator) {
    if(cell == null) {
      checkMandatoryCondition(attribute, null);
      return null;
    }

    Data data = null;

    try {
      switch(attribute.getType()) {
      case DECIMAL:
        data = DataBuilder.buildDecimal(ExcelReaderSupport.getNumericValue(evaluator, cell));
        break;
      case INTEGER:
        data = DataBuilder.buildInteger(ExcelReaderSupport.getNumericValue(evaluator, cell).longValue());
        break;
      case DATE:
        data = DataBuilder.buildDate(ExcelReaderSupport.getDateValue(evaluator, cell));
        break;
      case TEXT:
        String textValue = ExcelReaderSupport.getTextValue(evaluator, cell);

        if(textValue != null && textValue.trim().length() != 0) {
          data = DataBuilder.buildText(textValue);
        }

        break;
      }
    } catch(IllegalArgumentException ex) {
      throw new IllegalArgumentException("Wrong data type value for field '" + attribute.getName() + "': " + cell.toString());
    }

    // For TEXT-type attributes, if the attribute has a list of allowed values, validate that the value
    // is within that list.
    if(attribute.getType().equals(DataType.TEXT) && data != null) {
      checkValueAllowed(attribute, data);
    }

    checkMandatoryCondition(attribute, data);

    return data;
  }

  private void checkColumnsForMandatoryAttributesPresent() {
    List<ParticipantAttribute> allAttributes = new ArrayList<ParticipantAttribute>();
    allAttributes.addAll(participantMetadata.getEssentialAttributes());
    allAttributes.addAll(participantMetadata.getConfiguredAttributes());

    // Check that all attributes mandatory at enrollment are present.
    for(ParticipantAttribute attribute : allAttributes) {
      if(attribute.isMandatoryAtEnrollment()) {
        if(!attributeNameToColumnIndexMap.containsKey(attribute.getName().toUpperCase())) {
          throw new IllegalArgumentException("Invalid worksheet; no column exists for mandatory field '" + attribute.getName() + "'");
        }
      }
    }
  }

  private void checkMandatoryCondition(ParticipantAttribute attribute, Data attributeValue) {
    if(attribute.isMandatoryAtEnrollment()) {
      if(attributeValue == null || attributeValue.getValue() == null) {
        throw new IllegalArgumentException("No value for mandatory field: " + attribute.getName());
      }
    }
  }

  private void checkValueAllowed(ParticipantAttribute attribute, Data data) {
    Set<String> allowedValues = attribute.getAllowedValues();

    if(!allowedValues.isEmpty()) {
      String textValue = data.getValue();

      if(!allowedValues.contains(textValue)) {
        throw new IllegalArgumentException("Value not allowed for field '" + attribute.getName() + "': " + textValue);
      }
    }
  }

  private void checkUniqueEnrollmentId(Set<String> enrollmentIds, String enrollmentId) {
    if(enrollmentIds.contains(enrollmentId)) {
      throw new IllegalArgumentException("Duplicate " + ENROLLMENT_ID_ATTRIBUTE_NAME);
    }

    enrollmentIds.add(enrollmentId);
  }

  @SuppressWarnings("unchecked")
  private boolean rowContainsWhitespaceOnly(HSSFFormulaEvaluator evaluator, HSSFRow row) {
    boolean rowContainsWhitespaceOnly = true;

    Iterator cellIter = row.cellIterator();

    while(cellIter.hasNext()) {
      HSSFCell cell = (HSSFCell) cellIter.next();

      if(!ExcelReaderSupport.containsWhitespace(evaluator, cell)) {
        rowContainsWhitespaceOnly = false;
        break;
      }
    }

    return rowContainsWhitespaceOnly;
  }
}