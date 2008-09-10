package org.obiba.onyx.webapp.converter;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.wicket.util.convert.IConverter;
import org.obiba.onyx.core.domain.participant.Gender;

public class GenderConverter implements IConverter {

  private static final long serialVersionUID = 1L;

  public Object convertToObject(String value, Locale locale) {
    return Gender.valueOf(value);
  }

  public String convertToString(Object value, Locale locale) {
    String stringValue = null;

    if(value != null) {
      if(value instanceof Gender) {
        Gender genderValue = (Gender) value;
        ResourceBundle resourceBundle = ResourceBundle.getBundle("org.obiba.onyx.webapp.OnyxApplication", locale);
        stringValue = resourceBundle.getString("Gender."+(genderValue.equals(Gender.FEMALE) ? "F" : "M"));
      }
    }

    return stringValue;
  }

}
