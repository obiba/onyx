package org.obiba.onyx.jade.instrument.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockInstrumentExecutionService implements InstrumentExecutionService {

  private static final Logger log = LoggerFactory.getLogger(MockInstrumentExecutionService.class);

  private Participant participant;

  // Used to create bogus values
  private int value;

  public MockInstrumentExecutionService() {
    participant = new Participant().setBirthdate(new Date()).setCode("5487543534").setGender(Gender.FEMALE);
  }

  public void addOutputParameterValue(String name, Data value) {
    log.info("Received output parameter value '{}' -> '{}'", name, value.getValue());
  }

  public void addOutputParameterValues(Map<String, Data> values) {
    for(String key : values.keySet()) {
      addOutputParameterValue(key, values.get(key));
    }
  }

  public Map<String, Data> getInputParametersValue(String... parameters) {
    if(parameters != null) {
      Map<String, Data> values = new HashMap<String, Data>();
      for(String parameter : parameters) {
        values.put(parameter, new Data(DataType.INTEGER, value++));
      }
      return values;
    }
    return null;
  }

  public String getInstrumentOperator() {
    return "Gertrude Tremblay";
  }

  public Participant getParticipant() {
    return participant;
  }

  public void updateInstrumentState(Object state) {
    // ignore
  }

}
