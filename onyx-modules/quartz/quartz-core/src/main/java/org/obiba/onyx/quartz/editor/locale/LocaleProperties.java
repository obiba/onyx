/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.locale;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.springframework.util.Assert;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.ListMultimap;

/**
 *
 */
public class LocaleProperties implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<Locale> locales = new ArrayList<Locale>();

  private Map<IQuestionnaireElement, ListMultimap<Locale, KeyValue>> elementLabels = new HashMap<IQuestionnaireElement, ListMultimap<Locale, KeyValue>>();

  public List<Locale> getLocales() {
    return locales;
  }

  public void setLocales(List<Locale> locales) {
    this.locales = locales;
  }

  public Map<IQuestionnaireElement, ListMultimap<Locale, KeyValue>> getElementLabels() {
    return elementLabels;
  }

  public void setElementLabels(Map<IQuestionnaireElement, ListMultimap<Locale, KeyValue>> elementLabels) {
    this.elementLabels = elementLabels;
  }

  public void addElementLabels(IQuestionnaireElement element, Locale locale, String key, String value) {
    Assert.notNull(element);
    Assert.notNull(locale);
    ListMultimap<Locale, KeyValue> labels = elementLabels.get(element);
    if(labels == null) {
      labels = ArrayListMultimap.create();
      elementLabels.put(element, labels);
    }
    // TODO remove old keyValue before inserting the new one
    final KeyValue keyValue = new KeyValue(key, value);
    Collection<KeyValue> filter = Collections2.filter(labels.get(locale), new Predicate<KeyValue>() {

      @Override
      public boolean apply(KeyValue input) {
        return input.getKey().equals(keyValue.getKey());
      }

    });
    if(filter.isEmpty()) {
      labels.put(locale, keyValue);
    }
  }

  public void removeElementLabels(IQuestionnaireElement element) {
    elementLabels.remove(element);
  }

  public ListMultimap<Locale, KeyValue> getElementLabels(IQuestionnaireElement element) {
    return elementLabels.get(element);
  }

  public void addLocale(Questionnaire questionnaire, Locale locale) {
    if(!locales.contains(locale)) locales.add(locale);
    List<String> listKeys = new DefaultPropertyKeyProviderImpl().getProperties(questionnaire);
    for(String key : listKeys) {
      addElementLabels(questionnaire, locale, key, null);
    }
  }

  public void removeLocale(Questionnaire questionnaire, Locale locale) {
    locales.remove(locale);
    getElementLabels(questionnaire).removeAll(locale);
  }

  @Override
  public String toString() {
    return "locales: " + locales + ", elementLabels: " + elementLabels;
  }

  public class KeyValue implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;

    private String value;

    public KeyValue(String key, String value) {
      Assert.notNull(key);
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

    @Override
    public String toString() {
      return key + "=" + value;
    }

  }
}
