/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.core.wicket.consent;

import java.util.Arrays;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.marble.domain.consent.ConsentMode;

public class ManualConsentPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private ActiveConsentService activeConsentService;

  @SpringBean
  private ConsentService consentService;

  public ManualConsentPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    add(createConsentConfirmationRadio());

    Consent activeConsent = activeConsentService.getConsent();
    if(activeConsent.getLocale() == null) {
      activeConsent.setLocale(consentService.getSupportedConsentLocales().get(0));
    }

    if(activeConsent.getMode() == null) {
      activeConsent.setMode(ConsentMode.MANUAL);
    }
  }

  @SuppressWarnings("serial")
  private RadioChoice createConsentConfirmationRadio() {

    IChoiceRenderer choiceRenderer = new IChoiceRenderer() {

      public Object getDisplayValue(Object object) {
        return (new StringResourceModel("ConsentConfirmation." + object.toString(), ManualConsentPanel.this, null).getString());
      }

      public String getIdValue(Object object, int index) {
        return object.toString();
      }
    };

    RadioChoice consentConfirmation = new RadioChoice("consentConfirmation", new PropertyModel(activeConsentService, "consent.accepted"), Arrays.asList(new Boolean[] { true, false }), choiceRenderer) {

      @Override
      protected boolean localizeDisplayValues() {
        return true;
      }
    };

    consentConfirmation.setNullValid(true);
    consentConfirmation.setRequired(true);

    return consentConfirmation;
  }

}
