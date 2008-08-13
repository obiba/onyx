package org.obiba.onyx.jade.core.domain.instrument;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.service.InputDataSourceVisitor;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.springframework.beans.factory.annotation.Autowired;

public class InputSourceTest extends BaseDefaultSpringContextTestCase {

  @Autowired(required=true)
  InputDataSourceVisitor inputDataSourceVisitor;
  
  @Autowired(required=true)
  EntityQueryService queryService;
  
  @Test
  @Dataset
  public void testParticipantPropertyRetriever() {
    System.out.println("*** ParticipantPropertySourceTest: Testing Data");
    Participant participant = queryService.get(Participant.class, Long.valueOf("1"));

    ParticipantPropertySource participantPropertySource = new ParticipantPropertySource();
    participantPropertySource.setProperty("birthDate");
    Data resultData = inputDataSourceVisitor.getData(participant, participantPropertySource);
    Assert.assertEquals("1966-05-15", resultData.getValue().toString());
    Assert.assertEquals(DataType.DATE, resultData.getType());
    System.out.println("*** ParticipantPropertySourceTest: Step 1 Done successfully");
    
    participantPropertySource.setProperty("lastName");
    resultData = inputDataSourceVisitor.getData(participant, participantPropertySource);
    Assert.assertEquals("Dupont", resultData.getValue());
    Assert.assertEquals(DataType.TEXT, resultData.getType());
    System.out.println("*** ParticipantPropertySourceTest: Step 2 Done successfully");
    
    participantPropertySource.setProperty("gender");
    resultData = inputDataSourceVisitor.getData(participant, participantPropertySource);
    Assert.assertEquals("FEMALE", resultData.getValue());
    Assert.assertEquals(DataType.TEXT, resultData.getType());
    System.out.println("*** ParticipantPropertySourceTest: Step 3 Done successfully");
  }
  
  @Test
  @Dataset
  public void testOutputParameterRetriever() {
    System.out.println("*** OutputParameterSourceTest: Testing Data");
    Participant participant = queryService.get(Participant.class, Long.valueOf("1"));
    InstrumentType instrumentType = queryService.get(InstrumentType.class, Long.valueOf("2"));
    
    OutputParameterSource outputParameterSource = new OutputParameterSource();
    outputParameterSource.setParameterName("heigth");
    outputParameterSource.setInstrumentType(instrumentType);
    
    Data resultData = inputDataSourceVisitor.getData(participant, outputParameterSource);
    Assert.assertEquals(Long.valueOf(187), resultData.getValue());
    Assert.assertEquals(DataType.INTEGER, resultData.getType());
    System.out.println("*** OutputParameterSourceTest: Test Done successfully");
  }
}
