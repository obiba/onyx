/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket;

import java.util.List;
import java.util.Locale;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.wizard.QuestionnaireWizardPanel;
import org.obiba.onyx.wicket.StageModel;

/**
 * Quartz widget entry point.
 */
public class QuartzPanel extends Panel {

  private static final long serialVersionUID = 0L;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SpringBean
  private ModuleRegistry moduleRegistry;

  @SpringBean
  private QuestionnaireBundleManager questionnaireBundleManager;

  private QuestionnaireWizardPanel wizardPanel;

  public QuartzPanel(String id, Stage stage, boolean resuming) {
    super(id);

    // ONYX-154: Set the current questionnaire here. Previously, this was done only in
    // QuartzInProgressState's onEntry method, but in a multiple user session scenario
    // the questionnaire does not get set in all required sessions leading to a NullPointerException.
    Questionnaire questionnaire = questionnaireBundleManager.getBundle(stage.getName()).getQuestionnaire();
    activeQuestionnaireAdministrationService.setQuestionnaire(questionnaire);

    if(activeQuestionnaireAdministrationService.getLanguage() == null) setDefaultLanguage();

    add(wizardPanel = new QuestionnaireWizardPanel("content", new QuestionnaireModel(activeQuestionnaireAdministrationService.getQuestionnaire()), new StageModel(moduleRegistry, stage.getName()), resuming));
  }

  /**
   * Set the questionnaire default language to be the session language if it is part of available languages for the
   * questionnaire, or the first of the questionnaire available languages.
   */
  private void setDefaultLanguage() {
    Locale sessionLocale = getSession().getLocale();

    List<Locale> locales = activeQuestionnaireAdministrationService.getQuestionnaire().getLocales();

    if(locales.isEmpty() || locales.contains(sessionLocale)) activeQuestionnaireAdministrationService.setDefaultLanguage(sessionLocale);
    else
      activeQuestionnaireAdministrationService.setDefaultLanguage(locales.get(0));
  }

}
