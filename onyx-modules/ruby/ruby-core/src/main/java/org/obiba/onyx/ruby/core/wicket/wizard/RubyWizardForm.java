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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
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

  @SpringBean
  private TubeRegistrationConfiguration tubeRegistrationConfiguration;

  private WizardStepPanel observedContraIndicationStep;

  private WizardStepPanel askedContraIndicationStep;

  private WizardStepPanel tubeRegistrationStep;

  private WizardStepPanel validationStep;

  private StageModel stageModel;

  private ActionWindow actionWindow;

  private FeedbackPanel feedbackPanel;

  private boolean resuming;

  //
  // Constructors
  //

  public RubyWizardForm(String id, IModel model) {
    super(id, model);

    observedContraIndicationStep = new ObservedContraIndicationStep(getStepId());
    askedContraIndicationStep = new AskedContraIndicationStep(getStepId());
    tubeRegistrationStep = new TubeRegistrationStep(getStepId());
    validationStep = new ValidationStep(getStepId());
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

  public void setStageModel(StageModel stageModel) {
    this.stageModel = stageModel;
  }

  public void setActionWindow(ActionWindow window) {
    this.actionWindow = window;
  }

  public void setFeedbackPanel(FeedbackPanel feedbackPanel) {
    this.feedbackPanel = feedbackPanel;
  }

  public void initStartStep(boolean resuming) {
    this.resuming = resuming;

    WizardStepPanel startStep = null;

    if(resuming) {
      startStep = tubeRegistrationStep;
    } else {
      startStep = observedContraIndicationStep;
    }

    add(startStep);
    startStep.onStepInNext(this, null);
    startStep.handleWizardState(this, null);
  }

  public WizardStepPanel setUpWizardFlow() {
    WizardStepPanel startStep = null;
    WizardStepPanel lastStep = null;

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
}
