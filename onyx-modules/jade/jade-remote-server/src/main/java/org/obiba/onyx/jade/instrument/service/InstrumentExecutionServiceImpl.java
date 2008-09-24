package org.obiba.onyx.jade.instrument.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
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
    return getInstrumentRun().getUser().getFullName();
  }
  
  public String getParticipantFirstName() {
    return (getInstrumentRun().getParticipantInterview().getParticipant().getFirstName());
  }

  public String getParticipantLastName() {
    return (getInstrumentRun().getParticipantInterview().getParticipant().getLastName());
  }
  
  public Date getParticipantBirthDate() {
    return (getInstrumentRun().getParticipantInterview().getParticipant().getBirthDate());
  }

  public String getParticipantGender() {
    return (getInstrumentRun().getParticipantInterview().getParticipant().getGender().toString());
  }

  public String getParticipantID() {
    return (getInstrumentRun().getParticipantInterview().getParticipant().getBarcode());
  }

  public Map<String, Data> getInputParametersValue(String... parameters) {
    Map<String, Data> inputParametersValue = new HashMap<String, Data>();
    for(String parameterName : parameters) {
      InstrumentRunValue inputParameterValue = activeInstrumentRunService.getInputInstrumentRunValue(parameterName);
      inputParametersValue.put(inputParameterValue.getInstrumentParameter().getName(), inputParameterValue.getData());
    }
    log.info("getInputParametersValue(" + parameters + ")=" + inputParametersValue);
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
    InstrumentRunValue outputParameterValue = activeInstrumentRunService.getOutputInstrumentRunValue(name);
    outputParameterValue.setData(value);
    getPersistenceManager().save(outputParameterValue);
  }

  public void instrumentRunnerError(Exception error) {
    InstrumentRun run = getInstrumentRun();
    
    // Convert error Stack trace to a String
    Writer result = new StringWriter();
    PrintWriter printWriter = new PrintWriter(result);
    error.printStackTrace(printWriter);
   
    run.setRunnerError(result.toString());
    run.setStatus(InstrumentRunStatus.IN_ERROR);
    getPersistenceManager().save(run);
  }

  




}
