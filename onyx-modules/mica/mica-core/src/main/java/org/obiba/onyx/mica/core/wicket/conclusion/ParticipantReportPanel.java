package org.obiba.onyx.mica.core.wicket.conclusion;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.DynamicWebResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.DynamicWebResource.ResourceState;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.obiba.onyx.mica.core.service.impl.JadeReportContributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class ParticipantReportPanel extends Panel {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(BalsacConfirmationPanel.class);

  @SpringBean
  private JadeReportContributor jadeReportContributor;

  @SpringBean
  private ActiveConclusionService activeConclusionService;

  private Consent participantConsent;

  @SuppressWarnings("serial")
  public ParticipantReportPanel(String id) {

    super(id);
    setOutputMarkupId(true);

    participantConsent = activeConclusionService.getParticipantConsent();

    // Print participant consent form
    if(participantConsent.getPdfForm() != null) {
      byte[] consentPdf = participantConsent.getPdfForm();
      add(new InlineFrame("participantConsentDisplayFrame", new EmbeddedPdfPage(consentPdf)));
    } else {
      add(new WebMarkupContainer("participantConsentDisplayFrame").setVisible(false));
    }

    // Display participant report (embed pdf)
    byte[] reportPdf;
    try {
      reportPdf = IOUtils.toByteArray(jadeReportContributor.getReport(participantConsent.getLocale()));
    } catch(IOException e) {
      log.error("Cannot read the Participant Report");
      throw new RuntimeException(e);
    }
    add(new InlineFrame("participantReportDisplayFrame", new EmbeddedPdfPage(reportPdf)));

    // Add checkbox
    CheckBox printCheckBox = new CheckBox("printCheckBox", new Model());
    printCheckBox.setRequired(true);
    add(printCheckBox);
  }

  private class PdfResourceState extends ResourceState {
    private byte[] stream;

    public PdfResourceState(byte[] data) {
      stream = data;
    }

    public byte[] getData() {
      return stream;
    }

    public int getLength() {
      return stream.length;
    }

    public String getContentType() {
      return "application/pdf";
    }
  }

  // Download consent form from server and open it
  private Resource createConsentFormResource() {
    Resource r = new DynamicWebResource() {

      protected ResourceState getResourceState() {

        return new PdfResourceState(participantConsent.getPdfForm());
      }
    };
    return r;
  }

  // Create participant report form
  private Resource createParticipantReportResource() {
    Resource r = new DynamicWebResource() {

      protected ResourceState getResourceState() {

        InputStream in = jadeReportContributor.getReport(participantConsent.getLocale());

        try {
          return new PdfResourceState(IOUtils.toByteArray(in));
        } catch(IOException e) {
          throw new RuntimeException(e);
        }

      }
    };
    return r;
  }

  public void finish() {
  }
}