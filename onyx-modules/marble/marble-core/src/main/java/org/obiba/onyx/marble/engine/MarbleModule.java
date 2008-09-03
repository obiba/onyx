package org.obiba.onyx.marble.engine;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class MarbleModule implements Module, ApplicationContextAware {

  private ApplicationContext applicationContext;

  public IStageExecution createStageExecution(Interview interview, Stage stage, IStageExecution... dependsOn) {
    StageExecutionContext exec = new StageExecutionContext(interview, stage);

    AbstractStageState ready = (AbstractStageState) applicationContext.getBean("marbleReadyState");
    AbstractStageState inProgress = (AbstractStageState) applicationContext.getBean("marbleInProgressState");
    AbstractStageState completed = (AbstractStageState) applicationContext.getBean("marbleCompletedState");

    exec.addEdge(ready, TransitionEvent.START, inProgress);
    exec.addEdge(inProgress, TransitionEvent.CANCEL, ready);
    exec.addEdge(inProgress, TransitionEvent.COMPLETE, completed);
    exec.addEdge(completed, TransitionEvent.CANCEL, ready);

    exec.setInitialState(ready);

    return exec;
  }

  public String getName() {
    return "marble";
  }

  public void initialize() {
    // TODO Auto-generated method stub

  }

  public void shutdown() {
    // TODO Auto-generated method stub

  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}
