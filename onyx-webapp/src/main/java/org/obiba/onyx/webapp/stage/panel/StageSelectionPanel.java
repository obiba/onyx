/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.stage.panel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.obiba.onyx.webapp.action.panel.ActionsPanel;
import org.obiba.onyx.webapp.stage.page.StagePage;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StageSelectionPanel extends Panel {

  private static final long serialVersionUID = 6282742572162384139L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(StageSelectionPanel.class);

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ModuleRegistry moduleRegistry;

  private ActionWindow modal;

  private OnyxEntityList<Stage> list;

  private FeedbackPanel feedbackPanel;

  private InteractiveStage interactiveStage = null;

  @SuppressWarnings("serial")
  public StageSelectionPanel(String id, FeedbackPanel feedbackPanel) {
    super(id);
    setOutputMarkupId(true);

    this.feedbackPanel = feedbackPanel;

    interactiveStage = checkStageStatus();

    add(modal = new ActionWindow("modal") {

      @Override
      public void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action) {
        IStageExecution exec = activeInterviewService.getStageExecution(stage);
        if(!exec.isInteractive()) {
          interactiveStage = checkStageStatus();
          target.addComponent(StageSelectionPanel.this.feedbackPanel);
          target.addComponent(list);
          StageSelectionPanel.this.onActionPerformed(target, stage, action);
        } else {
          setResponsePage(new StagePage(new StageModel(moduleRegistry, stage)));
        }
      }

    });

    add(list = new OnyxEntityList<Stage>("list", new StageProvider(), new StageListColumnProvider(), new StringResourceModel("StageList", StageSelectionPanel.this, null)));
  }

  private InteractiveStage checkStageStatus() {
    for(Stage stage : moduleRegistry.listStages()) {
      IStageExecution exec = activeInterviewService.getStageExecution(stage);
      if(exec.isInteractive()) {
        log.warn("Wrong status for " + stage.getName());
        feedbackPanel.warn(getString("WrongStatusForStage", new Model(new ValueMap("name=" + stage.getDescription()))));
        return (new InteractiveStage(stage.getName(), activeInterviewService.getStatusAction().getUser()));
      }
    }
    return null;
  }

  abstract public void onViewComments(AjaxRequestTarget target);

  abstract public void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action);

  private class StageProvider extends SortableDataProvider {

    private static final long serialVersionUID = 6022606267778864539L;

    public StageProvider() {
    }

    public Iterator iterator(int first, int count) {
      List<Stage> stages = moduleRegistry.listStages();
      return stages.iterator();
    }

    public IModel model(Object object) {
      return new StageModel(moduleRegistry, (Stage) object);
    }

    public int size() {
      return moduleRegistry.listStages().size();
    }

  }

  private class StageListColumnProvider implements IColumnProvider, Serializable {

    private static final long serialVersionUID = -9121583835345457007L;

    private List<IColumn> columns = new ArrayList<IColumn>();

    private List<IColumn> additional = new ArrayList<IColumn>();

    @SuppressWarnings("serial")
    public StageListColumnProvider() {
      columns.add(new PropertyColumn(new Model("#"), "displayOrder"));

      columns.add(new AbstractColumn(new StringResourceModel("Name", StageSelectionPanel.this, null)) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {

          cellItem.add(new Label(componentId, new MessageSourceResolvableStringModel(new PropertyModel(rowModel, "description"))));
        }

      });

      columns.add(new AbstractColumn(new StringResourceModel("Status", StageSelectionPanel.this, null)) {

        public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
          cellItem.add(new Label(componentId, new Model() {
            public Object getObject() {
              Stage stage = (Stage) rowModel.getObject();
              IStageExecution exec = activeInterviewService.getStageExecution(stage);
              return exec.getMessage();
            }
          }));
        }

      });

      columns.add(new AbstractColumn(new StringResourceModel("StartEndTime", StageSelectionPanel.this, null)) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          Stage stage = (Stage) rowModel.getObject();
          cellItem.add(new StageStartEndTimePanel(componentId, stage));
        }

      });

      if(activeInterviewService.getInterview().getStatus().equals(InterviewStatus.IN_PROGRESS)) {
        columns.add(new AbstractColumn(new Model("Actions")) {

          public void populateItem(Item cellItem, String componentId, IModel rowModel) {
            Stage stage = (Stage) rowModel.getObject();
            IStageExecution exec = activeInterviewService.getStageExecution(stage);

            if(interactiveStage != null && !(interactiveStage.isValidForAction(stage))) {
              cellItem.add(new EmptyPanel(componentId));
            } else {
              cellItem.add(new ActionsPanel(componentId, rowModel, exec, modal));
            }
          }

        });
      }

      columns.add(new AbstractColumn(new StringResourceModel("Comments", StageSelectionPanel.this, null)) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          List<Action> interviewComments = activeInterviewService.getInterviewComments();
          Stage stage = (Stage) rowModel.getObject();

          boolean foundActionComments = false;
          for(Action action : interviewComments) {
            if(action.getStage() != null && action.getStage().equals(stage.getName())) {
              foundActionComments = true;
              break;
            }
          }

          // Show link only if there are existing comments for the selected Stage
          if(foundActionComments) {
            cellItem.add(new ViewCommentsActionPanel(componentId) {

              private static final long serialVersionUID = 1L;

              @Override
              public void onViewComments(AjaxRequestTarget target) {
                StageSelectionPanel.this.onViewComments(target);
              }

            });

          } else {
            cellItem.add(new Label(componentId, ""));
          }
        }

      });

    }

    public List<IColumn> getAdditionalColumns() {
      return additional;
    }

    public List<String> getColumnHeaderNames() {
      return null;
    }

    public List<IColumn> getDefaultColumns() {
      return columns;
    }

    public List<IColumn> getRequiredColumns() {
      return columns;
    }

  }

  private class InteractiveStage implements Serializable {

    private static final long serialVersionUID = -442910624514791105L;

    private String stageName;

    private User stageUser;

    public InteractiveStage(String stageName, User stageUser) {
      this.stageName = stageName;
      this.stageUser = stageUser;
    }

    public boolean isValidForAction(Stage currentStage) {
      if(!stageName.equals(currentStage.getName())) return false;
      if(!stageUser.getLogin().equals(OnyxAuthenticatedSession.get().getUser().getLogin())) return false;

      return true;
    }
  }
}
