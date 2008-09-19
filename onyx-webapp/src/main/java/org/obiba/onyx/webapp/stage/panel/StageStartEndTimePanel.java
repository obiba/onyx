package org.obiba.onyx.webapp.stage.panel;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.wicket.util.DateModelUtils;

public class StageStartEndTimePanel extends Panel {

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean(name="activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  private static final long serialVersionUID = 1L;

  public StageStartEndTimePanel(String id, Stage stage) {
    super(id);

    Action template = new Action();
    template.setInterview(activeInterviewService.getInterview());
    template.setStage(stage);

    List<Action> actionList = queryService.match(template);

    // Find the last action of type EXECUTE for this stage
    // Also save the last action in the list (most recent action)
    Action lastExecuteAction = null;
    Action lastAction = null;
    for(Action action : actionList) {
      if(action.getActionType() == ActionType.EXECUTE) lastExecuteAction = action;
      lastAction = action;
    }

    if(activeInterviewService.getStageExecution(stage).isCompleted()) {
      add(new Label("endTime", DateModelUtils.getShortDateTimeModel(new PropertyModel(lastAction, "dateTime"))));
      add(new Label("separator", "-"));
      add(new Label("startTime", DateModelUtils.getShortDateTimeModel(new PropertyModel(lastExecuteAction, "dateTime"))));
    } else {
      add(new Label("endTime", ""));
      add(new Label("separator", ""));
      add(new Label("startTime", ""));
    }

  }

}
