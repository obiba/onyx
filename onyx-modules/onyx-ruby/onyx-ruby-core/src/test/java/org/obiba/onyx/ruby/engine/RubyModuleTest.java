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

import java.util.Arrays;
import java.util.HashSet;
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
import org.obiba.onyx.ruby.core.service.ParticipantTubeRegistrationService;
import org.obiba.onyx.ruby.engine.state.RubyCompletedState;
import org.obiba.onyx.ruby.engine.state.RubyContraIndicatedState;
import org.obiba.onyx.ruby.engine.state.RubyInProgressState;
import org.obiba.onyx.ruby.engine.state.RubyInterruptedState;
import org.obiba.onyx.ruby.engine.state.RubyNotApplicableState;
import org.obiba.onyx.ruby.engine.state.RubyReadyState;
import org.obiba.onyx.ruby.engine.state.RubySkippedState;
import org.obiba.onyx.ruby.engine.state.RubyWaitingState;

public class RubyModuleTest {
  //
  // Constants
  //

  private static final String WAITING_STATE = "RubyWaitingState";

  private static final String READY_STATE = "RubyReadyState";

  private static final String SKIPPED_STATE = "RubySkippedState";

  private static final String CONTRAINDICATED_STATE = "RubyContraIndicatedState";

  private static final String IN_PROGRESS_STATE = "RubyInProgressState";

  private static final String INTERRUPTED_STATE = "RubyInterruptedState";

  private static final String NOT_APPLICABLE_STATE = "RubyNotApplicableState";

  private static final String PARTICIPANT_TUBE_REGISTRATION = "ParticipantTubeRegistration";

  private static final String REGISTERED_PARTICIPANT_TUBE = "RegisteredParticipantTube";

  private static final String BARCODE = "barcode";

  private static final String FIRST_RUBY_STAGE_NAME = "BloodSamplesCollection";

  private static final String SECOND_RUBY_STAGE_NAME = "UrineSamplesCollection";

  //
  // Instance Variables
  //

  private RubyModule rubyModule;

  private ApplicationContextMock applicationContextMock;

  private ModuleRegistry moduleRegistry;

  private ActiveInterviewService activeInterviewServiceMock;

  private ParticipantTubeRegistrationService participantTubeRegistrationServiceMock;

  private PersistenceManager persistenceManagerMock;

  private StageExecutionContext stageExecutionContext;

  private User user;

  private Participant participant;

  private Interview interview;

  private Stage firstStage;

  private Stage secondStage;

  //
  // Fixture Methods
  //

  @Before
  public void setUp() {
    // Create RubyModule (object under test).

    rubyModule = new RubyModule() {
      public java.util.List<Stage> getStages() {
        return Arrays.asList(firstStage, secondStage);
      }
    };

    //
    // Create domain objects for tests.
    //

    user = createUser(1l, "user", "test");
    participant = createParticipant(1l, "participant", "test");

    interview = createInterview(1l);
    interview.setParticipant(participant);

    firstStage = new Stage();
    firstStage.setName(FIRST_RUBY_STAGE_NAME);
    StageDependencyConditionMock firstStageDependencyConditionMock = new StageDependencyConditionMock();
    firstStage.setStageDependencyCondition(firstStageDependencyConditionMock);

    secondStage = new Stage();
    secondStage.setName(SECOND_RUBY_STAGE_NAME);
    StageDependencyConditionMock secondStageDependencyConditionMock = new StageDependencyConditionMock();
    secondStage.setStageDependencyCondition(secondStageDependencyConditionMock);

    //
    // Create test application context and add to it the necessary mocks.
    //

    applicationContextMock = new ApplicationContextMock();
    rubyModule.setApplicationContext(applicationContextMock);

    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
    applicationContextMock.putBean("activeInterviewService", activeInterviewServiceMock);

    participantTubeRegistrationServiceMock = createMock(ParticipantTubeRegistrationService.class);
    applicationContextMock.putBean("participantTubeRegistrationService", participantTubeRegistrationServiceMock);

    persistenceManagerMock = createMock(PersistenceManager.class);

    stageExecutionContext = new StageExecutionContext(interview, firstStage);
    stageExecutionContext.setPersistenceManager(persistenceManagerMock);
    applicationContextMock.putBean("stageExecutionContext", stageExecutionContext);

    AbstractStageState rubyWaitingState = new RubyWaitingState();
    applicationContextMock.putBean("rubyWaitingState", rubyWaitingState);
    AbstractStageState rubyReadyState = new RubyReadyState();
    applicationContextMock.putBean("rubyReadyState", rubyReadyState);
    AbstractStageState rubySkippedState = new RubySkippedState();
    applicationContextMock.putBean("rubySkippedState", rubySkippedState);
    AbstractStageState rubyContraIndicatedState = new RubyContraIndicatedState();
    applicationContextMock.putBean("rubyContraIndicatedState", rubyContraIndicatedState);
    RubyInProgressState rubyInProgressState = new RubyInProgressState();
    rubyInProgressState.setActiveInterviewService(activeInterviewServiceMock);
    rubyInProgressState.setParticipantTubeRegistrationService(participantTubeRegistrationServiceMock);
    applicationContextMock.putBean("rubyInProgressState", rubyInProgressState);
    AbstractStageState rubyInterruptedState = new RubyInterruptedState();
    applicationContextMock.putBean("rubyInterruptedState", rubyInterruptedState);
    AbstractStageState rubyCompletedState = new RubyCompletedState();
    applicationContextMock.putBean("rubyCompletedState", rubyCompletedState);
    AbstractStageState rubyNotApplicableState = new RubyNotApplicableState();
    applicationContextMock.putBean("rubyNotApplicableState", rubyNotApplicableState);

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

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, firstStage);
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
  public void testReadyToSkippedTransition() {
    setDependencyConditionSatisfied();

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, firstStage);
    exec.setModuleRegistry(moduleRegistry);

