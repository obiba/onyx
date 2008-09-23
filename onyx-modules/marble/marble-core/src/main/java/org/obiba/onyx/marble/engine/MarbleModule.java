package org.obiba.onyx.marble.engine;

import java.util.List;

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

  private List<Stage> stages;

  public IStageExecution createStageExecution(Interview interview, Stage stage, IStageExecution... dependsOn) {
    StageExecutionContext exec = (StageExecutionContext) applicationContext.getBean("stageExecutionContext");
    exec.setStage(stage);
    exec.setInterview(interview);

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
  }

  public void shutdown() {
  }

  public List<Stage> getStages() {
    return stages;
  }

  public void setStages(List<Stage> stages) {
    this.stages = stages;
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}
