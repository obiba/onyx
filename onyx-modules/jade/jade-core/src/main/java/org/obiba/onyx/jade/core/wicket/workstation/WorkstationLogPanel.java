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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLog;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.reusable.Dialog.WindowClosedCallback;

public class WorkstationLogPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private ExperimentalConditionService experimentalConditionService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private ExperimentalConditionLog selectedExperimentalConditionLog;

  private ExperimentalConditionHistoryPanel experimentalConditionHistoryPanel;

  private ExperimentalConditionDialogHelperPanel experimentalConditionDialogHelperPanel;

  public WorkstationLogPanel(String id) {
    super(id);

    experimentalConditionDialogHelperPanel = new ExperimentalConditionDialogHelperPanel("experimentalConditionDialogHelperPanel", null, null);
    add(experimentalConditionDialogHelperPanel);

    final List<ExperimentalConditionLog> experimentalConditionLogs = getExperimentalConditionLogs();
    if(experimentalConditionLogs.size() >= 1) selectedExperimentalConditionLog = experimentalConditionLogs.get(0);

    final DropDownChoice<ExperimentalConditionLog> workstationLogChoice = new DropDownChoice<ExperimentalConditionLog>("workstationLogChoice", new PropertyModel<ExperimentalConditionLog>(this, "selectedExperimentalConditionLog"), getExperimentalConditionLogs(), new ChoiceRenderer<ExperimentalConditionLog>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Object getDisplayValue(ExperimentalConditionLog object) {
        String name = object.getName() + "Log";
        return new SpringStringResourceModel(name, name).getString();
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
        experimentalConditionDialogHelperPanel.setExperimentalConditionLog(selectedExperimentalConditionLog, null);

        experimentalConditionDialogHelperPanel.getExperimentalConditionDialog().setWindowClosedCallback(new WindowClosedCallback() {
          private static final long serialVersionUID = 1L;

          public void onClose(AjaxRequestTarget target, Status status) {
            ExperimentalConditionHistoryPanel newExperimentalConditionHistoryPanel = getExperimentalConditionHistoryPanel();
            experimentalConditionHistoryPanel.replaceWith(newExperimentalConditionHistoryPanel);
            experimentalConditionHistoryPanel = newExperimentalConditionHistoryPanel;
            if(experimentalConditionLogs.size() == 0) experimentalConditionHistoryPanel.setVisible(false);
            target.addComponent(experimentalConditionHistoryPanel);
          }

        });
        SpringStringResourceModel experimentalConditionNameResource = new SpringStringResourceModel(selectedExperimentalConditionLog.getName(), selectedExperimentalConditionLog.getName());
        String experimentalConditionName = experimentalConditionNameResource.getString();
        experimentalConditionDialogHelperPanel.getExperimentalConditionDialog().setTitle(new StringResourceModel("ExperimentalConditionDialogTitle", WorkstationLogPanel.this, new Model<ValueMap>(new ValueMap("experimentalConditionName=" + experimentalConditionName))));
        experimentalConditionDialogHelperPanel.getExperimentalConditionDialog().show(target);
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
    if(experimentalConditionLogs.size() > 0) {
      template.setName(selectedExperimentalConditionLog.getName());
      template.setWorkstation(userSessionService.getWorkstation());
    }
    IModel<String> titleModel = new Model<String>();
    if(selectedExperimentalConditionLog != null) {
      titleModel = new SpringStringResourceModel(selectedExperimentalConditionLog.getName(), selectedExperimentalConditionLog.getName());
    }
    return new ExperimentalConditionHistoryPanel("experimentalConditionHistoryPanel", template, titleModel, 5);
  }

  private List<ExperimentalConditionLog> getExperimentalConditionLogs() {
    List<ExperimentalConditionLog> result = new ArrayList<ExperimentalConditionLog>();
    for(ExperimentalConditionLog log : experimentalConditionService.getExperimentalConditionLog()) {
      if(log instanceof InstrumentCalibration) continue;
      result.add(log);
    }
    return result;
  }

  @Override
  public boolean isVisible() {
    return !getExperimentalConditionLogs().isEmpty();
  }

}
