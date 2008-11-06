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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.stage.StageExecutionMemento;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageDependencyCondition;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.ruby.engine.state.RubyCompletedState;
import org.obiba.onyx.ruby.engine.state.RubyContraIndicatedState;
import org.obiba.onyx.ruby.engine.state.RubyInProgressState;
import org.obiba.onyx.ruby.engine.state.RubyInterruptedState;
import org.obiba.onyx.ruby.engine.state.RubyReadyState;
import org.obiba.onyx.ruby.engine.state.RubyWaitingState;

public class RubyModuleTest {

  private ApplicationContextMock applicationContextMock;

  private ActiveInterviewService activeInterviewServiceMock;

  private PersistenceManager persistenceManagerMock;

  private StageExecutionContext stageExecutionContext;

  private User user;

  private Participant participant;

  private Interview interview;

  private Stage stage;

  //
  // Fixture Methods
  //

  @Before
  public void setUp() {
    //
    // Create domain objects for tests.
    //
    user = createUser(1l, "user", "test");
    participant = createParticipant(1l, "participant", "test");

    interview = createInterview(1l);
    interview.setUser(user);
    interview.setParticipant(participant);

    stage = new Stage();
    stage.setName("SamplesCollection");

    StageDependencyConditionMock stageDependencyConditionMock = new StageDependencyConditionMock();
    stage.setStageDependencyCondition(stageDependencyConditionMock);

    //
    // Create test application context and add to it the necessary mocks.
    //
    applicationContextMock = new ApplicationContextMock();

    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
    applicationContextMock.putBean("activeInterviewService", activeInterviewServiceMock);

    persistenceManagerMock = createMock(PersistenceManager.class);

    stageExecutionContext = new StageExecutionContext(interview, stage);
    stageExecutionContext.setPersistenceManager(persistenceManagerMock);
    applicationContextMock.putBean("stageExecutionContext", stageExecutionContext);

    AbstractStageState rubyWaitingState = new RubyWaitingState();
    applicationContextMock.putBean("rubyWaitingState", rubyWaitingState);
    AbstractStageState rubyReadyState = new RubyReadyState();
    applicationContextMock.putBean("rubyReadyState", rubyReadyState);
    AbstractStageState rubyContraIndicatedState = new RubyContraIndicatedState();
    applicationContextMock.putBean("rubyContraIndicatedState", rubyContraIndicatedState);
    AbstractStageState rubyInProgressState = new RubyInProgressState();
    applicationContextMock.putBean("rubyInProgressState", rubyInProgressState);
    AbstractStageState rubyInterruptedState = new RubyInterruptedState();
    applicationContextMock.putBean("rubyInterruptedState", rubyInterruptedState);
    AbstractStageState rubyCompletedState = new RubyCompletedState();
    applicationContextMock.putBean("rubyCompletedState", rubyCompletedState);
  }

  //
  // Test Methods
  //

  @Test
  public void testInitialStateWhenDependencyConditionNotSatisfied() {
    testInitialState(null, RubyWaitingState.class.getSimpleName());
  }

  @Test
  public void testInitialStateWhenDependencyConditionSatisfied() {
    testInitialState(Boolean.TRUE, RubyReadyState.class.getSimpleName());
  }

  @Test
  public void testReadyToInProgressTransition() {
    RubyModule rubyModule = new RubyModule();
    rubyModule.setApplicationContext(applicationContextMock);

    // Set the dependency condition to TRUE (i.e., satisfied).
    ((StageDependencyConditionMock) stage.getStageDependencyCondition()).setDependencySatisfied(Boolean.TRUE);

    List<Stage> stages = new ArrayList<Stage>();
    stages.add(stage);
    rubyModule.setStages(stages);

    // Record expectations for mocks.
    StageExecutionMemento template = new StageExecutionMemento();
    template.setInterview(interview);
    template.setStage(stage.getName());
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(template).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(template).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Create the StageExecutionContext. Initial state should be READY, since the dependency condition
    // is satisfied.
    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);

    // Fire the START event.
    exec.castEvent(TransitionEvent.START);

