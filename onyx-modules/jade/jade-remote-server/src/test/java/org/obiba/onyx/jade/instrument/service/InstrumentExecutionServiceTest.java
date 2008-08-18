package org.obiba.onyx.jade.instrument.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.springframework.beans.factory.annotation.Autowired;

public class InstrumentExecutionServiceTest extends BaseDefaultSpringContextTestCase {
  
  @Autowired(required=true)
  PersistenceManager persistenceManager;
  
  @Autowired(required=true)
  InstrumentExecutionService instrumentExecutionService;
  
  @Test
  @Dataset
  public void getOperator() {
    System.out.println("*** InstrumentExecutionService getOperator: Testing Data");
    instrumentExecutionService.setInstrumentRun((Serializable) Long.valueOf("1"));
    Assert.assertEquals("technicien_1", instrumentExecutionService.getInstrumentOperator());
    System.out.println("*** InstrumentExecutionService getOperator: Test Done successfully");
  }
  
  @Test
  @Dataset
  public void getParticipant() {
    System.out.println("*** InstrumentExecutionService getParticipant: Testing Data");
    instrumentExecutionService.setInstrumentRun((Serializable) Long.valueOf("1"));
    Assert.assertEquals("Dupont", instrumentExecutionService.getParticipant().getLastName());
    Assert.assertEquals("Amélie", instrumentExecutionService.getParticipant().getFirstName());    
    System.out.println("*** InstrumentExecutionService getParticipant: Test Done successfully");
  }
  
  @Test
  @Dataset
  public void updateInstrumentRunState() {
    System.out.println("*** InstrumentExecutionService updateInstrumentRunState: Testing Data");
    instrumentExecutionService.setInstrumentRun((Serializable) Long.valueOf("1"));
    Object state = ("stateOk");
    instrumentExecutionService.updateInstrumentRunState(state);
    Assert.assertEquals("stateOk", instrumentExecutionService.getInstrumentRun().getState());
    System.out.println("*** InstrumentExecutionService updateInstrumentRunState: Test Done successfully");
  }
  
  @Test
  @Dataset
  public void addOutputParameterValues() {
    System.out.println("*** InstrumentExecutionService addOutputParameterValues: Testing Data");
    instrumentExecutionService.setInstrumentRun((Serializable) Long.valueOf("1"));
    Map<String, Data> valuesMap = new HashMap<String, Data>();
    valuesMap.put("density", new Data(DataType.INTEGER, Long.valueOf("125")));
    valuesMap.put("strength", new Data(DataType.INTEGER, Long.valueOf("32")));
    
    instrumentExecutionService.addOutputParameterValues(valuesMap);
    InstrumentRunValue template = new InstrumentRunValue();
    template.setInstrumentRun(instrumentExecutionService.getInstrumentRun());
    List<InstrumentRunValue> instrumentRunValues = persistenceManager.match(template);
    
    for (InstrumentRunValue instrumentRunValue : instrumentRunValues) {
      if (instrumentRunValue.getInstrumentParameter().getName().equals("density"))
        Assert.assertEquals(Long.valueOf("125"), instrumentRunValue.getData().getValue());
      if (instrumentRunValue.getInstrumentParameter().getName().equals("strength"))
        Assert.assertEquals(Long.valueOf("32"), instrumentRunValue.getData().getValue());
    }
    System.out.println("*** InstrumentExecutionService addOutputParameterValues: Test Done successfully");
  }
}













