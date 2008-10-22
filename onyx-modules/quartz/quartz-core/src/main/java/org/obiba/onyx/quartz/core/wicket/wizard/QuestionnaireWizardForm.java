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
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class QuestionnaireWizardForm extends WizardForm {

  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private WizardStepPanel languageSelectionStep;

  private WizardStepPanel conclusionStep;

  private StageModel stageModel;

  private ActionWindow actionWindow;

  private FeedbackPanel feedbackPanel;

  //
  // Constructors
  //

  public QuestionnaireWizardForm(String id, IModel questionnaireModel) {
    super(id);

    setModel(questionnaireModel);

    // Add Interrupt button.
    addInterruptButton();

    // Language selection step.
    languageSelectionStep = new LanguageSelectionStep(getStepId());

    // Conclusion step.
    conclusionStep = new ConclusionStep(getStepId());
  }

  //
  // WizardForm Methods
  //

  public void onCancel(AjaxRequestTarget target) {
    IStageExecution exec = activeInterviewService.getStageExecution((Stage) stageModel.getObject());
    ActionDefinition actionDef = exec.getActionDefinition(ActionType.STOP);
    if(actionDef != null) {
      actionWindow.show(target, stageModel, actionDef);
    }
  }

  public void onFinish(AjaxRequestTarget target, Form form) {
    IStageExecution exec = activeInterviewService.getStageExecution((Stage) stageModel.getObject());
    ActionDefinition actionDef = exec.getSystemActionDefinition(ActionType.COMPLETE);
    if(actionDef != null) {
      actionWindow.show(target, stageModel, actionDef);
    }
  }

  public void onError(AjaxRequestTarget target, Form form) {
    target.addComponent(feedbackPanel);
  }

  @Override
  public FeedbackPanel getFeedbackPanel() {
    return feedbackPanel;
  }

  //
  // Methods
  //

  public void initStartStep(boolean resuming) {
    WizardStepPanel startStep = resuming ? getResumeStep() : languageSelectionStep;

    add(startStep);
    startStep.onStepInNext(this, null);
    startStep.handleWizardState(this, null);
  }

  public void onInterrupt(AjaxRequestTarget target) {
    IStageExecution exec = activeInterviewService.getStageExecution((Stage) stageModel.getObject());
    ActionDefinition actionDef = exec.getActionDefinition(ActionType.INTERRUPT);

    if(actionDef != null) {
      actionWindow.show(target, stageModel, actionDef);
    }
  }

  public void setStageModel(StageModel stageModel) {
    this.stageModel = stageModel;
  }

  public void setActionWindow(ActionWindow window) {
    this.actionWindow = window;
  }

  public void setFeedbackPanel(FeedbackPanel feedbackPanel) {
    this.feedbackPanel = feedbackPanel;
  }

  private void addInterruptButton() {
    AjaxLink link = new AjaxLink("interrupt") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        onInterrupt(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("Interrupt", QuestionnaireWizardForm.this, null)));
    add(link);
  }

  /**
   * Returns the first page step.
   * 
   * @return first page step (or <code>null</code> if the questionnaire has no pages)
   */
  public WizardStepPanel getFirstPageStep() {
    Page startPage = activeQuestionnaireAdministrationService.startPage();
    return new PageStepPanel(getStepId(), startPage);
  }

  /**
   * Returns the previous step.
   * 
   * If the current page step is the first page step, returns the language selection step. Otherwise, the previous page
   * step is returned.
   * 
   * @return previous step
   */
  public WizardStepPanel getPreviousStep() {
    Page previousPage = activeQuestionnaireAdministrationService.previousPage();

    if(previousPage != null) {
      return new PageStepPanel(getStepId(), previousPage);
    } else {
      return languageSelectionStep;
    }
  }

  /**
   * Returns the next step.
   * 
   * If the current page step is the last page step, returns the conclusion step. Otherwise, the next page step is
   * returned.
   * 
   * @return next step
   */
  public WizardStepPanel getNextStep() {
    Page nextPage = activeQuestionnaireAdministrationService.nextPage();

    if(nextPage != null) {
      return new PageStepPanel(getStepId(), nextPage);
    } else {
      return conclusionStep;
    }
  }

  public WizardStepPanel getResumeStep() {
    Page resumePage = activeQuestionnaireAdministrationService.resumePage();

    if(resumePage != null) {
      return new PageStepPanel(getStepId(), resumePage);
    } else {
      return conclusionStep;
    }
  }
}
