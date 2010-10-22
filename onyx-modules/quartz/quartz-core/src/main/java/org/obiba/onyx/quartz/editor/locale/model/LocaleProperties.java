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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

/**
 *
 */
public class LocaleProperties implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<Locale> locales = new ArrayList<Locale>();

  private Map<IQuestionnaireElement, ElementLabels> elementLabels = new HashMap<IQuestionnaireElement, ElementLabels>();

  public List<Locale> getLocales() {
    return locales;
  }

  public void setLocales(List<Locale> locales) {
    this.locales = locales;
  }

  public Map<IQuestionnaireElement, ElementLabels> getElementLabels() {
    return elementLabels;
  }

  public void setElementLabels(Map<IQuestionnaireElement, ElementLabels> elementLabels) {
    this.elementLabels = elementLabels;
  }

  public void addElementLabels(IQuestionnaireElement element, Locale locale, String key, String value) {
    if(elementLabels.get(element) == null) {
      elementLabels.put(element, new ElementLabels());
    }
    elementLabels.get(element).addKeyValue(locale, key, value);
  }

  public class ElementLabels implements Serializable {

    private static final long serialVersionUID = 1L;

    private ListMultimap<Locale, KeyValue> labels = LinkedListMultimap.create();

    public ListMultimap<Locale, KeyValue> getLabels() {
      return labels;
    }

    public void setLabels(ListMultimap<Locale, KeyValue> labels) {
      this.labels = labels;
    }

    public void addKeyValue(Locale locale, String key, String value) {
      labels.put(locale, new KeyValue(key, value));
    }
  }

  public class KeyValue implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;

    private String value;

    public KeyValue(String key, String value) {
      this.key = key;
      this.value = value;
    }

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
  }
}
