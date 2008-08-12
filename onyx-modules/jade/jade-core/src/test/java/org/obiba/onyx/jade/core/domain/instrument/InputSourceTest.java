package org.obiba.onyx.jade.core.domain.instrument;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.InputDataSourceVisitor;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.springframework.beans.factory.annotation.Autowired;

public class InputSourceTest extends BaseDefaultSpringContextTestCase {

  @Autowired(required=true)
  InputDataSourceVisitor inputDataSourceVisitor;
  
  ParticipantInterview interview = new ParticipantInterview();

  @Test
  public void testInputParameter() {
    setData();
    
    testParticipantPropertyRetriever();
    testOutputParameterRetriever();
  }
  
  public void setData() {
    System.out.println("*** InputSourceTest: Setting Data");

    Participant participant = new Participant();
    participant.setBarcode("1524213535");
    participant.setLastName("Dupont");
    participant.setFirstName("Noémie");
    participant.setGender(Gender.FEMALE);
    participant.setBirthDate(new Date("1966/05/15"));
    interview.setParticipant(participant);
    interview.setId("12548");
    
    //instrument definition
    InstrumentType type = new InstrumentType();
    type.setId("1");
    type.setName("IT00125");
    Instrument instrument = new Instrument();
    instrument.setId("1");
    instrument.setInstrumentType(type);
    instrument.setName("Spirometer");

    //first instrument run
    InstrumentRun firstRun = new InstrumentRun();
    firstRun.setId("1");
    firstRun.setTimeStart(new Date("2008/08/10 08:16:00"));
    firstRun.setTimeEnd(new Date("2008/08/10 09:16:00"));
    firstRun.setParticipantInterview(interview);
    firstRun.setStatus(InstrumentRunStatus.COMPLETED);
    firstRun.setInstrument(instrument);

    //second instrument run
    InstrumentRun secondRun = new InstrumentRun();
    secondRun.setId("2");
    secondRun.setTimeStart(new Date("2008/08/12 08:16:00"));
    secondRun.setTimeEnd(new Date("2008/08/12 09:16:00"));
    secondRun.setParticipantInterview(interview);
    secondRun.setStatus(InstrumentRunStatus.COMPLETED);
    secondRun.setInstrument(instrument);

    //instrument parameter definition
    InstrumentOutputParameter parameter1 = new InstrumentOutputParameter();
    parameter1.setId("1");
    parameter1.setName("heigth");
    parameter1.setDataType(DataType.INTEGER);
    parameter1.setInstrument(instrument);
    InstrumentOutputParameter parameter2 = new InstrumentOutputParameter();
    parameter2.setId("2");
    parameter2.setName("weigth");
    parameter2.setDataType(DataType.INTEGER);
    parameter2.setInstrument(instrument);

    //run value definition
    InstrumentRunValue runValue1 = new InstrumentRunValue();
    runValue1.setInstrumentRun(firstRun);
    runValue1.setInstrumentParameter(parameter1);
    runValue1.setData(new Data(DataType.INTEGER, Long.valueOf("185")));

    InstrumentRunValue runValue2 = new InstrumentRunValue();
    runValue2.setInstrumentRun(firstRun);
    runValue2.setInstrumentParameter(parameter2);
    runValue2.setData(new Data(DataType.INTEGER, Long.valueOf("75")));
    
    InstrumentRunValue runValue3 = new InstrumentRunValue();
    runValue3.setInstrumentRun(secondRun);
    runValue3.setInstrumentParameter(parameter1);
    runValue3.setData(new Data(DataType.INTEGER, Long.valueOf("187")));
    
    InstrumentRunValue runValue4 = new InstrumentRunValue();
    runValue4.setInstrumentRun(secondRun);
    runValue4.setInstrumentParameter(parameter2);
    runValue4.setData(new Data(DataType.INTEGER, Long.valueOf("77")));
    
  }
  
  public void testParticipantPropertyRetriever() {
    System.out.println("*** ParticipantPropertySourceTest: Testing Data");
    
    ParticipantPropertySource participantPropertySource = new ParticipantPropertySource();
    
    try {
      participantPropertySource.setProperty("birthDate");
      Data resultData = inputDataSourceVisitor.getData(interview.getParticipant(), participantPropertySource);
      Assert.assertEquals(new Data(DataType.DATE, new Date("1966/05/15")).getValue(), resultData.getValue());
      Assert.assertEquals(DataType.DATE, resultData.getType());
      participantPropertySource.setProperty("lastName");
      resultData = inputDataSourceVisitor.getData(interview.getParticipant(), participantPropertySource);
      Assert.assertEquals("Dupont", resultData.getValue());
      Assert.assertEquals(DataType.TEXT, resultData.getType());
      participantPropertySource.setProperty("gender");
      resultData = inputDataSourceVisitor.getData(interview.getParticipant(), participantPropertySource);
      Assert.assertEquals("FEMALE", resultData.getValue());
      Assert.assertEquals(DataType.TEXT, resultData.getType());
    } catch (Exception ex) {
      System.out.println("*** EXCEPTION *** TestParticipantPropertyRetriever: " + ex.getMessage().toString());
    }

    System.out.println("*** ParticipantPropertySourceTest: Test Done successfully");
  }
  
  public void testOutputParameterRetriever() {
    
    System.out.println("*** OutputParameterSourceTest: Testing Data");
    
    OutputParameterSource outputParameterSource = new OutputParameterSource();
    
    try {
      outputParameterSource.setParameterName("heigth");
      Data resultData = inputDataSourceVisitor.getData(interview.getParticipant(), outputParameterSource);
      Assert.assertEquals("187", resultData.getValue());
      Assert.assertEquals(DataType.INTEGER, resultData.getType());
    } catch (Exception ex) {
      System.out.println("*** EXCEPTION *** TestOutputParameterRetriever: " + ex.getMessage().toString());
    }

    System.out.println("*** OutputParameterSourceTest: Test Done successfully");
  }
  
}
