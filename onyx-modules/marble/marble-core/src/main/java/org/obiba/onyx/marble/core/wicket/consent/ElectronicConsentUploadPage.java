package org.obiba.onyx.marble.core.wicket.consent;

import java.io.ByteArrayOutputStream;

import javax.servlet.ServletInputStream;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringBean;
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

  private PageParameters pageParams;

  private Boolean consentIsAccepted;

  public ElectronicConsentUploadPage(PageParameters pageParams) {
    super(pageParams);
    this.pageParams = pageParams;
    confirmConsent();

    // Don't save consent form if the participant has refused to sign it.
    if(consentIsAccepted) {
      uploadElectronicForm();
    }

    setResponsePage(ElectronicConsentSubmittedPage.class);
  }

  private void confirmConsent() {
    consentIsAccepted = pageParams.getBoolean("accepted");
    if(consentIsAccepted == null) {
      log.error("Missing \"accepted\" parameter in the PDF form submit button URL, this parameter must be set to true or false (true for the accept consent button, false for the refuse consent button)");
      throw new RuntimeException();
    } else {
      activeConsentService.getConsent().setAccepted(consentIsAccepted);
    }
  }

  private void uploadElectronicForm() {
    ByteArrayOutputStream convertedStream = new ByteArrayOutputStream();

    try {

      // Get inputstream on servlet request.
      ServletInputStream uploadedPdfStream = getWebRequestCycle().getWebRequest().getHttpServletRequest().getInputStream();

      byte[] readBuffer = new byte[1024];
      int bytesRead;

      // Read the PDF to an OutputStream.
      while((bytesRead = uploadedPdfStream.read(readBuffer)) > 0) {
        convertedStream.write(readBuffer, 0, bytesRead);
      }
    } catch(Exception ex) {
      log.error("Could not upload pdf consent form", ex);
      throw new RuntimeException(ex);
    }

    // Set the pdf content in the Consent.
    activeConsentService.getConsent().setPdfForm(convertedStream.toByteArray());
  }

}
