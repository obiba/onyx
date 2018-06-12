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
import java.util.List;
import java.util.Locale;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

public class LocalePropertiesUtils {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private QuestionnaireBundleManager questionnaireBundleManager;

  public LocaleProperties load(Questionnaire questionnaire, IQuestionnaireElement... elements) {
    LocaleProperties localeProperties = new LocaleProperties();
    load(localeProperties, questionnaire, elements);
    return localeProperties;
  }

  public void load(LocaleProperties localeProperties, Questionnaire questionnaire, IQuestionnaireElement... elements) {
    Assert.notNull(questionnaire);
    if(questionnaire.getName() == null) return;
    localeProperties.setLocales(new ArrayList<Locale>(questionnaire.getLocales()));
    QuestionnaireBundle bundle = questionnaireBundleManager.getBundle(questionnaire.getName());
    if(bundle != null) bundle.clearMessageSourceCache();

    if(elements != null) {
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
              logger.debug(e.getMessage(), e);
            }
            localeProperties.addElementLabels(element, locale, key, value, false);
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
