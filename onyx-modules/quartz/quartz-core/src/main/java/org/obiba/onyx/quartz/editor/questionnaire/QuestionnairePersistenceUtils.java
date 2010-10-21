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

import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl.QuestionnaireBundleManagerImpl;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.UniqueQuestionnaireElementNameBuilder;
import org.obiba.onyx.quartz.editor.EditedElement;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 *
 */
public class QuestionnairePersistenceUtils {

  // private final Logger log = LoggerFactory.getLogger(getClass());

  private QuestionnaireBundleManager questionnaireBundleManager;

  public QuestionnaireBuilder createBuilder(Questionnaire questionnaire) {
    QuestionnaireBuilder builder = QuestionnaireBuilder.getInstance(questionnaire);
    if(Questionnaire.STANDARD_UI.equals(questionnaire.getUiType())) {
      builder.setStandardUI();
    } else if(Questionnaire.SIMPLIFIED_UI.equals(questionnaire.getUiType())) {
      builder.setSimplifiedUI();
    }
    return builder;
  }

  public void persist(EditedElement<?> editedElement, QuestionnaireBuilder builder) throws Exception {

    UniqueQuestionnaireElementNameBuilder.ensureQuestionnaireVariableNamesAreUnique(builder.getQuestionnaire());

    // Create the bundle manager.
    QuestionnaireBundleManager writeBundleManager = new QuestionnaireBundleManagerImpl(questionnaireBundleManager.getRootDir());
    ((QuestionnaireBundleManagerImpl) writeBundleManager).setPropertyKeyProvider(builder.getPropertyKeyProvider());
    ((QuestionnaireBundleManagerImpl) writeBundleManager).setResourceLoader(new PathMatchingResourcePatternResolver());

    final Questionnaire questionnaire = builder.getQuestionnaire();

    // store xml file and locales
    QuestionnaireBundle bundle = writeBundleManager.createBundle(questionnaire);
    Iterable<Locale> localesToDelete = Iterables.filter(bundle.getAvailableLanguages(), new Predicate<Locale>() {
      @Override
      public boolean apply(Locale locale) {
        return !questionnaire.getLocales().contains(locale);
      }
    });
    for(Locale localeToDelete : localesToDelete) {
      bundle.deleteLanguage(localeToDelete);
    }
    if(editedElement.getPropertiesByLocale().entrySet().isEmpty()) {
      for(Locale locale : questionnaire.getLocales()) {
        bundle.updateLanguage(locale, new Properties());
      }
    } else {
      for(Entry<Locale, Properties> entry : editedElement.getPropertiesByLocale().entrySet()) {
        bundle.updateLanguage(entry.getKey(), entry.getValue());
      }
    }
  }

  @Required
  public void setQuestionnaireBundleManager(QuestionnaireBundleManager questionnaireBundleManager) {
    this.questionnaireBundleManager = questionnaireBundleManager;
  }

}
