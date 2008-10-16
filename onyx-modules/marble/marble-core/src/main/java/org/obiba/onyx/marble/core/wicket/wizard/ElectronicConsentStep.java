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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.marble.core.wicket.consent.ElectronicConsentPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class ElectronicConsentStep extends WizardStepPanel {

  private static final long serialVersionUID = 1L;

  private ElectronicConsentPanel electronicConsentPanel;
  
  @SpringBean
  private ActiveConsentService activeConsentService;

  public ElectronicConsentStep(String id) {
    super(id);
    setOutputMarkupId(true);    
    add(new Label("title", new StringResourceModel("ElectronicConsentTitle", this, null)));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    // No previous step
    form.getPreviousLink().setEnabled(true);
    form.getNextLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);
    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) { 
    setContent(target, electronicConsentPanel = new ElectronicConsentPanel(getContentId()));
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    electronicConsentPanel.save();
  }
}
