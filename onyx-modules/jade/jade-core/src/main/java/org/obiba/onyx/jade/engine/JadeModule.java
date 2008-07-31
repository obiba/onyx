package org.obiba.onyx.jade.engine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.engine.state.JadeInProgressState;
import org.obiba.onyx.jade.engine.state.JadeReadyState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JadeModule implements Module {

  private static final Logger log = LoggerFactory.getLogger(JadeModule.class);

  private EntityQueryService queryService;

  private Map<Serializable, Map<Serializable, StageExecutionContext>> interviewStageContexts = new HashMap<Serializable, Map<Serializable, StageExecutionContext>>();

  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }

  public String getName() {
    return "jade";
  }

  private InstrumentType getInstrumentType(Stage stage) {
    InstrumentType template = new InstrumentType(stage.getName(), null);
    return queryService.matchOne(template);
  }

  public void initialize() {
    log.info("initialize");
  }

  public void shutdown() {
    log.info("shutdown");
  }

  public IStageExecution getStageExecution(Interview interview, Stage stage) {
    Map<Serializable, StageExecutionContext> contexts = interviewStageContexts.get(interview.getId());
    if(contexts == null) {
      contexts = new HashMap<Serializable, StageExecutionContext>();
      interviewStageContexts.put(interview.getId(), contexts);
    }
    StageExecutionContext exec = contexts.get(stage.getId());

    if(exec == null) {
      // create
      exec = new StageExecutionContext(interview, stage);

      AbstractStageState ready = new JadeReadyState();
      AbstractStageState inProgress = new JadeInProgressState(getInstrumentType(stage));
      exec.addEdge(ready, TransitionEvent.START, inProgress);
      exec.addEdge(inProgress, TransitionEvent.CANCEL, ready);
      exec.setInitialState(ready);

      contexts.put(stage.getId(), exec);
    }

    return exec;
  }

}
