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
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateLog;
import org.obiba.onyx.core.io.support.ExcelReaderSupport;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

/**
 * Default ItemReader for reading Participant items from an Excel workbook
 */
public class ParticipantReader extends AbstractFileBasedParticipantReader {

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

  //
  // HSSF instance variables.
  //
  private Iterator<Row> rowIter;

  private HSSFFormulaEvaluator evaluator;

  private Row row;

  @Override
  public void open(ExecutionContext context) throws ItemStreamException {
    super.open(context);

    try {
      HSSFWorkbook wb = new HSSFWorkbook(getFileInputStream());
      HSSFSheet sheet = wb.getSheetAt(sheetNumber - 1);
      evaluator = new HSSFFormulaEvaluator(wb);

      initAttributeNameToColumnIndexMap(context, sheet.getRow(headerRowNumber - 1));

      rowIter = sheet.rowIterator();

      // Skip ahead to the first data row.
      skipToFirstDataRow();

    } catch(IOException e) {
      AppointmentUpdateLog.addLog(context, new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.ERROR,
          "Abort updating appointments: Reading file error: " + e.getMessage()));

      ValidationRuntimeException vex = new ValidationRuntimeException();
      vex.reject("ParticipantsListFileReadingError", new String[] { e.getMessage() }, "Reading file error: " + e.getMessage());
      throw vex;
    }

  }

  @Override
  public Participant read() throws Exception, UnexpectedInputException, ParseException {
    Participant participant = null;

    // Need this check because even though the row iterator only returns "physical" rows, rows containing
    // cells with whitespace only are also returned. We want to ignore those rows.
    if(row != null && !rowContainsWhitespaceOnly()) {
      participant = processParticipant();
      participant.setAppointment(processAppointment());
    }

    if(rowIter.hasNext()) {
      row = rowIter.next();
    } else {
      row = null;
    }
    return participant;
  }

  @Override
  public String getFilePattern() {
    return ".xls";
  }

  //
  // Local methods
  //
  @SuppressWarnings("unchecked")
  private void initAttributeNameToColumnIndexMap(ExecutionContext context, Row headerRow) {
    if(headerRow == null) {
      AppointmentUpdateLog.addLog(context, new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.ERROR,
          "Abort updating appointments: Reading file error: Null headerRow"));
      throw new IllegalArgumentException("Null headerRow");
    }

    attributeNameToColumnIndexMap = new CaseInsensitiveMap();

    Iterator<Cell> cellIter = headerRow.cellIterator();

    while(cellIter.hasNext()) {
      Cell cell = cellIter.next();

      if(cell != null) {
        if(cell.getCellType() != Cell.CELL_TYPE_STRING) {
          AppointmentUpdateLog.addLog(context, new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.ERROR,
              "Abort updating appointments: Reading file error: Header row contains unexpected cell type"));
          throw new IllegalArgumentException("Header row contains unexpected cell type");
        }

        String columnName = cell.getRichStringCellValue().getString();

        if(columnName != null) {
          String attributeName = columnNameToAttributeNameMap.get(columnName);

          if(attributeName != null) {
            if(!attributeNameToColumnIndexMap.containsKey(attributeName)) {
              attributeNameToColumnIndexMap.put(attributeName, cell.getColumnIndex());
            } else {
              AppointmentUpdateLog.addLog(context,
                  new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.ERROR,
                      "Abort updating appointments: Reading file error: Duplicate column for field: " + attributeName));
              throw new IllegalArgumentException("Duplicate column for field: " + attributeName);
            }
          }
        }
      }
    }

    log.info("attributeNameToColumnIndexMap: {}", attributeNameToColumnIndexMap);

    checkColumnsForMandatoryAttributesPresent();
  }

  private void skipToFirstDataRow() {
    while(true) {
      row = rowIter.next();
      if(row.getRowNum() >= (getFirstDataRowNumber() - 1)) {
        break;
      }
    }
  }

  private boolean rowContainsWhitespaceOnly() {
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

  private Participant processParticipant() {
    Participant participant = new Participant();

    setParticipantEssentialAttributes(participant, row, evaluator);
    setParticipantConfiguredAttributes(participant, row, evaluator);

    return participant;
  }

  private Appointment processAppointment() {
    Appointment appointment = new Appointment();
    Data data = null;

    data = getEssentialAttributeValue(ParticipantMetadata.ENROLLMENT_ID_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(ParticipantMetadata.ENROLLMENT_ID_ATTRIBUTE_NAME)), evaluator);
    String enrollmentId = (String) ((data != null) ? data.getValue() : null);
    appointment.setAppointmentCode(enrollmentId);

    data = getEssentialAttributeValue(ParticipantMetadata.APPOINTMENT_TIME_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(ParticipantMetadata.APPOINTMENT_TIME_ATTRIBUTE_NAME)), evaluator);
    Date appointmentTime = (data != null) ? (Date) data.getValue() : null;
    appointment.setDate(appointmentTime);

    return appointment;
  }

  protected void setParticipantEssentialAttributes(Participant participant, Row row, HSSFFormulaEvaluator evaluator) {
    participant.setRecruitmentType(RecruitmentType.ENROLLED);

    Data data = null;

    data = getEssentialAttributeValue(ParticipantMetadata.ENROLLMENT_ID_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(ParticipantMetadata.ENROLLMENT_ID_ATTRIBUTE_NAME)), evaluator);
    String enrollmentId = (String) ((data != null) ? data.getValue() : null);
    participant.setEnrollmentId(enrollmentId);

    data = getEssentialAttributeValue(ParticipantMetadata.ASSESSMENT_CENTER_ID_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(ParticipantMetadata.ASSESSMENT_CENTER_ID_ATTRIBUTE_NAME)), evaluator);
    String assessmentCenterId = (String) ((data != null) ? data.getValue() : null);
    participant.setSiteNo(assessmentCenterId);

    data = getEssentialAttributeValue(ParticipantMetadata.FIRST_NAME_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(ParticipantMetadata.FIRST_NAME_ATTRIBUTE_NAME)), evaluator);
    String firstName = (String) ((data != null) ? data.getValue() : null);
    participant.setFirstName(firstName);

    data = getEssentialAttributeValue(ParticipantMetadata.LAST_NAME_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(ParticipantMetadata.LAST_NAME_ATTRIBUTE_NAME)), evaluator);
    String lastName = (String) ((data != null) ? data.getValue() : null);
    participant.setLastName(lastName);

    if(attributeNameToColumnIndexMap.containsKey(ParticipantMetadata.BIRTH_DATE_ATTRIBUTE_NAME)) {
      data = getEssentialAttributeValue(ParticipantMetadata.BIRTH_DATE_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(ParticipantMetadata.BIRTH_DATE_ATTRIBUTE_NAME)), evaluator);
      Date birthDate = (data != null) ? (Date) data.getValue() : null;
      participant.setBirthDate(birthDate);
    }

    if(attributeNameToColumnIndexMap.containsKey(ParticipantMetadata.GENDER_ATTRIBUTE_NAME)) {
      data = getEssentialAttributeValue(ParticipantMetadata.GENDER_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(ParticipantMetadata.GENDER_ATTRIBUTE_NAME)), evaluator);
      String gender = (data != null) ? data.getValueAsString() : "";
      if(gender.equals("M")) {
        participant.setGender(Gender.MALE);
      } else if(gender.equals("F")) {
        participant.setGender(Gender.FEMALE);
      } else {
        participant.setGender(null);
      }
    }
  }

  protected void setParticipantConfiguredAttributes(Participant participant, Row row, HSSFFormulaEvaluator evaluator) {
    for(ParticipantAttribute configuredAttribute : getParticipantMetadata().getConfiguredAttributes()) {
      if(configuredAttribute.isAssignableAtEnrollment() && attributeNameToColumnIndexMap.containsKey(configuredAttribute.getName())) {
        Cell cell = row.getCell(attributeNameToColumnIndexMap.get(configuredAttribute.getName()));
        setConfiguredAttribute(participant, configuredAttribute, cell, evaluator);
      }
    }
  }

  private
      void
      setConfiguredAttribute(Participant participant, ParticipantAttribute attribute, Cell cell, HSSFFormulaEvaluator evaluator) {
    Data data = getAttributeValue(attribute, cell, evaluator);
    participant.setConfiguredAttributeValue(attribute.getName(), data);
  }

  private Data getEssentialAttributeValue(String attributeName, Cell cell, HSSFFormulaEvaluator evaluator) {
    ParticipantAttribute attribute = getParticipantMetadata().getEssentialAttribute(attributeName);
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
   * @throws IllegalArgumentException if the cell type is not compatible with the attribute type
   */
  @SuppressWarnings("incomplete-switch")
  private Data getAttributeValue(ParticipantAttribute attribute, Cell cell, HSSFFormulaEvaluator evaluator) {

    if(cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) return null;
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
      if(attribute.isMandatoryAtEnrollment()) {
        throw new IllegalArgumentException("Wrong data type value for field '" + attribute.getName() + "': " + cell.toString());
      }
      return null;
    }

    return data;
  }

  //
  // Getters and setters
  //
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

  public Row getRow() {
    return row;
  }
}
