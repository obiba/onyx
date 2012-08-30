package org.obiba.onyx.quartz.core.wicket.wizard;

import java.util.Locale;

import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardPanel;

public class QuestionnaireWizardPanel extends WizardPanel<Questionnaire> {

  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private final FeedbackWindow feedbackWindow;

  //
  // Constructors
  //

  @SuppressWarnings("serial")
  public QuestionnaireWizardPanel(String id, IModel<Questionnaire> questionnaireModel, StageModel stageModel, boolean resuming) {
    super(id, questionnaireModel);

    getQuestionnaireWizardForm().setStageModel(stageModel);
    getQuestionnaireWizardForm().initStartStep(resuming);

    feedbackWindow = new FeedbackWindow("feedback") {
      @Override
      public Locale getLocale() {
        return QuestionnaireWizardPanel.this.getLocale();
      }
    };
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

  }

  //
  // WizardPanel Methods
  //

  @Override
  public WizardForm createForm(String componentId) {
    return new QuestionnaireWizardForm(componentId, (IModel<Questionnaire>) getDefaultModel()) {
      private static final long serialVersionUID = 1L;

      @Override
      public FeedbackWindow getFeedbackWindow() {
        return feedbackWindow;
      }
    };
  }

  //
  // Methods
  //

  public QuestionnaireWizardForm getQuestionnaireWizardForm() {
    return (QuestionnaireWizardForm) getWizardForm();
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
