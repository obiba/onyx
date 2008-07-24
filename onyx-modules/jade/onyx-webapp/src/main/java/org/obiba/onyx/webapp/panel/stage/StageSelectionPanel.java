package org.obiba.onyx.webapp.panel.stage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.util.StringUtil;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageExecution;
import org.obiba.onyx.engine.StageExecutionStatus;
import org.obiba.onyx.webapp.page.stage.StagePage;
import org.obiba.onyx.webapp.panel.OnyxEntityList;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.obiba.wicket.markup.html.table.SortableDataProviderEntityServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StageSelectionPanel extends Panel {

  private static final long serialVersionUID = 6282742572162384139L;

  private static final Logger log = LoggerFactory.getLogger(StageSelectionPanel.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ModuleRegistry moduleRegistry;
  
  private FeedbackPanel feedbackPanel;

  public StageSelectionPanel(String id, FeedbackPanel feedbackPanel) {
    super(id);
    setOutputMarkupId(true);
    
    this.feedbackPanel = feedbackPanel;
    
    add(new OnyxEntityList<Stage>("list", new StageProvider(), new StageListColumnProvider(), new StringResourceModel("StageList", StageSelectionPanel.this, null)));
  }

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
      columns.add(new PropertyColumn(new StringResourceModel("Name", StageSelectionPanel.this, null), "name", "name"));
      columns.add(new PropertyColumn(new StringResourceModel("Description", StageSelectionPanel.this, null), "description", "description"));
      columns.add(new AbstractColumn(new StringResourceModel("DependsOn", StageSelectionPanel.this, null)) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          Stage stage = (Stage)rowModel.getObject();
          String dependsOn = "";
          for (Stage dep : stage.getDependsOnStages()) {
            if (dependsOn.length()>0)
              dependsOn += ", ";
            dependsOn += dep.getName();
          }
          cellItem.add(new Label(componentId, dependsOn));
        }
        
      });
      //columns.add(new PropertyColumn(new Model("Module"), "module", "module"));
      columns.add(new AbstractColumn(new Model("")) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          cellItem.add(new StageStarter(componentId, new DetachableEntityModel(queryService, rowModel.getObject())));
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

  private class StageStarter extends Fragment {

    private static final long serialVersionUID = -4496489530512108516L;

    @SuppressWarnings("serial")
    public StageStarter(String id, final DetachableEntityModel model) {
      super(id, "startFragment", StageSelectionPanel.this, model);

      Form form = new Form("form", model);
      add(form);
      
      

      Button button = new Button("start") {

        @Override
        public void onSubmit() {
          log.info("Start " + model.getObject());
          setResponsePage(new StagePage(model));
        }
      };
      form.add(button);
      
      AjaxButton cancelButton = new AjaxButton("cancel") {

        @Override
        protected void onSubmit(AjaxRequestTarget target, Form form) {
          moduleRegistry.getModule(((Stage) model.getObject()).getModule()).getCurrentStageExecution().stop();
          target.addComponent(StageSelectionPanel.this);
          target.addComponent(feedbackPanel);
        }
        
      };
      cancelButton.setVisible(false);      
      form.add(cancelButton);
      
      // stage execution context
      Stage stage = (Stage) model.getObject();
      Module module = moduleRegistry.getModule(stage.getModule());
      StageExecution exec = module.getCurrentStageExecution();
      if(!exec.getStatus().equals(StageExecutionStatus.READY)) {
        if(exec.getStage().getName().equals(stage.getName())) {
          button.add(new AttributeModifier("value", new Model("Resume")));
          cancelButton.setVisible(true);
        }
        else {
          button.setEnabled(false);
        }
      }
      
      // stage dependencies
      for (Stage dep : stage.getDependsOnStages()) {
        if (!module.isCompleted(dep)) {
          button.setEnabled(false);
          break;
        }
      }
      
    }
  }
}
