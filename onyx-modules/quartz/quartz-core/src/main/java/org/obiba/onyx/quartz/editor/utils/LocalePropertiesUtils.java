/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties.ElementLabels;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties.KeyValue;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties2;
import org.springframework.beans.factory.annotation.Required;

public class LocalePropertiesUtils {

  private QuestionnaireBundleManager questionnaireBundleManager;

  public List<LocaleProperties2> loadLocaleProperties(IModel<? extends IQuestionnaireElement> elementModel, IModel<Questionnaire> questionnaireModel) {
    List<LocaleProperties2> listLocaleProperties = new ArrayList<LocaleProperties2>();
    final Questionnaire questionnaire = questionnaireModel.getObject();
    for(Locale locale : questionnaire.getLocales()) {
      LocaleProperties2 localeProperties = new LocaleProperties2(locale, elementModel.getObject());
      for(LocaleProperties2.KeyValue property : localeProperties.getKeysValues()) {
        if(StringUtils.isNotBlank(elementModel.getObject().getName())) {
          QuestionnaireBundle bundle = questionnaireBundleManager.getClearedMessageSourceCacheBundle(questionnaire.getName());
          if(bundle != null) {
            property.setValue(QuestionnaireStringResourceModelHelper.getNonRecursiveResolutionMessage(bundle, elementModel.getObject(), property.getKey(), new Object[0], locale));
          }
        }
      }
      listLocaleProperties.add(localeProperties);
    }
    return listLocaleProperties;
  }

  public LocaleProperties load(Questionnaire questionnaire, IQuestionnaireElement... elements) {
    return load(questionnaire, new LocaleProperties(), elements);
  }

  public LocaleProperties load(Questionnaire questionnaire, LocaleProperties localeProperties, IQuestionnaireElement... elements) {
    localeProperties.setLocales(questionnaire.getLocales());
    for(IQuestionnaireElement element : elements) {
      QuestionnaireBundle bundle = questionnaireBundleManager.getClearedMessageSourceCacheBundle(questionnaire.getName());
      List<String> listKeys = new DefaultPropertyKeyProviderImpl().getProperties(element);
      for(Locale locale : localeProperties.getLocales()) {
        for(String key : listKeys) {
          if(bundle != null) {
            String value = QuestionnaireStringResourceModelHelper.getNonRecursiveResolutionMessage(bundle, element, key, new Object[0], locale);
            localeProperties.addElementLabels(element, locale, key, value);
          }
        }
      }
    }
    return localeProperties;
  }

  public void transformKeyToFullKey(Questionnaire questionnaire, LocaleProperties localeProperties) {
    DefaultPropertyKeyProviderImpl defaultPropertyKeyProviderImpl = new DefaultPropertyKeyProviderImpl();
    for(Entry<IQuestionnaireElement, ElementLabels> entry : localeProperties.getElementLabels().entrySet()) {
      for(KeyValue keyValue : entry.getValue().getLabels().values()) {
        keyValue.setValue(defaultPropertyKeyProviderImpl.getPropertyKey(entry.getKey(), keyValue.getKey()));
      }
    }
  }

  /**
   * Change key like 'label' to '<type>.<name>.label'
   * @param localeProperties
   */
  private void localePropertiesToMapToPersist(LocaleProperties localeProperties) {
    Map<Locale, Properties> mapLocaleProperties = new HashMap<Locale, Properties>();
    for(Locale locale : localeProperties.getLocales()) {
      for(Entry<IQuestionnaireElement, ElementLabels> entry : localeProperties.getElementLabels().entrySet()) {
        for(Entry<Locale, KeyValue> entryLocaleKeyValue : entry.getValue().getLabels().entries()) {
          if(locale.equals(entryLocaleKeyValue.getKey())) {

            // for(int i = 0; i < entryLocaleKeyValue.getValue().length; i++) {
            // String fullKey = localeProp.getKeysValues()[i].getFullKey();
            // String value = localeProp.getKeysValues()[i].getValue();
            // properties.setProperty(fullKey, value != null ? value : "");
            // }

          }

        }
      }
    }

  }

  @Required
  public void setQuestionnaireBundleManager(QuestionnaireBundleManager questionnaireBundleManager) {
    this.questionnaireBundleManager = questionnaireBundleManager;
  }
}
