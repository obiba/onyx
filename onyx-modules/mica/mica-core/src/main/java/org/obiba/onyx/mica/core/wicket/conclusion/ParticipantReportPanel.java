package org.obiba.onyx.mica.core.wicket.conclusion;

import java.util.List;
import java.util.Locale;

import javax.print.PrintException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.io.support.LocalizedResourceLoader;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.mica.core.service.impl.VariableReportContributor;
import org.obiba.onyx.print.PdfPrintingService;
import org.obiba.wicket.markup.html.form.LocaleDropDownChoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class ParticipantReportPanel extends Panel {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(BalsacConfirmationPanel.class);

  @SpringBean
  private VariableReportContributor variableReportContributor;

  @SpringBean
  private ConsentService consentService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private PdfPrintingService pdfPrintingService;

  private Consent participantConsent;

  private Locale reportLocale;

  @SpringBean(name = "consentFormTemplateLoader")
  private LocalizedResourceLoader reportTemplateLoader;

  private AjaxLink printParticipantReport;

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
    printParticipantReport = new AjaxLink("printParticipantReport") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        try {
          pdfPrintingService.printPdf(variableReportContributor.getReport(getReportLocale()));
        } catch(PrintException e) {
          log.error("Participant Report cannot be printed", e);
        }

      }

    };
    printParticipantReport.setEnabled(false);
    printParticipantReport.setOutputMarkupId(true);
    add(printParticipantReport);

    // Add checkbox
    CheckBox printCheckBox = new CheckBox("printCheckBox", new Model());
    printCheckBox.setRequired(true);
    add(printCheckBox);

    // Add language dropdown
    add(createLanguageDropDown(reportTemplateLoader.getAvailableLocales()));

  }

  private DropDownChoice createLanguageDropDown(List<Locale> locales) {
    final LocaleDropDownChoice languageDropDown = new LocaleDropDownChoice("reportLanguage", new PropertyModel(ParticipantReportPanel.this, "reportLocale"), locales);
    languageDropDown.setUseSessionLocale(true);
    languageDropDown.setNullValid(true);
    languageDropDown.setOutputMarkupId(true);

    languageDropDown.add(new OnChangeAjaxBehavior() {

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        languageDropDown.setNullValid(false);
        printParticipantReport.setEnabled(true);
        target.addComponent(printParticipantReport);
        target.addComponent(languageDropDown);
      }
    });

    return languageDropDown;
  }

  public Locale getReportLocale() {
    return reportLocale;
  }

  public void setReportLocale(Locale reportLocale) {
    this.reportLocale = reportLocale;
  }

}