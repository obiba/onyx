package org.obiba.onyx.mica.core.wicket.conclusion;

import javax.print.PrintException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.mica.core.service.impl.JadeReportContributor;
import org.obiba.onyx.print.PdfPrintingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class ParticipantReportPanel extends Panel {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(BalsacConfirmationPanel.class);

  @SpringBean
  private JadeReportContributor jadeReportContributor;

  @SpringBean
  private ConsentService consentService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private PdfPrintingService pdfPrintingService;

  private Consent participantConsent;

  @SuppressWarnings("serial")
  public ParticipantReportPanel(String id) {

    super(id);
    setOutputMarkupId(true);

    participantConsent = consentService.getConsent(activeInterviewService.getInterview());

    // Print participant consent form
    if(participantConsent.getPdfForm() != null) {
      add(new AjaxLink("printConsentForm") {

        public void onClick(AjaxRequestTarget target) {
          byte[] consentPdf = participantConsent.getPdfForm();
          try {
            pdfPrintingService.printPdf(consentPdf);
          } catch(PrintException e) {
            log.error("Participant Consent form cannot be printed", e);
          }
        }

      });

    } else {
      add(new WebMarkupContainer("printConsentForm").setVisible(false));
    }

    // Print participant report
    add(new AjaxLink("printParticipantReport") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        try {
          pdfPrintingService.printPdf(jadeReportContributor.getReport(participantConsent.getLocale()));
        } catch(PrintException e) {
          log.error("Participant Report cannot be printed", e);
        }

      }

    });

    // Add checkbox
    CheckBox printCheckBox = new CheckBox("printCheckBox", new Model());
    printCheckBox.setRequired(true);
    add(printCheckBox);
  }

  public void finish() {
  }
}