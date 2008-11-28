package org.obiba.onyx.marble.core.wicket.consent;

import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.marble.core.service.ActiveConsentService;

public class ElectronicConsentSubmittedPage extends WebPage implements IHeaderContributor {

  @SpringBean
  private ActiveConsentService activeConsentService;

  String finishLinkId;

  public ElectronicConsentSubmittedPage(String finishLinkId) {
    super();
    Boolean consentIsAccepted = activeConsentService.getConsent().isAccepted();
    if(consentIsAccepted) {
      add(new Label("message", new StringResourceModel("MessageAccepted", this, null)));
    } else {
      add(new Label("message", new StringResourceModel("MessageRefused", this, null)));
    }

    this.finishLinkId = finishLinkId;

  }

  public void renderHead(IHeaderResponse response) {
    response.renderOnLoadJavascript("parent.document.getElementById('" + finishLinkId + "').click();");
  }
}
