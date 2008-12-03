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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    HSSFSheet sheet = wb.getSheetAt(0);
    HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);

    initAttributeNameToColumnIndexMap(sheet.getRow(headerRowNumber - 1));

    Iterator<HSSFRow> rowIter = (Iterator<HSSFRow>) sheet.rowIterator();

    // Skip ahead to the first data row.
    HSSFRow row = skipToFirstDataRow(rowIter);

    // Go ahead and process all the data rows.
    int line = 0;

    while(row != null) {
      line = row.getRowNum() + 1;

      Participant participant = null;

      try {
        participant = processParticipant(row, evaluator);
        participant.setAppointment(processAppointment(row, evaluator));
      } catch(IllegalArgumentException ex) {
        throw new IllegalArgumentException("Error at line " + line + ": " + ex.getMessage());
      }

      // Notify listeners that a participant has been processed.
      for(IParticipantReadListener listener : listeners) {
        listener.onParticipantRead(line, participant);
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
  }

  public void setColumnNameToAttributeNameMap(Map<String, String> columnNameToAttributeNameMap) {
    // Initialize columnNameToAttributeNameMap. Convert all keys to UPPERCASE.
    this.columnNameToAttributeNameMap = new HashMap<String, String>();

    Iterator<String> mapIter = columnNameToAttributeNameMap.keySet().iterator();
    while(mapIter.hasNext()) {
      String key = mapIter.next();
      this.columnNameToAttributeNameMap.put(key.toUpperCase(), columnNameToAttributeNameMap.get(key));
    }

    // Set default mappings for essential attributes.
    for(ParticipantAttribute essentialAttribute : participantMetadata.getEssentialAttributes()) {
      if(!essentialAttribute.isAssignableAtEnrollment()) {
        continue;
      }

      String essentialAttributeName = essentialAttribute.getName();

      if(!columnNameToAttributeNameMap.containsValue(essentialAttributeName)) {
        this.columnNameToAttributeNameMap.put(essentialAttributeName.toUpperCase(), essentialAttributeName);
      }
    }

    // Set default mappings for configured attributes.
    for(ParticipantAttribute configuredAttribute : participantMetadata.getConfiguredAttributes()) {
      String configuredAttributeName = configuredAttribute.getName();

      if(!columnNameToAttributeNameMap.containsValue(configuredAttributeName)) {
        this.columnNameToAttributeNameMap.put(configuredAttributeName.toUpperCase(), configuredAttributeName);
      }
    }
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

    int firstCellNum = headerRow.getFirstCellNum();
    int lastCellNum = headerRow.getLastCellNum();

    for(int cellNum = firstCellNum; cellNum < lastCellNum; cellNum++) {
      HSSFCell cell = headerRow.getCell(cellNum);

      if(cell != null) {
        if(cell.getCellType() != HSSFCell.CELL_TYPE_STRING) {
          throw new IllegalArgumentException("Header row contains unexpected cell type");
        }

        String columnName = cell.getRichStringCellValue().getString();

        if(columnName != null) {
          String attributeName = (String) columnNameToAttributeNameMap.get(columnName.toUpperCase());

          if(attributeName != null) {
            attributeNameToColumnIndexMap.put(attributeName.toUpperCase(), cellNum);
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
    ParticipantAttribute attribute = null;
    Data data = null;

    Appointment appointment = new Appointment();

    attribute = participantMetadata.getEssentialAttribute(ENROLLMENT_ID_ATTRIBUTE_NAME);
    data = getAttributeValue(attribute, row.getCell(attributeNameToColumnIndexMap.get(ENROLLMENT_ID_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    checkMandatoryCondition(attribute, data);
    String enrollmentId = data.getValue();
    appointment.setAppointmentCode(enrollmentId);

    attribute = participantMetadata.getEssentialAttribute(APPOINTMENT_TIME_ATTRIBUTE_NAME);
    data = getAttributeValue(attribute, row.getCell(attributeNameToColumnIndexMap.get(APPOINTMENT_TIME_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    checkMandatoryCondition(attribute, data);
    Date appointmentTime = data.getValue();
    appointment.setDate(appointmentTime);

    return appointment;
  }

  protected void setParticipantEssentialAttributes(Participant participant, HSSFRow row, HSSFFormulaEvaluator evaluator) {
    participant.setRecruitmentType(RecruitmentType.ENROLLED);

    ParticipantAttribute attribute = null;
    Data data = null;

    attribute = participantMetadata.getEssentialAttribute(ENROLLMENT_ID_ATTRIBUTE_NAME);
    data = getAttributeValue(attribute, row.getCell(attributeNameToColumnIndexMap.get(ENROLLMENT_ID_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    checkMandatoryCondition(attribute, data);
    String enrollmentId = data.getValue();
    participant.setEnrollmentId(enrollmentId);

    attribute = participantMetadata.getEssentialAttribute(ASSESSMENT_CENTER_ID_ATTRIBUTE_NAME);
    data = getAttributeValue(attribute, row.getCell(attributeNameToColumnIndexMap.get(ASSESSMENT_CENTER_ID_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    checkMandatoryCondition(attribute, data);
    String assessmentCenterId = data.getValue();
    participant.setSiteNo(assessmentCenterId);

    attribute = participantMetadata.getEssentialAttribute(FIRST_NAME_ATTRIBUTE_NAME);
    data = getAttributeValue(attribute, row.getCell(attributeNameToColumnIndexMap.get(FIRST_NAME_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    checkMandatoryCondition(attribute, data);
    String firstName = data.getValue();
    participant.setFirstName(firstName);

    attribute = participantMetadata.getEssentialAttribute(LAST_NAME_ATTRIBUTE_NAME);
    data = getAttributeValue(attribute, row.getCell(attributeNameToColumnIndexMap.get(LAST_NAME_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    checkMandatoryCondition(attribute, data);
    String lastName = data.getValue();
    participant.setLastName(lastName);

    attribute = participantMetadata.getEssentialAttribute(BIRTH_DATE_ATTRIBUTE_NAME);
    data = getAttributeValue(attribute, row.getCell(attributeNameToColumnIndexMap.get(BIRTH_DATE_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    checkMandatoryCondition(attribute, data);
    Date birthDate = data.getValue();
    participant.setBirthDate(birthDate);

    attribute = participantMetadata.getEssentialAttribute(GENDER_ATTRIBUTE_NAME);
    data = getAttributeValue(attribute, row.getCell(attributeNameToColumnIndexMap.get(GENDER_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    checkMandatoryCondition(attribute, data);
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

  private void setConfiguredAttribute(Participant participant, ParticipantAttribute attribute, HSSFCell cell, HSSFFormulaEvaluator evaluator) {
    Data data = getAttributeValue(attribute, cell, evaluator);

    checkMandatoryCondition(attribute, data);

    participant.setConfiguredAttributeValue(attribute.getName(), data);
  }

  private Data getAttributeValue(ParticipantAttribute attribute, HSSFCell cell, HSSFFormulaEvaluator evaluator) {
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
      List<String> allowedValues = attribute.getAllowedValues();

      if(!allowedValues.isEmpty()) {
        String textValue = data.getValue();

        if(!allowedValues.contains(textValue)) {
          throw new IllegalArgumentException("Value not allowed for field '" + attribute.getName() + "': " + textValue);
        }
      }
    }

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
}