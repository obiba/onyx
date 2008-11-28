/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.marble.core.wicket.consent;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.wicket.wizard.WizardForm;

public class ElectronicConsentPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private ActiveConsentService activeConsentService;

  private WizardForm form;

  public ElectronicConsentPanel(String id, WizardForm form) {
    super(id);
    setOutputMarkupId(true);
    this.form = form;
  }

  @Override
  protected void onBeforeRender() {
    Boolean consentIsAccepted = activeConsentService.getConsent().isAccepted();
    Boolean consentIsValid = activeConsentService.validateElectronicConsent();
    Boolean consentIsSubmitted = activeConsentService.isConsentFormSubmitted();
    Component finishButton = form.getFinishLink();

    // If consent form not submitted yet, display a new blank form in the IFrame.
    if(!consentIsSubmitted) {
      addOrReplace(new InlineFrame("pdfSubmitFrame", new ElectronicConsentPage(finishButton)));

      // If consent form is submitted and valid, display confirmation page in the IFrame.
    } else if(!consentIsAccepted || (consentIsAccepted && consentIsValid)) {
      addOrReplace(new InlineFrame("pdfSubmitFrame", new ElectronicConsentSubmittedPage(finishButton.getId())));

      // If submitted but invalid, display current form in the IFrame + error message.
    } else if(consentIsAccepted && !consentIsValid) {
      addOrReplace(new InlineFrame("pdfSubmitFrame", new ElectronicConsentPage(finishButton)));
      error(getString("InvalidConsentForm"));
    }
    super.onBeforeRender();
  }
}
