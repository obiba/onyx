/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire;

import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl.QuestionnaireBundleManagerImpl;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.UniqueQuestionnaireElementNameBuilder;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 *
 */
public class QuestionnairePersistenceUtils {

  // private final Logger log = LoggerFactory.getLogger(getClass());

  private QuestionnaireBundleManager questionnaireBundleManager;

  private LocalePropertiesUtils localePropertiesUtils;

  private QuestionnaireBuilder createBuilder(Questionnaire questionnaire) {
    QuestionnaireBuilder builder = QuestionnaireBuilder.getInstance(questionnaire);
    if(Questionnaire.STANDARD_UI.equals(questionnaire.getUiType())) {
      builder.setStandardUI();
    } else if(Questionnaire.SIMPLIFIED_UI.equals(questionnaire.getUiType())) {
      builder.setSimplifiedUI();
    }
    return builder;
  }

  private void persist(QuestionnaireBuilder builder, LocaleProperties localeProperties) throws Exception {

    UniqueQuestionnaireElementNameBuilder.ensureQuestionnaireVariableNamesAreUnique(builder.getQuestionnaire());

    // Create the bundle manager.
    QuestionnaireBundleManager writeBundleManager = new QuestionnaireBundleManagerImpl(questionnaireBundleManager.getRootDir());
    ((QuestionnaireBundleManagerImpl) writeBundleManager).setPropertyKeyProvider(builder.getPropertyKeyProvider());
    ((QuestionnaireBundleManagerImpl) writeBundleManager).setResourceLoader(new PathMatchingResourcePatternResolver());

    final Questionnaire questionnaire = builder.getQuestionnaire();

    // store xml file and locales
    QuestionnaireBundle bundle = writeBundleManager.createBundle(questionnaire);
    localePropertiesUtils.persist(bundle, localeProperties);

  }

  public void persist(Questionnaire questionnaire, LocaleProperties localeProperties) throws Exception {
    persist(createBuilder(questionnaire), localeProperties);
  }

  @Required
  public void setQuestionnaireBundleManager(QuestionnaireBundleManager questionnaireBundleManager) {
    this.questionnaireBundleManager = questionnaireBundleManager;
  }

  @Required
  public void setLocalePropertiesUtils(LocalePropertiesUtils localePropertiesUtils) {
    this.localePropertiesUtils = localePropertiesUtils;
  }

}
