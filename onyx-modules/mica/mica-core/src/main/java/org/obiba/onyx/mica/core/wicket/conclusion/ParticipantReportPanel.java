package org.obiba.onyx.mica.core.wicket.conclusion;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.obiba.onyx.wicket.reusable.ReusableDialogProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class ParticipantReportPanel extends Panel {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(BalsacConfirmationPanel.class);

  public ParticipantReportPanel(String id) {

    super(id);
    setOutputMarkupId(true);

    class ReportLink extends AjaxLink {

      private static final long serialVersionUID = 1L;

      public ReportLink(String id) {
        super(id);
      }

      @Override
      public void onClick(AjaxRequestTarget target) {
        ((ReusableDialogProvider) getPage()).getPrintableReportsDialog().show(target);
      }
    }

    ReportLink printReportLink = new ReportLink("printReport");
    printReportLink.add(new Label("reportLabel", new ResourceModel("PrintReport")));
    printReportLink.add(new AttributeAppender("class", true, new Model("ui-corner-all"), " "));
    add(printReportLink);

    // Add checkbox
    CheckBox printCheckBox = new CheckBox("printCheckBox", new Model());
    printCheckBox.setRequired(true);
    add(printCheckBox);

  }

}