package org.obiba.onyx.jade.engine;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.ITransitionSource;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class JadeModule implements Module, ApplicationContextAware {

  private static final Logger log = LoggerFactory.getLogger(JadeModule.class);

  private ApplicationContext applicationContext;

  public String getName() {
    return "jade";
  }

  public void initialize() {
    log.info("initialize");
  }

  public void shutdown() {
    log.info("shutdown");
  }

  public IStageExecution createStageExecution(Interview interview, Stage stage, IStageExecution... dependsOn) {
    StageExecutionContext exec = new StageExecutionContext(interview, stage);
    
    AbstractStageState ready = (AbstractStageState) applicationContext.getBean("jadeReadyState");
    AbstractStageState inProgress = (AbstractStageState) applicationContext.getBean("jadeInProgressState");
    AbstractStageState skipped = (AbstractStageState) applicationContext.getBean("jadeSkippedState");
    AbstractStageState completed = (AbstractStageState) applicationContext.getBean("jadeCompletedState");
    exec.addEdge(ready, TransitionEvent.START, inProgress);
    exec.addEdge(ready, TransitionEvent.SKIP, skipped);
    exec.addEdge(inProgress, TransitionEvent.CANCEL, ready);
    exec.addEdge(inProgress, TransitionEvent.COMPLETE, completed);
    exec.addEdge(skipped, TransitionEvent.CANCEL, ready);
    exec.addEdge(completed, TransitionEvent.CANCEL, ready);
    
    if (dependsOn != null && dependsOn.length>0) {
      AbstractStageState waiting = (AbstractStageState) applicationContext.getBean("jadeWaitingState");
      exec.addEdge(waiting, TransitionEvent.VALID, ready);
      exec.addEdge(waiting, TransitionEvent.INVALID, waiting);
      exec.addEdge(waiting, TransitionEvent.SKIP, skipped);
      exec.addEdge(ready, TransitionEvent.INVALID, waiting);
      exec.addEdge(skipped, TransitionEvent.INVALID, waiting);
      exec.addEdge(completed, TransitionEvent.INVALID, waiting);
      exec.setInitialState(waiting);
      
      for (IStageExecution dependsOnExec : dependsOn) {
        if (dependsOnExec instanceof ITransitionSource) {
          ((ITransitionSource)dependsOnExec).addTransitionListener(exec);
          waiting.addDependsOnStageExecution(dependsOnExec);
          ready.addDependsOnStageExecution(dependsOnExec);
          skipped.addDependsOnStageExecution(dependsOnExec);
          completed.addDependsOnStageExecution(dependsOnExec);
        }
      }
    }
    else {
      exec.setInitialState(ready);
    }

    return exec;
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}
