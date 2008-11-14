/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class RubyModule implements Module, ApplicationContextAware {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(RubyModule.class);

  private static final String WAITING_STATE_BEAN = "rubyWaitingState";

  private static final String READY_STATE_BEAN = "rubyReadyState";

  private static final String CONTRAINDICATED_STATE_BEAN = "rubyContraIndicatedState";

  private static final String IN_PROGRESS_STATE_BEAN = "rubyInProgressState";

  private static final String INTERRUPTED_STATE_BEAN = "rubyInterruptedState";

  private static final String COMPLETED_STATE_BEAN = "rubyCompletedState";

  private static final String NOT_APPLICABLE_STATE_BEAN = "rubyNotApplicableState";

  //
  // Instance Variables
  //

  private ApplicationContext applicationContext;

  private ActiveInterviewService activeInterviewService;

  private List<Stage> stages;

  //
  // Module Methods
  //

  public String getName() {
    return "ruby";
  }

  public List<Stage> getStages() {
    return stages;
  }

  public void setStages(List<Stage> stages) {
    this.stages = stages;
  }

  public void initialize() {
    log.info("initialize");
  }

  public void shutdown() {
    log.info("shutdown");
  }

  public IStageExecution createStageExecution(Interview interview, Stage stage) {
    // Initialize stage execution context.
    StageExecutionContext exec = (StageExecutionContext) applicationContext.getBean("stageExecutionContext");
    exec.setStage(stage);
    exec.setInterview(interview);

    // Initial state transitions.
    Map<String, AbstractStageState> states = new HashMap<String, AbstractStageState>();
    states.put(WAITING_STATE_BEAN, (AbstractStageState) applicationContext.getBean(WAITING_STATE_BEAN));
    states.put(READY_STATE_BEAN, (AbstractStageState) applicationContext.getBean(READY_STATE_BEAN));
    states.put(CONTRAINDICATED_STATE_BEAN, (AbstractStageState) applicationContext.getBean(CONTRAINDICATED_STATE_BEAN));
    states.put(IN_PROGRESS_STATE_BEAN, (AbstractStageState) applicationContext.getBean(IN_PROGRESS_STATE_BEAN));
    states.put(INTERRUPTED_STATE_BEAN, (AbstractStageState) applicationContext.getBean(INTERRUPTED_STATE_BEAN));
    states.put(COMPLETED_STATE_BEAN, (AbstractStageState) applicationContext.getBean(COMPLETED_STATE_BEAN));
    states.put(NOT_APPLICABLE_STATE_BEAN, (AbstractStageState) applicationContext.getBean(NOT_APPLICABLE_STATE_BEAN));

    initTransitionsFromWaiting(exec, states);
    initTransitionsFromReady(exec, states);
    initTransitionsFromContraIndicated(exec, states);
    initTransitionsFromInProgress(exec, states);
    initTransitionsFromInterrupted(exec, states);
    initTransitionsFromCompleted(exec, states);
    initTransitionsFromNotApplicable(exec, states);

    // Set initial state.
    setInitialState(stage, exec, states);

    return exec;
  }

  //
  // ApplicationContextAware Methods
  //

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  //
  // Methods
  //

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  private void initTransitionsFromWaiting(StageExecutionContext exec, Map<String, AbstractStageState> states) {
    AbstractStageState waiting = states.get(WAITING_STATE_BEAN);

    AbstractStageState ready = states.get(READY_STATE_BEAN);
    exec.addEdge(waiting, TransitionEvent.VALID, ready);

    AbstractStageState notApplicable = states.get(NOT_APPLICABLE_STATE_BEAN);
    exec.addEdge(waiting, TransitionEvent.NOTAPPLICABLE, notApplicable);
  }

  private void initTransitionsFromReady(StageExecutionContext exec, Map<String, AbstractStageState> states) {
    AbstractStageState ready = states.get(READY_STATE_BEAN);

    AbstractStageState waiting = states.get(WAITING_STATE_BEAN);
    exec.addEdge(ready, TransitionEvent.INVALID, waiting);

    AbstractStageState inProgress = states.get(IN_PROGRESS_STATE_BEAN);
    exec.addEdge(ready, TransitionEvent.START, inProgress);

    AbstractStageState notApplicable = states.get(NOT_APPLICABLE_STATE_BEAN);
    exec.addEdge(ready, TransitionEvent.NOTAPPLICABLE, notApplicable);
  }

  private void initTransitionsFromContraIndicated(StageExecutionContext exec, Map<String, AbstractStageState> states) {
    AbstractStageState contraIndicated = states.get(CONTRAINDICATED_STATE_BEAN);

    AbstractStageState waiting = states.get(WAITING_STATE_BEAN);
    exec.addEdge(contraIndicated, TransitionEvent.INVALID, waiting);

    AbstractStageState ready = states.get(READY_STATE_BEAN);
    exec.addEdge(contraIndicated, TransitionEvent.CANCEL, ready);

    AbstractStageState notApplicable = states.get(NOT_APPLICABLE_STATE_BEAN);
    exec.addEdge(contraIndicated, TransitionEvent.NOTAPPLICABLE, notApplicable);
  }

  private void initTransitionsFromInProgress(StageExecutionContext exec, Map<String, AbstractStageState> states) {
    AbstractStageState inProgress = states.get(IN_PROGRESS_STATE_BEAN);

    AbstractStageState ready = states.get(READY_STATE_BEAN);
    exec.addEdge(inProgress, TransitionEvent.CANCEL, ready);

    AbstractStageState contraIndicated = states.get(CONTRAINDICATED_STATE_BEAN);
    exec.addEdge(inProgress, TransitionEvent.CONTRAINDICATED, contraIndicated);

    AbstractStageState interrupted = states.get(INTERRUPTED_STATE_BEAN);
    exec.addEdge(inProgress, TransitionEvent.INTERRUPT, interrupted);

    AbstractStageState completed = states.get(COMPLETED_STATE_BEAN);
    exec.addEdge(inProgress, TransitionEvent.COMPLETE, completed);

    AbstractStageState notApplicable = states.get(NOT_APPLICABLE_STATE_BEAN);
    exec.addEdge(inProgress, TransitionEvent.NOTAPPLICABLE, notApplicable);
  }

  private void initTransitionsFromInterrupted(StageExecutionContext exec, Map<String, AbstractStageState> states) {
    AbstractStageState interrupted = states.get(INTERRUPTED_STATE_BEAN);

    AbstractStageState waiting = states.get(WAITING_STATE_BEAN);
    exec.addEdge(interrupted, TransitionEvent.INVALID, waiting);

    AbstractStageState ready = states.get(READY_STATE_BEAN);
    exec.addEdge(interrupted, TransitionEvent.CANCEL, ready); // missing from diagram

    AbstractStageState inProgress = states.get(IN_PROGRESS_STATE_BEAN);
    exec.addEdge(interrupted, TransitionEvent.RESUME, inProgress);

    AbstractStageState notApplicable = states.get(NOT_APPLICABLE_STATE_BEAN);
    exec.addEdge(interrupted, TransitionEvent.NOTAPPLICABLE, notApplicable);
  }

  private void initTransitionsFromCompleted(StageExecutionContext exec, Map<String, AbstractStageState> states) {
    AbstractStageState completed = states.get(COMPLETED_STATE_BEAN);

    AbstractStageState waiting = states.get(WAITING_STATE_BEAN);
    exec.addEdge(completed, TransitionEvent.INVALID, waiting);

    AbstractStageState ready = states.get(READY_STATE_BEAN);
    exec.addEdge(completed, TransitionEvent.CANCEL, ready);

    AbstractStageState notApplicable = states.get(NOT_APPLICABLE_STATE_BEAN);
    exec.addEdge(completed, TransitionEvent.NOTAPPLICABLE, notApplicable);
  }

  private void initTransitionsFromNotApplicable(StageExecutionContext exec, Map<String, AbstractStageState> states) {
    AbstractStageState notApplicable = states.get(NOT_APPLICABLE_STATE_BEAN);

    AbstractStageState waiting = states.get(WAITING_STATE_BEAN);
    exec.addEdge(notApplicable, TransitionEvent.INVALID, waiting);

    AbstractStageState ready = states.get(READY_STATE_BEAN);
    exec.addEdge(notApplicable, TransitionEvent.VALID, ready);
  }

  private void setInitialState(Stage stage, StageExecutionContext exec, Map<String, AbstractStageState> states) {
    AbstractStageState waiting = states.get(WAITING_STATE_BEAN);
    AbstractStageState ready = states.get(READY_STATE_BEAN);
    AbstractStageState notApplicable = states.get(NOT_APPLICABLE_STATE_BEAN);

    if(stage.getStageDependencyCondition() == null) {
      exec.setInitialState(ready);
    } else {
      if(stage.getStageDependencyCondition().isDependencySatisfied(activeInterviewService) == null) {
        exec.setInitialState(waiting);
      } else if(stage.getStageDependencyCondition().isDependencySatisfied(activeInterviewService) == true) {
        exec.setInitialState(ready);
      } else {
        exec.setInitialState(notApplicable);
      }
    }
  }
}