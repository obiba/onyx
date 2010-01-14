/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.wizard;

import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.quartz.core.wicket.questionnaire.LanguageSelectorPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

/**
 * First step for questionnaire workflow. WizardStepPanel that calls LanguageSelectionPanel and implements actions to be
 * done.
 * 
 * @author acarey
 */
public class LanguageSelectionStep extends WizardStepPanel {

  private static final long serialVersionUID = 5343357448108404508L;

  private LanguageSelectorPanel selectorPanel;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SuppressWarnings("serial")
  public LanguageSelectionStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new Label(getTitleId(), new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "label")));

    add(selectorPanel = new LanguageSelectorPanel(getContentId()));
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    super.onStepOutNext(form, target);
    QuestionnaireWizardForm questionnaireForm = (QuestionnaireWizardForm) form;
    Locale language = selectorPanel.getLanguage();

    if(language != null) {
      activeQuestionnaireAdministrationService.start(activeInterviewService.getParticipant(), language);
      setNextStep(questionnaireForm.getFirstPageStep());
    } else {
      setNextStep(null);
    }
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getPreviousLink().setVisible(false);
    form.getNextLink().setVisible(true);
    ((QuestionnaireWizardForm) form).getInterruptLink().setEnabled(false);
    ((QuestionnaireWizardForm) form).getBeginLink().setVisible(false);
    ((QuestionnaireWizardForm) form).getEndLink().setVisible(false);
    form.getFinishLink().setVisible(false);

    if(target != null) {
      target.addComponent(form);
    }
  }

  @Override
  public void onStepInPrevious(WizardForm form, AjaxRequestTarget target) {
    super.onStepInPrevious(form, target);
    ((QuestionnaireWizardForm) form).getProgressBar().setVisible(false);
  }

}
