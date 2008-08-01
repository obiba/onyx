package org.obiba.onyx.jade.engine;

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.jade.engine.state.JadeCompletedState;
import org.obiba.onyx.jade.engine.state.JadeInProgressState;
import org.obiba.onyx.jade.engine.state.JadeReadyState;
import org.obiba.onyx.jade.engine.state.JadeSkippedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JadeModule implements Module {

  private static final Logger log = LoggerFactory.getLogger(JadeModule.class);

  private EntityQueryService queryService;

  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }

  public String getName() {
    return "jade";
  }

  public void initialize() {
    log.info("initialize");
  }

  public void shutdown() {
    log.info("shutdown");
  }

  public IStageExecution createStageExecution(Interview interview, Stage stage) {
    StageExecutionContext exec = new StageExecutionContext(interview, stage);

    AbstractStageState ready = new JadeReadyState();
    AbstractStageState inProgress = new JadeInProgressState(stage);
    AbstractStageState skipped = new JadeSkippedState();
    AbstractStageState completed = new JadeCompletedState();
    exec.addEdge(ready, TransitionEvent.START, inProgress);
    exec.addEdge(ready, TransitionEvent.SKIP, skipped);
    exec.addEdge(inProgress, TransitionEvent.CANCEL, ready);
    exec.addEdge(inProgress, TransitionEvent.COMPLETE, completed);
    exec.addEdge(skipped, TransitionEvent.CANCEL, ready);
    exec.addEdge(completed, TransitionEvent.CANCEL, ready);
    exec.setInitialState(ready);

    return exec;
  }

}
