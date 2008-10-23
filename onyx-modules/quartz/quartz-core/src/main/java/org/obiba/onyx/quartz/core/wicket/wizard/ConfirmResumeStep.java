package org.obiba.onyx.quartz.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.quartz.core.wicket.questionnaire.ConfirmResumePanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class ConfirmResumeStep extends WizardStepPanel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private ConfirmResumePanel confirmResumePanel;

  //
  // Constructors
  //

  public ConfirmResumeStep(String id, String questionNumberToResumeAt) {
    super(id);

    setOutputMarkupId(true);

    add(new Label(getTitleId(), new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "label")));

    add(confirmResumePanel = new ConfirmResumePanel(getContentId(), questionNumberToResumeAt));
  }

  //
  // WizardStepPanel Methods
  //

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getNextLink().setEnabled(true);
    form.getPreviousLink().setEnabled(false);
    ((QuestionnaireWizardForm) form).getInterruptLink().setEnabled(false);
    form.getFinishLink().setEnabled(false);

    if(target != null) {
      target.addComponent(form);
    }
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    QuestionnaireWizardForm questionnaireWizardForm = (QuestionnaireWizardForm) form;
    setNextStep(questionnaireWizardForm.getResumeStep());
  }
}
