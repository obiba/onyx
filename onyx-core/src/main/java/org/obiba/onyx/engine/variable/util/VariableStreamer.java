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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.obiba.onyx.engine.variable.Attribute;
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

  private static final String CATEGORIES = "Categories";

  private static final String TYPE = "Type";

  private static final String UNIT = "Unit";

  private static final String MULTIPLE = "Multiple";

  private static final String REPEATABLE = "Repeatable";

  private static final String LOCALE = "Locale";

  private static final String VALUE = "Value";

  /**
   * The de-serializer.
   */
  private XStream xstream;

  private VariableStreamer() {
    initializeXStream();
  }

  /**
   * XML deserialize from the stream in UTF-8 encoding.
   * @param is
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> T fromXML(InputStream is) {
    Object obj = fromXML(is, "UTF-8");
    return (T) obj;
  }

  /**
   * XML deserialize from the stream in the given encoding.
   * @param is
   * @param encoding
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> T fromXML(InputStream is, String encoding) {
    VariableStreamer streamer = new VariableStreamer();
    T obj = (T) streamer.xstream.fromXML(createReader(is, encoding));
    if(obj instanceof Variable) {
      setParentInstance((Variable) obj);
    }
    return obj;
  }

  private static Reader createReader(InputStream is, String encoding) {
    return new InputStreamReader(is, Charset.forName(encoding));
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

  /**
   * XML serialize variable in a string.
   * @param variable
   */
  public static String toXML(Variable variable) {
    VariableStreamer streamer = new VariableStreamer();
    return streamer.xstream.toXML(variable);
  }

  /**
   * XML serialize variable in a stream using given encoding.
   * @param variable
   * @param os
   * @param encoding
   */
  public static void toXML(Variable variable, OutputStream os, String encoding) {
    VariableStreamer streamer = new VariableStreamer();
    streamer.xstream.toXML(variable, createWriter(os, encoding));
  }

  /**
   * XML serialize variable in a stream using UTF-8 encoding.
   * @param variable
   * @param os
   */
  public static void toXML(Variable variable, OutputStream os) {
    toXML(variable, os, "UTF-8");
  }

  /**
   * XML serialize variable in a string.
   * @param variableData
   * @return
   */
  public static String toXML(VariableData variableData) {
    VariableStreamer streamer = new VariableStreamer();
    return streamer.xstream.toXML(variableData);
  }

  /**
   * XML serialize variable data set in a stream using given encoding.
   * @param variableDataSet
   * @param os
   * @param encoding
   */
  public static void toXML(VariableDataSet variableDataSet, OutputStream os, String encoding) {
    VariableStreamer streamer = new VariableStreamer();
    streamer.xstream.toXML(variableDataSet, createWriter(os, encoding));
  }

  /**
   * XML serialize variable data set in a stream UTF-8 encoding.
   * @param variableDataSet
   * @param os
   */
  public static void toXML(VariableDataSet variableDataSet, OutputStream os) {
    toXML(variableDataSet, os, "UTF-8");
  }

  /**
   * XML serialize variable data set in a string.
   * @param variableDataSet
   * @return
   */
  public static String toXML(VariableDataSet variableDataSet) {
    VariableStreamer streamer = new VariableStreamer();
    return streamer.xstream.toXML(variableDataSet);
  }

  /**
   * Wrap stream in a writer and add xml header for the given encoding.
   * @param os
   * @param encoding
   * @return
   */
  private static Writer createWriter(OutputStream os, String encoding) {
    Writer writer = new OutputStreamWriter(os, Charset.forName(encoding));
    try {
      writer.write("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n");
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return writer;
  }

  private void initializeXStream() {
    xstream = new XStream();
    xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
    xstream.processAnnotations(Variable.class);
    xstream.processAnnotations(Attribute.class);
    xstream.processAnnotations(Category.class);
    xstream.processAnnotations(VariableDataSet.class);
    xstream.processAnnotations(VariableData.class);
    xstream.autodetectAnnotations(true);

    xstream.alias("data", Data.class);
    xstream.useAttributeFor(Data.class, "type");
  }

  //
  // CSV
  //

  public static void toCSV(Variable variable, OutputStream os, IVariablePathNamingStrategy variablePathNamingStrategy) {
    CSVWriter writer = new CSVWriter(new OutputStreamWriter(os));
    writer.writeNext(new String[] { PATH, NAME, CATEGORIES, TYPE, UNIT, MULTIPLE, REPEATABLE, KEY });
    csvWrite(writer, variable, variablePathNamingStrategy);
    try {
      writer.close();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void toCSVAttributes(Variable variable, OutputStream os, IVariablePathNamingStrategy variablePathNamingStrategy) {
    CSVWriter writer = new CSVWriter(new OutputStreamWriter(os));
    writer.writeNext(new String[] { PATH, KEY, LOCALE, VALUE });
    csvAttributesWrite(writer, variable, variablePathNamingStrategy);
    try {
      writer.close();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static String toCSV(Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    CSVWriter writer = new CSVWriter(new StringWriter());
    writer.writeNext(new String[] { PATH, NAME, CATEGORIES, TYPE, UNIT, MULTIPLE, REPEATABLE, KEY });
    csvWrite(writer, variable, variablePathNamingStrategy);
    return writer.toString();
  }

  public static String toCSVAttributes(Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    CSVWriter writer = new CSVWriter(new StringWriter());
    writer.writeNext(new String[] { PATH, KEY, LOCALE, VALUE });
    csvAttributesWrite(writer, variable, variablePathNamingStrategy);
    return writer.toString();
  }

  private static void csvWrite(CSVWriter writer, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    if(variable.getDataType() != null) {
      String[] nextLine = new String[8];
      nextLine[0] = variablePathNamingStrategy.getPath(variable);
      nextLine[1] = variable.getName();
      nextLine[2] = getCategories(variable, "\n");
      nextLine[3] = variable.getDataType().toString();
      nextLine[4] = variable.getUnit();
      nextLine[5] = Boolean.toString(variable.isMultiple());
      nextLine[6] = Boolean.toString(variable.isRepeatable());
      nextLine[7] = variable.getKey();
      writer.writeNext(nextLine);
    }
    for(Variable child : variable.getVariables()) {
      csvWrite(writer, child, variablePathNamingStrategy);
    }
  }

  private static void csvAttributesWrite(CSVWriter writer, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    if(variable.getAttributes().size() > 0) {
      String path = variablePathNamingStrategy.getPath(variable);
      for(Attribute attribute : variable.getAttributes()) {
        if(attribute.getValue() != null) {
          String[] nextLine = new String[4];
          nextLine[0] = path;
          nextLine[1] = attribute.getKey();
          if(attribute.getLocale() != null) {
            nextLine[2] = attribute.getLocale().toString();
          }
          nextLine[3] = attribute.getValue().toString();
          writer.writeNext(nextLine);
        }
      }
    }
    for(Variable child : variable.getVariables()) {
      csvAttributesWrite(writer, child, variablePathNamingStrategy);
    }
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
    cell.setCellValue(new HSSFRichTextString(CATEGORIES));
    cell = row.createCell(3);
    cell.setCellValue(new HSSFRichTextString(TYPE));
    cell = row.createCell(4);
    cell.setCellValue(new HSSFRichTextString(UNIT));
    cell = row.createCell(5);
    cell.setCellValue(new HSSFRichTextString(MULTIPLE));
    cell = row.createCell(6);
    cell.setCellValue(new HSSFRichTextString(REPEATABLE));
    cell = row.createCell(7);
    cell.setCellValue(new HSSFRichTextString(KEY));

    HSSFCellStyle categoryTextStyle = wb.createCellStyle();
    categoryTextStyle.setWrapText(true);

    xlsWrite(wb, sheet, categoryTextStyle, 1, variable, variablePathNamingStrategy);

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

  public static void toXLSAttributes(Variable variable, OutputStream os, IVariablePathNamingStrategy variablePathNamingStrategy) {
    // create a new workbook
    HSSFWorkbook wb = new HSSFWorkbook();
    // create a new sheet
    HSSFSheet sheet = wb.createSheet("Variables Attributes");
    HSSFRow row = sheet.createRow(0);
    HSSFCell cell = row.createCell(0);

    cell.setCellValue(new HSSFRichTextString(PATH));
    cell = row.createCell(1);
    cell.setCellValue(new HSSFRichTextString(KEY));
    cell = row.createCell(2);
    cell.setCellValue(new HSSFRichTextString(LOCALE));
    cell = row.createCell(3);
    cell.setCellValue(new HSSFRichTextString(VALUE));

    xlsAttributeWrite(wb, sheet, 1, variable, variablePathNamingStrategy);

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

  private static int xlsWrite(HSSFWorkbook wb, HSSFSheet sheet, HSSFCellStyle categoryTextStyle, int rowCount, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    if(variable.getDataType() != null) {
      HSSFRow row = sheet.createRow(rowCount);
      HSSFCell cell = row.createCell(0);
      cell.setCellValue(new HSSFRichTextString(variablePathNamingStrategy.getPath(variable)));
      cell = row.createCell(1);
      cell.setCellValue(new HSSFRichTextString(variable.getName()));
      cell = row.createCell(2);
      cell.setCellStyle(categoryTextStyle);
      cell.setCellValue(new HSSFRichTextString(getCategories(variable, "\n")));
      cell = row.createCell(3);
      cell.setCellValue(new HSSFRichTextString(variable.getDataType().toString()));
      cell = row.createCell(4);
      cell.setCellValue(new HSSFRichTextString(variable.getUnit()));
      if(variable.isMultiple()) {
        cell = row.createCell(5);
        cell.setCellValue(new HSSFRichTextString("true"));
      }
      if(variable.isRepeatable()) {
        cell = row.createCell(6);
        cell.setCellValue(new HSSFRichTextString("true"));
      }
      cell = row.createCell(7);
      cell.setCellValue(new HSSFRichTextString(variable.getKey()));
      rowCount++;
    }
    for(Variable child : variable.getVariables()) {
      rowCount = xlsWrite(wb, sheet, categoryTextStyle, rowCount, child, variablePathNamingStrategy);
    }
    return rowCount;
  }

  private static int xlsAttributeWrite(HSSFWorkbook wb, HSSFSheet sheet, int rowCount, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    if(variable.getAttributes().size() > 0) {
      for(Attribute attribute : variable.getAttributes()) {
        if(attribute.getValue() != null) {
          HSSFRow row = sheet.createRow(rowCount++);
          HSSFCell cell = row.createCell(0);
          cell.setCellValue(new HSSFRichTextString(variablePathNamingStrategy.getPath(variable)));
          cell = row.createCell(1);
          cell.setCellValue(new HSSFRichTextString(attribute.getKey()));
          if(attribute.getLocale() != null) {
            cell = row.createCell(2);
            cell.setCellValue(new HSSFRichTextString(attribute.getLocale().toString()));
          }
          cell = row.createCell(3);
          cell.setCellValue(new HSSFRichTextString(attribute.getValue().toString()));
        }
      }
    }
    for(Variable child : variable.getVariables()) {
      rowCount = xlsAttributeWrite(wb, sheet, rowCount, child, variablePathNamingStrategy);
    }
    return rowCount;
  }
}
