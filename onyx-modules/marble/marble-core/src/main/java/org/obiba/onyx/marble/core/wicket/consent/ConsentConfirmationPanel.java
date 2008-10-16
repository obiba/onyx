package org.obiba.onyx.marble.core.wicket.consent;

import java.util.Arrays;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.marble.core.service.ActiveConsentService;

public class ConsentConfirmationPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private ActiveConsentService activeConsentService;

  public ConsentConfirmationPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    add(createConsentConfirmationRadio());
  }

  @SuppressWarnings("serial")
  private RadioChoice createConsentConfirmationRadio() {

    IChoiceRenderer choiceRenderer = new IChoiceRenderer() {

      public Object getDisplayValue(Object object) {
        return (new StringResourceModel("ConsentConfirmation." + object.toString(), ConsentConfirmationPanel.this, null).getString());
      }

      public String getIdValue(Object object, int index) {
        return object.toString();
      }
    };

    RadioChoice consentConfirmation = new RadioChoice("consentConfirmation", new PropertyModel(activeConsentService.getConsent(), "accepted"), Arrays.asList(new Boolean[] { true, false }), choiceRenderer) {

      @Override
      protected boolean localizeDisplayValues() {
        return true;
      }
    };

    consentConfirmation.setNullValid(true);
    consentConfirmation.setRequired(true);

    return consentConfirmation;
  }

  public void save() {
  }

}
