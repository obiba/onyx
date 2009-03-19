/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.engine.state;

import java.util.Arrays;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.stage.StageExecutionMemento;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.impl.DefaultActiveInterviewServiceImpl;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.InverseStageDependencyCondition;
import org.obiba.onyx.engine.MultipleStageDependencyCondition;
import org.obiba.onyx.engine.PreviousStageDependencyCondition;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageDependencyCondition;
import org.obiba.onyx.engine.MultipleStageDependencyCondition.Operator;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class StageExecutionTest extends BaseDefaultSpringContextTestCase {

  private static final Logger log = LoggerFactory.getLogger(StageExecutionTest.class);

  @Autowired(required = true)
  PersistenceManager persistenceManager;

  @Autowired(required = true)
  StageExecutionContext context1;

  @Autowired(required = true)
  StageExecutionContext context2;

  @Autowired(required = true)
  StageExecutionContext context3;

  ActiveInterviewService activeInterviewService;

  @Before
  public void setUp() {

    activeInterviewService = new DefaultActiveInterviewServiceImpl() {
      @Override
      public PersistenceManager getPersistenceManager() {
        return persistenceManager;
      }

      @Override
      public IStageExecution getStageExecution(String stageName) {
        if(stageName.equals("dummy1")) return context1;
        if(stageName.equals("dummy2")) return context2;
        if(stageName.equals("dummy3")) return context3;
        throw new IllegalArgumentException("Unexpected stage name " + stageName);
      }

      @Override
      public IStageExecution getStageExecution(Stage stage) {
        return getStageExecution(stage.getName());
      }
    };

    Stage stage1 = new Stage();
    stage1.setName("dummy1");

    Interview interview = persistenceManager.save(new Interview());
    context1.setStage(stage1);
    context1.setInterview(interview);
    ReadyState ready = new ReadyState();
    InProgressState progress = new InProgressState();
    CompletedState completed = new CompletedState();

    context1.addEdge(ready, TransitionEvent.START, progress);
    context1.addEdge(progress, TransitionEvent.CANCEL, ready);
    context1.addEdge(progress, TransitionEvent.COMPLETE, completed);
    context1.addEdge(completed, TransitionEvent.CANCEL, ready);
    context1.addEdge(completed, TransitionEvent.RESUME, progress);
    context1.setInitialState(ready);

    Stage stage2 = new Stage();
    stage2.setName("dummy2");
    stage2.setStageDependencyCondition(new PreviousStageDependencyCondition("dummy1"));

    context2.setStage(stage2);
    context2.setInterview(interview);
    WaitingState waiting = new WaitingState();
    ready = new ReadyState();
    progress = new InProgressState();
    completed = new CompletedState();

    context2.addEdge(waiting, TransitionEvent.VALID, ready);
    context2.addEdge(waiting, TransitionEvent.INVALID, waiting);
    context2.addEdge(ready, TransitionEvent.INVALID, waiting);
    context2.addEdge(ready, TransitionEvent.START, progress);
    context2.addEdge(progress, TransitionEvent.CANCEL, ready);
    context2.addEdge(progress, TransitionEvent.COMPLETE, completed);
    context2.addEdge(completed, TransitionEvent.CANCEL, ready);
    context2.addEdge(completed, TransitionEvent.INVALID, waiting);
    context1.addTransitionListener(context2);
    context2.setInitialState(waiting);

    Stage stage3 = new Stage();
    stage3.setName("dummy3");
    InverseStageDependencyCondition inverseCondition = new InverseStageDependencyCondition();
    inverseCondition.setStageDependencyCondition(new SpecificStageDependencyCondition("dummy2"));

    MultipleStageDependencyCondition multipleCondition = new MultipleStageDependencyCondition();
    multipleCondition.setOperator(Operator.AND);
    multipleCondition.setConditions(Arrays.asList(inverseCondition, new PreviousStageDependencyCondition("dummy1")));

    stage3.setStageDependencyCondition(multipleCondition);

    context3.setStage(stage3);
    context3.setInterview(interview);
    NotApplicableState notApplicable = new NotApplicableState();
    waiting = new WaitingState();
    ready = new ReadyState();
    progress = new InProgressState();
    completed = new CompletedState();

    context3.addEdge(waiting, TransitionEvent.VALID, ready);
    context3.addEdge(waiting, TransitionEvent.INVALID, waiting);
    context3.addEdge(ready, TransitionEvent.INVALID, waiting);
    context3.addEdge(ready, TransitionEvent.START, progress);
    context3.addEdge(progress, TransitionEvent.CANCEL, ready);
    context3.addEdge(progress, TransitionEvent.COMPLETE, completed);
    context3.addEdge(completed, TransitionEvent.CANCEL, ready);
    context3.addEdge(completed, TransitionEvent.INVALID, waiting);
    context3.addEdge(ready, TransitionEvent.NOTAPPLICABLE, notApplicable);
    context3.addEdge(notApplicable, TransitionEvent.INVALID, waiting);
    context3.addEdge(completed, TransitionEvent.NOTAPPLICABLE, notApplicable);
    context1.addTransitionListener(context3);
    context2.addTransitionListener(context3);
    context3.setInitialState(waiting);
  }

  private StageExecutionMemento getMemento(StageExecutionContext context) {
    StageExecutionMemento memento = new StageExecutionMemento();
    memento.setStage(context.getStage().getName());
    memento.setInterview(context.getInterview());
    return persistenceManager.matchOne(memento);
  }

  private void assertInitialState() {
    assertStateName(context1, ReadyState.NAME);

    StageExecutionMemento memento = getMemento(context1);
    Assert.assertNull("Memento is not null", memento);
    memento = getMemento(context2);
    Assert.assertNull("Memento is not null", memento);
    memento = getMemento(context3);
    Assert.assertNull("Memento is not null", memento);
  }

  @Test
  public void testSimpleTransition() {
    assertInitialState();

    // Send the START event in the first state machine
    doAction(ActionType.EXECUTE);
    assertStateName(context1, InProgressState.NAME);
    Assert.assertEquals(false, context1.isCompleted());
    assertStateClass(context1, InProgressState.class);

    assertStateName(context2, WaitingState.NAME);
    assertStateClass(context2, WaitingState.class);

    assertStateName(context3, WaitingState.NAME);
    assertStateClass(context3, WaitingState.class);
  }

  @Test
  public void testCancelInProgress() {
    doAction(ActionType.EXECUTE);
    assertStateName(context1, InProgressState.NAME);
    assertStateName(context2, WaitingState.NAME);
    assertStateName(context3, WaitingState.NAME);

    doAction(ActionType.STOP);
    assertStateName(context1, ReadyState.NAME);
    Assert.assertEquals(false, context1.isCompleted());
    assertStateName(context2, WaitingState.NAME);
    assertStateName(context3, WaitingState.NAME);
  }

  @Test
  public void testCommentDuringInProgress() {
    doAction(ActionType.EXECUTE);
    assertStateName(context1, InProgressState.NAME);
    assertStateName(context2, WaitingState.NAME);
    assertStateName(context3, WaitingState.NAME);

    doAction(ActionType.COMMENT);
    assertStateName(context1, InProgressState.NAME);
    assertStateName(context2, WaitingState.NAME);
    assertStateName(context3, WaitingState.NAME);
  }

  @Test
  public void testComplete() {
    doAction(ActionType.EXECUTE);
    assertStateName(context1, InProgressState.NAME);
    assertStateName(context2, WaitingState.NAME);
    assertStateName(context3, WaitingState.NAME);

    // complete the first stage and make assert that the second becomes ready
    doAction(ActionType.COMPLETE);
    assertStateName(context1, CompletedState.NAME);
    assertStateClass(context1, CompletedState.class);
    Assert.assertEquals(true, context1.isCompleted());
    Assert.assertEquals(false, context1.isInteractive());

    assertStateName(context2, ReadyState.NAME);
    assertStateClass(context2, ReadyState.class);

    assertStateName(context3, ReadyState.NAME);
    assertStateClass(context3, ReadyState.class);

  }

  /**
   * Test of transitional state: ONYX-383.
   */
  @Test
  public void testInProgressCompleteInProgressComplete() {
    doAction(ActionType.EXECUTE);
    assertStateName(context1, InProgressState.NAME);
    assertStateName(context2, WaitingState.NAME);
    assertStateName(context3, WaitingState.NAME);

    // complete the first stage and make assert that the second becomes ready
    doAction(ActionType.COMPLETE);
    assertStateName(context1, CompletedState.NAME);
    assertStateClass(context1, CompletedState.class);
    Assert.assertEquals(true, context1.isCompleted());
    Assert.assertEquals(false, context1.isInteractive());

    assertStateName(context2, ReadyState.NAME);
    assertStateClass(context2, ReadyState.class);

    assertStateName(context3, ReadyState.NAME);
    assertStateClass(context3, ReadyState.class);

    doAction(context2, ActionType.EXECUTE);
    assertStateName(context2, InProgressState.NAME);

    // complete the second stage
    doAction(context2, ActionType.COMPLETE);
    assertStateName(context2, CompletedState.NAME);
    assertStateClass(context2, CompletedState.class);

    // 1 goes in progress, 2 does not change
    doAction(ActionType.EXECUTE);
    assertStateName(context1, InProgressState.NAME);
    assertStateName(context2, CompletedState.NAME);

    // 1 goes completed, 2 is still completed
    doAction(ActionType.COMPLETE);
    assertStateName(context1, CompletedState.NAME);
    assertStateName(context2, CompletedState.NAME);

    // 1 goes in progress, 2 does not change
    doAction(ActionType.EXECUTE);
    assertStateName(context1, InProgressState.NAME);
    assertStateName(context2, CompletedState.NAME);

    // 1 goes in ready, 2 goes waiting
    doAction(ActionType.STOP);
    assertStateName(context1, ReadyState.NAME);
    assertStateName(context2, WaitingState.NAME);

  }

  @Test
  public void testCancelCompleted() {
    doAction(ActionType.EXECUTE);
    doAction(ActionType.COMPLETE);
    doAction(ActionType.STOP);

    assertStateName(context1, ReadyState.NAME);
    Assert.assertEquals(false, context1.isInteractive());
    assertStateClass(context1, ReadyState.class);

    assertStateName(context2, WaitingState.NAME);
    assertStateClass(context2, WaitingState.class);

    assertStateName(context3, WaitingState.NAME);
    assertStateClass(context3, WaitingState.class);
  }

  @Test
  public void testCancelFirstWhenBothAreCompleted() {
    doAction(ActionType.EXECUTE);
    doAction(ActionType.COMPLETE);
    doAction(context2, ActionType.EXECUTE);
    doAction(context2, ActionType.COMPLETE);

    assertStateName(context1, CompletedState.NAME);
    assertStateName(context2, CompletedState.NAME);
    assertStateName(context3, NotApplicableState.NAME);

    doAction(ActionType.STOP);
    assertStateName(context1, ReadyState.NAME);
    Assert.assertEquals(false, context1.isInteractive());
    assertStateClass(context1, ReadyState.class);

    assertStateName(context2, WaitingState.NAME);
    assertStateClass(context2, WaitingState.class);

    assertStateName(context3, WaitingState.NAME);
    assertStateClass(context3, WaitingState.class);
  }

  @Test
  public void testCompleteThenCancelAndCompleteAgain() {
    doAction(ActionType.EXECUTE);
    doAction(ActionType.COMPLETE);
    doAction(ActionType.STOP);
    doAction(ActionType.EXECUTE);
    doAction(ActionType.COMPLETE);

    assertStateName(context1, CompletedState.NAME);
  }

  @Test
  public void testMultipleAndInverseCondition() {
    doAction(ActionType.EXECUTE);
    doAction(ActionType.COMPLETE);

    assertStateName(context1, CompletedState.NAME);
    assertStateName(context2, ReadyState.NAME);
    assertStateName(context3, ReadyState.NAME);
    assertStateClass(context3, ReadyState.class);

    doAction(context3, ActionType.EXECUTE);
    doAction(context3, ActionType.COMPLETE);

    assertStateName(context3, CompletedState.NAME);
    assertStateClass(context3, CompletedState.class);

    doAction(context2, ActionType.EXECUTE);
    doAction(context2, ActionType.COMPLETE);
    assertStateName(context3, NotApplicableState.NAME);
  }

  private void doAction(ActionType type) {
    doAction(context1, type);
  }

  /**
   * Mimics an Action on the specified context.
   * @param context
   * @param definition
   */
  private void doAction(StageExecutionContext context, ActionType type) {
    type.act(context, new Action(type));
  }

  private void assertStateName(StageExecutionContext context, String stateName) {
    String codes[] = context.getMessage().getCodes();
    Assert.assertTrue(Arrays.asList(codes).contains(stateName));
  }

  private void assertStateClass(StageExecutionContext context, Class<?> stateClass) {
    StageExecutionMemento memento = getMemento(context);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals(stateClass.getSimpleName(), memento.getState());
  }

  public class WaitingState extends AbstractStageState {

    public static final String NAME = "Waiting";

    public WaitingState() {
      setActiveInterviewService(StageExecutionTest.this.activeInterviewService);
    }

    @Override
    protected void addUserActions(Set<ActionType> types) {
      // No user actions
    }

    @Override
    public void execute(Action action) {
      super.execute(action);
      log.info("Stage {} is starting", super.getStage().getName());
      castEvent(TransitionEvent.START);
    }

    public String getName() {
      return NAME;
    }

  }

  public class ReadyState extends AbstractStageState {
    public static final String NAME = "Ready";

    public ReadyState() {
      setActiveInterviewService(StageExecutionTest.this.activeInterviewService);
    }

    @Override
    protected void addUserActions(Set<ActionType> types) {
      types.add(ActionType.EXECUTE);
      types.add(ActionType.COMMENT);
    }

    @Override
    public void execute(Action action) {
      super.execute(action);
      log.info("Stage {} is starting", super.getStage().getName());
      castEvent(TransitionEvent.START);
    }

    public String getName() {
      return NAME;
    }

    @Override
    protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
      if(transitionEvent.equals(TransitionEvent.VALID)) return false;
      else
        return true;
    }
  }

  public class InProgressState extends AbstractStageState {
    public static final String NAME = "InProgress";

    public InProgressState() {
      setActiveInterviewService(StageExecutionTest.this.activeInterviewService);
    }

    @Override
    protected void addUserActions(Set<ActionType> types) {
      types.add(ActionType.STOP);
      types.add(ActionType.COMMENT);
    }

    @Override
    protected void addSystemActions(Set<ActionType> types) {
      types.add(ActionType.COMPLETE);
    }

    @Override
    public void stop(Action action) {
      log.info("Stage {} is stopping", super.getStage().getName());
      // Invalidate current instrument run
      castEvent(TransitionEvent.CANCEL);
    }

    @Override
    public void complete(Action action) {
      log.info("Stage {} is completing", super.getStage().getName());
      // Finish current instrument run
      castEvent(TransitionEvent.COMPLETE);
    }

    @Override
    public boolean isInteractive() {
      return true;
    }

    public String getName() {
      return NAME;
    }

  }

  public class CompletedState extends AbstractStageState {
    public static final String NAME = "Completed";

    public CompletedState() {
      setActiveInterviewService(StageExecutionTest.this.activeInterviewService);
    }

    @Override
    protected void addUserActions(Set<ActionType> types) {
      types.add(ActionType.STOP);
      types.add(ActionType.EXECUTE);
      types.add(ActionType.COMMENT);
    }

    @Override
    public void stop(Action action) {
      super.execute(action);
      log.info("Stage {} is cancelling", super.getStage().getName());
      castEvent(TransitionEvent.CANCEL);
    }

    @Override
    public boolean isCompleted() {
      return true;
    }

    public String getName() {
      return NAME;
    }

    @Override
    public void execute(Action action) {
      castEvent(TransitionEvent.RESUME);
    }

    @Override
    protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
      if(transitionEvent.equals(TransitionEvent.VALID)) return false;
      else
        return true;
    }
  }

  public class NotApplicableState extends AbstractStageState {

    public static final String NAME = "NotApplicableState";

    public NotApplicableState() {
      setActiveInterviewService(StageExecutionTest.this.activeInterviewService);
    }

    @Override
    protected void addUserActions(Set<ActionType> types) {
    }

    public String getName() {
      return NAME;
    }

    @Override
    protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
      if(transitionEvent.equals(TransitionEvent.NOTAPPLICABLE)) return false;
      else
        return true;
    }
  }

  public class SpecificStageDependencyCondition implements StageDependencyCondition {

    private static final long serialVersionUID = 1L;

    private String stageName;

    public SpecificStageDependencyCondition() {
    }

    public SpecificStageDependencyCondition(String name) {
      this.stageName = name;
    }

    public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
      return activeInterviewService.getStageExecution(stageName).isCompleted();
    }

    public boolean isDependentOn(String stageName) {
      return this.stageName.equals(stageName);
    }

    public String getStageName() {
      return stageName;
    }

    public void setStageName(String stageName) {
      this.stageName = stageName;
    }

    @Override
    public String toString() {
      return "[" + getClass().getSimpleName() + ":" + stageName + "]";
    }
  }

}
