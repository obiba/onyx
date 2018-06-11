/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.obiba.onyx.jade.core.wicket.instrument.InstrumentInputParameterPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class InputParametersStep extends WizardStepPanel {

  private static final long serialVersionUID = 1L;

  private InstrumentInputParameterPanel instrumentInputParameterPanel;

  public InputParametersStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new EmptyPanel(getTitleId()).setVisible(false));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {

    // Disable previous button when not needed.
    WizardStepPanel previousStep = this.getPreviousStep();
    if(previousStep == null || previousStep.equals(this)) {
      form.getPreviousLink().setVisible(false);
    } else {
      form.getPreviousLink().setVisible(true);
    }

    form.getNextLink().setVisible(true);
    form.getFinishLink().setVisible(false);
    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    super.onStepInNext(form, target);
    setContent(target, instrumentInputParameterPanel = new InstrumentInputParameterPanel(getContentId()));
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    super.onStepOutNext(form, target);
    instrumentInputParameterPanel.save();
  }
}
