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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentMeasurementType;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLog;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.reusable.Dialog.WindowClosedCallback;
import org.obiba.wicket.markup.html.border.SeparatorMarkupComponentBorder;

public class ActionsPanel extends Panel {

  private static final long serialVersionUID = 5855667390712874428L;

  private Dialog editInstrumentWindow;

  private Dialog deleteInstrumentConfirmationWindow;

  @SpringBean
  private ExperimentalConditionService experimentalConditionService;

  @SpringBean
  private InstrumentRunService instrumentRunService;

  @SpringBean
  private InstrumentService instrumentService;

  private ExperimentalConditionDialogHelperPanel experimentalConditionDialogHelperPanel;

  public ActionsPanel(String id, IModel<InstrumentMeasurementType> model) {
    super(id, model);
    setOutputMarkupId(true);

    InstrumentMeasurementType instrumentMeasurementType = (InstrumentMeasurementType) model.getObject();

    experimentalConditionDialogHelperPanel = new ExperimentalConditionDialogHelperPanel("experimentalConditionDialogHelperPanel", new Model<Instrument>(instrumentMeasurementType.getInstrument()), new Model<Instrument>(instrumentMeasurementType.getInstrument()));
    add(experimentalConditionDialogHelperPanel);

    editInstrumentWindow = createEditInstrumentWindow("editInstrumentWindow");
    add(editInstrumentWindow);

    deleteInstrumentConfirmationWindow = createDeleteInstrumentConfirmationDialogWindow("deleteConfirmationDialog");
    add(deleteInstrumentConfirmationWindow);

    RepeatingView repeating = new RepeatingView("link");
    add(repeating);
    SeparatorMarkupComponentBorder border = new SeparatorMarkupComponentBorder();

    for(LinkInfo linkInfo : getListOfLinkInfo(instrumentMeasurementType.getInstrument())) {
      AjaxLink<LinkInfo> link = new AjaxLink<LinkInfo>(repeating.newChildId(), new Model<LinkInfo>(linkInfo)) {
        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          getModelObject().onClick(target);
        }

      };
      link.add(new Label("action", new StringResourceModel(linkInfo.name, null)).setRenderBodyOnly(true));
      link.setComponentBorder(border);
      link.setVisible(linkInfo.isVisible());
      repeating.add(link);
    }

  }

  private List<LinkInfo> getListOfLinkInfo(Instrument instrument) {
    List<LinkInfo> linkInfoList = new ArrayList<LinkInfo>();
    linkInfoList.add(new CalibrateLinkInfo("Calibrate", instrument));
    linkInfoList.add(new EditLinkInfo("Edit", instrument));
    linkInfoList.add(new DeleteLinkInfo("Delete", instrument));
    return linkInfoList;
  }

  private abstract class LinkInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    protected Instrument instrument;

    public LinkInfo(String name, Instrument instrument) {
      this.name = name;
      this.instrument = instrument;
    }

    public boolean isVisible() {
      return true;
    }

    public String getName() {
      return name;
    }

    public Instrument getInstrument() {
      return instrument;
    }

    public abstract void onClick(AjaxRequestTarget target);
  }

  private class CalibrateLinkInfo extends LinkInfo {
    private static final long serialVersionUID = 1L;

    public CalibrateLinkInfo(String name, Instrument instrument) {
      super(name, instrument);
    }

    @Override
    public boolean isVisible() {
      return instrument.getStatus().equals(InstrumentStatus.ACTIVE) && experimentalConditionService.instrumentCalibrationExists(getInstrumentMeasurementType().getType());
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
      List<ExperimentalConditionLog> log = new ArrayList<ExperimentalConditionLog>();
      for(InstrumentCalibration cal : experimentalConditionService.getInstrumentCalibrationsByType(getInstrumentMeasurementType().getType())) {
        log.add((ExperimentalConditionLog) cal);
      }
      List<InstrumentCalibration> instrumentCalibrations = experimentalConditionService.getInstrumentCalibrationsByType(getInstrumentMeasurementType().getType());
      InstrumentCalibration instrumentCalibration = null;
      if(!instrumentCalibrations.isEmpty()) {
        instrumentCalibration = instrumentCalibrations.get(0);
      }
      experimentalConditionDialogHelperPanel.setExperimentalConditionLog(instrumentCalibration, log);

      experimentalConditionDialogHelperPanel.getExperimentalConditionDialog().setWindowClosedCallback(new WindowClosedCallback() {
        private static final long serialVersionUID = 1L;

        public void onClose(AjaxRequestTarget target, Status status) {
          target.addComponent(ActionsPanel.this.findParent(WorkstationPanel.class).getInstrumentMeasurementTypeList());
        }

      });
      SpringStringResourceModel experimentalConditionNameResource = new SpringStringResourceModel(getInstrumentMeasurementType().getType() + ".description", getInstrumentMeasurementType().getType());
      String experimentalConditionName = experimentalConditionNameResource.getString();
      experimentalConditionDialogHelperPanel.getExperimentalConditionDialog().setTitle(new StringResourceModel("ExperimentalConditionDialogTitle", ActionsPanel.this, new Model<ValueMap>(new ValueMap("experimentalConditionName=" + experimentalConditionName))));
      experimentalConditionDialogHelperPanel.getExperimentalConditionDialog().show(target);
    }
  }

  private class EditLinkInfo extends LinkInfo {
    private static final long serialVersionUID = 1L;

    public EditLinkInfo(String name, Instrument instrument) {
      super(name, instrument);
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
      InstrumentPanel component = new InstrumentPanel("content", new Model<Instrument>(instrument), editInstrumentWindow, true);
      component.add(new AttributeModifier("class", true, new Model<String>("obiba-content instrument-panel-content-edit")));
      editInstrumentWindow.setContent(component);
      editInstrumentWindow.show(target);
    }

  }

  private Dialog createEditInstrumentWindow(String id) {
    Dialog addInstrumentDialog = new Dialog(id);
    addInstrumentDialog.setTitle(new ResourceModel("EditInstrument"));
    addInstrumentDialog.setHeightUnit("em");
    addInstrumentDialog.setWidthUnit("em");
    addInstrumentDialog.setInitialHeight(20);
    addInstrumentDialog.setInitialWidth(34);
    addInstrumentDialog.setType(Dialog.Type.PLAIN);
    addInstrumentDialog.setOptions(Dialog.Option.OK_CANCEL_OPTION, "Save");
    return addInstrumentDialog;
  }

  private InstrumentMeasurementType getInstrumentMeasurementType() {
    return (InstrumentMeasurementType) (ActionsPanel.this.getDefaultModelObject());
  }

  private class DeleteLinkInfo extends LinkInfo {
    private static final long serialVersionUID = 1L;

    public DeleteLinkInfo(String name, Instrument instrument) {
      super(name, instrument);
    }

    @Override
    public boolean isVisible() {
      InstrumentRun template = new InstrumentRun();
      template.setInstrument(instrument);
      return instrumentRunService.getInstrumentRuns(template).isEmpty();
    }

    @Override
    public void onClick(AjaxRequestTarget target) {

      String instrumentType = new SpringStringResourceModel(getInstrumentMeasurementType().getType() + ".description", getInstrumentMeasurementType().getType()).getString();
      StringResourceModel questionModel = new StringResourceModel("DeleteInstrumentConfirmationQuestion", ActionsPanel.this, new Model<ValueMap>(new ValueMap("instrumentType=" + instrumentType + ",barcode=" + instrument.getBarcode())));
      Label question = new Label("content", questionModel);
      question.add(new AttributeModifier("class", true, new Model<String>("obiba-content delete-instrument-dialog")));
      deleteInstrumentConfirmationWindow.setContent(question);

      deleteInstrumentConfirmationWindow.setCloseButtonCallback(new CloseButtonCallback() {
        private static final long serialVersionUID = 1L;

        public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {
          if(status.equals(Status.YES)) {
            deleteInstrumentCalibrations(instrument.getBarcode());
            instrumentService.deleteInstrumentMeasurementType(getInstrumentMeasurementType());
          }
          return true;
        }

      });

      deleteInstrumentConfirmationWindow.setWindowClosedCallback(new WindowClosedCallback() {
        private static final long serialVersionUID = 1L;

        public void onClose(AjaxRequestTarget target, Status status) {
          if(status.equals(Status.YES)) {
            // Refresh instrument list.
            target.addComponent(ActionsPanel.this.findParent(WorkstationPanel.class).getInstrumentMeasurementTypeList());
          }
        }

      });

      deleteInstrumentConfirmationWindow.show(target);
    }

    private void deleteInstrumentCalibrations(String barcode) {
      List<ExperimentalCondition> experimentalConditions = getExperimentalConditions(barcode);
      for(ExperimentalCondition ec : experimentalConditions) {
        experimentalConditionService.deleteExperimentalCondition(ec);
      }
    }

    private List<ExperimentalCondition> getExperimentalConditions(String barcode) {
      ExperimentalCondition template = new ExperimentalCondition();
      ExperimentalConditionValue ecv = new ExperimentalConditionValue();
      ecv.setAttributeType(DataType.TEXT);
      ecv.setAttributeName(ExperimentalConditionService.INSTRUMENT_BARCODE);
      ecv.setData(new Data(DataType.TEXT, barcode));
      ecv.setExperimentalCondition(template);
      template.addExperimentalConditionValue(ecv);

      return experimentalConditionService.getExperimentalConditions(template);
    }
  }

  private Dialog createDeleteInstrumentConfirmationDialogWindow(String id) {
    Dialog deleteInstrumentConfirmationDialog = new Dialog(id);
    deleteInstrumentConfirmationDialog.setTitle(new ResourceModel("DeleteInstrumentConfirmationTitle"));
    deleteInstrumentConfirmationDialog.setHeightUnit("em");
    deleteInstrumentConfirmationDialog.setWidthUnit("em");
    deleteInstrumentConfirmationDialog.setInitialHeight(8);
    deleteInstrumentConfirmationDialog.setInitialWidth(34);
    deleteInstrumentConfirmationDialog.setType(Dialog.Type.PLAIN);
    deleteInstrumentConfirmationDialog.setOptions(Dialog.Option.YES_NO_OPTION);
    return deleteInstrumentConfirmationDialog;
  }
}