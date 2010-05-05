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

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;

public class ExcelReaderSupport {
  public static Boolean getBooleanValue(HSSFFormulaEvaluator evaluator, Cell cell) {
    Boolean rvalue = false;

    if(cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
      evaluator.evaluate(cell);
      CellValue cellValue = evaluator.evaluate(cell);

      switch(cellValue.getCellType()) {
      case Cell.CELL_TYPE_BOOLEAN:
        rvalue = cellValue.getBooleanValue();
        break;
      case Cell.CELL_TYPE_STRING:
        rvalue = Boolean.parseBoolean(cellValue.getStringValue());
        break;
      }
    } else {
      switch(cell.getCellType()) {
      case Cell.CELL_TYPE_BOOLEAN:
        rvalue = cell.getBooleanCellValue();
        break;
      case Cell.CELL_TYPE_STRING:
        rvalue = Boolean.parseBoolean(cell.getRichStringCellValue().getString());
        break;
      }
    }

    if(rvalue == null) {
      throw new IllegalArgumentException("Unexpected cell type");
    }

    return rvalue;
  }

  public static Date getDateValue(HSSFFormulaEvaluator evaluator, Cell cell) {
    Date rvalue = null;

    if(cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
      CellValue cellValue = evaluator.evaluate(cell);

      if(cellValue.getCellType() == Cell.CELL_TYPE_NUMERIC) {
        rvalue = cell.getDateCellValue();
      }
    } else {
      if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
        rvalue = cell.getDateCellValue();
      }
    }

    if(rvalue == null) {
      throw new IllegalArgumentException("Unexpected cell type");
    }

    return rvalue;
  }

  public static Double getNumericValue(HSSFFormulaEvaluator evaluator, Cell cell) {
    Double rvalue = null;

    if(cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
      CellValue cellValue = evaluator.evaluate(cell);

      if(cellValue.getCellType() == Cell.CELL_TYPE_NUMERIC) {
        rvalue = cellValue.getNumberValue();
      }
    } else {
      if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
        rvalue = cell.getNumericCellValue();
      }
    }

    if(rvalue == null) {
      throw new IllegalArgumentException("Unexpected cell type");
    }

    return rvalue;
  }

  public static String getTextValue(HSSFFormulaEvaluator evaluator, Cell cell) {
    String rvalue = null;

    if(cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
      CellValue cellValue = evaluator.evaluate(cell);

      if(cellValue.getCellType() == Cell.CELL_TYPE_STRING) {
        rvalue = cellValue.getStringValue();
      }
    } else {
      switch(cell.getCellType()) {
      case Cell.CELL_TYPE_STRING:
        rvalue = cell.getRichStringCellValue().getString();
        break;
      case Cell.CELL_TYPE_NUMERIC:
        // If the cell type is NUMERIC, cast the value as a long and return it as a String.
        rvalue = (Long.valueOf((long) cell.getNumericCellValue())).toString();
        break;
      case Cell.CELL_TYPE_BLANK:
        rvalue = "";
      }
    }

    if(rvalue == null) {
      throw new IllegalArgumentException("Unexpected cell type");
    }

    return rvalue;
  }

  public static boolean containsWhitespace(HSSFFormulaEvaluator evaluator, Cell cell) {
    boolean containsWhitespace = false;

    String textValue = null;

    if(cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
      CellValue cellValue = evaluator.evaluate(cell);

      if(cellValue.getCellType() == Cell.CELL_TYPE_STRING) {
        textValue = cellValue.getStringValue();
      }
    } else if(cell.getCellType() == Cell.CELL_TYPE_STRING) {
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