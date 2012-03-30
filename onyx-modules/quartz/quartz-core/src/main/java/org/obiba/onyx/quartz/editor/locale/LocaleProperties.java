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

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.springframework.util.Assert;

/**
 *
 */
public class LocaleProperties implements Serializable {

  private static final long serialVersionUID = 1L;

//  private final transient Logger logger = LoggerFactory.getLogger(getClass());

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

  public void addElementLabel(IQuestionnaireElement element, ListMultimap<Locale, KeyValue> elementLabel) {
    elementLabels.put(element, elementLabel);
  }

  public void addElementLabels(IQuestionnaireElement element, Locale locale, String key, String value,
      boolean replaceIfExists) {
    Assert.notNull(element);
    Assert.notNull(locale);
    ListMultimap<Locale, KeyValue> labels = elementLabels.get(element);
    if(labels == null) {
      labels = ArrayListMultimap.create();
      elementLabels.put(element, labels);
    }
    KeyValue existing = getKeyValue(element, locale, key);
    if(existing == null) {
      labels.put(locale, new KeyValue(key, value));
    } else if(replaceIfExists) {
      existing.setValue(value);
    }
  }

  public void removeElementLabels(IQuestionnaireElement element) {
    elementLabels.remove(element);
  }

  public boolean hasElementLabels(IQuestionnaireElement element) {
    return elementLabels.containsKey(element);
  }

  public ListMultimap<Locale, KeyValue> getElementLabels(IQuestionnaireElement element) {
    return elementLabels.get(element);
  }

  public KeyValue getKeyValue(IQuestionnaireElement element, Locale locale, final String key) {
    ListMultimap<Locale, KeyValue> labels = getElementLabels(element);
    if(labels != null) {
      List<KeyValue> keyValueList = labels.get(locale);
      if(keyValueList != null) {
        try {
          return Iterables.find(keyValueList, new Predicate<KeyValue>() {
            @Override
            public boolean apply(@Nullable KeyValue keyValue) {
              return keyValue != null && StringUtils.equals(keyValue.getKey(), key);
            }
          });
        } catch(NoSuchElementException e) {
          // do nothing
        }
      }
    }
    return null;
  }

  public String getLabel(IQuestionnaireElement element, Locale locale, String key) {
    KeyValue keyValue = getKeyValue(element, locale, key);
    return keyValue == null ? null : keyValue.getValue();
  }

  public void removeValue(IQuestionnaireElement element, final String key) {
    for(Locale locale : getLocales()) {
      ListMultimap<Locale, KeyValue> labels = getElementLabels(element);
      if(labels != null) {
        List<KeyValue> keyValueList = labels.get(locale);
        if(keyValueList != null && !keyValueList.isEmpty()) {
          Iterables.removeIf(keyValueList, new Predicate<KeyValue>() {
            @Override
            public boolean apply(@Nullable KeyValue keyValue) {
              return keyValue != null && StringUtils.equals(keyValue.getKey(), key);
            }
          });
        }
      }
    }
  }

  public void removeValue(IQuestionnaireElement element, Locale locale, final String key) {
    ListMultimap<Locale, KeyValue> labels = getElementLabels(element);
    if(labels != null) {
      List<KeyValue> keyValueList = labels.get(locale);
      if(keyValueList != null && !keyValueList.isEmpty()) {
        Iterables.removeIf(keyValueList, new Predicate<KeyValue>() {
          @Override
          public boolean apply(@Nullable KeyValue keyValue) {
            return keyValue != null && StringUtils.equals(keyValue.getKey(), key);
          }
        });
      }
    }
  }

  public void addLocale(Questionnaire questionnaire, Locale locale) {
    if(!locales.contains(locale)) locales.add(locale);
    for(String key : questionnaire.getPropertyKeyProvider().getProperties(questionnaire)) {
      addElementLabels(questionnaire, locale, key, null, false);
    }
  }

  public void removeLocale(@SuppressWarnings("TypeMayBeWeakened") Questionnaire questionnaire, Locale locale) {
    locales.remove(locale);
    getElementLabels(questionnaire).removeAll(locale);
  }

  public void updateValue(IQuestionnaireElement element, String key, String value) {
    for(Locale locale : getLocales()) {
      KeyValue keyValue = getKeyValue(element, locale, key);
      if(keyValue != null) keyValue.setValue(value);
    }
  }

  public void remove(@SuppressWarnings("TypeMayBeWeakened") Questionnaire questionnaire,
      IQuestionnaireElement... elements) {
    if(questionnaire.getName() != null && elements != null) {
      for(IQuestionnaireElement element : elements) {
        removeElementLabels(element);
      }
    }
  }

  @Override
  public String toString() {
    return "locales: " + locales + ", elementLabels: " + elementLabels;
  }

  public void persist(QuestionnaireBundle bundle) {

    List<Locale> questionnaireLocales = bundle.getQuestionnaire().getLocales();
    @SuppressWarnings("unchecked")
    Collection<Locale> deletedLocales = CollectionUtils.subtract(bundle.getAvailableLanguages(), questionnaireLocales);
    for(Locale localeToDelete : deletedLocales) {
      bundle.deleteLanguage(localeToDelete);
    }
    Map<Locale, Properties> localePropertiesMap = toLocalePropertiesMap(bundle.getQuestionnaire());
    if(localePropertiesMap.entrySet().isEmpty()) {
      for(Locale locale : questionnaireLocales) {
        bundle.updateLanguage(locale, new Properties());
      }
    } else {
      for(Map.Entry<Locale, Properties> entry : localePropertiesMap.entrySet()) {
        bundle.updateLanguage(entry.getKey(), entry.getValue());
      }
    }
  }

  private Map<Locale, Properties> toLocalePropertiesMap(Questionnaire questionnaire) {
    Map<Locale, Properties> mapLocaleProperties = new HashMap<Locale, Properties>();
    for(Locale locale : getLocales()) {
      Properties properties = new Properties();
      for(Map.Entry<IQuestionnaireElement, ListMultimap<Locale, KeyValue>> entry : getElementLabels().entrySet()) {
        IQuestionnaireElement element = entry.getKey();
        List<KeyValue> keyValueList = entry.getValue().get(locale);
        for(KeyValue keyValue : keyValueList) {
          String fullKey = questionnaire.getPropertyKeyProvider().getPropertyKey(element, keyValue.getKey());
          String value = keyValue.getValue();
          properties.setProperty(fullKey, value != null ? value.replaceAll("\n", "<br/>") : "");
        }
      }
      mapLocaleProperties.put(locale, properties);
    }
    return mapLocaleProperties;
  }

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static LocaleProperties createForNewQuestionnaire(Questionnaire questionnaire) {
    LocaleProperties localeProperties = new LocaleProperties();
    localeProperties.setLocales(new ArrayList<Locale>(questionnaire.getLocales()));
    List<String> listKeys = questionnaire.getPropertyKeyProvider().getProperties(questionnaire);
    for(Locale locale : localeProperties.getLocales()) {
      for(String key : listKeys) {
        localeProperties.addElementLabels(questionnaire, locale, key, null, false);
      }
    }
    return localeProperties;
  }

  public static class KeyValue implements Serializable {

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

    public KeyValue duplicate() {
      return new KeyValue(key, value);
    }

    @Override
    public String toString() {
      return key + "=" + value;
    }

  }
}
