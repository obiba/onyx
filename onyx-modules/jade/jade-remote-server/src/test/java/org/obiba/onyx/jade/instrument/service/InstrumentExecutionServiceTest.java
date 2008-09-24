package org.obiba.onyx.jade.instrument.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.Dataset;
import org.obiba.core.test.spring.DbUnitAwareTestExecutionListener;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"InstrumentExecutionServiceTest-context.xml"})
@TransactionConfiguration(transactionManager="transactionManager")
@TestExecutionListeners(value={DependencyInjectionTestExecutionListener.class,DirtiesContextTestExecutionListener.class,TransactionalTestExecutionListener.class,DbUnitAwareTestExecutionListener.class})
public class InstrumentExecutionServiceTest {
  
  @Autowired(required=true)
  PersistenceManager persistenceManager;
  
  @Autowired(required=true)
  InstrumentExecutionService instrumentExecutionService;
  
  @Autowired(required=true)
  ApplicationContext applicationContext;
  
  @Autowired(required=true)
  ActiveInstrumentRunService activeInstrumentRunService;
  
  @Test
  @Dataset
  public void getOperator() {
    Assert.assertNotNull("No InstrumentRun", activeInstrumentRunService.getInstrumentRun());
    Assert.assertEquals("tech technicien_1", instrumentExecutionService.getInstrumentOperator());
  }
  
  @Test
  @Dataset
  public void getParticipant() {
    Assert.assertNotNull("No InstrumentRun", activeInstrumentRunService.getInstrumentRun());
    Assert.assertEquals("Dupont", instrumentExecutionService.getParticipantLastName());
    Assert.assertEquals("Laura", instrumentExecutionService.getParticipantFirstName());   
  }
  
  @Test
  @Dataset
  public void updateInstrumentRunState() {
//    Assert.assertNotNull("No InstrumentRun", activeInstrumentRunService.getInstrumentRun());
//    instrumentExecutionService.updateInstrumentRunState("IN_ERROR");
//    Assert.assertEquals("IN_ERROR", activeInstrumentRunService.getInstrumentRun().getStatus().toString());
  }
  
  @Test
  @Dataset
  public void addOutputParameterValues() {
//    Map<String, Data> valuesMap = new HashMap<String, Data>();
//    valuesMap.put("density", new Data(DataType.INTEGER, Long.valueOf("125")));
//    valuesMap.put("strength", new Data(DataType.INTEGER, Long.valueOf("32")));
//    
//    instrumentExecutionService.addOutputParameterValues(valuesMap);
//    InstrumentRunValue template = new InstrumentRunValue();
//    template.setInstrumentRun(activeInstrumentRunService.getInstrumentRun());
//    List<InstrumentRunValue> instrumentRunValues = persistenceManager.match(template);
//    
//    for (InstrumentRunValue instrumentRunValue : instrumentRunValues) {
//      if (instrumentRunValue.getInstrumentParameter().getName().equals("density"))
//        Assert.assertEquals(Long.valueOf("125"), instrumentRunValue.getData().getValue());
//      if (instrumentRunValue.getInstrumentParameter().getName().equals("strength"))
//        Assert.assertEquals(Long.valueOf("32"), instrumentRunValue.getData().getValue());
//    }
  }
}













