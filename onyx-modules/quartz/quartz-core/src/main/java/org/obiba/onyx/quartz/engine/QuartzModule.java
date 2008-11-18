/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.engine;

import java.util.List;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class QuartzModule implements Module, ApplicationContextAware {

  private static final Logger log = LoggerFactory.getLogger(QuartzModule.class);

  private ApplicationContext applicationContext;

  private ActiveInterviewService activeInterviewService;

  private List<Stage> stages;

  public String getName() {
    return "quartz";
  }

  public void initialize() {
    log.info("initialize");
  }

  public void shutdown() {
    log.info("shutdown");
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

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public IStageExecution createStageExecution(Interview interview, Stage stage) {
    StageExecutionContext exec = (StageExecutionContext) applicationContext.getBean("stageExecutionContext");
    Boolean condition = stage.getStageDependencyCondition().isDependencySatisfied(activeInterviewService);

    exec.setStage(stage);
    exec.setInterview(interview);

    AbstractStageState ready = (AbstractStageState) applicationContext.getBean("quartzReadyState");
    AbstractStageState waiting = (AbstractStageState) applicationContext.getBean("quartzWaitingState");
    AbstractStageState notApplicable = (AbstractStageState) applicationContext.getBean("quartzNotApplicableState");
    AbstractStageState skipped = (AbstractStageState) applicationContext.getBean("quartzSkippedState");
    AbstractStageState inProgress = (AbstractStageState) applicationContext.getBean("quartzInProgressState");
    AbstractStageState completed = (AbstractStageState) applicationContext.getBean("quartzCompletedState");
    AbstractStageState interrupted = (AbstractStageState) applicationContext.getBean("quartzInterruptedState");

    exec.addEdge(waiting, TransitionEvent.VALID, ready);
    exec.addEdge(waiting, TransitionEvent.NOTAPPLICABLE, notApplicable);

    exec.addEdge(ready, TransitionEvent.INVALID, waiting);
    exec.addEdge(ready, TransitionEvent.NOTAPPLICABLE, notApplicable);
    exec.addEdge(ready, TransitionEvent.SKIP, skipped);
    exec.addEdge(ready, TransitionEvent.START, inProgress);

    exec.addEdge(skipped, TransitionEvent.NOTAPPLICABLE, notApplicable);
    exec.addEdge(skipped, TransitionEvent.CANCEL, ready);

    exec.addEdge(inProgress, TransitionEvent.CANCEL, ready);
    exec.addEdge(inProgress, TransitionEvent.INTERRUPT, interrupted);
    exec.addEdge(inProgress, TransitionEvent.COMPLETE, completed);

    exec.addEdge(completed, TransitionEvent.CANCEL, ready);
    exec.addEdge(completed, TransitionEvent.NOTAPPLICABLE, notApplicable);
    exec.addEdge(completed, TransitionEvent.RESUME, inProgress);

    exec.addEdge(interrupted, TransitionEvent.CANCEL, ready);
    exec.addEdge(interrupted, TransitionEvent.RESUME, inProgress);
    exec.addEdge(interrupted, TransitionEvent.NOTAPPLICABLE, notApplicable);

    exec.addEdge(notApplicable, TransitionEvent.VALID, ready);
    exec.addEdge(notApplicable, TransitionEvent.INVALID, waiting);

    AbstractStageState initialState;
    if(stage.getStageDependencyCondition() == null) {
      initialState = ready;
    } else {
      if(condition == null) {
        initialState = waiting;
      } else if(condition == true) {
        initialState = ready;
      } else {
        initialState = notApplicable;
      }
    }
    exec.setInitialState(initialState);

    return exec;
  }

}
