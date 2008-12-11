/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.util.data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataBuilder {

  private static final Logger log = LoggerFactory.getLogger(DataBuilder.class);

  public static Data build(Serializable value) {
    Class<?> valueClass = value.getClass();

    if(valueClass.isAssignableFrom(Boolean.class)) {
      return buildBoolean((Boolean) value);
    } else if(value instanceof Date) {
      return buildDate((Date) value);
    } else if(valueClass.isAssignableFrom(Double.class) || valueClass.isAssignableFrom(Float.class)) {
      return buildDecimal(value.toString());
    } else if(valueClass.isAssignableFrom(Integer.class) || valueClass.isAssignableFrom(Long.class)) {
      return buildInteger(value.toString());
    } else if(valueClass.isAssignableFrom(String.class)) {
      return buildText((String) value);
    } else {
      throw new IllegalArgumentException("Cannot determine DataType for " + value);
    }
  }

  public static Data buildBoolean(Boolean booleanValue) {
    return new Data(DataType.BOOLEAN, booleanValue);
  }

  public static Data buildBoolean(String booleanValue) {
    return new Data(DataType.BOOLEAN, Boolean.parseBoolean(booleanValue));
  }

  public static Data buildDate(Date date) {
    return new Data(DataType.DATE, date);
  }

  public static Data buildDate(String date) {
    try {
      return new Data(DataType.DATE, SimpleDateFormat.getInstance().parse(date));
    } catch(ParseException e) {
      throw new IllegalArgumentException("Unable to parse date string: " + date, e);
    }
  }

  public static Data buildDecimal(Double doubleValue) {
    return new Data(DataType.DECIMAL, doubleValue);
  }

  public static Data buildDecimal(String doubleValue) {
    return new Data(DataType.DECIMAL, Double.parseDouble(doubleValue));
  }

  public static Data buildDecimal(Float floatValue) {
    return new Data(DataType.DECIMAL, floatValue);
  }

  public static Data buildInteger(Long longValue) {
    return new Data(DataType.INTEGER, longValue);
  }

  public static Data buildInteger(String longValue) {
    return new Data(DataType.INTEGER, Long.parseLong(longValue));
  }

  public static Data buildInteger(Integer integerValue) {
    return new Data(DataType.INTEGER, integerValue);
  }

  public static Data buildText(String text) {
    return new Data(DataType.TEXT, text);
  }

  public static Data build(DataType type, String value) {
    switch(type) {
    case BOOLEAN:
      return buildBoolean(value);
    case DECIMAL:
      return buildDecimal(value);
    case INTEGER:
      return buildInteger(value);
    case TEXT:
      return buildText(value);
    case DATE:
      return buildDate(value);
    }
    return null;
  }

  public static Data buildBinary(InputStream inputStream) {

    ByteArrayOutputStream convertedStream = new ByteArrayOutputStream();
    byte[] readBuffer = new byte[1024];
    int bytesRead;

    try {
      while((bytesRead = inputStream.read(readBuffer)) > 0) {
        convertedStream.write(readBuffer, 0, bytesRead);
      }
    } catch(IOException couldNotReadStream) {
      log.error("Error while reading binary data stream", couldNotReadStream);
      throw new RuntimeException(couldNotReadStream);
    } finally {
      try {
        inputStream.close();
      } catch(IOException e) {
        log.warn("Could not close inputStream", e);
      }
    }

    return new Data(DataType.DATA, convertedStream.toByteArray());
  }

  public static Data buildBinary(File file) {

    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(file);
    } catch(FileNotFoundException fileNotFound) {
      log.error("The file specified was not found", fileNotFound);

      try {
        if(inputStream != null) inputStream.close();
      } catch(IOException e) {
        log.warn("Could not close inputStream", e);
      }

      throw new RuntimeException(fileNotFound);
    }
    return buildBinary(inputStream);

  }

}
