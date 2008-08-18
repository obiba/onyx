package org.obiba.onyx.jade.instrument.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.util.data.Data;

public class InstrumentExecutionServiceImpl extends PersistenceManagerAwareService implements InstrumentExecutionService {
  
  private InstrumentRun instrumentRun;
    
  public String getInstrumentOperator() {
    return (instrumentRun.getUser().getName()); 
  }

  public Participant getParticipant() {
    return (instrumentRun.getParticipantInterview().getParticipant());
  }

  public Map<String, Data> getInputParametersValue(String... parameters) {
    Map<String, Data> inputParametersValue = new HashMap<String, Data>();
    
    for (String parameter : parameters) {
      InstrumentInputParameter inputParameterTemplate = new InstrumentInputParameter();
      inputParameterTemplate.setName(parameter);
      InstrumentRunValue inputParameterValueTemplate = new InstrumentRunValue();
      inputParameterValueTemplate.setInstrumentParameter(getPersistenceManager().matchOne(inputParameterTemplate));
      inputParameterValueTemplate.setInstrumentRun(instrumentRun);
      
      InstrumentRunValue inputParameterValue = getPersistenceManager().matchOne(inputParameterValueTemplate);
      inputParametersValue.put(inputParameterValue.getInstrumentParameter().getName(), inputParameterValue.getData());
    }
    return (inputParametersValue);
  }  

  public void addOutputParameterValues(Map<String, Data> values) {
    for(String keyStr : values.keySet()) {
      addOutputParameterValue(keyStr, values.get(keyStr));
    }
  }

  public void addOutputParameterValue(String name, Data value) {
    InstrumentOutputParameter template = new InstrumentOutputParameter();
    template.setName(name);
    template.setInstrument(instrumentRun.getInstrument());
    InstrumentOutputParameter instrumentOutputParameter = getPersistenceManager().matchOne(template);
    
    InstrumentRunValue outputParameterValue = new InstrumentRunValue();
    outputParameterValue.setInstrumentParameter(instrumentOutputParameter);
    outputParameterValue.setInstrumentRun(instrumentRun);
    
    if(getPersistenceManager().matchOne(outputParameterValue) != null) {
      outputParameterValue = getPersistenceManager().matchOne(outputParameterValue);
    }
    
    outputParameterValue.setData(value);

    getPersistenceManager().save(outputParameterValue);
  }

  public void updateInstrumentRunState(Object state) {
    instrumentRun.setState(state.toString());
    instrumentRun = getPersistenceManager().save(instrumentRun);
  }

  public void setInstrumentRun(Serializable id) {
    this.instrumentRun = getPersistenceManager().get(InstrumentRun.class, id);
  }
  
  public InstrumentRun getInstrumentRun() {
    return (instrumentRun);
  }
  
}
