package org.obiba.onyx.quartz.core.wicket.wizard;

import java.util.Locale;

import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.reusable.FeedbackWindow;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardPanel;

public class QuestionnaireWizardPanel extends WizardPanel {

  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  //
  // Constructors
  //

  public QuestionnaireWizardPanel(String id, IModel questionnaireModel, StageModel stageModel, boolean resuming) {
    super(id, questionnaireModel);

    getQuestionnaireWizardForm().setStageModel(stageModel);
    getQuestionnaireWizardForm().initStartStep(resuming);
  }

  //
  // WizardPanel Methods
  //

  @Override
  public WizardForm createForm(String componentId) {
    return new QuestionnaireWizardForm(componentId, getModel());
  }

  //
  // Methods
  //

  public QuestionnaireWizardForm getQuestionnaireWizardForm() {
    return (QuestionnaireWizardForm) getWizardForm();
  }

  public void setActionWindow(ActionWindow window) {
    getQuestionnaireWizardForm().setActionWindow(window);
  }

  public void setFeedbackWindow(FeedbackWindow feedbackWindow) {
    getQuestionnaireWizardForm().setFeedbackWindow(feedbackWindow);
  }

  public FeedbackWindow getFeedbackWindow() {
    return getQuestionnaireWizardForm().getFeedbackWindow();
  }

  /**
   * Get the locale of the questionnaire.
   */
  @Override
  public Locale getLocale() {
    Locale locale = activeQuestionnaireAdministrationService.getLanguage();
    return locale != null ? locale : super.getLocale();
  }
}
