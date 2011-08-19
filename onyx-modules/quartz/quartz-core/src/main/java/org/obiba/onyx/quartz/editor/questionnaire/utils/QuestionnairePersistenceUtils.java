/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire.utils;

import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.UniqueQuestionnaireElementNameBuilder;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 *
 */
public class QuestionnairePersistenceUtils {

  // private final Logger log = LoggerFactory.getLogger(getClass());

  private QuestionnaireBundleManager questionnaireBundleManager;

  private LocalePropertiesUtils localePropertiesUtils;

  private QuestionnaireRegister questionnaireRegister;

  public void persist(Questionnaire questionnaire, LocaleProperties localeProperties) throws Exception {

    QuestionnaireBuilder builder = QuestionnaireBuilder.getInstance(questionnaire);
    if(Questionnaire.STANDARD_UI.equals(questionnaire.getUiType())) {
      builder.setStandardUI();
    } else if(Questionnaire.SIMPLIFIED_UI.equals(questionnaire.getUiType())) {
      builder.setSimplifiedUI();
    }

    UniqueQuestionnaireElementNameBuilder.ensureQuestionnaireVariableNamesAreUnique(builder.getQuestionnaire());

    // store xml file
    QuestionnaireBundle bundle = questionnaireBundleManager.createBundle(builder.getQuestionnaire(), false);
    questionnaireBundleManager.flushBundle(bundle);

    // store locales
    if(localeProperties != null) localePropertiesUtils.persist(bundle, localeProperties);

    questionnaireRegister.register(questionnaire);
  }

  public void persist(Questionnaire questionnaire) throws Exception {
    persist(questionnaire, null);
  }

  public void delete(Questionnaire questionnaire) {
    questionnaireBundleManager.deleteBundle(questionnaire);
    questionnaireRegister.unregister(questionnaire);
  }

  @Required
  public void setQuestionnaireBundleManager(QuestionnaireBundleManager questionnaireBundleManager) {
    this.questionnaireBundleManager = questionnaireBundleManager;
  }

  @Required
  public void setLocalePropertiesUtils(LocalePropertiesUtils localePropertiesUtils) {
    this.localePropertiesUtils = localePropertiesUtils;
  }

  @Required
  public void setQuestionnaireRegister(QuestionnaireRegister questionnaireRegister) {
    this.questionnaireRegister = questionnaireRegister;
  }

}
