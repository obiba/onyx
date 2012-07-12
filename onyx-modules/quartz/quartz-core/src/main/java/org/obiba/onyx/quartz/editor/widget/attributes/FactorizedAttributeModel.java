package org.obiba.onyx.quartz.editor.widget.attributes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * represents attributes factorize by the pair namespace/name
 */
public class FactorizedAttributeModel implements Serializable {

  private String namespace;

  private String name;

  private Map<Locale, IModel<String>> values;

  public FactorizedAttributeModel(List<Locale> locales) {
    values = new HashMap<Locale, IModel<String>>();
    values.put(null, new Model<String>());
    for(Locale locale : locales) {
      values.put(locale, new Model<String>());
    }

  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<Locale, IModel<String>> getValues() {
    return values;
  }

  public void setValue(Locale locale, String value) {
    if(values == null) {
      values = new HashMap<Locale, IModel<String>>();
    }
    if(values.containsKey(locale)) {
      values.get(locale).setObject(value);
    } else {
      values.put(locale, new Model<String>(value));
    }
  }
}
