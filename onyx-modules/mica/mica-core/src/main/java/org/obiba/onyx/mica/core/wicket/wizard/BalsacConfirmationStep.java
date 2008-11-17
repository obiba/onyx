/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.mica.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.mica.core.wicket.conclusion.BalsacConfirmationPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class BalsacConfirmationStep extends WizardStepPanel {

  private static final long serialVersionUID = 1L;

  private BalsacConfirmationPanel balsacConfirmationPanel;

  WizardStepPanel participantReportStep;

  public BalsacConfirmationStep(String id, WizardStepPanel participantReportStep) {
    super(id);
    setOutputMarkupId(true);
    this.participantReportStep = participantReportStep;

    add(new Label("title", new StringResourceModel("BalsacTitle", this, null)));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getPreviousLink().setEnabled(false);
    form.getNextLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    setContent(target, balsacConfirmationPanel = new BalsacConfirmationPanel(getContentId()));
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    balsacConfirmationPanel.save();
  }
}
