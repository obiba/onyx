package org.obiba.onyx.webapp.stage.panel;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.wicket.util.DateUtils;

public class StageStartEndTimePanel extends Panel {

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  private static final long serialVersionUID = 1L;

  public StageStartEndTimePanel(String id, Stage stage) {
    super(id);

    Action template = new Action();
    template.setInterview(activeInterviewService.getInterview());
    template.setStage(stage);

    List<Action> actionList = queryService.match(template);

    if(activeInterviewService.getStageExecution(stage).isCompleted()) {
      add(new Label("endTime", DateUtils.getShortDateTimeModel(new PropertyModel(actionList.get(actionList.size() - 1), "dateTime"))));
      add(new Label("separator", "-")); 
      add(new Label("startTime", DateUtils.getShortDateTimeModel(new PropertyModel(actionList.get(0), "dateTime"))));      
    } else {
      add(new Label("endTime", ""));
      add(new Label("separator", ""));      
      add(new Label("startTime", ""));      
    }

  }

}
