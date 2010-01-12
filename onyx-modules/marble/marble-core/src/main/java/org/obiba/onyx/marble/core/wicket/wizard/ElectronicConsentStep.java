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
import org.obiba.onyx.marble.core.wicket.consent.ElectronicConsentPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class ElectronicConsentStep extends WizardStepPanel {

  private static final long serialVersionUID = 1L;

  public ElectronicConsentStep(String id, WizardStepPanel consentConfirmationStep) {
    super(id);
    setOutputMarkupId(true);
    add(new Label("title", new StringResourceModel("ElectronicConsentTitle", this, null)));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getPreviousLink().setVisible(true);
    form.getNextLink().setVisible(false);
    form.getFinishLink().setVisible(true);
    form.getCancelLink().setVisible(true);
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    super.onStepInNext(form, target);
    setContent(target, new ElectronicConsentPanel(getContentId(), form));

    // Replace the WizardForm css to get rid of the blue border that surrounds the form.
    // This border is taking too much space when the electronic consent form is displayed.
    form.changeWizardFormStyle("wizard-consent");

    target.appendJavascript("Resizer.resizeConsentFrame();");
  }

  @Override
  public void onStepInPrevious(WizardForm form, AjaxRequestTarget target) {
    super.onStepInPrevious(form, target);
    form.changeWizardFormStyle("wizard-consent");
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    super.onStepOutNext(form, target);
    form.changeWizardFormStyle("wizard");
  }

  @Override
  public void onStepOutPrevious(WizardForm form, AjaxRequestTarget target) {
    super.onStepOutPrevious(form, target);
    form.changeWizardFormStyle("wizard");
  }

  @Override
  public void onStepOutNextError(WizardForm form, AjaxRequestTarget target) {
    super.onStepOutNextError(form, target);
    form.changeWizardFormStyle("wizard-consent");
  }

}
