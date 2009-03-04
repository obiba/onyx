/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.core.wicket.wizard;

import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public abstract class ConsentWizardForm extends WizardForm {

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveConsentService activeConsentService;

  private WizardStepPanel consentModeSelectionStep;

  private WizardStepPanel electronicConsentStep;

  private WizardStepPanel consentConfirmationStep;

  // Consent Mode
  private static String MANUAL_AND_ELECTRONIC = "Manual and electronic";

  public ConsentWizardForm(String id, IModel interviewConsentModel) {
    super(id, interviewConsentModel);

    consentConfirmationStep = new ManualConsentStep(getStepId());
    electronicConsentStep = new ElectronicConsentStep(getStepId(), consentConfirmationStep);
    consentModeSelectionStep = new ConsentModeSelectionStep(getStepId(), electronicConsentStep, consentConfirmationStep);

    WizardStepPanel startStep = setupWizardFlow();

    startStep.onStepInNext(this, null);
    startStep.handleWizardState(this, null);
    add(startStep);
  }

  private WizardStepPanel setupWizardFlow() {
    // get the consent mode variable value
    String consentMode = activeConsentService.getConsentMode();
    WizardStepPanel startStep;

    if(consentMode.equalsIgnoreCase(MANUAL_AND_ELECTRONIC)) {
      startStep = consentModeSelectionStep;
      startStep.setPreviousStep(startStep);
      electronicConsentStep.setPreviousStep(startStep);
    } else {
      consentConfirmationStep.setPreviousStep(null);
      startStep = consentConfirmationStep;
    }

    return startStep;
  }

  public ActiveInterviewService getActiveInterviewService() {
    return activeInterviewService;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public WizardStepPanel getConsentModeSelectionStep() {
    return consentModeSelectionStep;
  }

  public void setConsentModeSelectionStep(WizardStepPanel consentModeSelectionStep) {
    this.consentModeSelectionStep = consentModeSelectionStep;
  }

  public WizardStepPanel getElectronicConsentStep() {
    return electronicConsentStep;
  }

  public void setElectronicConsentStep(WizardStepPanel electronicConsentStep) {
    this.electronicConsentStep = electronicConsentStep;
  }

  public WizardStepPanel getConsentConfirmationStep() {
    return consentConfirmationStep;
  }

  public void setConsentConfirmationStep(WizardStepPanel consentConfirmationStep) {
    this.consentConfirmationStep = consentConfirmationStep;
  }

}
