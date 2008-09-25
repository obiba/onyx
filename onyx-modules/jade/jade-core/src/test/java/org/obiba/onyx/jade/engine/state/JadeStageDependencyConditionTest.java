package org.obiba.onyx.jade.engine.state;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;

public class JadeStageDependencyConditionTest {

  private ActiveInterviewService activeInterviewServiceMock;
  
  private EntityQueryService queryServiceMock;
  
  private InstrumentService instrumentServiceMock;
  
  private InstrumentRunService instrumentRunServiceMock;
  
  private JadeStageDependencyCondition dependencyCondition; 
  
  @Before
  public void setup() {

    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
    queryServiceMock = createMock(EntityQueryService.class);
    instrumentServiceMock = createMock(InstrumentService.class);
    instrumentRunServiceMock = createMock(InstrumentRunService.class);
    
    dependencyCondition = new JadeStageDependencyCondition();
    dependencyCondition.setStageName("stageMock");
    dependencyCondition.setQueryService(queryServiceMock);
    dependencyCondition.setInstrumentService(instrumentServiceMock);
    dependencyCondition.setInstrumentRunService(instrumentRunServiceMock);
    
  }
  
  @Test
  public void testUncompletedCondition() {

    IStageExecution stageExecution = new StageExecutionContext() {
      @Override
      public String getName() {
        return "stageMock";
      };

      @Override
      public boolean isCompleted() {
        return false;
      };
    };

    expect(activeInterviewServiceMock.getStageExecution("stageMock")).andReturn(stageExecution);

    replay(activeInterviewServiceMock);

    Boolean conditionResult = dependencyCondition.isDependencySatisfied(activeInterviewServiceMock);

    verify(activeInterviewServiceMock);

    Assert.assertEquals(null, conditionResult);
  }

  @Test
  public void testCompletedNoResultCondition() {

    IStageExecution stageExecution = new StageExecutionContext() {
      @Override
      public String getName() {
        return "stageMock";
      };

      @Override
      public boolean isCompleted() {
        return true;
      };
    };
        
    expect(activeInterviewServiceMock.getStageExecution("stageMock")).andReturn(stageExecution);
    expect(activeInterviewServiceMock.getParticipant()).andReturn(newTestParticipant());
    expect(queryServiceMock.matchOne((ParticipantInterview) EasyMock.anyObject())).andReturn(new ParticipantInterview());
    expect(instrumentServiceMock.getInstrumentType("stageMock")).andReturn(newTestInstrumentType());
    expect(instrumentRunServiceMock.getLastCompletedInstrumentRun((ParticipantInterview) EasyMock.anyObject(), (InstrumentType) EasyMock.anyObject())).andReturn(newTestInstrumentRun(false));
    
    replay(activeInterviewServiceMock);
    replay(queryServiceMock);
    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);

    Boolean conditionResult = dependencyCondition.isDependencySatisfied(activeInterviewServiceMock);

    verify(activeInterviewServiceMock);
    verify(queryServiceMock);
    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertEquals((Boolean)false, conditionResult);
  }

  @Test
  public void testCompletedWithResultCondition() {

    IStageExecution stageExecution = new StageExecutionContext() {
      @Override
      public String getName() {
        return "stageMock";
      };

      @Override
      public boolean isCompleted() {
        return true;
      };
    };
        
    expect(activeInterviewServiceMock.getStageExecution("stageMock")).andReturn(stageExecution);
    expect(activeInterviewServiceMock.getParticipant()).andReturn(newTestParticipant());
    expect(queryServiceMock.matchOne((ParticipantInterview) EasyMock.anyObject())).andReturn(new ParticipantInterview());
    expect(instrumentServiceMock.getInstrumentType("stageMock")).andReturn(newTestInstrumentType());
    expect(instrumentRunServiceMock.getLastCompletedInstrumentRun((ParticipantInterview) EasyMock.anyObject(), (InstrumentType) EasyMock.anyObject())).andReturn(newTestInstrumentRun(true));
    
    replay(activeInterviewServiceMock);
    replay(queryServiceMock);
    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);

    Boolean conditionResult = dependencyCondition.isDependencySatisfied(activeInterviewServiceMock);

    verify(activeInterviewServiceMock);
    verify(queryServiceMock);
    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertEquals((Boolean)true, conditionResult);
  }
  
  private Participant newTestParticipant() {
    Participant p = new Participant();

    p.setId("1");
    p.setBarcode("1");
    p.setFirstName("Suzan");
    p.setLastName("Tremblay");
    p.setGender(Gender.FEMALE);

    return (p);
  }
  
  private InstrumentType newTestInstrumentType() {
    InstrumentType iT = new InstrumentType();
    
    iT.setName("stageMock");
    
    return (iT);
  }
  
  private InstrumentRun newTestInstrumentRun(boolean wantResults){
    InstrumentRun iR = new InstrumentRun();
    
    if (wantResults == true){
      iR.addInstrumentRunValue(new InstrumentRunValue());
    }
    
    return (iR);
  };
}
