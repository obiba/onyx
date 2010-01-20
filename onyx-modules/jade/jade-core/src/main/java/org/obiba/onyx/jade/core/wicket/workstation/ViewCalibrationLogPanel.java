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
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentMeasurementType;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.DialogBuilder;
import org.obiba.onyx.wicket.reusable.Dialog.Option;

public class ViewCalibrationLogPanel extends Panel {
  private static final long serialVersionUID = 1L;

  @SpringBean
  private ExperimentalConditionService experimentalConditionService;

  public ViewCalibrationLogPanel(String id, IModel<InstrumentMeasurementType> model) {
    super(id, model);

    ResourceModel logTitleModel = new ResourceModel("CalibrationHistory");
    final Dialog logDialog = DialogBuilder.buildDialog("calibrationLogDialog", logTitleModel, getExperimentalConditionHistoryPanel()).setOptions(Option.CLOSE_OPTION).getDialog();
    logDialog.setHeightUnit("em");
    logDialog.setWidthUnit("em");
    logDialog.setInitialHeight(20);
    logDialog.setInitialWidth(50);
    add(logDialog);

    AjaxLink<InstrumentMeasurementType> viewCalibrationLink = new AjaxLink<InstrumentMeasurementType>("viewCalibrationLog", model) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        logDialog.show(target);
      }
    };
    ContextImage commentIcon = new ContextImage("viewCalibrationLogImage", new Model<String>("icons/loupe_button.png"));
    viewCalibrationLink.add(commentIcon);
    add(viewCalibrationLink);
  }

  @Override
  public boolean isVisible() {
    if(experimentalConditionService.instrumentCalibrationExists(getInstrumentMeasurementType().getType())) {

      List<InstrumentCalibration> instrumentCalibrations = experimentalConditionService.getInstrumentCalibrationsByType(getInstrumentMeasurementType().getType());
      List<ExperimentalCondition> calibrations = new ArrayList<ExperimentalCondition>();
      for(InstrumentCalibration instrumentCalibration : instrumentCalibrations) {
        calibrations.addAll(getExperimentalConditionsForInstrumentCalibration(instrumentCalibration));
      }

      if(calibrations.size() > 0) {
        return true;
      }
    }
    return false;
  }

  private List<ExperimentalCondition> getExperimentalConditionsForInstrumentCalibration(InstrumentCalibration instrumentCalibration) {
    Instrument instrument = getInstrumentMeasurementType().getInstrument();
    ExperimentalCondition template = new ExperimentalCondition();
    template.setName(instrumentCalibration.getName());

    ExperimentalConditionValue ecv = new ExperimentalConditionValue();
    ecv.setAttributeType(DataType.TEXT);
    ecv.setAttributeName(ExperimentalConditionService.INSTRUMENT_BARCODE);
    ecv.setData(new Data(DataType.TEXT, instrument.getBarcode()));
    ecv.setExperimentalCondition(template);
    template.addExperimentalConditionValue(ecv);

    return experimentalConditionService.getExperimentalConditions(template);
  }

  private ExperimentalConditionHistoryPanel getExperimentalConditionHistoryPanel() {
    Instrument instrument = getInstrumentMeasurementType().getInstrument();
    if(experimentalConditionService.instrumentCalibrationExists(getInstrumentMeasurementType().getType())) {
      List<InstrumentCalibration> calibrations = experimentalConditionService.getInstrumentCalibrationsByType(getInstrumentMeasurementType().getType());
      return new ExperimentalConditionHistoryPanel("content", calibrations, 5, instrument);
    }
    ExperimentalCondition template = new ExperimentalCondition();
    return new ExperimentalConditionHistoryPanel("content", template, new Model<String>("titleModel"), 5);
  }

  private InstrumentMeasurementType getInstrumentMeasurementType() {
    return (InstrumentMeasurementType) getDefaultModelObject();
  }

}