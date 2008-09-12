package org.obiba.onyx.core.etl.participant;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractParticipantExcelReader implements IParticipantReader {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractParticipantExcelReader.class);

  private int startAtLine = 2;

  private List<IParticipantReadListener> listeners = new ArrayList<IParticipantReadListener>();

  public void addParticipantReadListener(IParticipantReadListener listener) {
    listeners.add(listener);
  }

  public void removeParticipantReadListener(IParticipantReadListener listener) {
    listeners.remove(listener);
  }

  public void setStartAtLine(int startAtLine) {
    this.startAtLine = startAtLine;
  }
  
  protected int getStartAtLine() {
    return startAtLine;
  }

  @SuppressWarnings("unchecked")
  public void process(InputStream input) throws IOException, ValidationRuntimeException {
    HSSFWorkbook wb = new HSSFWorkbook(input);
    HSSFSheet sheet = wb.getSheetAt(0);
    HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(sheet, wb);

    List<String> cells = new ArrayList<String>();
    int line = 0;
    for(Iterator<HSSFRow> rit = (Iterator<HSSFRow>) sheet.rowIterator(); rit.hasNext();) {
      HSSFRow row = rit.next();
      line = row.getRowNum() + 1;
      cells.clear();

      if(line >= getStartAtLine()) {

        Participant participant = processParticipant(row, evaluator);
        participant.setAppointment(processAppointment(row, evaluator));
        for(IParticipantReadListener listener : listeners) {
          listener.onParticipantRead(line, participant);
        }
      }
    }
  }

  protected abstract Participant processParticipant(HSSFRow row, HSSFFormulaEvaluator evaluator);

  protected abstract Appointment processAppointment(HSSFRow row, HSSFFormulaEvaluator evaluator);

  protected Boolean getBooleanValue(HSSFRow row, HSSFFormulaEvaluator evaluator, HSSFCell cell) {
    if(cell == null) return null;

    boolean rvalue = false;

    if(cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
      evaluator.setCurrentRow(row);
      evaluator.evaluate(cell);
      HSSFFormulaEvaluator.CellValue cellValue = evaluator.evaluate(cell);

      switch(cellValue.getCellType()) {
      case HSSFCell.CELL_TYPE_BOOLEAN:
        rvalue = cellValue.getBooleanValue();
        break;
      case HSSFCell.CELL_TYPE_NUMERIC:
        rvalue = Boolean.parseBoolean(Long.valueOf((long) cellValue.getNumberValue()).toString());
        break;
      case HSSFCell.CELL_TYPE_STRING:
        rvalue = Boolean.parseBoolean(cellValue.getRichTextStringValue().getString());
        break;
      }
    } else {
      switch(cell.getCellType()) {
      case HSSFCell.CELL_TYPE_BOOLEAN:
        rvalue = cell.getBooleanCellValue();
        break;
      case HSSFCell.CELL_TYPE_NUMERIC:
        rvalue = Boolean.parseBoolean(Long.valueOf((long) cell.getNumericCellValue()).toString());
        // cells.add(column + ":" + Double.toString(cell.getNumericCellValue()) + " || " + cell.getDateCellValue());
        break;
      case HSSFCell.CELL_TYPE_STRING:
        rvalue = Boolean.parseBoolean(cell.getRichStringCellValue().getString());
        break;
      }
    }

    return rvalue;
  }

  protected Date getDateValue(HSSFRow row, HSSFFormulaEvaluator evaluator, HSSFCell cell) {
    if(cell == null) return null;

    Date rvalue = null;

    if(cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
      evaluator.setCurrentRow(row);
      evaluator.evaluate(cell);
      HSSFFormulaEvaluator.CellValue cellValue = evaluator.evaluate(cell);

      switch(cellValue.getCellType()) {
      case HSSFCell.CELL_TYPE_NUMERIC:
        rvalue = cell.getDateCellValue();
        break;
      }
    } else {
      switch(cell.getCellType()) {
      case HSSFCell.CELL_TYPE_NUMERIC:
        rvalue = cell.getDateCellValue();
        break;
      }
    }

    return rvalue;
  }

  protected Double getNumericValue(HSSFRow row, HSSFFormulaEvaluator evaluator, HSSFCell cell) {
    if(cell == null) return null;

    Double rvalue = null;

    if(cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
      evaluator.setCurrentRow(row);
      evaluator.evaluate(cell);
      HSSFFormulaEvaluator.CellValue cellValue = evaluator.evaluate(cell);

      switch(cellValue.getCellType()) {
      case HSSFCell.CELL_TYPE_NUMERIC:
        rvalue = cellValue.getNumberValue();
        break;
      }
    } else {
      switch(cell.getCellType()) {
      case HSSFCell.CELL_TYPE_NUMERIC:
        rvalue = cell.getNumericCellValue();
        break;
      }
    }

    return rvalue;
  }

  protected String getTextValue(HSSFRow row, HSSFFormulaEvaluator evaluator, HSSFCell cell) {
    if(cell == null) return null;

    String rvalue = null;

    if(cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
      evaluator.setCurrentRow(row);
      evaluator.evaluate(cell);
      HSSFFormulaEvaluator.CellValue cellValue = evaluator.evaluate(cell);

      switch(cellValue.getCellType()) {
      case HSSFCell.CELL_TYPE_STRING:
        rvalue = cellValue.getRichTextStringValue().getString();
        break;
      }
    } else {
      switch(cell.getCellType()) {
      case HSSFCell.CELL_TYPE_STRING:
        rvalue = cell.getRichStringCellValue().getString();
        break;
      }
    }

    return rvalue;
  }

}
