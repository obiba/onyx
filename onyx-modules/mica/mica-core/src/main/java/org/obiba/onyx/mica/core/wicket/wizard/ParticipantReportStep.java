/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.mica.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.mica.core.wicket.conclusion.ParticipantReportPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class ParticipantReportStep extends WizardStepPanel {

  private static final long serialVersionUID = 1L;

  private ParticipantReportPanel participantReportPanel;

  public ParticipantReportStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new Label("title", new StringResourceModel("ParticipantReportTitle", this, null)));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {

    // Previous link disabled if Balsac confirmation not required.
    ConclusionWizardForm conclusionForm = (ConclusionWizardForm) form;
    if(conclusionForm.getActiveConclusionService().isBalsacConfirmationRequired()) {
      form.getPreviousLink().setEnabled(true);
    } else {
      form.getPreviousLink().setEnabled(false);
    }

    form.getNextLink().setEnabled(false);
    form.getFinishLink().setEnabled(true);
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    setContent(target, participantReportPanel = new ParticipantReportPanel(getContentId()));
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    participantReportPanel.finish();
  }

  public ParticipantReportPanel getParticipantReportPanel() {
    return participantReportPanel;
  }

  public void setParticipantReportPanel(ParticipantReportPanel participantReportPanel) {
    this.participantReportPanel = participantReportPanel;
  }
}
