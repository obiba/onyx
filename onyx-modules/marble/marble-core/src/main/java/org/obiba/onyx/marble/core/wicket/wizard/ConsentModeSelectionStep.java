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
import org.obiba.onyx.marble.core.wicket.consent.ConsentModeSelectionPanel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.obiba.onyx.marble.domain.consent.ConsentMode;

public class ConsentModeSelectionStep extends WizardStepPanel {

  private static final long serialVersionUID = 1L;

  private ConsentModeSelectionPanel consentModeSelectionPanel;
  
  @SpringBean
  private ActiveConsentService activeConsentService;  
  
  WizardStepPanel electronicConsentStep;
  WizardStepPanel consentConfirmationStep;
   
  public ConsentModeSelectionStep(String id, WizardStepPanel electronicConsentStep, WizardStepPanel consentConfirmationStep) {
    super(id);
    setOutputMarkupId(true);
    this.electronicConsentStep = electronicConsentStep;
    this.consentConfirmationStep = consentConfirmationStep;
    
    add(new Label("title", new StringResourceModel("ConsentModeSelectionTitle", this, null)));
  }

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getPreviousLink().setEnabled(false);
    form.getNextLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    setContent(target, consentModeSelectionPanel = new ConsentModeSelectionPanel(getContentId()));
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    if( activeConsentService.getMode().equals(ConsentMode.MANUAL) ) {
      consentConfirmationStep.setPreviousStep(this);
      setNextStep(consentConfirmationStep);
    } else {
      consentConfirmationStep.setPreviousStep(electronicConsentStep);
      setNextStep(electronicConsentStep);
    }
    consentModeSelectionPanel.save();
  }
}
