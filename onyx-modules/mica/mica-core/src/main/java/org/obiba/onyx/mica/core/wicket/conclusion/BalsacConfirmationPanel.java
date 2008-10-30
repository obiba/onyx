package org.obiba.onyx.mica.core.wicket.conclusion;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.obiba.onyx.mica.domain.conclusion.Conclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BalsacConfirmationPanel extends Panel {

  private static final long serialVersionUID = 1839206247478532673L;

  private static final Logger log = LoggerFactory.getLogger(BalsacConfirmationPanel.class);

  private static final String YES = "yes";

  private static final String NO = "no";

  @SpringBean
  private ActiveConclusionService activeConclusionService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  private BalsacSelection selectionModel;

  private TextField balsacBarcode;

  private final RadioGroup radioGroup;

  @SuppressWarnings("serial")
  public BalsacConfirmationPanel(String id) {

    super(id);
    setOutputMarkupId(true);

    selectionModel = new BalsacSelection();

    radioGroup = new RadioGroup("radioGroup", new Model());
    add(radioGroup);

    ListView radioList = new ListView("radioItem", Arrays.asList(new String[] { YES, NO })) {

      @Override
      protected void populateItem(final ListItem item) {
        final String key = item.getModelObjectAsString();
        Radio radio = new Radio("radio", item.getModel());
        radio.add(new AjaxEventBehavior("onchange") {

          @Override
          protected void onEvent(AjaxRequestTarget target) {
            log.info("onChange={}", key);

            if(key.equals(YES)) {
              radioGroup.setModel(item.getModel());
              selectionModel.setAccepted(true);
              enableBarcodeField(true);
              target.addComponent(BalsacConfirmationPanel.this);
            } else {
              if(key.equals(NO)) {
                radioGroup.setModel(item.getModel());
                selectionModel.setAccepted(false);
                enableBarcodeField(false);
                target.addComponent(BalsacConfirmationPanel.this);
              }
            }
          }
        });

        radio.setLabel(new StringResourceModel(key, BalsacConfirmationPanel.this, null));

        FormComponentLabel radioLabel = new FormComponentLabel("radioLabel", radio);
        item.add(radioLabel);
        radioLabel.add(radio);
        radioLabel.add(new Label("label", radio.getLabel()).setRenderBodyOnly(true));
      }

    }.setReuseItems(true);
    radioGroup.add(radioList);
    radioGroup.setRequired(true);

    add(new Label("balsacLabel", new StringResourceModel("BalsacBarcode", this, null)));

    balsacBarcode = new TextField("BalsacBarcode", new PropertyModel(activeConclusionService.getConclusion(), "barcode"), RequiredTextField.class);
    balsacBarcode.setOutputMarkupId(true);
    add(balsacBarcode);

    enableBarcodeField(false);
  }

  public void save() {
    activeConclusionService.setConclusion(null);
    Conclusion conclusion = new Conclusion();
    conclusion.setInterview(activeInterviewService.getInterview());
    activeConclusionService.setConclusion(conclusion);

    if(radioGroup.getValue().equals(YES)) {
      activeConclusionService.getConclusion().setAccepted(true);
      activeConclusionService.getConclusion().setBarcode(balsacBarcode.getValue());
    } else {
      activeConclusionService.getConclusion().setAccepted(false);

    }
    activeConclusionService.save();
  }

  private void enableBarcodeField(boolean visible) {
    balsacBarcode.setEnabled(visible);
    balsacBarcode.setRequired(visible);
  }

  @SuppressWarnings("serial")
  private class BalsacSelection implements Serializable {

    public Boolean isAccepted() {
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
}