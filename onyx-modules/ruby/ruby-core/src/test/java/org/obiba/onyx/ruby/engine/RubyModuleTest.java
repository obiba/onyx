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
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageDependencyCondition;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.ruby.engine.state.RubyCompletedState;
import org.obiba.onyx.ruby.engine.state.RubyContraIndicatedState;
import org.obiba.onyx.ruby.engine.state.RubyInProgressState;
import org.obiba.onyx.ruby.engine.state.RubyInterruptedState;
import org.obiba.onyx.ruby.engine.state.RubyNotApplicableState;
import org.obiba.onyx.ruby.engine.state.RubyReadyState;
import org.obiba.onyx.ruby.engine.state.RubyWaitingState;

public class RubyModuleTest {
  //
  // Constants
  //

  private static final String WAITING_STATE = "RubyWaitingState";

  private static final String READY_STATE = "RubyReadyState";

  private static final String CONTRAINDICATED_STATE = "RubyContraIndicatedState";

  private static final String IN_PROGRESS_STATE = "RubyInProgressState";

  private static final String INTERRUPTED_STATE = "RubyInterruptedState";

  private static final String NOT_APPLICABLE_STATE = "RubyNotApplicableState";

  //
  // Instance Variables
  //

  private RubyModule rubyModule;

  private ApplicationContextMock applicationContextMock;

  private ModuleRegistry moduleRegistry;

  private ActiveInterviewService activeInterviewServiceMock;

  private ActiveTubeRegistrationService activeTubeRegistrationServiceMock;

  private PersistenceManager persistenceManagerMock;

  private StageExecutionContext stageExecutionContext;

  private User user;

  private Participant participant;

  private Interview interview;

  private Stage stage;

  private ParticipantTubeRegistration participantTubeRegistration;

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

    participantTubeRegistration = new ParticipantTubeRegistration();

    //
    // Create test application context and add to it the necessary mocks.
    //
    applicationContextMock = new ApplicationContextMock();

    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
    applicationContextMock.putBean("activeInterviewService", activeInterviewServiceMock);

    activeTubeRegistrationServiceMock = createMock(ActiveTubeRegistrationService.class);
    applicationContextMock.putBean("activeTubeRegistrationService", activeTubeRegistrationServiceMock);

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
    RubyInProgressState rubyInProgressState = new RubyInProgressState();
    rubyInProgressState.setActiveInterviewService(activeInterviewServiceMock);
    rubyInProgressState.setActiveTubeRegistrationService(activeTubeRegistrationServiceMock);
    applicationContextMock.putBean("rubyInProgressState", rubyInProgressState);
    AbstractStageState rubyInterruptedState = new RubyInterruptedState();
    applicationContextMock.putBean("rubyInterruptedState", rubyInterruptedState);
    AbstractStageState rubyCompletedState = new RubyCompletedState();
    applicationContextMock.putBean("rubyCompletedState", rubyCompletedState);
    AbstractStageState rubyNotApplicableState = new RubyNotApplicableState();
    applicationContextMock.putBean("rubyNotApplicableState", rubyNotApplicableState);

    rubyModule = new RubyModule();
    rubyModule.setApplicationContext(applicationContextMock);

    List<Stage> stages = new ArrayList<Stage>();
    stages.add(stage);
    rubyModule.setStages(stages);

    moduleRegistry = new ModuleRegistry();
    moduleRegistry.registerModule(rubyModule);
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
  public void testWaitingToNotApplicableTransition() {
    setDependencyConditionNotSatisfied();

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);
    exec.setModuleRegistry(moduleRegistry);

    // Set the current state to WAITING.
    StageExecutionMemento memento = createMemento(WAITING_STATE);
    exec.restoreFromMemento(memento);

    // Record expectations for mocks.
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(memento).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(memento).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Fire a NOTAPPLICABLE event to trigger a transition to the NOT APPLICABLE state.
    exec.castEvent(TransitionEvent.NOTAPPLICABLE);

