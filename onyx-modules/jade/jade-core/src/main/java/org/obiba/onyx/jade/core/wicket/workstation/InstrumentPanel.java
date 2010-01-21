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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentMeasurementType;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentUsage;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.onyx.wicket.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.reusable.Dialog.WindowClosedCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentPanel extends Panel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InstrumentPanel.class);

  //
  // Instance Variables
  //

  @SpringBean
  private InstrumentService instrumentService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private FeedbackWindow feedback;

  private final InstrumentFragment instrumentFragment;

  private Model<InstrumentMeasurementType> instrumentMeasurementTypeModel;

  private DropDownChoice<InstrumentType> measurementDropDown;

  /**
   * Indicates whether the <code>InstrumentPanel</code> should be created in "edit" mode (as opposed to "add" mode).
   */
  private boolean editMode;

  //
  // Constructors
  //

  public InstrumentPanel(String id, IModel<Instrument> instrumentModel, Dialog instrumentWindow, boolean editMode) {
    super(id, instrumentModel);
    setDefaultModel(instrumentModel);
    if(!editMode) {
      instrumentMeasurementTypeModel = new Model<InstrumentMeasurementType>(new InstrumentMeasurementType());
    }

    this.editMode = editMode;
    if(!editMode) {
      instrumentModel.getObject().setStatus(InstrumentStatus.ACTIVE);
      instrumentModel.getObject().setWorkstation(userSessionService.getWorkstation());
    }

    add(feedback = new FeedbackWindow("feedback"));
    feedback.setOutputMarkupId(true);

    add(instrumentFragment = new InstrumentFragment("instrumentFragmentContent", instrumentModel));
    instrumentFragment.setOutputMarkupId(true);

    addMeasurementField();
    addBarcodeField();
    registerWindowCallbacks(instrumentWindow);
  }

  private Instrument getInstrument() {
    return (Instrument) getDefaultModelObject();
  }

  //
  // Methods
  //

  public void setInstrumentUsage(InstrumentUsage usage) {
    if(usage.equals(InstrumentUsage.RESERVED)) {
      getInstrument().setStatus(InstrumentStatus.ACTIVE);
      getInstrument().setWorkstation(userSessionService.getWorkstation());
    } else if(usage.equals(InstrumentUsage.SHARED)) {
      getInstrument().setStatus(InstrumentStatus.ACTIVE);
      getInstrument().setWorkstation(null);
    } else {
      getInstrument().setStatus(InstrumentStatus.INACTIVE);
      getInstrument().setWorkstation(null);
    }
  }

  public InstrumentUsage getInstrumentUsage() {
    return getInstrument().getUsage();
  }

  /**
   * @param editMode
   * @param instrumentTypeModel
   */
  private void addMeasurementField() {
    if(editMode) {
      String str = "";
      for(InstrumentMeasurementType type : instrumentService.getWorkstationInstrumentMeasurementTypes(getInstrument())) {
        if(str.length() > 0) {
          str += ", ";
        }
        str += new SpringStringResourceModel(type.getType() + ".description", type.getType()).getString();
      }
      add(new Label("measurements", new Model<String>(str)).setEnabled(false));
      add(new DropDownChoice<InstrumentType>("measurementSelect").setVisible(false));

    } else {
      // Model saves an instrument type name (as a String) and returns the associated InstrumentType.
      PropertyModel<InstrumentType> instrumentTypeModel = new PropertyModel<InstrumentType>(instrumentMeasurementTypeModel, "type") {
        private static final long serialVersionUID = 1L;

        @Override
        public InstrumentType getObject() {
          return instrumentService.getInstrumentType(instrumentMeasurementTypeModel.getObject().getType());
        }

      };

      // an instrument type list model, that excludes the types the instrument is already associated with.
      IModel<List<InstrumentType>> instrumentTypesModel = new LoadableDetachableModel<List<InstrumentType>>() {

        @Override
        protected List<InstrumentType> load() {
          List<InstrumentType> instrumentTypes;

          // check if instrument is already persisted
          if(getInstrument().getId() == null) {
            instrumentTypes = new ArrayList<InstrumentType>(instrumentService.getInstrumentTypes().values());
          } else {
            instrumentTypes = new ArrayList<InstrumentType>();

            for(InstrumentType type : instrumentService.getInstrumentTypes().values()) {
              boolean alreadyAssociated = false;
              for(InstrumentMeasurementType associatedType : instrumentService.getWorkstationInstrumentMeasurementTypes(getInstrument())) {
                if(associatedType.getType().equals(type.getName())) {
                  alreadyAssociated = true;
                  break;
                }
              }
              if(!alreadyAssociated) {
                instrumentTypes.add(type);
              }
            }
          }

          // sort the list
          Collections.sort(instrumentTypes, new Comparator<InstrumentType>() {

            public int compare(InstrumentType o1, InstrumentType o2) {
              String localName1 = new SpringStringResourceModel(o1.getName() + ".description", o1.getName()).getString();
              String localName2 = new SpringStringResourceModel(o2.getName() + ".description", o2.getName()).getString();
              return localName1.compareTo(localName2);
            }

          });

          return instrumentTypes;
        }

      };

      measurementDropDown = new DropDownChoice<InstrumentType>("measurementSelect", instrumentTypeModel, instrumentTypesModel, new IChoiceRenderer<InstrumentType>() {
        private static final long serialVersionUID = 1L;

        public Object getDisplayValue(InstrumentType object) {
          return new SpringStringResourceModel(object.getName() + ".description", object.getName()).getString();
        }

        public String getIdValue(InstrumentType object, int index) {
          return object.getName();
        }
      });

      measurementDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
          // a do nothing ajax callback to automatically submit the value of this input when changed
        }

      });

      measurementDropDown.setLabel(new ResourceModel("Measurement"));
      measurementDropDown.setRequired(true);
      measurementDropDown.setOutputMarkupId(true);
      add(measurementDropDown);

      add(new Label("measurements").setVisible(false));
    }
  }

  /**
   * @param instrumentModel
   * @param editMode
   */
  private void addBarcodeField() {
    TextField<String> barcode = new TextField<String>("barcode", new PropertyModel<String>(getDefaultModel(), "barcode"));
    barcode.setEnabled(!editMode);

    barcode.add(new AjaxFormComponentUpdatingBehavior("onchange") {
      private static final long serialVersionUID = 1L;

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        String barcodeInput = getInstrument().getBarcode();

        if(barcodeInput != null) {
          Instrument instrument = instrumentService.getInstrumentByBarcode(barcodeInput);
          if(instrument != null) {
            // Disable form when displaying existing instrument.
            instrumentFragment.getName().setEnabled(false);
            instrumentFragment.getVendor().setEnabled(false);
            instrumentFragment.getModel().setEnabled(false);
            instrumentFragment.getSerialNumber().setEnabled(false);

            // Set usage to RESERVED, so that clicking Register re-assigns instrument to current workstation.
            setDefaultModelObject(instrument);
            setInstrumentUsage(InstrumentUsage.RESERVED);

            displayInstrument(instrument);

          } else {
            // In case of back and forth, between existing and non-existing instruments.
            setDefaultModelObject(new Instrument());

            // Enable form in order to add a new instrument.
            instrumentFragment.getName().setEnabled(true);
            instrumentFragment.getVendor().setEnabled(true);
            instrumentFragment.getModel().setEnabled(true);
            instrumentFragment.getSerialNumber().setEnabled(true);
            instrumentFragment.getInstructions().setVisible(true);
            clearForm(target);
          }
        } else {
          clearForm(target);
        }

        target.addComponent(measurementDropDown);
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
  }

  /**
   * @param instrumentWindow
   */
  private void registerWindowCallbacks(Dialog instrumentWindow) {
    // actions to perform on submit
    instrumentWindow.setCloseButtonCallback(new CloseButtonCallback() {
      private static final long serialVersionUID = 1L;

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {

        if(status != null && status.equals(Dialog.Status.ERROR)) {
          displayFeedback(target);
          return false;
        }

        return true;
      }
    });

    instrumentWindow.setWindowClosedCallback(new WindowClosedCallback() {
      private static final long serialVersionUID = 1L;

      public void onClose(AjaxRequestTarget target, Status status) {

        if(status != null && !status.equals(Dialog.Status.CANCELLED) && !status.equals(Dialog.Status.WINDOW_CLOSED)) {
          instrumentService.updateInstrument(getInstrument());

          if(!editMode) {
            InstrumentMeasurementType instrumentMeasurementType = instrumentMeasurementTypeModel.getObject();
            instrumentService.addInstrumentMeasurementType(getInstrument(), instrumentMeasurementType);
          }
        }
        target.addComponent(InstrumentPanel.this.findParent(WorkstationPanel.class).getInstrumentMeasurementTypeList());
        target.appendJavascript("styleWorkstationNavigationBar();");
      }
    });
  }

  private void displayInstrument(Instrument instrument) {
    InstrumentPanel.this.setDefaultModelObject(instrument);
    instrumentFragment.getInstructions().setVisible(false);
  }

  private void clearForm(AjaxRequestTarget target) {
    instrumentFragment.getName().setDefaultModelObject(null);
    instrumentFragment.getVendor().setDefaultModelObject(null);
    instrumentFragment.getModel().setDefaultModelObject(null);
    instrumentFragment.getSerialNumber().setDefaultModelObject(null);

    getInstrument().setStatus(InstrumentStatus.ACTIVE);
    getInstrument().setWorkstation(userSessionService.getWorkstation());
  }

  private void displayFeedback(AjaxRequestTarget target) {
    FeedbackWindow feedback = InstrumentPanel.this.getFeedback();
    feedback.setContent(new FeedbackPanel("content"));
    feedback.show(target);
  }

  public FeedbackWindow getFeedback() {
    return feedback;
  }

  public void setFeedback(FeedbackWindow feedback) {
    this.feedback = feedback;
  }

  //
  // Inner Classes
  //

  class InstrumentFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Label instructions;

    private TextField name;

    private TextField vendor;

    private TextField model;

    private TextField serialNumber;

    private RadioGroup instrumentRadioGroup;

    private Radio reserveInstrument;

    private Component reserveInstrumentLabel;

    private Radio shareInstrument;

    private Component shareInstrumentLabel;

    private Radio deactivateInstrument;

    private Component deactivateInstrumentLabel;

    public InstrumentFragment(String id, IModel<Instrument> instrumentModel) {
      super(id, "instrumentFragment", InstrumentPanel.this);

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

      // model field
      serialNumber = new TextField("serialNumber", new PropertyModel(instrumentModel, "serialNumber"));
      add(serialNumber);

      instrumentRadioGroup = new RadioGroup<InstrumentUsage>("instrumentRadioGroup", new PropertyModel<InstrumentUsage>(InstrumentPanel.this, "instrumentUsage")) {
        private static final long serialVersionUID = 1L;

        public boolean isVisible() {
          return InstrumentPanel.this.editMode;
        }
      };
      add(instrumentRadioGroup);

      instrumentRadioGroup.add(reserveInstrument = new Radio<InstrumentUsage>("reserveInstrument", new Model<InstrumentUsage>(InstrumentUsage.RESERVED)));
      instrumentRadioGroup.add(reserveInstrumentLabel = new FormComponentLabel("reserveInstrumentLabel", reserveInstrument));
      instrumentRadioGroup.add(shareInstrument = new Radio<InstrumentUsage>("shareInstrument", new Model<InstrumentUsage>(InstrumentUsage.SHARED)));
      instrumentRadioGroup.add(shareInstrumentLabel = new FormComponentLabel("shareInstrumentLabel", shareInstrument));
      instrumentRadioGroup.add(deactivateInstrument = new Radio<InstrumentUsage>("deactivateInstrument", new Model<InstrumentUsage>(InstrumentUsage.OUT_OF_SERVICE)));
      instrumentRadioGroup.add(deactivateInstrumentLabel = new FormComponentLabel("deactivateInstrumentLabel", deactivateInstrument));
    }

    public Label getInstructions() {
      return instructions;
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

    public TextField getSerialNumber() {
      return serialNumber;
    }
  }
}
