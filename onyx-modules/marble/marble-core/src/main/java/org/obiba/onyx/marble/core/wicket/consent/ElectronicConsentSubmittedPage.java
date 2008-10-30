package org.obiba.onyx.marble.core.wicket.consent;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.marble.core.service.ActiveConsentService;

public class ElectronicConsentSubmittedPage extends WebPage {

  @SpringBean
  private ActiveConsentService activeConsentService;

  public ElectronicConsentSubmittedPage() {
    super();
    Boolean consentIsAccepted = activeConsentService.getConsent().isAccepted();
    if(consentIsAccepted) {
      add(new Label("message", new StringResourceModel("MessageAccepted", this, null)));
    } else {
      add(new Label("message", new StringResourceModel("MessageRefused", this, null)));
    }
  }
}
