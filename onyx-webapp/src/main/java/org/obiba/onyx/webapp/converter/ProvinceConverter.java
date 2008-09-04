package org.obiba.onyx.webapp.converter;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.wicket.util.convert.IConverter;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Province;

public class ProvinceConverter implements IConverter {

  private static final long serialVersionUID = 1L;

  @Override
  public Object convertToObject(String value, Locale locale) {
    return Province.valueOf(value);
  }

  @Override
  public String convertToString(Object value, Locale locale) {
    String stringValue = null;

    if(value != null) {
      if(value instanceof Province) {
        Province provinceValue = (Province) value;
        stringValue = provinceValue.toString();
      }
    }

    return stringValue;
  }

}
