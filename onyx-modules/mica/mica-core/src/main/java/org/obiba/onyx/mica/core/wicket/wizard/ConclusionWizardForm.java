/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.mica.core.wicket.wizard;

import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.obiba.onyx.mica.domain.conclusion.Conclusion;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConclusionWizardForm extends WizardForm {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ConclusionWizardForm.class);

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveConclusionService activeConclusionService;

  private WizardStepPanel balsacConfirmationStep;

  private WizardStepPanel participantReportStep;

  public ConclusionWizardForm(String id, IModel interviewConclusionModel) {
    super(id);

    activeConclusionService.setConclusion((Conclusion) interviewConclusionModel.getObject());

    participantReportStep = new ParticipantReportStep(getStepId());
    balsacConfirmationStep = new BalsacConfirmationStep(getStepId(), participantReportStep);

    WizardStepPanel startStep = setupStaticWizardFlow();

    startStep.onStepInNext(this, null);
    startStep.handleWizardState(this, null);
    add(startStep);
  }

  private WizardStepPanel setupStaticWizardFlow() {
    WizardStepPanel startStep = balsacConfirmationStep;
    startStep.setPreviousStep(startStep);
    participantReportStep.setPreviousStep(startStep);
    return startStep;
  }

  public ActiveInterviewService getActiveInterviewService() {
    return activeInterviewService;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public ActiveConclusionService getActiveConclusionService() {
    return activeConclusionService;
  }

  public void setActiveConclusionService(ActiveConclusionService activeConclusionService) {
    this.activeConclusionService = activeConclusionService;
  }

  public WizardStepPanel getBalsacConfirmationStep() {
    return balsacConfirmationStep;
  }

  public void setBalsacConfirmationStep(WizardStepPanel balsacConfirmationStep) {
    this.balsacConfirmationStep = balsacConfirmationStep;
  }

  public WizardStepPanel getParticipantReportStep() {
    return participantReportStep;
  }

  public void setParticipantReportStep(WizardStepPanel participantReportStep) {
    this.participantReportStep = participantReportStep;
  }

}