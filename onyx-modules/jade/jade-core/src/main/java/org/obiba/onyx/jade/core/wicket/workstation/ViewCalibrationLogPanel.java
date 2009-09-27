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

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.DialogBuilder;
import org.obiba.onyx.wicket.reusable.Dialog.Option;

public class ViewCalibrationLogPanel extends Panel {
  private static final long serialVersionUID = 1L;

  @SpringBean
  private ExperimentalConditionService experimentalConditionService;

  public ViewCalibrationLogPanel(String id, IModel<Instrument> model) {
    super(id, model);

    ResourceModel logTitleModel = new ResourceModel("CalibrationHistory");
    final Dialog logDialog = DialogBuilder.buildDialog("calibrationLogDialog", logTitleModel, getExperimentalConditionHistoryPanel()).setOptions(Option.CLOSE_OPTION).getDialog();
    logDialog.setInitialHeight(203);
    logDialog.setInitialWidth(1000);
    add(logDialog);

    AjaxLink<Instrument> viewCalibrationLink = new AjaxLink<Instrument>("viewCalibrationLog", model) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        System.out.println("click");
        logDialog.show(target);

      }
    };
    ContextImage commentIcon = new ContextImage("viewCalibrationLogImage", new Model<String>("icons/loupe_button.png"));
    viewCalibrationLink.add(commentIcon);
    add(viewCalibrationLink);
  }

  @Override
  public boolean isVisible() {
    Instrument instrument = (Instrument) getDefaultModelObject();
    if(experimentalConditionService.instrumentCalibrationExists(instrument.getType())) {
      InstrumentCalibration calibrate = experimentalConditionService.getInstrumentCalibrationByType(instrument.getType());
      ExperimentalCondition template = new ExperimentalCondition();
      template.setName(calibrate.getName());
      List<ExperimentalCondition> calibrations = experimentalConditionService.getExperimentalConditions(template, null, new SortingClause("time", false));
      if(calibrations.size() > 0) {
        return true;
      }
    }
    return false;
  }

  private ExperimentalConditionHistoryPanel getExperimentalConditionHistoryPanel() {
    Instrument instrument = (Instrument) getDefaultModelObject();
    if(experimentalConditionService.instrumentCalibrationExists(instrument.getType())) {
      InstrumentCalibration calibrate = experimentalConditionService.getInstrumentCalibrationByType(instrument.getType());
      ExperimentalCondition template = new ExperimentalCondition();
      template.setName(calibrate.getName());
      SpringStringResourceModel instrumentTitleModel = new SpringStringResourceModel(calibrate.getName());
      return new ExperimentalConditionHistoryPanel("content", template, new Model<String>(instrumentTitleModel.getString()), 5);
    }
    ExperimentalCondition template = new ExperimentalCondition();
    return new ExperimentalConditionHistoryPanel("content", template, new Model<String>("titleModel"), 5);
  }

}
