package org.obiba.onyx.quartz.core.wicket.wizard;

import java.util.Locale;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
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

  private QuestionnaireWizardForm wizardForm;

  //
  // Constructors
  //

  public QuestionnaireWizardPanel(String id, IModel questionnaireModel, StageModel stageModel, boolean resuming) {
    super(id, questionnaireModel);

    wizardForm.setStageModel(stageModel);
    wizardForm.initStartStep(resuming);
  }

  //
  // WizardPanel Methods
  //

  @Override
  public WizardForm createForm(String componentId) {
    wizardForm = new QuestionnaireWizardForm(componentId, getModel());

    return wizardForm;
  }

  //
  // Methods
  //

  public WizardForm getWizardForm() {
    return wizardForm;
  }

  public void setActionWindow(ActionWindow window) {
    wizardForm.setActionWindow(window);
  }

  public void setFeedbackPanel(FeedbackPanel feedbackPanel) {
    wizardForm.setFeedbackPanel(feedbackPanel);
  }

  public FeedbackPanel getFeedbackPanel() {
    return wizardForm.getFeedbackPanel();
  }

  /**
   * Get the locale of the questionnaire.
   */
  @Override
  public Locale getLocale() {
    return activeQuestionnaireAdministrationService.getLanguage();
  }
}
