package org.obiba.onyx.quartz.engine.state;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.Serializable;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.ITransitionEventSink;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;

public class QuartzInProgressStateTest {

  private ITransitionEventSink eventSinkMock;

  private QuestionnaireParticipantService questionnaireParticipantServiceMock;

  private ActiveInterviewService activeInterviewServiceMock;

  private QuartzInProgressState inProgressState;

  /**
   * Tests that the questionnaireParticipant is deleted if stage canceled or not applicable
   */

  @Before
  public void setUp() {
    eventSinkMock = createMock(ITransitionEventSink.class);
    questionnaireParticipantServiceMock = createMock(QuestionnaireParticipantService.class);
    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
  }

  @Test
  public void testQuestionnaireParticipantDeleteWhenCancel() {

    inProgressState = new QuartzInProgressState() {
      @Override
      protected Boolean areDependenciesCompleted() {
        return true;
      }
    };

    setInProgressState();

    expect(activeInterviewServiceMock.getParticipant()).andReturn(new Participant());
    expect(questionnaireParticipantServiceMock.getLastQuestionnaireParticipant((Participant) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(new QuestionnaireParticipant());
    questionnaireParticipantServiceMock.deleteQuestionnaireParticipant((Serializable) EasyMock.anyObject());
    eventSinkMock.castEvent(TransitionEvent.CANCEL);

    Action inProgressAction = new Action();

    replay(eventSinkMock);
    replay(activeInterviewServiceMock);
    replay(questionnaireParticipantServiceMock);

    inProgressState.stop(inProgressAction);

    verify(eventSinkMock);
    verify(activeInterviewServiceMock);
    verify(questionnaireParticipantServiceMock);
  }

  @Test
  public void testQuestionnaireParticipantDeleteWhenInvalid() {

    inProgressState = new QuartzInProgressState() {
      @Override
      protected Boolean areDependenciesCompleted() {
        return null;
      }
    };

    setInProgressState();

    expect(activeInterviewServiceMock.getParticipant()).andReturn(new Participant());
    expect(questionnaireParticipantServiceMock.getLastQuestionnaireParticipant((Participant) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(new QuestionnaireParticipant());
    questionnaireParticipantServiceMock.deleteQuestionnaireParticipant((Serializable) EasyMock.anyObject());
    eventSinkMock.castEvent(TransitionEvent.INVALID);

    Action inProgressAction = new Action();

    replay(eventSinkMock);
    replay(activeInterviewServiceMock);
    replay(questionnaireParticipantServiceMock);

    inProgressState.stop(inProgressAction);

    verify(eventSinkMock);
    verify(activeInterviewServiceMock);
    verify(questionnaireParticipantServiceMock);
  }

  private void setInProgressState() {
    inProgressState.setStage(newTestStage());
    inProgressState.setEventSink(eventSinkMock);
    inProgressState.setQuestionnaireParticipantService(questionnaireParticipantServiceMock);
    inProgressState.setActiveInterviewService(activeInterviewServiceMock);
  }

  private Stage newTestStage() {
    Stage s = new Stage();
    s.setName("QUE1");
    return (s);
  }

}
