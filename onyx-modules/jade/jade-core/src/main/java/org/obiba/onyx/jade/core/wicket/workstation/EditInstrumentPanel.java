/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.workstation;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.onyx.wicket.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.reusable.Dialog.WindowClosedCallback;

/**
 * 
 */
public class EditInstrumentPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private InstrumentService instrumentService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private FeedbackWindow feedback;

  private final InstrumentFragment instrumentFragment;

  private DropDownChoice<InstrumentType> measurementDropDown;

  private boolean currentWorkstation = false;

  /**
   * @param id
   * @param model
   */
  public EditInstrumentPanel(String id, IModel<Instrument> instrumentModel, Dialog addInstrumentWindow) {
    super(id, instrumentModel);
    setDefaultModel(instrumentModel);

    add(feedback = new FeedbackWindow("feedback"));
    feedback.setOutputMarkupId(true);

    add(instrumentFragment = new InstrumentFragment("instrumentFragmentContent", instrumentModel));
    instrumentFragment.setOutputMarkupId(true);

    // Model saves an instrument type name (as a String) and returns the associated InstrumentType.
    PropertyModel<InstrumentType> instrumentTypeModel = new PropertyModel<InstrumentType>(instrumentModel, "type") {
      private static final long serialVersionUID = 1L;

      @Override
      public InstrumentType getObject() {
        return instrumentService.getInstrumentType(((Instrument) getDefaultModelObject()).getType());
      }

    };

    // measurement field
    measurementDropDown = new DropDownChoice<InstrumentType>("measurementSelect", instrumentTypeModel, new ArrayList<InstrumentType>(instrumentService.getInstrumentTypes().values()), new IChoiceRenderer<InstrumentType>() {
      private static final long serialVersionUID = 1L;

      public Object getDisplayValue(InstrumentType object) {
        return new SpringStringResourceModel(object.getName() + ".description", object.getName()).getString();
      }

      public String getIdValue(InstrumentType object, int index) {
        return object.getName();
      }
    });

    measurementDropDown.setLabel(new ResourceModel("Measurement"));
    measurementDropDown.setRequired(true);
    measurementDropDown.setOutputMarkupId(true);
    add(measurementDropDown);

    // barcode field
    TextField barcode = new TextField("barcode", new PropertyModel(instrumentModel, "barcode"));
    barcode.add(new AjaxFormComponentUpdatingBehavior("onchange") {
      private static final long serialVersionUID = 1L;

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        String barcodeInput = ((Instrument) getDefaultModelObject()).getBarcode();

        if(barcodeInput != null) {
          Instrument instrument = instrumentService.getInstrumentByBarcode(barcodeInput);
          if(instrument != null) {
            // Disable form when displaying existing instrument.
            instrumentFragment.getName().setEnabled(false);
            instrumentFragment.getVendor().setEnabled(false);
            instrumentFragment.getModel().setEnabled(false);
            instrumentFragment.getWorkstation().setEnabled(false);
            displayInstrument(instrument);
            target.addComponent(measurementDropDown);
            target.appendJavascript("$('.obiba-button-ok').attr('disabled','true');$('.obiba-button-ok').css('color','rgba(0, 0, 0, 0.2)');$('.obiba-button-ok').css('border-color','rgba(0, 0, 0, 0.2)');");

          } else {
            // Enable form in order to add a new instrument.
            instrumentFragment.getName().setEnabled(true);
            instrumentFragment.getVendor().setEnabled(true);
            instrumentFragment.getModel().setEnabled(true);
            instrumentFragment.getWorkstation().setEnabled(true);
            target.appendJavascript("$('.obiba-button-ok').attr('disabled','');$('.obiba-button-ok').css('color','');$('.obiba-button-ok').css('border-color','');");
            instrumentFragment.getInstructions().setVisible(true);
            clearForm(target);
          }
        } else {
          clearForm(target);
        }
        target.addComponent(instrumentFragment);
      }

      @Override
      protected void onError(AjaxRequestTarget target, RuntimeException e) {
        clearForm(target);
        target.addComponent(instrumentFragment);
      }

    });

    barcode.setRequired(true);
    barcode.setOutputMarkupId(true);
    add(barcode);

    // actions to perform on submit
    addInstrumentWindow.setCloseButtonCallback(new CloseButtonCallback() {
      private static final long serialVersionUID = 1L;

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {

        if(status != null && status.equals(Dialog.Status.ERROR)) {
          displayFeedback(target);
          return false;
        }

        return true;
      }
    });

    addInstrumentWindow.setWindowClosedCallback(new WindowClosedCallback() {
      private static final long serialVersionUID = 1L;

      public void onClose(AjaxRequestTarget target, Status status) {

        if(status != null && !status.equals(Dialog.Status.CANCELLED) && !status.equals(Dialog.Status.WINDOW_CLOSED)) {
          Instrument instrument = (Instrument) getDefaultModelObject();
          instrument.setStatus(InstrumentStatus.ACTIVE);
          if(EditInstrumentPanel.this.currentWorkstation) {
            instrument.setWorkstation(userSessionService.getWorkstation());
          } else {
            instrument.setWorkstation(null);
          }
          instrumentService.updateInstrument(instrument);
        }
        target.addComponent(EditInstrumentPanel.this.findParent(WorkstationPanel.class).getInstrumentList());
      }
    });
  }

  private void displayInstrument(Instrument instrument) {
    EditInstrumentPanel.this.setDefaultModelObject(instrument);
    measurementDropDown.setChoices(Arrays.asList(new InstrumentType[] { instrumentService.getInstrumentType(instrument.getType()) }));

    measurementDropDown.setEnabled(false);
    if(instrument.getWorkstation() != null) {
      currentWorkstation = (instrument.getWorkstation().equals(userSessionService.getWorkstation()));
      if(!currentWorkstation) {
        instrumentFragment.getWorkstation().setEnabled(false);
        instrument.setWorkstation(null);
      }
    } else {
      currentWorkstation = false;
    }
    instrumentFragment.getInstructions().setVisible(false);
  }

  private void clearForm(AjaxRequestTarget target) {
    currentWorkstation = false;
    if(measurementDropDown.isEnabled() == false) {
      measurementDropDown.setEnabled(true);
      measurementDropDown.setChoices(new ArrayList<InstrumentType>(instrumentService.getInstrumentTypes().values()));
      target.addComponent(measurementDropDown);
      EditInstrumentPanel.this.setDefaultModelObject(new Instrument());
    }
    instrumentFragment.getName().setDefaultModelObject(null);
    instrumentFragment.getVendor().setDefaultModelObject(null);
    instrumentFragment.getModel().setDefaultModelObject(null);
    instrumentFragment.getWorkstation().setEnabled(true);
  }

  private void displayFeedback(AjaxRequestTarget target) {
    FeedbackWindow feedback = EditInstrumentPanel.this.getFeedback();
    feedback.setContent(new FeedbackPanel("content"));
    feedback.show(target);
  }

  @SuppressWarnings("unchecked")
  public class InstrumentFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Label instructions;

    private CheckBox workstation;

    private TextField name;

    private TextField vendor;

    private TextField model;

    public InstrumentFragment(String id, IModel<Instrument> instrumentModel) {
      super(id, "instrumentFragment", EditInstrumentPanel.this);

      // instructions
      instructions = new Label("instructions", new StringResourceModel("AddInstrumentInstructions", this, null));
      instructions.setVisible(false);
      instructions.setOutputMarkupId(true);
      add(instructions);

      // name field
      name = new TextField("name", new PropertyModel(instrumentModel, "name"));
      add(name);

      // vendor field
      vendor = new TextField("vendor", new PropertyModel(instrumentModel, "vendor"));
      add(vendor);

      // model field
      model = new TextField("model", new PropertyModel(instrumentModel, "model"));
      add(model);

      add(workstation = new CheckBox("workstation", new PropertyModel(EditInstrumentPanel.this, "currentWorkstation")));
      add(new FormComponentLabel("workstationLabel", workstation));
    }

    public Label getInstructions() {
      return instructions;
    }

    public CheckBox getWorkstation() {
      return workstation;
    }

    public TextField getName() {
      return name;
    }

    public TextField getVendor() {
      return vendor;
    }

    public TextField getModel() {
      return model;
    }
  }

  public FeedbackWindow getFeedback() {
    return feedback;
  }

  public void setFeedback(FeedbackWindow feedback) {
    this.feedback = feedback;
  }
}
