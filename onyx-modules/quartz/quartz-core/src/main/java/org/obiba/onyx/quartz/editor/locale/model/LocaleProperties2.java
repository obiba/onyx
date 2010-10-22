/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.locale.model;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;

/**
 * Class representing a locale and his list of properties (keys and values)
 */
public class LocaleProperties2 implements Serializable {

  private static final long serialVersionUID = 1L;

  private Locale locale;

  private KeyValue[] keysValues;

  public LocaleProperties2(Locale locale, IQuestionnaireElement element) {
    this.locale = locale;
    List<String> listSourceKeys = new DefaultPropertyKeyProviderImpl().getProperties(element);
    keysValues = new KeyValue[listSourceKeys.size()];
    for(int i = 0; i < listSourceKeys.size(); i++) {
      keysValues[i] = new KeyValue(listSourceKeys.get(i), null);
    }
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public KeyValue[] getKeysValues() {
    return keysValues;
  }

  public void setKeysValues(KeyValue[] keysValues) {
    this.keysValues = keysValues;
  }

  public class KeyValue implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;

    private String fullKey;

    private String value;

    public KeyValue(String key, String value) {
      this.key = key;
      this.value = value;
    }

    /**
     * key without context (label or description)
     * @return
     */
    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    /**
     * key with context (Question.name.label or Category.name.description) MAY BE NOT INITIALIZED YET
     * @return
     */
    public String getFullKey() {
      return fullKey;
    }

    public void setFullKey(String fullKey) {
      this.fullKey = fullKey;
    }
  }
}
