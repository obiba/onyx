/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.io.support;

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;

public class ExcelReaderSupport {
  public static Boolean getBooleanValue(HSSFFormulaEvaluator evaluator, HSSFCell cell) {
    if(cell == null) return null;

    Boolean rvalue = false;

    if(cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
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
        rvalue = Boolean.parseBoolean(cellValue.getStringValue());
        break;
      }
    } else {
      switch(cell.getCellType()) {
      case HSSFCell.CELL_TYPE_BOOLEAN:
        rvalue = cell.getBooleanCellValue();
        break;
      case HSSFCell.CELL_TYPE_NUMERIC:
        rvalue = Boolean.parseBoolean(Long.valueOf((long) cell.getNumericCellValue()).toString());
        break;
      case HSSFCell.CELL_TYPE_STRING:
        rvalue = Boolean.parseBoolean(cell.getRichStringCellValue().getString());
        break;
      }
    }

    if(rvalue == null) {
      throw new IllegalArgumentException("Unexpected cell type");
    }

    return rvalue;
  }

  public static Date getDateValue(HSSFFormulaEvaluator evaluator, HSSFCell cell) {
    if(cell == null) return null;

    Date rvalue = null;

    if(cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
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

    if(rvalue == null) {
      throw new IllegalArgumentException("Unexpected cell type");
    }

    return rvalue;
  }

  public static Double getNumericValue(HSSFFormulaEvaluator evaluator, HSSFCell cell) {
    if(cell == null) return null;

    Double rvalue = null;

    if(cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
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

    if(rvalue == null) {
      throw new IllegalArgumentException("Unexpected cell type");
    }

    return rvalue;
  }

  public static String getTextValue(HSSFFormulaEvaluator evaluator, HSSFCell cell) {
    if(cell == null) return null;

    String rvalue = null;

    if(cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
      evaluator.evaluate(cell);
      HSSFFormulaEvaluator.CellValue cellValue = evaluator.evaluate(cell);

      switch(cellValue.getCellType()) {
      case HSSFCell.CELL_TYPE_STRING:
        rvalue = cellValue.getStringValue();
        break;
      }
    } else {
      switch(cell.getCellType()) {
      case HSSFCell.CELL_TYPE_STRING:
        rvalue = cell.getRichStringCellValue().getString();
        break;
      case HSSFCell.CELL_TYPE_NUMERIC:
        // If the cell type is NUMERIC, cast the value as a long and return it as a String.
        rvalue = (new Long((new Double(cell.getNumericCellValue())).longValue())).toString();
      }
    }

    if(rvalue == null) {
      throw new IllegalArgumentException("Unexpected cell type");
    }

    return rvalue;
  }

  public static boolean containsWhitespace(HSSFFormulaEvaluator evaluator, HSSFCell cell) {
    if(cell == null) return false;

    boolean containsWhitespace = false;

    String textValue = null;

    if(cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
      evaluator.evaluate(cell);
      HSSFFormulaEvaluator.CellValue cellValue = evaluator.evaluate(cell);

      switch(cellValue.getCellType()) {
      case HSSFCell.CELL_TYPE_STRING:
        textValue = cellValue.getStringValue();
        break;
      }
    } else if(cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
      textValue = cell.getRichStringCellValue().getString();
    }

    if(textValue != null) {
      if(textValue.trim().length() == 0) {
        containsWhitespace = true;
      }
    }

    return containsWhitespace;
  }
}
