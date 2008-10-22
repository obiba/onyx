package org.obiba.onyx.mica.core.wicket.conclusion;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class ParticipantReportPanel extends Panel {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(BalsacConfirmationPanel.class);

  @SpringBean
  private ActiveConclusionService activeConclusionService;

  // private BalsacSelection selectionModel;

  @SuppressWarnings("serial")
  public ParticipantReportPanel(String id) {

    super(id);
    setOutputMarkupId(true);

    // Print participant consent form
    add(new AjaxLink("uploadConsentForm") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        // Upload consent form from server and open it
      }

    });

    // Print participant consent form
    add(new AjaxLink("uploadParticipantReport") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        // Upload report to participant template, fill it and open it
        System.out.println("*********************clicked*********************");
      }

    });

    // add checkbox component and its behaviour
  }

  @SuppressWarnings("serial")
  private class BalsacSelection implements Serializable {

    public Boolean isAccapted() {
      return activeConclusionService.getConclusion().isAccepted();
    }

    public void setAccepted(Boolean accepted) {
      activeConclusionService.getConclusion().setAccepted(accepted);
    }

    public String getBarcode() {
      return activeConclusionService.getConclusion().getBarcode();
    }

    public void setBarcode(String barcode) {
      activeConclusionService.getConclusion().setBarcode(barcode);
    }
  }

  public void finish() {

  }
}