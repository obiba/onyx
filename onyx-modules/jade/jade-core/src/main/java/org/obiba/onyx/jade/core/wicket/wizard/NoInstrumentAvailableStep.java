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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.obiba.onyx.jade.core.wicket.instrument.NoInstrumentAvailablePanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class NoInstrumentAvailableStep extends WizardStepPanel {

  private static final long serialVersionUID = 1L;

  public NoInstrumentAvailableStep(String id) {
    super(id);

    add(new Label("title", new ResourceModel("NoInstrumentAvailableTitle")));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getNextLink().setVisible(false);
    form.getPreviousLink().setVisible(false);
    form.getFinishLink().setVisible(false);
    if(target != null) {
      target.addComponent(form.getNextLink());
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getFinishLink());
    }
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    super.onStepInNext(form, target);
    setContent(target, new NoInstrumentAvailablePanel(getContentId()));
  }

}
