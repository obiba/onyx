package org.obiba.onyx.marble.core.wicket.consent;

import java.io.ByteArrayOutputStream;

import javax.servlet.ServletInputStream;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.io.Streams;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page is called by the PDF form submit button. It reads the submitted PDF through the servlet request.
 */
public class ElectronicConsentUploadPage extends WebPage {

  private static final Logger log = LoggerFactory.getLogger(ElectronicConsentUploadPage.class);

  @SpringBean
  private ActiveConsentService activeConsentService;

  public ElectronicConsentUploadPage(PageParameters pageParams) {
    super(pageParams);
    // Don't save consent form if the participant has refused to sign it.
    if(confirmConsent(pageParams)) {
      uploadElectronicForm();
    }

    setResponsePage(ElectronicConsentSubmittedPage.class);
  }

  private boolean confirmConsent(PageParameters pageParams) {
    boolean consentIsAccepted = pageParams.getBoolean("accepted");
    log.info("Consent accepted={}", consentIsAccepted);
    activeConsentService.getConsent().setAccepted(consentIsAccepted);
    return consentIsAccepted;
  }

  private void uploadElectronicForm() {
    ByteArrayOutputStream convertedStream = new ByteArrayOutputStream();

    try {
      // Get inputstream on servlet request.
      ServletInputStream uploadedPdfStream = getWebRequestCycle().getWebRequest().getHttpServletRequest().getInputStream();
      Streams.copy(uploadedPdfStream, convertedStream);
      convertedStream.close();
    } catch(Exception ex) {
      log.error("Could not upload pdf consent form", ex);
      throw new RuntimeException(ex);
    }

    // Set the pdf content in the Consent.
    activeConsentService.getConsent().setPdfForm(convertedStream.toByteArray());
  }

}
