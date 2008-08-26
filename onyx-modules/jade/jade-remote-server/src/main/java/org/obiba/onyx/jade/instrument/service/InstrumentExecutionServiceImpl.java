package org.obiba.onyx.jade.instrument.service;

import java.util.HashMap;
import java.util.Map;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class InstrumentExecutionServiceImpl extends PersistenceManagerAwareService implements InstrumentExecutionService {

  private static final Logger log = LoggerFactory.getLogger(InstrumentExecutionServiceImpl.class);

  private ActiveInstrumentRunService activeInstrumentRunService;

  public void setActiveInstrumentRunService(ActiveInstrumentRunService activeInstrumentRunService) {
    this.activeInstrumentRunService = activeInstrumentRunService;
  }

  private InstrumentRun getInstrumentRun() {
    return activeInstrumentRunService.getInstrumentRun();
  }

  public String getInstrumentOperator() {
    return getInstrumentRun().getUser().getName();
  }

  public Participant getParticipant() {
    return (getInstrumentRun().getParticipantInterview().getParticipant());
  }

  public Map<String, Data> getInputParametersValue(String... parameters) {
    log.info("getInputParametersValue(" + parameters + ")");
    log.info("instrumentRun=" + getInstrumentRun());
    Map<String, Data> inputParametersValue = new HashMap<String, Data>();

    for(String parameterName : parameters) {
      InstrumentRunValue inputParameterValue = activeInstrumentRunService.getInputInstrumentRunValue(parameterName);
      inputParametersValue.put(inputParameterValue.getInstrumentParameter().getName(), inputParameterValue.getData());
    }

    return (inputParametersValue);
  }

  public Data getInputParameterValue(String parameterName) {
    return activeInstrumentRunService.getInputInstrumentRunValue(parameterName).getData();
  }  
  
  public void addOutputParameterValues(Map<String, Data> values) {
    for(Map.Entry<String, Data> entry : values.entrySet()) {
      addOutputParameterValue(entry.getKey(), entry.getValue());
    }
  }

  public void addOutputParameterValue(String name, Data value) {
    log.info("addOutputParameterValue(" + name + ", " + value + ")");
    log.info("instrumentRun=" + getInstrumentRun());
    InstrumentRunValue outputParameterValue = activeInstrumentRunService.getOutputInstrumentRunValue(name);
    outputParameterValue.setData(value);
    getPersistenceManager().save(outputParameterValue);
  }

  public void updateInstrumentRunState(Object state) {
    InstrumentRun run = getInstrumentRun();
    InstrumentRunStatus status = InstrumentRunStatus.valueOf(state.toString());
    if(status != null) {
      run.setStatus(status);
      getPersistenceManager().save(run);
    }
  }


}
