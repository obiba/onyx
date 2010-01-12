/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.wizard;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.onyx.wicket.reusable.WizardAdministrationWindow;
import org.obiba.onyx.wicket.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.reusable.Dialog.WindowClosedCallback;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Samples Collection wizard form.
 */
public class RubyWizardForm extends WizardForm {

  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(RubyWizardForm.class);

  //
  // Instance Variables
  //

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean(name = "activeTubeRegistrationService")
  private ActiveTubeRegistrationService activeTubeRegistrationService;

  private WizardStepPanel observedContraIndicationStep;

  private WizardStepPanel askedContraIndicationStep;

  private WizardStepPanel tubeRegistrationStep;

  private WizardStepPanel validationStep;

  private StageModel stageModel;

  private ActionWindow actionWindow;

  private FeedbackWindow feedbackWindow;

  protected WizardAdministrationWindow adminWindow;

  private boolean adminWindowClosed = false;

  private boolean resuming;

  //
  // Constructors
  //

  public RubyWizardForm(String id, IModel model) {
    super(id, model);

    // Add Interrupt button.
    addInterruptLink();

    observedContraIndicationStep = new ObservedContraIndicationStep(getStepId());
    askedContraIndicationStep = new AskedContraIndicationStep(getStepId());
    tubeRegistrationStep = new TubeRegistrationStep(getStepId());
    validationStep = new ValidationStep(getStepId());

    createModalAdministrationPanel();

    // admin button
    AjaxLink link = new AjaxLink("adminLink") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        adminWindow.setInterruptState(getInterruptLink().isEnabled());
        if(getCancelLink() != null) adminWindow.setCancelState(getCancelLink().isEnabled());
        adminWindow.show(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("Administration", this, null)));
    link.add(new AttributeAppender("class", new Model("ui-corner-all"), " "));
    add(link);
  }

  @Override
  public FeedbackWindow getFeedbackWindow() {
    return feedbackWindow;
  }

  //
  // Methods
  //

  public void setStageModel(StageModel stageModel) {
    this.stageModel = stageModel;
  }

  public void setActionWindow(ActionWindow window) {
    this.actionWindow = window;
  }

  public ActionWindow getActionWindow() {
    return actionWindow;
  }

  public void setFeedbackWindow(FeedbackWindow feedbackWindow) {
    this.feedbackWindow = feedbackWindow;
  }

  public Component getInterruptLink() {
    return get("interrupt");
  }

  private void addInterruptLink() {
    AjaxLink link = new AjaxLink("interrupt") {
      private static final long serialVersionUID = 0L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        onInterrupt(target);
      }

    };
    link.add(new AttributeModifier("value", true, new StringResourceModel("Interrupt", RubyWizardForm.this, null)));
    link.add(new AttributeAppender("class", new Model("ui-corner-all"), " "));
    add(link);
  }

  public void initStartStep(boolean resuming) {
    this.resuming = resuming;

    WizardStepPanel startStep = setUpWizardFlow();

    add(startStep);
    startStep.onStepInNext(this, null);
    startStep.handleWizardState(this, null);
  }

  public WizardStepPanel setUpWizardFlow() {
    WizardStepPanel startStep = null;
    WizardStepPanel lastStep = null;

    if(!resuming) {
      // are there observed contra-indications to display ?
      if(activeTubeRegistrationService.hasContraindications(Contraindication.Type.OBSERVED)) {
        if(startStep == null) {
          startStep = observedContraIndicationStep;
          lastStep = startStep;
        } else {
          lastStep.setNextStep(observedContraIndicationStep);
          observedContraIndicationStep.setPreviousStep(lastStep);
          lastStep = observedContraIndicationStep;
        }
      } else {
        log.debug("No contra-indications of type OBSERVED. Skipping step.");
      }

      // are there asked contra-indications to display ?
      if(activeTubeRegistrationService.hasContraindications(Contraindication.Type.ASKED)) {
        if(startStep == null) {
          startStep = askedContraIndicationStep;
          lastStep = startStep;
        } else {
          lastStep.setNextStep(askedContraIndicationStep);
          askedContraIndicationStep.setPreviousStep(lastStep);
          lastStep = askedContraIndicationStep;
        }
      } else {
        log.debug("No contra-indications of type ASKED. Skipping step.");
      }
    }

    if(startStep == null) {
      startStep = tubeRegistrationStep;
      lastStep = startStep;
    } else {
      lastStep.setNextStep(tubeRegistrationStep);
      tubeRegistrationStep.setPreviousStep(lastStep);
      lastStep = tubeRegistrationStep;
    }

    if(startStep == null) {
      startStep = validationStep;
      lastStep = startStep;
    } else {
      lastStep.setNextStep(validationStep);
      validationStep.setPreviousStep(lastStep);
      lastStep = validationStep;
    }

    return startStep;
  }

  @SuppressWarnings("serial")
  private void createModalAdministrationPanel() {
    // Create modal feedback window
    adminWindow = new WizardAdministrationWindow("adminWindow");

    adminWindow.setCancelLink("CancelSampleCollection");

    adminWindow.setCloseButtonCallback(new CloseButtonCallback() {

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {
        adminWindowClosed = true;
        return true;
      }

    });

    adminWindow.setWindowClosedCallback(new WindowClosedCallback() {
      public void onClose(AjaxRequestTarget target, Status status) {
        switch(status) {
        case OTHER:
          onInterrupt(target);
          break;
        case SUCCESS:
          onFinishSubmit(target, RubyWizardForm.this);
          break;
        case ERROR:
          onFinishError(target, RubyWizardForm.this);
          break;
        case CLOSED:
          onCancelClick(target);
        }
      }
    });
    adminWindowClosed = false;
    add(adminWindow);
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
    showFeedbackWindow(target);
  }

  public void onInterrupt(AjaxRequestTarget target) {
    IStageExecution exec = activeInterviewService.getStageExecution((Stage) stageModel.getObject());
    ActionDefinition actionDef = exec.getActionDefinition(ActionType.INTERRUPT);

    if(actionDef != null) {
      actionWindow.show(target, stageModel, actionDef);
    }
  }

  public boolean isResuming() {
    return resuming;
  }
}
