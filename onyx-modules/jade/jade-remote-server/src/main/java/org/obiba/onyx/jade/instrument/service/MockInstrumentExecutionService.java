package org.obiba.onyx.jade.instrument.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

public class MockInstrumentExecutionService implements InstrumentExecutionService {

  private Participant participant;

  // Used to create bogus values
  private int value;

  public MockInstrumentExecutionService() {
    participant = new Participant().setBirthdate(new Date()).setCode("5487543534").setGender(Gender.FEMALE);
  }

  public void addOutputParameterValue(String name, Data value) {
  }

  public void addOutputParameterValues(Map<String, Data> values) {
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
