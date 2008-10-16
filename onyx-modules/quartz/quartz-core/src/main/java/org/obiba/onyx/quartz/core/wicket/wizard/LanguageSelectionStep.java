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

    add(new Label(getTitleId(), new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "label", null)));

    add(selectorPanel = new LanguageSelectorPanel(getContentId()));
  }
  
  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
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
    // No previous step
    form.getPreviousLink().setEnabled(false);
    form.getNextLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);
    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }
  }
}
