package org.obiba.onyx.mica.core.wicket.conclusion;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BalsacConfirmationPanel extends Panel {

  private static final long serialVersionUID = 1839206247478532673L;

  private static final Logger log = LoggerFactory.getLogger(BalsacConfirmationPanel.class);

  private static final String YES = "yes";

  private static final String NO = "no";

  private String selectedRadio;

  private String barcode;

  @SpringBean
  private ActiveConclusionService activeConclusionService;

  private BalsacSelection selectionModel;

  private TextField balsacBarcode;

  private final RadioGroup radioGroup;

  @SuppressWarnings("serial")
  public BalsacConfirmationPanel(String id) {

    super(id);
    setOutputMarkupId(true);

    selectionModel = new BalsacSelection();

    radioGroup = new RadioGroup("radioGroup", new PropertyModel(this, "selectedRadio"));
    radioGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        log.info("radioGroup.onUpdate={}", selectedRadio);
        if(selectedRadio.equals(NO)) {
          log.info("");
          selectionModel.setAccepted(false);
        } else {
          if(selectedRadio.equals(YES)) {
            log.info("");
            selectionModel.setAccepted(true);
          }
        }
        target.addComponent(BalsacConfirmationPanel.this);
        target.appendJavascript("Resizer.resizeWizard();");
      }
    });

    add(radioGroup);

    ListView radioList = new ListView("radioItem", Arrays.asList(new String[] { YES, NO })) {

      @Override
      protected void populateItem(final ListItem item) {
        final String key = item.getModelObjectAsString();
        Radio radio = new Radio("radio", item.getModel());

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

    balsacBarcode = new TextField("BalsacBarcode", new PropertyModel(BalsacConfirmationPanel.this, "barcode"), String.class) {
      @Override
      public boolean isEnabled() {

        // The text area is displayed only when "YES" radiobutton is chosen
        if(selectedRadio != null && selectedRadio.equals(YES)) {
          return true;
        } else {
          balsacBarcode.clearInput();
          return false;
        }
      }

      @Override
      public boolean isRequired() {
        return isVisible();
      }
    };
    balsacBarcode.add(new PatternValidator(activeConclusionService.getBalsacBarcodePattern()));
    balsacBarcode.setOutputMarkupId(true);
    add(balsacBarcode);
  }

  public void save() {
    if(radioGroup.getValue().equals(YES)) {
      activeConclusionService.getConclusion().setAccepted(true);
      activeConclusionService.getConclusion().setBarcode(balsacBarcode.getValue());
    } else {
      activeConclusionService.getConclusion().setAccepted(false);

    }
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

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }
}