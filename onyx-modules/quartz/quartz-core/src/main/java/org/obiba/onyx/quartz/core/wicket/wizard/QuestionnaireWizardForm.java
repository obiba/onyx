package org.obiba.onyx.quartz.core.wicket.wizard;

import java.util.Locale;

import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public abstract class QuestionnaireWizardForm extends WizardForm {

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private WizardStepPanel languageSelectionStep;
  
  private WizardStepPanel conclusionStep;

  public QuestionnaireWizardForm(String id) {
    super(id);

    WizardStepPanel startStep = null;

    languageSelectionStep = new LanguageSelectionStep(getStepId());

    startStep = languageSelectionStep;

    add(startStep);
    startStep.onStepInNext(this, null);
    startStep.handleWizardState(this, null);
  }
  
  public WizardStepPanel setUpWizardFlow() {
    WizardStepPanel startStep = null;
    WizardStepPanel lastStep = null;

    Locale language = activeQuestionnaireAdministrationService.getLanguage();
    if (language == null) throw new IllegalStateException("Language is not supposed to be null in current active questionnaire.");
    
    // TODO: Implémenter la suite de cette step
    /*if(startStep == null) {
      startStep = conclusionStep;
      lastStep = startStep;
    } else {
      lastStep.setNextStep(conclusionStep);
      conclusionStep.setPreviousStep(lastStep);
      lastStep = conclusionStep;
    }*/

    return startStep;
  }
}
