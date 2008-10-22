package org.obiba.onyx.quartz.core.wicket.wizard;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
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

  public QuestionnaireWizardPanel(String id, IModel model, StageModel stageModel) {
    super(id, model);
    wizardForm.setStageModel(stageModel);
  }

  //
  // WizardPanel Methods
  //

  @Override
  public WizardForm createForm(String componentId) {
    Questionnaire questionnaire = activeQuestionnaireAdministrationService.getQuestionnaire();
    wizardForm = new QuestionnaireWizardForm(componentId, new Model(questionnaire));

    return wizardForm;
  }

  //
  // Methods
  //

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
