package org.obiba.onyx.jade.instrument.service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
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
    participant = new Participant();
    participant.setBirthDate(new Date());
    participant.setBarcode("5487543534");
    participant.setGender(Gender.FEMALE);
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

  public void updateInstrumentRunState(Object state) {
    // ignore
  }
  
  public void setInstrumentRun(Serializable id) {
    //ignore
  }
  
  public InstrumentRun getInstrumentRun() {
    return (new InstrumentRun());
  }

}
