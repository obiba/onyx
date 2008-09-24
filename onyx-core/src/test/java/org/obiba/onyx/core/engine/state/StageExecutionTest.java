package org.obiba.onyx.core.engine.state;

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
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.InverseStageDependencyCondition;
import org.obiba.onyx.engine.MultipleStageDependencyCondition;
import org.obiba.onyx.engine.Operator;
import org.obiba.onyx.engine.PreviousStageDependencyCondition;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageDependencyCondition;
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

  private boolean noResult = true;
  
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
    multipleCondition.setLeftStageDependencyCondition(inverseCondition);
    multipleCondition.setOperator(Operator.AND);
    multipleCondition.setRightStageDependencyCondition(new PreviousStageDependencyCondition("dummy1"));
    
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
    Assert.assertEquals("Ready", context1.getMessage());
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
    doAction(ActionDefinitionBuilder.START_ACTION);
    Assert.assertEquals("InProgress", context1.getMessage());
    Assert.assertEquals(false, context1.isCompleted());
    StageExecutionMemento memento = getMemento(context1);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("InProgressState", memento.getState());
    
    Assert.assertEquals("Waiting", context2.getMessage());
    memento = getMemento(context2);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("WaitingState", memento.getState());
    
    Assert.assertEquals("Waiting", context3.getMessage());
    memento = getMemento(context3);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("WaitingState", memento.getState());
  }

  @Test
  public void testCancelInProgress() {
    doAction(ActionDefinitionBuilder.START_ACTION);
    Assert.assertEquals("InProgress", context1.getMessage());
    Assert.assertEquals("Waiting", context2.getMessage());
    Assert.assertEquals("Waiting", context3.getMessage());

    doAction(ActionDefinitionBuilder.CANCEL_ACTION);
    Assert.assertEquals("Ready", context1.getMessage());
    Assert.assertEquals(false, context1.isCompleted());
    Assert.assertEquals("Waiting", context2.getMessage());
    Assert.assertEquals("Waiting", context3.getMessage());
  }

  @Test
  public void testCommentDuringInProgress() {
    doAction(ActionDefinitionBuilder.START_ACTION);
    Assert.assertEquals("InProgress", context1.getMessage());
    Assert.assertEquals("Waiting", context2.getMessage());
    Assert.assertEquals("Waiting", context3.getMessage());

    doAction(ActionDefinitionBuilder.COMMENT_ACTION);
    Assert.assertEquals("InProgress", context1.getMessage());
    Assert.assertEquals("Waiting", context2.getMessage());
    Assert.assertEquals("Waiting", context3.getMessage());
  }

  @Test
  public void testCompleteInProgress() {
    doAction(ActionDefinitionBuilder.START_ACTION);
    Assert.assertEquals("InProgress", context1.getMessage());
    Assert.assertEquals("Waiting", context2.getMessage());
    Assert.assertEquals("Waiting", context3.getMessage());

    // complete the first stage and make assert that the second becomes ready
    doAction(ActionDefinitionBuilder.COMPLETE_ACTION);
    Assert.assertEquals(true, context1.isCompleted());
    Assert.assertEquals("Completed", context1.getMessage());
    Assert.assertEquals(false, context1.isInteractive());
    
    Assert.assertEquals("Ready", context2.getMessage());
    StageExecutionMemento memento = getMemento(context2);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("ReadyState", memento.getState());
    
    Assert.assertEquals("Ready", context3.getMessage());
    memento = getMemento(context3);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("ReadyState", memento.getState());
  }

  @Test
  public void testCancelCompleted() {
    doAction(ActionDefinitionBuilder.START_ACTION);
    doAction(ActionDefinitionBuilder.COMPLETE_ACTION);
    doAction(ActionDefinitionBuilder.CANCEL_ACTION);
    Assert.assertEquals("Ready", context1.getMessage());
    Assert.assertEquals(false, context1.isInteractive());
    StageExecutionMemento memento = getMemento(context1);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("ReadyState", memento.getState());
    
    Assert.assertEquals("Waiting", context2.getMessage());
    memento = getMemento(context2);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("WaitingState", memento.getState());

    Assert.assertEquals("Waiting", context3.getMessage());
    memento = getMemento(context3);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("WaitingState", memento.getState());
  }

  @Test
  public void testCancelFirstWhenBothAreCompleted() {
    doAction(ActionDefinitionBuilder.START_ACTION);
    doAction(ActionDefinitionBuilder.COMPLETE_ACTION);
    doAction(context2, ActionDefinitionBuilder.START_ACTION);
    doAction(context2, ActionDefinitionBuilder.COMPLETE_ACTION);
    Assert.assertEquals("Completed", context1.getMessage());
    Assert.assertEquals("Completed", context2.getMessage());
    Assert.assertEquals("Not Applicable", context3.getMessage());

    doAction(ActionDefinitionBuilder.CANCEL_ACTION);
    Assert.assertEquals("Ready", context1.getMessage());
    Assert.assertEquals(false, context1.isInteractive());
    StageExecutionMemento memento = getMemento(context1);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("ReadyState", memento.getState());
    Assert.assertEquals("Waiting", context2.getMessage());
    memento = getMemento(context2);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("WaitingState", memento.getState());
    
    Assert.assertEquals("Waiting", context3.getMessage());
    memento = getMemento(context3);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("WaitingState", memento.getState());
  }

  @Test
  public void testCompleteThenCancelAndCompleteAgain() {
    doAction(ActionDefinitionBuilder.START_ACTION);
    doAction(ActionDefinitionBuilder.COMPLETE_ACTION);
    doAction(ActionDefinitionBuilder.CANCEL_ACTION);
    doAction(ActionDefinitionBuilder.START_ACTION);
    doAction(ActionDefinitionBuilder.COMPLETE_ACTION);
    Assert.assertEquals("Completed", context1.getMessage());
  }

  @Test
  public void testMultipleAndInverseCondition() {
    doAction(ActionDefinitionBuilder.START_ACTION);
    doAction(ActionDefinitionBuilder.COMPLETE_ACTION);
    
    Assert.assertEquals("Completed", context1.getMessage());
    Assert.assertEquals("Ready", context2.getMessage());
    Assert.assertEquals("Ready", context3.getMessage());
    StageExecutionMemento memento = getMemento(context3);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("ReadyState", memento.getState());
    
    doAction(context3, ActionDefinitionBuilder.START_ACTION);
    doAction(context3, ActionDefinitionBuilder.COMPLETE_ACTION);
    
    Assert.assertEquals("Completed", context3.getMessage());
    memento = getMemento(context3);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("CompletedState", memento.getState());
    
    doAction(context2, ActionDefinitionBuilder.START_ACTION);
    doAction(context2, ActionDefinitionBuilder.COMPLETE_ACTION);
    Assert.assertEquals("Not Applicable", context3.getMessage());
  }
  
  private void doAction(ActionDefinition definition) {
    doAction(context1, definition);
  }

  /**
   * Mimics an Action on the specified context.
   * @param context
   * @param definition
   */
  private void doAction(StageExecutionContext context, ActionDefinition definition) {
    definition.getType().act(context, new Action(definition));
  }

  public class WaitingState extends AbstractStageState {

    public WaitingState() {
      setActiveInterviewService(activeInterviewService);
    }

    @Override
    public void execute(Action action) {
      super.execute(action);
      log.info("Stage {} is starting", super.getStage().getName());
      castEvent(TransitionEvent.START);
    }

    @Override
    public String getMessage() {
      return "Waiting";
    }

    public String getName() {
      return "Waiting";
    }

  }

  public class ReadyState extends AbstractStageState {

    public ReadyState() {
      setActiveInterviewService(activeInterviewService);
      addAction(ActionDefinitionBuilder.START_ACTION);
      addAction(ActionDefinitionBuilder.COMMENT_ACTION);
    }

    @Override
    public void execute(Action action) {
      super.execute(action);
      log.info("Stage {} is starting", super.getStage().getName());
      castEvent(TransitionEvent.START);
    }

    @Override
    public String getMessage() {
      return "Ready";
    }

    public String getName() {
      return "Ready";
    }
    
    @Override
    protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
      if(transitionEvent.equals(TransitionEvent.VALID)) return false;
      else
        return true;
    }
  }

  public class InProgressState extends AbstractStageState {

    public InProgressState() {
      setActiveInterviewService(activeInterviewService);
      addAction(ActionDefinitionBuilder.CANCEL_ACTION);
      addSystemAction(ActionDefinitionBuilder.COMPLETE_ACTION);
      addAction(ActionDefinitionBuilder.COMMENT_ACTION);
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

    @Override
    public String getMessage() {
      return "InProgress";
    }

    public String getName() {
      return "InProgress";
    }

  }

  public class CompletedState extends AbstractStageState {

    public CompletedState() {
      setActiveInterviewService(activeInterviewService);
      addAction(ActionDefinitionBuilder.CANCEL_ACTION);
      addAction(ActionDefinitionBuilder.COMMENT_ACTION);
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

    @Override
    public String getMessage() {
      return "Completed";
    }

    public String getName() {
      return "Completed";
    }
    
    @Override
    protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
      if(transitionEvent.equals(TransitionEvent.VALID)) return false;
      else
        return true;
    }
  }
  
  public class NotApplicableState extends AbstractStageState {

    public NotApplicableState() {
      setActiveInterviewService(activeInterviewService);
    }

    @Override
    public String getMessage() {
      return "Not Applicable";
    }

    public String getName() {
      return "NotApplicableState";
    }
    
    @Override
    protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
      if(transitionEvent.equals(TransitionEvent.NOTAPPLICABLE)) return false;
      else
        return true;
    }
  }
  
  public class SpecificStageDependencyCondition extends StageDependencyCondition {

    private static final long serialVersionUID = 1L;

    private String stageName;

    public SpecificStageDependencyCondition() {
    }

    public SpecificStageDependencyCondition(String name) {
      this.stageName = name;
    }

    @Override
    public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
      return activeInterviewService.getStageExecution(stageName).isCompleted();
    }

    @Override
    public boolean isDependentOn(String stageName) {
      return this.stageName.equals(stageName);
    }

    public String getStageName() {
      return stageName;
    }

    public void setStageName(String stageName) {
      this.stageName = stageName;
    }
  }
  
}