    // Verify mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the IN PROGRESS state.
    StageExecutionMemento memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyInProgressState.class.getSimpleName(), memento.getState());
  }

  @Test
  public void testInProgressToCompletedTransition() {
    RubyModule rubyModule = new RubyModule();
    rubyModule.setApplicationContext(applicationContextMock);

    // Set the dependency condition to TRUE (i.e., satisfied).
    ((StageDependencyConditionMock) stage.getStageDependencyCondition()).setDependencySatisfied(Boolean.TRUE);

    List<Stage> stages = new ArrayList<Stage>();
    stages.add(stage);
    rubyModule.setStages(stages);

    // Record expectations for mocks.
    StageExecutionMemento template = new StageExecutionMemento();
    template.setInterview(interview);
    template.setStage(stage.getName());
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(template).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(template).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Create the StageExecutionContext. Initial state should be READY, since the dependency condition
    // is satisfied.
    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);

    // Fire the START event then the COMPLETE event. This should transition us to COMPLETED (by way
    // of IN PROGRESS).
    exec.castEvent(TransitionEvent.START);
    exec.castEvent(TransitionEvent.COMPLETE);

    // Verify mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the COMPLETED state.
    StageExecutionMemento memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyCompletedState.class.getSimpleName(), memento.getState());
  }

  @Test
  public void testInProgressToContraIndicatedTransition() {
    RubyModule rubyModule = new RubyModule();
    rubyModule.setApplicationContext(applicationContextMock);

    // Set the dependency condition to TRUE (i.e., satisfied).
    ((StageDependencyConditionMock) stage.getStageDependencyCondition()).setDependencySatisfied(Boolean.TRUE);

    List<Stage> stages = new ArrayList<Stage>();
    stages.add(stage);
    rubyModule.setStages(stages);

    // Record expectations for mocks.
    StageExecutionMemento template = new StageExecutionMemento();
    template.setInterview(interview);
    template.setStage(stage.getName());
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(template).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(template).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Create the StageExecutionContext. Initial state should be READY, since the dependency condition
    // is satisfied.
    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);

    // Fire the START event then the CONTRAINDICATE event. This should transition us to CONTRAINDICATED (by way
    // of IN PROGRESS).
    exec.castEvent(TransitionEvent.START);
    exec.castEvent(TransitionEvent.CONTRAINDICATED);

    // Verify mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the CONTRAINDICATED state.
    StageExecutionMemento memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyContraIndicatedState.class.getSimpleName(), memento.getState());
  }

  @Test
  public void testInProgressToInterruptedTransition() {
    RubyModule rubyModule = new RubyModule();
    rubyModule.setApplicationContext(applicationContextMock);

    // Set the dependency condition to TRUE (i.e., satisfied).
    ((StageDependencyConditionMock) stage.getStageDependencyCondition()).setDependencySatisfied(Boolean.TRUE);

    List<Stage> stages = new ArrayList<Stage>();
    stages.add(stage);
    rubyModule.setStages(stages);

    // Record expectations for mocks.
    StageExecutionMemento template = new StageExecutionMemento();
    template.setInterview(interview);
    template.setStage(stage.getName());
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(template).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(template).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Create the StageExecutionContext. Initial state should be READY, since the dependency condition
    // is satisfied.
    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);

    // Fire the START event then the INTERRUPT event. This should transition us to INTERRUPTED (by way
    // of IN PROGRESS).
    exec.castEvent(TransitionEvent.START);
    exec.castEvent(TransitionEvent.INTERRUPT);

    // Verify mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the INTERRUPTED state.
    StageExecutionMemento memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyInterruptedState.class.getSimpleName(), memento.getState());
  }

  @Test
  public void testInterruptedToInProgressTransition() {
    RubyModule rubyModule = new RubyModule();
    rubyModule.setApplicationContext(applicationContextMock);

    // Set the dependency condition to TRUE (i.e., satisfied).
    ((StageDependencyConditionMock) stage.getStageDependencyCondition()).setDependencySatisfied(Boolean.TRUE);

    List<Stage> stages = new ArrayList<Stage>();
    stages.add(stage);
    rubyModule.setStages(stages);

    // Record expectations for mocks.
    StageExecutionMemento template = new StageExecutionMemento();
    template.setInterview(interview);
    template.setStage(stage.getName());
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(template).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(template).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Create the StageExecutionContext. Initial state should be READY, since the dependency condition
    // is satisfied.
    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);

    // Fire the START event, the INTERRUPT event, then the RESUME event. This should transition us to IN PROGRESS (by
    // way
    // of IN PROGRESS and INTERRUPTED).
    exec.castEvent(TransitionEvent.START);
    exec.castEvent(TransitionEvent.INTERRUPT);
    exec.castEvent(TransitionEvent.RESUME);

    // Verify mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the IN PROGRESS state.
    StageExecutionMemento memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyInProgressState.class.getSimpleName(), memento.getState());
  }

  @Test
  public void testContraIndicatedToReadyTransition() {
    RubyModule rubyModule = new RubyModule();
    rubyModule.setApplicationContext(applicationContextMock);

    // Set the dependency condition to TRUE (i.e., satisfied).
    ((StageDependencyConditionMock) stage.getStageDependencyCondition()).setDependencySatisfied(Boolean.TRUE);

    List<Stage> stages = new ArrayList<Stage>();
    stages.add(stage);
    rubyModule.setStages(stages);

    // Record expectations for mocks.
    StageExecutionMemento template = new StageExecutionMemento();
    template.setInterview(interview);
    template.setStage(stage.getName());
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(template).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(template).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Create the StageExecutionContext. Initial state should be READY, since the dependency condition
    // is satisfied.
    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);

    // Fire the START event, the CONTRAINDICATE event, then the CANCEL event. This should transition us to READY (by way
    // of IN PROGRESS and CONTRAINDICATED).
    exec.castEvent(TransitionEvent.START);
    exec.castEvent(TransitionEvent.CONTRAINDICATED);
    exec.castEvent(TransitionEvent.CANCEL);

    // Verify mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the READY state.
    StageExecutionMemento memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyReadyState.class.getSimpleName(), memento.getState());
  }

  //
  // Helper Methods
  //

  private void testInitialState(Boolean dependencySatisfied, String expectedInitialState) {
    RubyModule rubyModule = new RubyModule();
    rubyModule.setApplicationContext(applicationContextMock);

    ((StageDependencyConditionMock) stage.getStageDependencyCondition()).setDependencySatisfied(dependencySatisfied);

    List<Stage> stages = new ArrayList<Stage>();
    stages.add(stage);
    rubyModule.setStages(stages);

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);
    StageExecutionMemento memento = (StageExecutionMemento) exec.saveToMemento(null);

    Assert.assertEquals(expectedInitialState, memento.getState());
  }

  private User createUser(long id, String firstName, String lastName) {
    User user = new User();
    user.setId(id);
    user.setFirstName(firstName);
    user.setLastName(lastName);

    return user;
  }

  private Participant createParticipant(long id, String firstName, String lastName) {
    Participant participant = new Participant();
    participant.setId(id);
    participant.setFirstName(firstName);
    participant.setLastName(lastName);

    return participant;
  }

  private Interview createInterview(long id) {
    Interview interview = new Interview();
    interview.setId(id);

    return interview;
  }

  //
  // Inner Classes
  //

  private class StageDependencyConditionMock implements StageDependencyCondition {
    private Boolean dependencySatisfied;

    private Set<String> stageDependencies;

    public StageDependencyConditionMock() {
      stageDependencies = new HashSet<String>();
    }

    public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
      return dependencySatisfied;
    }

    public boolean isDependentOn(String stageName) {
      return stageDependencies.contains(stageName);
    }

    public void setDependencySatisfied(Boolean dependencySatisfied) {
      this.dependencySatisfied = dependencySatisfied;
    }

    public void addStageDependency(String stageName) {
      stageDependencies.add(stageName);
    }

    public void removeStageDependency(String stageName) {
      stageDependencies.remove(stageName);
    }
  }
}
