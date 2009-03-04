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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.marble.core.wicket.consent.ManualConsentPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class ManualConsentStep extends WizardStepPanel {

  private static final long serialVersionUID = 1L;

  public ManualConsentStep(String id) {
    super(id);
    setOutputMarkupId(true);

    add(new Label("title", new StringResourceModel("ConsentConfirmationTitle", this, null)));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    if(getPreviousStep() != null) form.getPreviousLink().setEnabled(true);
    form.getNextLink().setEnabled(false);
    form.getFinishLink().setEnabled(true);
    form.getCancelLink().setEnabled(true);
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    super.onStepInNext(form, target);
    setContent(target, new ManualConsentPanel(getContentId()));
  }

}
