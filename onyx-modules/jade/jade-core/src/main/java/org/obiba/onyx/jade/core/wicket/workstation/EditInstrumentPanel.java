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
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
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

  @SpringBean
  private UserSessionService userSessionService;

  private FeedbackWindow feedback;

  private final InstrumentFragment instrumentFragment;

  /**
   * @param id
   * @param model
   */
  @SuppressWarnings("unchecked")
  public EditInstrumentPanel(String id, IModel<Instrument> instrumentModel, Dialog addInstrumentWindow) {
    super(id, instrumentModel);
    setDefaultModel(instrumentModel);

    add(feedback = new FeedbackWindow("feedback"));
    feedback.setOutputMarkupId(true);

    add(instrumentFragment = new InstrumentFragment("instrumentFragmentContent", instrumentModel));
    instrumentFragment.setOutputMarkupId(true);

    // measurement field
    DropDownChoice measurementDropDown = new DropDownChoice("measurementSelect", new PropertyModel(instrumentModel, "type"), getAvailableInstrumentTypesList(), new IChoiceRenderer() {
      private static final long serialVersionUID = 1L;

      public Object getDisplayValue(Object object) {
        return new SpringStringResourceModel(object.toString()).getString();
      }

      public String getIdValue(Object object, int index) {
        return object.toString();
      }
    });
    measurementDropDown.setLabel(new ResourceModel("Measurement"));
    measurementDropDown.setRequired(true);
    add(measurementDropDown);

    // barcode field
    TextField barcode = new TextField("barcode", new PropertyModel(instrumentModel, "barcode"));
    barcode.add(new AjaxFormComponentUpdatingBehavior("onchange") {
      private static final long serialVersionUID = 1L;

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        String barcode = ((Instrument) getDefaultModelObject()).getBarcode();
        if(barcode != null) {
          Instrument instrument = instrumentService.getInstrumentByBarcode(barcode);
          if(instrument != null) {
            EditInstrumentPanel.this.setDefaultModelObject(instrument);
            instrumentFragment.getInstructions().setVisible(false);
          } else {
            instrumentFragment.getInstructions().setVisible(true);
          }
          target.addComponent(instrumentFragment);
        }
      }

      @Override
      protected void onError(AjaxRequestTarget target, RuntimeException e) {
        super.onError(target, e);
      }
    });

    barcode.setRequired(true);
    add(barcode);

    // actions to perform on submit
    addInstrumentWindow.setCloseButtonCallback(new CloseButtonCallback() {
      private static final long serialVersionUID = 1L;

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {

        if(status != null && status.equals(Dialog.Status.ERROR)) {
          FeedbackWindow feedback = EditInstrumentPanel.this.getFeedback();
          feedback.setContent(new FeedbackPanel("content"));
          feedback.show(target);
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
          instrumentService.updateInstrument(instrument);
        }
      }
    });
  }

  private List<String> getAvailableInstrumentTypesList() {
    List<String> instrumentTypes = new ArrayList<String>();

    List<String> workstationInstrumentTypes = instrumentService.getWorkstationInstrumentTypes(userSessionService.getWorkstation());
    for(String instrumentType : instrumentService.getInstrumentTypes().keySet()) {
      if(!workstationInstrumentTypes.contains(instrumentType) && !instrumentTypes.contains(instrumentType)) {
        instrumentTypes.add(instrumentType);
      }
    }

    return instrumentTypes;
  }

  @SuppressWarnings("unchecked")
  public class InstrumentFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Label instructions;

    public InstrumentFragment(String id, IModel<Instrument> instrumentModel) {
      super(id, "instrumentFragment", EditInstrumentPanel.this);

      // instructions
      instructions = new Label("instructions", new StringResourceModel("AddInstrumentInstructions", this, null));
      instructions.setVisible(false);
      instructions.setOutputMarkupId(true);
      add(instructions);

      // name field
      TextField name = new TextField("name", new PropertyModel(instrumentModel, "name"));
      name.setRequired(true);
      add(name);

      // vendor field
      TextField vendor = new TextField("vendor", new PropertyModel(instrumentModel, "vendor"));
      vendor.setRequired(true);
      add(vendor);

      // model field
      TextField model = new TextField("model", new PropertyModel(instrumentModel, "model"));
      model.setRequired(true);
      add(model);

      // CheckBox workstation = new CheckBox("workstation", new PropertyModel(instrumentModel, "workstation"));
    }

    public Label getInstructions() {
      return instructions;
    }
  }

  public FeedbackWindow getFeedback() {
    return feedback;
  }

  public void setFeedback(FeedbackWindow feedback) {
    this.feedback = feedback;
  }
}
