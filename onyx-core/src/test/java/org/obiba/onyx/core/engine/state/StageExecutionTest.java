package org.obiba.onyx.core.engine.state;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.stage.StageExecutionMemento;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.PreviousStageDependencyCondition;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.AbstractStageState;
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

  ActiveInterviewService activeInterviewService;

  @Before
  public void setUp() {

    activeInterviewService = EasyMock.createMock(ActiveInterviewService.class);
  //  EasyMock.expect(activeInterviewService.getStageExecution("dummy1")).andReturn(context1);
    EasyMock.expect(activeInterviewService.getStageExecution("dummy2")).andReturn(context2);
    EasyMock.replay(activeInterviewService);

    Stage stage1 = new Stage();
    stage1.setName("dummy1");
    stage1 = persistenceManager.save(stage1);

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
    stage2 = persistenceManager.save(stage2);

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
  }

  private StageExecutionMemento getMemento(StageExecutionContext context) {
    StageExecutionMemento memento = new StageExecutionMemento();
    memento.setStage(context.getStage());
    memento.setInterview(context.getInterview());
    return persistenceManager.matchOne(memento);
  }

  @Test
  public void testTransition() {
    Assert.assertEquals("Ready", context1.getMessage());
    StageExecutionMemento memento = getMemento(context1);
    Assert.assertNull("Memento is not null", memento);
    memento = getMemento(context2);
    Assert.assertNull("Memento is not null", memento);

    doTransition(ActionDefinitionBuilder.START_ACTION);
    Assert.assertEquals("InProgress", context1.getMessage());
    Assert.assertEquals(false, context1.isCompleted());
    memento = getMemento(context1);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("InProgressState", memento.getState());
    Assert.assertEquals("Waiting", context2.getMessage());
    memento = getMemento(context2);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("WaitingState", memento.getState());

    doTransition(ActionDefinitionBuilder.CANCEL_ACTION);
    Assert.assertEquals("Ready", context1.getMessage());
    Assert.assertEquals(false, context1.isCompleted());

    doTransition(ActionDefinitionBuilder.START_ACTION);
    Assert.assertEquals("InProgress", context1.getMessage());
    Assert.assertEquals(true, context1.isInteractive());

    doTransition(ActionDefinitionBuilder.COMMENT_ACTION);
    Assert.assertEquals("InProgress", context1.getMessage());

    doTransition(ActionDefinitionBuilder.COMPLETE_ACTION);
    Assert.assertEquals(true, context1.isCompleted());
    Assert.assertEquals("Completed", context1.getMessage());
    Assert.assertEquals(false, context1.isInteractive());
    Assert.assertEquals("Ready", context2.getMessage());
    memento = getMemento(context2);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("ReadyState", memento.getState());

    doTransition(ActionDefinitionBuilder.COMMENT_ACTION);
    Assert.assertEquals("Completed", context1.getMessage());

    doTransition(ActionDefinitionBuilder.CANCEL_ACTION);
    Assert.assertEquals("Ready", context1.getMessage());

    doTransition(ActionDefinitionBuilder.COMMENT_ACTION);
    Assert.assertEquals("Ready", context1.getMessage());
    Assert.assertEquals(false, context1.isInteractive());
    memento = getMemento(context1);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("ReadyState", memento.getState());
    Assert.assertEquals("Waiting", context2.getMessage());
    memento = getMemento(context2);
    Assert.assertNotNull("Memento is null", memento);
    Assert.assertEquals("WaitingState", memento.getState());

  }

  private void doTransition(ActionDefinition definition) {
    definition.getType().act(context1, new Action(definition));
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
  }
}
