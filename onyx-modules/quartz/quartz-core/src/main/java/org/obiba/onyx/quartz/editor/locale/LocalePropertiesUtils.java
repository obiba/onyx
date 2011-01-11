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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;

public class LocalePropertiesUtils {

  private transient final Logger logger = LoggerFactory.getLogger(getClass());

  private QuestionnaireBundleManager questionnaireBundleManager;

  public LocaleProperties load(Questionnaire questionnaire, IQuestionnaireElement... elements) {
    LocaleProperties localeProperties = new LocaleProperties();
    load(localeProperties, questionnaire, elements);
    return localeProperties;
  }

  public void load(LocaleProperties localeProperties, Questionnaire questionnaire, IQuestionnaireElement... elements) {
    if(questionnaire.getName() == null) return;
    localeProperties.setLocales(new ArrayList<Locale>(questionnaire.getLocales()));
    QuestionnaireBundle bundle = questionnaireBundleManager.getBundle(questionnaire.getName());
    if(bundle != null) bundle.clearMessageSourceCache();

    for(IQuestionnaireElement element : elements) {
      List<String> listKeys = questionnaire.getPropertyKeyProvider().getProperties(element);
      for(Locale locale : localeProperties.getLocales()) {
        for(String key : listKeys) {
          String value = null;
          try {
            if(element.getName() != null) {
              value = QuestionnaireStringResourceModelHelper.getMessage(bundle, element, key, new Object[0], locale);
            }
          } catch(Exception e) {
            if(logger.isDebugEnabled()) logger.debug(e.getMessage(), e);
          }
          localeProperties.addElementLabels(element, locale, key, value);
        }
      }
    }
  }

  public void remove(LocaleProperties localeProperties, Questionnaire questionnaire, IQuestionnaireElement... elements) {
    if(questionnaire.getName() == null) return;
    for(IQuestionnaireElement element : elements) {
      localeProperties.removeElementLabels(element);
    }
  }

  public void updateValue(LocaleProperties localeProperties, IQuestionnaireElement element, final String key, String value) {
    for(Locale locale : localeProperties.getLocales()) {
      Iterable<KeyValue> iterableEqualKey = Iterables.filter(localeProperties.getElementLabels(element).get(locale), new Predicate<KeyValue>() {

        @Override
        public boolean apply(KeyValue input) {
          return input.getKey().equals(key);
        }
      });
      iterableEqualKey.iterator().next().setValue(value);
    }
  }

  public LocaleProperties loadForNewQuestionnaire(Questionnaire questionnaire) {
    LocaleProperties localeProperties = new LocaleProperties();
    localeProperties.setLocales(new ArrayList<Locale>(questionnaire.getLocales()));
    List<String> listKeys = questionnaire.getPropertyKeyProvider().getProperties(questionnaire);
    for(Locale locale : localeProperties.getLocales()) {
      for(String key : listKeys) {
        localeProperties.addElementLabels(questionnaire, locale, key, null);
      }
    }
    return localeProperties;
  }

  private Map<Locale, Properties> toLocalePropertiesMap(LocaleProperties localeProperties, Questionnaire questionnaire) {
    Map<Locale, Properties> mapLocaleProperties = new HashMap<Locale, Properties>();
    for(Locale locale : localeProperties.getLocales()) {
      Properties properties = new Properties();
      for(Entry<IQuestionnaireElement, ListMultimap<Locale, KeyValue>> entry : localeProperties.getElementLabels().entrySet()) {
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

  public void persist(final QuestionnaireBundle bundle, LocaleProperties localeProperties) {

    List<Locale> questionnaireLocales = bundle.getQuestionnaire().getLocales();
    @SuppressWarnings("unchecked")
    Collection<Locale> deletedLocales = CollectionUtils.subtract(bundle.getAvailableLanguages(), questionnaireLocales);
    for(Locale localeToDelete : deletedLocales) {
      bundle.deleteLanguage(localeToDelete);
    }
    Map<Locale, Properties> localePropertiesMap = toLocalePropertiesMap(localeProperties, bundle.getQuestionnaire());
    if(localePropertiesMap.entrySet().isEmpty()) {
      for(Locale locale : questionnaireLocales) {
        bundle.updateLanguage(locale, new Properties());
      }
    } else {
      for(Entry<Locale, Properties> entry : localePropertiesMap.entrySet()) {
        bundle.updateLanguage(entry.getKey(), entry.getValue());
      }
    }
  }

  @Required
  public void setQuestionnaireBundleManager(QuestionnaireBundleManager questionnaireBundleManager) {
    this.questionnaireBundleManager = questionnaireBundleManager;
  }
}
