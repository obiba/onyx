package org.obiba.onyx.webapp.stage.page;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StagePage extends BasePage {

  private static final Logger log = LoggerFactory.getLogger(StagePage.class);

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  public StagePage(DetachableEntityModel stageModel) {
    super();

    Stage stage = (Stage) stageModel.getObject();
    IStageExecution exec = activeInterviewService.getStageExecution(stage);

    if(!exec.isInteractive()) {
      add(new Label("action", "not applicable..."));
      add(new EmptyPanel("stage-component"));
    } else {
      add(new Label("action", "starting..."));
      add(exec.getWidget("stage-component"));
    }

  }

}
