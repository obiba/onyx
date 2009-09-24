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
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.core.domain.condition.ExperimentalCondition;
import org.obiba.onyx.core.domain.condition.ExperimentalConditionLog;
import org.obiba.onyx.core.domain.condition.InstrumentCalibration;
import org.obiba.onyx.core.service.ExperimentalConditionService;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.reusable.Dialog.WindowClosedCallback;

public class WorkstationLogPanel extends ExperimentalConditionDialog {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private ExperimentalConditionService experimentalConditionService;

  private ExperimentalConditionLog selectedExperimentalConditionLog;

  private ExperimentalConditionHistoryPanel experimentalConditionHistoryPanel;

  public WorkstationLogPanel(String id) {
    super(id);

    final List<ExperimentalConditionLog> experimentalConditionLogs = getExperimentalConditionLogs();
    if(experimentalConditionLogs.size() >= 1) selectedExperimentalConditionLog = experimentalConditionLogs.get(0);

    final DropDownChoice workstationLogChoice = new DropDownChoice<ExperimentalConditionLog>("workstationLogChoice", new PropertyModel(this, "selectedExperimentalConditionLog"), getExperimentalConditionLogs(), new ChoiceRenderer<ExperimentalConditionLog>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Object getDisplayValue(ExperimentalConditionLog object) {
        return new StringResourceModel(object.getName(), WorkstationLogPanel.this, null, object.getName()).getString();
      }

      @Override
      public String getIdValue(ExperimentalConditionLog object, int index) {
        return object.getName();
      }

    });

    workstationLogChoice.add(new OnChangeAjaxBehavior() {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        ExperimentalConditionHistoryPanel newExperimentalConditionHistoryPanel = getExperimentalConditionHistoryPanel();
        experimentalConditionHistoryPanel.replaceWith(newExperimentalConditionHistoryPanel);
        experimentalConditionHistoryPanel = newExperimentalConditionHistoryPanel;
        if(experimentalConditionLogs.size() == 0) experimentalConditionHistoryPanel.setVisible(false);
        target.addComponent(experimentalConditionHistoryPanel);
        workstationLogChoice.updateModel();
      }

    });
    add(workstationLogChoice);

    AjaxLink addWorkstationLogButton = new AjaxLink("addWorkstationLogButton") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        setExperimentalConditionLog(selectedExperimentalConditionLog);

        getExperimentalConditionDialog().setWindowClosedCallback(new WindowClosedCallback() {
          private static final long serialVersionUID = 1L;

          public void onClose(AjaxRequestTarget target, Status status) {
            ExperimentalConditionHistoryPanel newExperimentalConditionHistoryPanel = getExperimentalConditionHistoryPanel();
            experimentalConditionHistoryPanel.replaceWith(newExperimentalConditionHistoryPanel);
            experimentalConditionHistoryPanel = newExperimentalConditionHistoryPanel;
            if(experimentalConditionLogs.size() == 0) experimentalConditionHistoryPanel.setVisible(false);
            target.addComponent(experimentalConditionHistoryPanel);
          }

        });
        StringResourceModel experimentalConditionNameResource = new StringResourceModel(selectedExperimentalConditionLog.getName(), WorkstationLogPanel.this, null);
        String experimentalConditionName = experimentalConditionNameResource.getObject();
        getExperimentalConditionDialog().setTitle(new StringResourceModel("ExperimentalConditionDialogTitle", WorkstationLogPanel.this, new Model<ValueMap>(new ValueMap("experimentalConditionName=" + experimentalConditionName))));
        getExperimentalConditionDialog().show(target);
      }

    };
    add(addWorkstationLogButton);
    if(experimentalConditionLogs.size() == 0) addWorkstationLogButton.setVisible(false);

    experimentalConditionHistoryPanel = getExperimentalConditionHistoryPanel();
    add(experimentalConditionHistoryPanel);
    if(experimentalConditionLogs.size() == 0) experimentalConditionHistoryPanel.setVisible(false);

  }

  private ExperimentalConditionHistoryPanel getExperimentalConditionHistoryPanel() {
    List<ExperimentalConditionLog> experimentalConditionLogs = getExperimentalConditionLogs();
    ExperimentalCondition template = new ExperimentalCondition();
    if(experimentalConditionLogs.size() > 0) template.setName(selectedExperimentalConditionLog.getName());
    IModel titleModel = new StringResourceModel(selectedExperimentalConditionLog.getName(), WorkstationLogPanel.this, null, selectedExperimentalConditionLog.getName());
    return new ExperimentalConditionHistoryPanel("experimentalConditionHistoryPanel", template, titleModel, 3);
  }

  private List<ExperimentalConditionLog> getExperimentalConditionLogs() {
    List<ExperimentalConditionLog> result = new ArrayList<ExperimentalConditionLog>();
    for(ExperimentalConditionLog log : experimentalConditionService.getExperimentalConditionLog()) {
      if(log instanceof InstrumentCalibration) continue;
      result.add(log);
    }
    return result;
  }

}
