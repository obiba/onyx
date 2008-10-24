package org.obiba.onyx.quartz.core.wicket.wizard;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
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
}
