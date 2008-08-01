package org.obiba.onyx.jade.core.wicket.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

public class JadePanel extends Panel {

  private static final long serialVersionUID = -6692482689347742363L;

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  @SuppressWarnings("serial")
  public JadePanel(String id, final Stage stage) {
    super(id);
    InstrumentType type = getInstrumentType(stage);
    setModel(new DetachableEntityModel(queryService, type));

    add(new Label("description", type.getDescription()));

    add(new InstrumentLauncherPanel("launcher", getModel()));

    add(new InstrumentPanel("content", getModel()) {

      @Override
      public void onCancel(AjaxRequestTarget target) {
        IStageExecution exec = activeInterviewService.getStageExecution(stage);
        ActionDefinition actionDef = exec.getActionDefinition(ActionType.STOP);
        if(actionDef != null) {
          // TODO fill action
          Action action = new Action(actionDef);
          activeInterviewService.doAction(stage, action);
        }
      }

      @Override
      public void onFinish(AjaxRequestTarget target, Form form) {
        IStageExecution exec = activeInterviewService.getStageExecution(stage);
        ActionDefinition actionDef = exec.getSystemActionDefinition(ActionType.COMPLETE);
        if(actionDef != null) {
          // TODO fill action
          Action action = new Action(actionDef);
          activeInterviewService.doAction(stage, action);
        }
      }

    });
  }

  private InstrumentType getInstrumentType(Stage stage) {
    InstrumentType template = new InstrumentType(stage.getName(), null);
    return queryService.matchOne(template);
  }

}
