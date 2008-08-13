package org.obiba.onyx.jade.core.wicket.panel;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.wicket.wizard.InstrumentWizardForm;
import org.obiba.onyx.wicket.IEngineComponentAware;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

public class JadePanel extends Panel implements IEngineComponentAware {

  private static final long serialVersionUID = -6692482689347742363L;

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  private ActionWindow actionWindow;

  private FeedbackPanel feedbackPanel;

  private JadeModel model;

  @SuppressWarnings("serial")
  public JadePanel(String id, Stage stage) {
    super(id);
    InstrumentType type = getInstrumentType(stage);
    // get a fresh stage from hibernate
    model = new JadeModel(new DetachableEntityModel(queryService, queryService.get(Stage.class, stage.getId())), new DetachableEntityModel(queryService, type));

    add(new Label("description", type.getDescription()));

    add(new InstrumentLauncherPanel("launcher", model.getIntrumentTypeModel()));

    add(new WizardPanel("content", model.getIntrumentTypeModel()) {

      @Override
      public WizardForm createForm(String componentId) {
        return new InstrumentWizardForm(componentId, getModel(), JadePanel.this) {

          @Override
          public void onCancel(AjaxRequestTarget target) {
            IStageExecution exec = activeInterviewService.getStageExecution(model.getStage());
            ActionDefinition actionDef = exec.getActionDefinition(ActionType.STOP);
            if(actionDef != null) {
              actionWindow.show(target, model.getStageModel(), actionDef);
            }
          }

          @Override
          public void onFinish(AjaxRequestTarget target, Form form) {
            IStageExecution exec = activeInterviewService.getStageExecution(model.getStage());
            ActionDefinition actionDef = exec.getSystemActionDefinition(ActionType.COMPLETE);
            if(actionDef != null) {
              actionWindow.show(target, model.getStageModel(), actionDef);
            }
          }

        };
      }

    });
  }

  private InstrumentType getInstrumentType(Stage stage) {
    InstrumentType template = new InstrumentType(stage.getName(), null);
    return queryService.matchOne(template);
  }

  public void setActionWindwon(ActionWindow window) {
    this.actionWindow = window;
  }

  public void setFeedbackPanel(FeedbackPanel feedbackPanel) {
    this.feedbackPanel = feedbackPanel;
  }

  public FeedbackPanel getFeedbackPanel() {
    return feedbackPanel;
  }

  @SuppressWarnings("serial")
  private class JadeModel implements Serializable {
    private IModel intrumentTypeModel;

    private IModel stageModel;

    public JadeModel(IModel stageModel, IModel instrumentTypeModel) {
      this.intrumentTypeModel = instrumentTypeModel;
      this.stageModel = stageModel;
    }

    public InstrumentType getIntrumentType() {
      return (InstrumentType) intrumentTypeModel.getObject();
    }

    public Stage getStage() {
      return (Stage) stageModel.getObject();
    }

    public IModel getIntrumentTypeModel() {
      return intrumentTypeModel;
    }

    public IModel getStageModel() {
      return stageModel;
    }

  }

}
