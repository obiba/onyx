/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.obiba.onyx.engine.variable.Category;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDataSet;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

import com.thoughtworks.xstream.XStream;

/**
 * Dump the variables into XML, CSV or XLS streams.
 */
public class VariableStreamer {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(VariableStreamer.class);

  private static final String PATH = "Path";

  private static final String NAME = "Name";

  private static final String KEY = "Key";

  private static final String REFERENCES = "References";

  private static final String CATEGORIES = "Categories";

  private static final String TYPE = "Type";

  private static final String UNIT = "Unit";

  /**
   * The de-serializer.
   */
  private XStream xstream;

  private VariableStreamer() {
    initializeXStream();
  }

  public static String toXML(Variable variable) {
    VariableStreamer streamer = new VariableStreamer();
    return streamer.xstream.toXML(variable);
  }

  public static Variable fromXML(InputStream is) {
    VariableStreamer streamer = new VariableStreamer();
    return setParentInstance((Variable) streamer.xstream.fromXML(is));
  }

  /**
   * Reconstructs the parent instance from a list of children.
   * @param parent
   * @return
   */
  private static Variable setParentInstance(Variable parent) {
    // Relink the parent instance
    if(parent.getVariables() != null) {
      for(Variable child : parent.getVariables()) {
        child.setParent(parent);
        setParentInstance(child);
      }
    }
    return parent;
  }

  //
  // XML
  //

  public static void toXML(Variable variable, OutputStream os) {
    VariableStreamer streamer = new VariableStreamer();
    streamer.xstream.toXML(variable, os);
  }

  public static String toXML(VariableData variableData) {
    VariableStreamer streamer = new VariableStreamer();
    return streamer.xstream.toXML(variableData);
  }

  public static void toXML(VariableData variableData, OutputStream os) {
    VariableStreamer streamer = new VariableStreamer();
    streamer.xstream.toXML(variableData, os);
  }

  public static String toXML(VariableDataSet variableDataSet) {
    VariableStreamer streamer = new VariableStreamer();
    return streamer.xstream.toXML(variableDataSet);
  }

  public static void toXML(VariableDataSet variableDataSet, OutputStream os) {
    VariableStreamer streamer = new VariableStreamer();
    streamer.xstream.toXML(variableDataSet, os);
  }

  private void initializeXStream() {
    xstream = new XStream();
    xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
    xstream.processAnnotations(Variable.class);
    xstream.processAnnotations(Category.class);
    xstream.autodetectAnnotations(true);

    xstream.alias("data", Data.class);
    xstream.useAttributeFor(Data.class, "type");
  }

  //
  // CSV
  //

  public static void toCSV(Variable variable, OutputStream os, IVariablePathNamingStrategy variablePathNamingStrategy) {
    CSVWriter writer = new CSVWriter(new OutputStreamWriter(os));
    writer.writeNext(new String[] { PATH, NAME, KEY, REFERENCES, CATEGORIES, TYPE, UNIT });
    csvWrite(writer, variable, variablePathNamingStrategy);
    try {
      writer.close();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static String toCSV(Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    StringWriter writer = new StringWriter();
    csvWrite(new CSVWriter(writer), variable, variablePathNamingStrategy);
    return writer.toString();
  }

  private static void csvWrite(CSVWriter writer, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    if(variable.getDataType() != null) {
      String[] nextLine = new String[7];
      nextLine[0] = variablePathNamingStrategy.getPath(variable);
      nextLine[1] = variable.getName();
      nextLine[2] = variable.getKey();
      nextLine[3] = getReferences(variable);
      nextLine[4] = getCategories(variable, "\n");
      nextLine[5] = variable.getDataType().toString();
      nextLine[6] = variable.getUnit();
      writer.writeNext(nextLine);
    }
    for(Variable child : variable.getVariables()) {
      csvWrite(writer, child, variablePathNamingStrategy);
    }
  }

  private static String getReferences(Variable variable) {
    String value = "";
    for(String reference : variable.getReferences()) {
      if(value.length() != 0) {
        value += "," + reference;
      } else {
        value = reference;
      }
    }
    return value;
  }

  private static String getCategories(Variable variable, String separator) {
    String value = "";
    for(Category category : variable.getCategories()) {
      String str = category.getName();
      if(category.getAlternateName() != null) {
        str = category.getAlternateName() + "=" + str;
      }
      if(value.length() != 0) {
        value += separator + str;
      } else {
        value = str;
      }
    }
    return value;
  }

  //
  // Excel
  //

  public static void toXLS(Variable variable, OutputStream os, IVariablePathNamingStrategy variablePathNamingStrategy) {
    // create a new workbook
    HSSFWorkbook wb = new HSSFWorkbook();
    // create a new sheet
    HSSFSheet sheet = wb.createSheet("Variables");

    HSSFRow row = sheet.createRow(0);

    HSSFCell cell = row.createCell(0);
    cell.setCellValue(new HSSFRichTextString(PATH));
    cell = row.createCell(1);
    cell.setCellValue(new HSSFRichTextString(NAME));
    cell = row.createCell(2);
    cell.setCellValue(new HSSFRichTextString(KEY));
    cell = row.createCell(3);
    cell.setCellValue(new HSSFRichTextString(REFERENCES));
    cell = row.createCell(4);
    cell.setCellValue(new HSSFRichTextString(CATEGORIES));
    cell = row.createCell(5);
    cell.setCellValue(new HSSFRichTextString(TYPE));
    cell = row.createCell(6);
    cell.setCellValue(new HSSFRichTextString(UNIT));

    HSSFRichTextString str = new HSSFRichTextString();
    str.clearFormatting();

    xlsWrite(wb, sheet, 1, variable, variablePathNamingStrategy);

    // write the workbook to the output stream
    // close our file (don't blow out our file handles
    try {
      wb.write(os);
      os.close();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private static int xlsWrite(HSSFWorkbook wb, HSSFSheet sheet, int rowCount, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    if(variable.getDataType() != null) {
      HSSFRow row = sheet.createRow(rowCount);
      HSSFCell cell = row.createCell(0);
      cell.setCellValue(new HSSFRichTextString(variablePathNamingStrategy.getPath(variable)));
      cell = row.createCell(1);
      cell.setCellValue(new HSSFRichTextString(variable.getName()));
      cell = row.createCell(2);
      cell.setCellValue(new HSSFRichTextString(variable.getKey()));
      cell = row.createCell(3);
      cell.setCellValue(new HSSFRichTextString(getReferences(variable)));
      cell = row.createCell(4);
      HSSFCellStyle cs = wb.createCellStyle();
      cs.setWrapText(true);
      cell.setCellStyle(cs);
      cell.setCellValue(new HSSFRichTextString(getCategories(variable, "\n")));
      cell = row.createCell(5);
      cell.setCellValue(new HSSFRichTextString(variable.getDataType().toString()));
      cell = row.createCell(6);
      cell.setCellValue(new HSSFRichTextString(variable.getUnit()));
      rowCount++;
    }
    for(Variable child : variable.getVariables()) {
      rowCount = xlsWrite(wb, sheet, rowCount, child, variablePathNamingStrategy);
    }
    return rowCount;
  }
}
