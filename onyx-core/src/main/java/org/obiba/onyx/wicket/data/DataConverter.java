package org.obiba.onyx.wicket.data;

import java.io.Serializable;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converters.DateConverter;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataConverter implements IConverter {

  private static final long serialVersionUID = 3639500916194340039L;

  private static final Logger log = LoggerFactory.getLogger(DataConverter.class);
  
  private DataType type;

  public DataConverter(DataType type) {
    this.type = type;
  }

  public Object convertToObject(String value, Locale locale) {
    
    Data data = null;
    
    try {
    switch(type) {
    case BOOLEAN:
      data = new Data(type, Boolean.valueOf(value));
      break;

    case DATE:
      DateConverter dateConverter = new DateConverter();
      data = new Data(type, (Serializable)dateConverter.convertToObject(value, locale));
      break;

    case DECIMAL:
      data = new Data(type, Double.valueOf(value));
      break;

    case INTEGER:
      data = new Data(type, Long.valueOf(value));
      break;

    case TEXT:
      data = new Data(type, value);
      break;

    case DATA:
      // TODO
      break;

    default:
      break;
    }
    } catch (Exception ex) {
      log.warn(ex.getMessage());
      ConversionException cex = new ConversionException(ex.getMessage());
      throw cex;
    }
    return data;
  }

  public String convertToString(Object value, Locale locale) {
    Data data = (Data) value;
    if(data == null || data.getValue() == null) return null;
    return data.getValue().toString();
  }

}
