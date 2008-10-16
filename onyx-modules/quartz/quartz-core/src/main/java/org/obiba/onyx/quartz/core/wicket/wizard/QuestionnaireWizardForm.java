package org.obiba.onyx.quartz.core.wicket.wizard;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public abstract class QuestionnaireWizardForm extends WizardForm {

  //
  // Instance Variables
  //
  
  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private WizardStepPanel languageSelectionStep;
  
  private WizardStepPanel conclusionStep;

  //
  // Constructors
  //
  
  public QuestionnaireWizardForm(String id, IModel questionnaireModel) {
    super(id);

    setModel(questionnaireModel);

    WizardStepPanel startStep = null;

    // First step: Language selection.
    languageSelectionStep = new LanguageSelectionStep(getStepId());

    // Last step: Conclusion.
    conclusionStep = new ConclusionStep(getStepId());

    startStep = languageSelectionStep;

    add(startStep);
    startStep.onStepInNext(this, null);
    startStep.handleWizardState(this, null);
  }
  
  //
  // Methods
  //

  /**
   * Returns the first page step.
   * 
   * @return first page step (or <code>null</code> if the questionnaire has no pages)
   */
  public WizardStepPanel getFirstPageStep() {
    Page startPage = activeQuestionnaireAdministrationService.getStartPage();
    return new PageStepPanel(getStepId(), startPage);
  }

  /**
   * Given the current page step, returns the previous step.
   * 
   * If the current page step is the first page step, returns the language selection step.
   * Otherwise, the previous page step is returned.
   * 
   * @param currentStep current page step
   * @return previous step
   */
  public WizardStepPanel getPreviousStep(PageStepPanel currentStep) {
    Page previousPage = activeQuestionnaireAdministrationService.getPreviousPage();

    if(previousPage != null) {
      return new PageStepPanel(getStepId(), previousPage);
    } else {
      return languageSelectionStep;
    }
  }

  /**
   * Given the current page step, returns the next step.
   * 
   * If the current page step is the last page step, returns the conclusion step.
   * Otherwise, the next page step is returned.
   * 
   * @param currentStep current page step
   * @return next step
   */
  public WizardStepPanel getNextStep(PageStepPanel currentStep) {
    Page nextPage = activeQuestionnaireAdministrationService.getNextPage();

    if(nextPage != null) {
      return new PageStepPanel(getStepId(), nextPage);
    } else {
      return conclusionStep;
    }
  }
}