    // Set the current state to READY.
    StageExecutionMemento memento = createMemento(READY_STATE);
    exec.restoreFromMemento(memento);

    // Record expectations for mocks.
    expect(persistenceManagerMock.matchOne(EasyMock.anyObject())).andReturn(memento).anyTimes();
    expect(persistenceManagerMock.save(EasyMock.anyObject())).andReturn(memento).anyTimes();

    // Stop recording expectations.
    replay(persistenceManagerMock);

    // Fire a SKIP event to trigger a transition to the SKIPPED state.
    exec.castEvent(TransitionEvent.SKIP);

    // Verify expectations of mocks.
    verify(persistenceManagerMock);

    // Verify that we have now transitioned to the SKIPPED state.
    memento = (StageExecutionMemento) exec.saveToMemento(null);
    Assert.assertEquals(RubySkippedState.class.getSimpleName(), memento.getState());
  }

  @Test
  public void testReadyToInProgressTransition() {
    setDependencyConditionSatisfied();

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, firstStage);
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

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, firstStage);
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

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, firstStage);
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

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, firstStage);
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

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, firstStage);
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

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, firstStage);
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

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, firstStage);
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

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, firstStage);
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
    rubyModule = new RubyModule() {
      public java.util.List<Stage> getStages() {
        return Arrays.asList(firstStage);
      }
    };

    rubyModule.setApplicationContext(applicationContextMock);

    ((StageDependencyConditionMock) firstStage.getStageDependencyCondition()).setDependencySatisfied(dependencySatisfied);

    StageExecutionContext exec = (StageExecutionContext) rubyModule.createStageExecution(interview, firstStage);
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
    ((StageDependencyConditionMock) firstStage.getStageDependencyCondition()).setDependencySatisfied(Boolean.TRUE);
  }

  private void setDependencyConditionNotSatisfied() {
    ((StageDependencyConditionMock) firstStage.getStageDependencyCondition()).setDependencySatisfied(null);
  }

  private StageExecutionMemento createMemento(String stateName) {
    StageExecutionMemento memento = new StageExecutionMemento();
    memento.setInterview(interview);
    memento.setStage(firstStage.getName());
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

    public Boolean isDependencySatisfied(Stage stage, ActiveInterviewService activeInterviewService) {
      return dependencySatisfied;
    }

    public boolean isDependentOn(Stage stage, String stageName) {
      return stageDependencies.contains(stageName);
    }

    public void setDependencySatisfied(Boolean dependencySatisfied) {
      this.dependencySatisfied = dependencySatisfied;
    }

  }
}
