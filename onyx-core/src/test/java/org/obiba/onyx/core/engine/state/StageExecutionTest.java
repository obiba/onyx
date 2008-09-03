package org.obiba.onyx.core.engine.state;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StageExecutionTest extends BaseDefaultSpringContextTestCase {

  private static final Logger log = LoggerFactory.getLogger(StageExecutionTest.class);

  StageExecutionContext context;

  @Before
  public void setUp() {
    Stage stage = new Stage();
    stage.setName("dummy");
    context = new StageExecutionContext(new Interview(), stage);
    ReadyState ready = new ReadyState();
    InProgressState progress = new InProgressState();
    CompletedState completed = new CompletedState();
    
    context.addEdge(ready, TransitionEvent.START, progress);
    context.addEdge(progress, TransitionEvent.CANCEL, ready);
    context.addEdge(progress, TransitionEvent.COMPLETE, completed);
    context.addEdge(completed, TransitionEvent.CANCEL, ready);
    context.setInitialState(ready);
  }

  @Test
  public void testTransition() {
    Assert.assertEquals("Ready", context.getMessage());
    doTransition(ActionDefinition.START_ACTION);
    Assert.assertEquals("InProgress", context.getMessage());
    Assert.assertEquals(false, context.isCompleted());
    doTransition(ActionDefinition.CANCEL_ACTION);
    Assert.assertEquals("Ready", context.getMessage());
    Assert.assertEquals(false, context.isCompleted());
    doTransition(ActionDefinition.START_ACTION);
    Assert.assertEquals("InProgress", context.getMessage());
    Assert.assertEquals(true, context.isInteractive());
    doTransition(ActionDefinition.COMMENT_ACTION);
    Assert.assertEquals("InProgress", context.getMessage());
    doTransition(ActionDefinition.COMPLETE_ACTION);
    Assert.assertEquals(true, context.isCompleted());
    Assert.assertEquals("Completed", context.getMessage());
    Assert.assertEquals(false, context.isInteractive());
    doTransition(ActionDefinition.COMMENT_ACTION);
    Assert.assertEquals("Completed", context.getMessage());
    doTransition(ActionDefinition.CANCEL_ACTION);
    Assert.assertEquals("Ready", context.getMessage());
    doTransition(ActionDefinition.COMMENT_ACTION);
    Assert.assertEquals("Ready", context.getMessage());
    Assert.assertEquals(false, context.isInteractive());
  }
  
  private void doTransition(ActionDefinition definition) {
    definition.getType().act(context, new Action(definition));
  }

  public class ReadyState extends AbstractStageState {

    public ReadyState() {
      addAction(ActionDefinition.START_ACTION);
      addAction(ActionDefinition.COMMENT_ACTION);
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

    @Override
    public String getName() {
      return "Ready";
    }

  }

  public class InProgressState extends AbstractStageState {

    public InProgressState() {
      addAction(ActionDefinition.CANCEL_ACTION);
      addSystemAction(ActionDefinition.COMPLETE_ACTION);
      addAction(ActionDefinition.COMMENT_ACTION);
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
    
    @Override
    public String getName() {
      return "InProgress";
    }

  }

  public class CompletedState extends AbstractStageState {

    public CompletedState() {
      addAction(ActionDefinition.CANCEL_ACTION);
      addAction(ActionDefinition.COMMENT_ACTION);
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
    
    @Override
    public String getName() {
      return "Completed";
    }
  }
}
