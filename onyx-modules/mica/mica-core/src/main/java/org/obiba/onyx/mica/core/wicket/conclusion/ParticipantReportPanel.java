package org.obiba.onyx.mica.core.wicket.conclusion;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.DynamicWebResource;
import org.apache.wicket.markup.html.DynamicWebResource.ResourceState;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.mica.core.service.impl.JadeReportContributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class ParticipantReportPanel extends Panel {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(BalsacConfirmationPanel.class);

  @SpringBean
  private ActiveConsentService activeConsentService;

  @SpringBean
  private JadeReportContributor jadeReportContributor;

  @SuppressWarnings("serial")
  public ParticipantReportPanel(String id) {

    super(id);
    setOutputMarkupId(true);

    // Print participant consent form
    if(activeConsentService.getConsent().getPdfForm() != null) {
      add(new ResourceLink("consentFormLink", this.createConsentFormResource()));
    } else {
      add(new EmptyPanel("consentFormLink").setVisible(false));
    }

    // Print participant report
    add(new ResourceLink("participantReportLink", this.createParticipantReportResource()));

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
  @SuppressWarnings("unused")
  private Resource createConsentFormResource() {
    Resource r = new DynamicWebResource() {

      protected ResourceState getResourceState() {

        return new PdfResourceState(activeConsentService.getConsent().getPdfForm());
      }
    };
    return r;
  }

  // Create participant report form
  @SuppressWarnings("unused")
  private Resource createParticipantReportResource() {
    Resource r = new DynamicWebResource() {

      protected ResourceState getResourceState() {

        InputStream in = jadeReportContributor.getReport(activeConsentService.getConsent().getLocale());

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