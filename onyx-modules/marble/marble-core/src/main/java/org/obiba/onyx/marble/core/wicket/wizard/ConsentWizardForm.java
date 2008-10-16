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
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConsentWizardForm extends WizardForm {

  private static final Logger log = LoggerFactory.getLogger(ConsentWizardForm.class);

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveConsentService activeConsentService;

  private WizardStepPanel consentModeSelectionStep;

  private WizardStepPanel electronicConsentStep;

  private WizardStepPanel consentConfirmationStep;

  public ConsentWizardForm(String id, IModel interviewConsentModel) {
    super(id);

    activeConsentService.setConsent((Consent) interviewConsentModel.getObject());

    electronicConsentStep = new ElectronicConsentStep(getStepId());
    consentConfirmationStep = new ConsentConfirmationStep(getStepId());
    consentModeSelectionStep = new ConsentModeSelectionStep(getStepId(), electronicConsentStep, consentConfirmationStep);
    
    WizardStepPanel startStep = setupStaticWizardFlow();
   
    startStep.onStepInNext(this, null);
    startStep.handleWizardState(this, null);
    add(startStep);

  }

  private WizardStepPanel setupStaticWizardFlow() {
    WizardStepPanel startStep = consentModeSelectionStep;
    startStep.setPreviousStep(startStep);  
    electronicConsentStep.setPreviousStep(startStep);
    electronicConsentStep.setNextStep(consentConfirmationStep);
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