    // Verify expectations of mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the NOT APPLICABLE state.
    memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyNotApplicableState.class.getSimpleName(), memento.getState());
  }

  @Test
  public void testReadyToInProgressTransition() {
    setDependencyConditionSatisfied();

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);
    exec.setModuleRegistry(moduleRegistry);

    // Set the current state to READY.
    StageExecutionMemento memento = createMemento(READY_STATE);
    exec.restoreFromMemento(memento);

    // Record expectations for mocks.
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(memento).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(memento).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Fire a START event to trigger a transition to the IN PROGRESS state.
    exec.castEvent(TransitionEvent.START);

    // Verify expectations of mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the IN PROGRESS state.
    memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyInProgressState.class.getSimpleName(), memento.getState());
  }

  @Test
  public void testInProgressToCompletedTransition() {
    setDependencyConditionSatisfied();

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);
    exec.setModuleRegistry(moduleRegistry);

    // Set the current state to IN PROGRESS.
    StageExecutionMemento memento = createMemento(IN_PROGRESS_STATE);
    exec.restoreFromMemento(memento);

    // Record expectations for mocks.
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(memento).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(memento).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Fire a COMPLETE event to trigger a transition to the COMPLETED state.
    exec.castEvent(TransitionEvent.COMPLETE);

    // Verify expectations of mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the COMPLETED state.
    memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyCompletedState.class.getSimpleName(), memento.getState());
  }

  @Test
  public void testInProgressToContraIndicatedTransition() {
    setDependencyConditionSatisfied();

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);
    exec.setModuleRegistry(moduleRegistry);

    // Set the current state to IN PROGRESS.
    StageExecutionMemento memento = createMemento(IN_PROGRESS_STATE);
    exec.restoreFromMemento(memento);

    // Record expectations for mocks.
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(memento).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(memento).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Fire a CONTRAINDICATE event to trigger a transition to the CONTRAINDICATED state.
    exec.castEvent(TransitionEvent.CONTRAINDICATED);

    // Verify expectations of mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the CONTRAINDICATED state.
    memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyContraIndicatedState.class.getSimpleName(), memento.getState());
  }

  @Test
  public void testInProgressToInterruptedTransition() {
    setDependencyConditionSatisfied();

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);
    exec.setModuleRegistry(moduleRegistry);

    // Set the current state to IN PROGRESS.
    StageExecutionMemento memento = createMemento(IN_PROGRESS_STATE);
    exec.restoreFromMemento(memento);

    // Record expectations for mocks.
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(memento).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(memento).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Fire an INTERRUPT event to trigger a transition to the INTERRUPTED state.
    exec.castEvent(TransitionEvent.INTERRUPT);

    // Verify expectations of mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the INTERRUPTED state.
    memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyInterruptedState.class.getSimpleName(), memento.getState());
  }

  @Test
  public void testInterruptedToInProgressTransition() {
    setDependencyConditionSatisfied();

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);
    exec.setModuleRegistry(moduleRegistry);

    // Set the current state to INTERRUPTED.
    StageExecutionMemento memento = createMemento(INTERRUPTED_STATE);
    exec.restoreFromMemento(memento);

    // Record expectations for mocks.
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(memento).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(memento).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Fire a RESUME event to trigger a transition to the IN PROGRESS state.
    exec.castEvent(TransitionEvent.RESUME);

    // Verify expectations of mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the IN PROGRESS state.
    memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyInProgressState.class.getSimpleName(), memento.getState());
  }

  @Test
  public void testContraIndicatedToReadyTransition() {
    setDependencyConditionSatisfied();

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);
    exec.setModuleRegistry(moduleRegistry);

    // Set the current state to CONTRAINDICATED.
    StageExecutionMemento memento = createMemento(CONTRAINDICATED_STATE);
    exec.restoreFromMemento(memento);

    // Record expectations for mocks.
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(memento).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(memento).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Fire a CANCEL event to trigger a transition to the READY state.
    exec.castEvent(TransitionEvent.CANCEL);

    // Verify expectations of mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the READY state.
    memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyReadyState.class.getSimpleName(), memento.getState());
  }

  @Test
  public void testNotApplicableToReadyTransition() {
    setDependencyConditionNotSatisfied();

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);
    exec.setModuleRegistry(moduleRegistry);

    // Set the current state to NOT APPLICABLE.
    StageExecutionMemento memento = createMemento(NOT_APPLICABLE_STATE);
    exec.restoreFromMemento(memento);

    // Record expectations for mocks.
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(memento).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(memento).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Fire a VALID event to trigger a transition to the READY state.
    exec.castEvent(TransitionEvent.VALID);

    // Verify expectations of mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the READY state.
    memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyReadyState.class.getSimpleName(), memento.getState());
  }

  @Test
  public void testNotApplicableToWaitingTransition() {
    setDependencyConditionNotSatisfied();

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, stage);
    exec.setModuleRegistry(moduleRegistry);

    // Set the current state to NOT APPLICABLE.
    StageExecutionMemento memento = createMemento(NOT_APPLICABLE_STATE);
    exec.restoreFromMemento(memento);

    // Record expectations for mocks.
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(memento).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(memento).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Fire an INVALID event to trigger a transition to the WAITING state.
    exec.castEvent(TransitionEvent.INVALID);

    // Verify expectations of mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the WAITING state.
    memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubyWaitingState.class.getSimpleName(), memento.getState());
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

  private void setDependencyConditionSatisfied() {
    ((StageDependencyConditionMock) stage.getStageDependencyCondition()).setDependencySatisfied(Boolean.TRUE);
  }

  private void setDependencyConditionNotSatisfied() {
    ((StageDependencyConditionMock) stage.getStageDependencyCondition()).setDependencySatisfied(null);
  }

  private StageExecutionMemento createMemento(String stateName) {
    StageExecutionMemento memento = new StageExecutionMemento();
    memento.setInterview(interview);
    memento.setStage(stage.getName());
    memento.setState(stateName);
    return memento;
  }

  //
  // Inner Classes
  //

  private static class StageDependencyConditionMock implements StageDependencyCondition {
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
