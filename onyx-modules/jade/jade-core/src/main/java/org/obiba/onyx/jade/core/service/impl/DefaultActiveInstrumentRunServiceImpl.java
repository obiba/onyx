package org.obiba.onyx.jade.core.service.impl;

import java.io.Serializable;
import java.util.Date;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentComputedOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameterAlgorithm;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultActiveInstrumentRunServiceImpl extends PersistenceManagerAwareService implements ActiveInstrumentRunService {

  private UserSessionService userSessionService;
  
  private Serializable currentRunId = null;

  private Serializable instrumentTypeId = null;

  public InstrumentRun start(Participant participant, Instrument instrument) {

    if(currentRunId != null) {
      InstrumentRun currentRun = getInstrumentRun();
      if(currentRun.getStatus().equals(InstrumentRunStatus.IN_PROGRESS)) {
        cancel();
      }
      currentRun = null;
    }

    ParticipantInterview participantInterviewTemplate = new ParticipantInterview();
    participantInterviewTemplate.setParticipant(participant);
    ParticipantInterview participantInterview = getPersistenceManager().matchOne(participantInterviewTemplate);
    if(participantInterview == null) {
      participantInterview = getPersistenceManager().save(participantInterviewTemplate);
    }

    InstrumentRun currentRun = new InstrumentRun();
    currentRun.setParticipantInterview(participantInterview);
    currentRun.setInstrument(instrument);
    currentRun.setStatus(InstrumentRunStatus.IN_PROGRESS);
    currentRun.setTimeStart(new Date());
    currentRun.setUser(userSessionService.getUser());
    getPersistenceManager().save(currentRun);
    currentRunId = currentRun.getId();

    return currentRun;
  }

  public void cancel() {
    end(InstrumentRunStatus.CANCELED);
  }

  public void fail() {
    end(InstrumentRunStatus.IN_ERROR);
  }

  public void complete() {
    end(InstrumentRunStatus.COMPLETED);
  }

  public void end() {
    if(currentRunId == null) return;

    InstrumentRun currentRun = getInstrumentRun();
    currentRun.setTimeEnd(new Date());

    getPersistenceManager().save(currentRun);
  }
  
  private void end(InstrumentRunStatus status) {
    if(currentRunId == null) return;

    InstrumentRun currentRun = getInstrumentRun();
    currentRun.setTimeEnd(new Date());
    currentRun.setStatus(status);
    
    getPersistenceManager().save(currentRun);
  }

  public InstrumentRun getInstrumentRun() {
    if(currentRunId == null) return null;

    return getPersistenceManager().get(InstrumentRun.class, currentRunId);
  }

  public Participant getParticipant() {
    if(currentRunId == null) return null;

    return getInstrumentRun().getParticipantInterview().getParticipant();
  }

  public void reset() {
    currentRunId = null;
    instrumentTypeId = null;
  }

  public void update(InstrumentRun currentRun) {
    if(currentRunId == null) return;
    if(currentRun == null) throw new IllegalArgumentException("Current instrument run cannot be null");
    if(!currentRunId.equals(currentRun.getId())) throw new IllegalArgumentException("Unexpected given current instrument run");

    getPersistenceManager().save(currentRun);
  }

  public void update(InstrumentRunValue currentRunValue) {
    if(currentRunId == null) return;
    if(currentRunValue.getInstrumentRun() == null) throw new IllegalArgumentException("Current instrument run cannot be null");
    if(!currentRunId.equals(currentRunValue.getInstrumentRun().getId())) throw new IllegalArgumentException("Unexpected given current instrument run");

    getPersistenceManager().save(currentRunValue);
  }

  public void computeOutputParameters() {
    if(currentRunId == null) return;

    InstrumentRun currentRun = getInstrumentRun();

    InstrumentOutputParameter template = new InstrumentOutputParameter();
    template.setInstrument(currentRun.getInstrument());
    template.setCaptureMethod(InstrumentParameterCaptureMethod.COMPUTED);

    // TODO quick and dirty implementation, to be checked
    for(InstrumentOutputParameter param : getPersistenceManager().match(template)) {
      InstrumentComputedOutputParameter computedParam = (InstrumentComputedOutputParameter) param;
      if(computedParam.getAlgorithm().equals(InstrumentOutputParameterAlgorithm.AVERAGE)) {
        InstrumentRunValue computedRunValue = getOutputInstrumentRunValue(computedParam.getName());

        double sum = 0;
        int count = 0;
        for(InstrumentOutputParameter p : computedParam.getInstrumentOutputParameters()) {
          count++;
          InstrumentRunValue runValue = currentRun.getInstrumentRunValue(p);
          if(runValue.getDataType().equals(DataType.DECIMAL)) {
            Double value = runValue.getValue();
            sum += value;
          } else if(runValue.getDataType().equals(DataType.INTEGER)) {
            Long value = runValue.getValue();
            sum += value.doubleValue();
          }
        }
        double avg = sum / count;

        Serializable avgValue = null;
        if(computedRunValue.getDataType().equals(DataType.DECIMAL)) {
          long avgInt = Math.round(avg * 100);
          avgValue = (double) avgInt / 100;
        } else if(computedRunValue.getDataType().equals(DataType.INTEGER)) {
          avgValue = Math.round(avg);
        }

        if(avgValue != null) {
          computedRunValue.setData(new Data(computedRunValue.getDataType(), avgValue));
        }

        getPersistenceManager().save(computedRunValue);
      }
    }
  }

  public InstrumentRunValue getOutputInstrumentRunValue(String parameterName) {
    if(currentRunId == null) return null;

    InstrumentRun currentRun = getInstrumentRun();

    InstrumentOutputParameter instrumentOutputParameter = new InstrumentOutputParameter();
    instrumentOutputParameter.setName(parameterName);
    instrumentOutputParameter.setInstrument(currentRun.getInstrument());
    instrumentOutputParameter = getPersistenceManager().matchOne(instrumentOutputParameter);

    if(instrumentOutputParameter == null) {
      throw new IllegalArgumentException("No such output parameter name for instrument " + currentRun.getInstrument().getName() + " :" + parameterName);
    }

    InstrumentRunValue valueTemplate = new InstrumentRunValue();
    valueTemplate.setInstrumentParameter(instrumentOutputParameter);
    valueTemplate.setInstrumentRun(currentRun);

    InstrumentRunValue outputParameterValue = getPersistenceManager().matchOne(valueTemplate);

    if(outputParameterValue == null) {
      valueTemplate.setCaptureMethod(instrumentOutputParameter.getCaptureMethod());
      outputParameterValue = getPersistenceManager().save(valueTemplate);
    }

    return outputParameterValue;
  }

  public InstrumentRunValue getInputInstrumentRunValue(String parameterName) {
    if(currentRunId == null) return null;

    InstrumentRun currentRun = getInstrumentRun();

    InstrumentInputParameter instrumentInputParameter = new InstrumentInputParameter();
    instrumentInputParameter.setName(parameterName);
    instrumentInputParameter.setInstrument(currentRun.getInstrument());
    instrumentInputParameter = getPersistenceManager().matchOne(instrumentInputParameter);

    if(instrumentInputParameter == null) {
      throw new IllegalArgumentException("No such input parameter name for instrument " + currentRun.getInstrument().getName() + " :" + parameterName);
    }

    InstrumentRunValue valueTemplate = new InstrumentRunValue();
    valueTemplate.setInstrumentParameter(instrumentInputParameter);
    valueTemplate.setInstrumentRun(currentRun);

    InstrumentRunValue inputParameterValue = getPersistenceManager().matchOne(valueTemplate);

    if(inputParameterValue == null) {
      valueTemplate.setCaptureMethod(instrumentInputParameter.getCaptureMethod());
      inputParameterValue = getPersistenceManager().save(valueTemplate);
    }

    return inputParameterValue;
  }

  public InstrumentRunValue getInterpretativeInstrumentRunValue(String parameterName) {
    if(currentRunId == null) return null;

    InstrumentRun currentRun = getInstrumentRun();

    InterpretativeParameter instrumentInterpretativeParameter = new InterpretativeParameter();
    instrumentInterpretativeParameter.setName(parameterName);
    instrumentInterpretativeParameter.setInstrument(currentRun.getInstrument());
    instrumentInterpretativeParameter = getPersistenceManager().matchOne(instrumentInterpretativeParameter);

    if(instrumentInterpretativeParameter == null) {
      throw new IllegalArgumentException("No such interpretative parameter name for instrument " + currentRun.getInstrument().getName() + " :" + parameterName);
    }

    InstrumentRunValue valueTemplate = new InstrumentRunValue();
    valueTemplate.setInstrumentParameter(instrumentInterpretativeParameter);
    valueTemplate.setInstrumentRun(currentRun);

    InstrumentRunValue interpretativeParameterValue = getPersistenceManager().matchOne(valueTemplate);

    if(interpretativeParameterValue == null) {
      valueTemplate.setCaptureMethod(instrumentInterpretativeParameter.getCaptureMethod());
      interpretativeParameterValue = getPersistenceManager().save(valueTemplate);
    }

    return interpretativeParameterValue;
  }

  public InstrumentType getInstrumentType() {
    return getPersistenceManager().get(InstrumentType.class, instrumentTypeId);
  }

  public void setInstrumentType(InstrumentType instrumentType) {
    this.instrumentTypeId = instrumentType.getId();
  }

  public Instrument getInstrument() {
    return getInstrumentRun().getInstrument();
  }

  public ContraIndication getContraIndication() {
    return getInstrumentRun().getContraIndication();
  }

  public void setContraIndication(ContraIndication contraIndication) {
    InstrumentRun currentRun = getInstrumentRun();

    currentRun.setContraIndication(contraIndication);
    getPersistenceManager().save(currentRun);
  }

  public String getOtherContraIndication() {
    return getInstrumentRun().getOtherContraIndication();
  }

  public void setOtherContraIndication(String otherContraIndication) {
    InstrumentRun currentRun = getInstrumentRun();

    currentRun.setOtherContraIndication(otherContraIndication);
    getPersistenceManager().save(currentRun);
  }

  public InstrumentRunStatus getInstrumentRunStatus() {
    return getInstrumentRun().getStatus();
  }

  public void setInstrumentRunStatus(InstrumentRunStatus status) {
    InstrumentRun currentRun = getInstrumentRun();

    currentRun.setStatus(status);
    getPersistenceManager().save(currentRun);
  }

  public UserSessionService getUserSessionService() {
    return userSessionService;
  }

  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }

}
