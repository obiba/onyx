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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.wicket.wizard.WizardForm;

public class ConclusionStep extends QuestionnaireWizardStepPanel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  //
  // Constructors
  //

  public ConclusionStep(String id) {
    super(id);

    setOutputMarkupId(true);

    Questionnaire questionnaire = activeQuestionnaireAdministrationService.getQuestionnaire();

    add(new Label(getTitleId(), new StringResourceModel("Conclusion", ConclusionStep.this, null)));

    add(new Label(getContentId(), new QuestionnaireStringResourceModel(questionnaire, "conclusion")).setEscapeModelStrings(false));
  }

  //
  // WizardStepPanel Methods
  //

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getNextLink().setEnabled(false);
    form.getPreviousLink().setEnabled(true);
    ((QuestionnaireWizardForm) form).getInterruptLink().setEnabled(false);
    form.getFinishLink().setEnabled(true).add(new AttributeModifier("style", new Model("display:inline;")));

    if(target != null) {
      target.addComponent(form);
    }
  }

  @Override
  public void onStepOutPrevious(WizardForm form, AjaxRequestTarget target) {
    QuestionnaireWizardForm questionnaireWizardForm = (QuestionnaireWizardForm) form;
    setPreviousStep(questionnaireWizardForm.getLastPageStep());
    onPageStep(target);
  }
}