package org.obiba.onyx.webapp.stage.panel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.webapp.action.panel.ActionsPanel;
import org.obiba.onyx.webapp.panel.OnyxEntityList;
import org.obiba.onyx.webapp.stage.page.StagePage;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.obiba.wicket.markup.html.table.SortableDataProviderEntityServiceImpl;

public abstract class StageSelectionPanel extends Panel {

  private static final long serialVersionUID = 6282742572162384139L;

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  private ActionWindow modal;

  private OnyxEntityList<Stage> list;

  @SuppressWarnings("serial")
  public StageSelectionPanel(String id, final FeedbackPanel feedbackPanel) {
    super(id);
    setOutputMarkupId(true);

    add(modal = new ActionWindow("modal") {

      @Override
      public void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action) {
        IStageExecution exec = activeInterviewService.getStageExecution(stage);
        if(!exec.isInteractive()) {
          target.addComponent(feedbackPanel);
          target.addComponent(list);
          StageSelectionPanel.this.onActionPerformed(target, stage, action);
        } else {
          setResponsePage(new StagePage(new DetachableEntityModel(queryService, stage)));
        }
      }

    });

    add(list = new OnyxEntityList<Stage>("list", new StageProvider(), new StageListColumnProvider(), new StringResourceModel("StageList", StageSelectionPanel.this, null)));
  }

  abstract public void onViewComments(AjaxRequestTarget target);

  abstract public void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action);

  private class StageProvider extends SortableDataProviderEntityServiceImpl<Stage> {

    private static final long serialVersionUID = 6022606267778864539L;

    public StageProvider() {
      super(queryService, Stage.class);
      setSort(new SortParam("displayOrder", true));
    }

  }

  private class StageListColumnProvider implements IColumnProvider, Serializable {

    private static final long serialVersionUID = -9121583835345457007L;

    private List<IColumn> columns = new ArrayList<IColumn>();

    private List<IColumn> additional = new ArrayList<IColumn>();

    @SuppressWarnings("serial")
    public StageListColumnProvider() {
      columns.add(new PropertyColumn(new Model("#"), "displayOrder", "displayOrder"));
      columns.add(new PropertyColumn(new StringResourceModel("Name", StageSelectionPanel.this, null), "description", "description"));
      additional.add(new AbstractColumn(new StringResourceModel("DependsOn", StageSelectionPanel.this, null)) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          Stage stage = (Stage) rowModel.getObject();
          String dependsOn = "";
          for(Stage dep : stage.getDependsOnStages()) {
            if(dependsOn.length() > 0) dependsOn += ", ";
            dependsOn += dep.getName();
          }
          cellItem.add(new Label(componentId, dependsOn));
        }

      });
      columns.add(new AbstractColumn(new StringResourceModel("Status", StageSelectionPanel.this, null)) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          Stage stage = (Stage) rowModel.getObject();
          IStageExecution exec = activeInterviewService.getStageExecution(stage);

          cellItem.add(new Label(componentId, exec.getMessage()));
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
            cellItem.add(new ActionsPanel(componentId, new DetachableEntityModel(queryService, stage), exec, modal));
          }

        });
      }

      columns.add(new AbstractColumn(new StringResourceModel("Comments", StageSelectionPanel.this, null)) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          List<Action> interviewComments = activeInterviewService.getInterviewComments();
          Stage stage = (Stage) rowModel.getObject();

          boolean foundActionComments = false;
          for(Action action : interviewComments) {
            if(action.getStage() != null && action.getStage().getId() == stage.getId()) {
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

}
