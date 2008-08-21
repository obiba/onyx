package org.obiba.onyx.jade.core.service.impl;

import java.io.Serializable;
import java.util.Date;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentComputedOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameterAlgorithm;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
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

  private InstrumentRun currentRun = null;

  public InstrumentRun start(Participant participant, Instrument instrument) {
    if(currentRun != null) {
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

    currentRun = new InstrumentRun();
    currentRun.setParticipantInterview(participantInterview);
    currentRun.setInstrument(instrument);
    currentRun.setStatus(InstrumentRunStatus.IN_PROGRESS);
    currentRun.setTimeStart(new Date());

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

  private void end(InstrumentRunStatus status) {
    if(currentRun == null) return;

    currentRun.setStatus(status);
    currentRun.setTimeEnd(new Date());

    getPersistenceManager().save(currentRun);

    for(InstrumentRunValue value : currentRun.getInstrumentRunValues()) {
      getPersistenceManager().save(value);
    }
  }

  public InstrumentRun getInstrumentRun() {
    return currentRun;
  }

  public InstrumentRun refresh() {
    if(currentRun == null) return null;
    return getPersistenceManager().refresh(currentRun);
  }

  public Participant getParticipant() {
    if(currentRun == null) return null;

    return currentRun.getParticipantInterview().getParticipant();
  }

  public void reset() {
    currentRun = null;
  }

  public void validate() {
    if(currentRun == null) return;

    getPersistenceManager().save(currentRun);

    for(InstrumentRunValue value : currentRun.getInstrumentRunValues()) {
      getPersistenceManager().save(value);
    }
  }

  public void computeOutputParameters() {
    if(currentRun == null) return;

    InstrumentOutputParameter template = new InstrumentOutputParameter();
    template.setInstrument(currentRun.getInstrument());
    template.setCaptureMethod(InstrumentParameterCaptureMethod.COMPUTED);

    // TODO quick and dirty implementation, to be checked
    for(InstrumentOutputParameter param : getPersistenceManager().match(template)) {
      InstrumentComputedOutputParameter computedParam = (InstrumentComputedOutputParameter) param;
      if(computedParam.getAlgorithm().equals(InstrumentOutputParameterAlgorithm.AVERAGE)) {
        InstrumentRunValue computedRunValue = currentRun.getInstrumentRunValue(computedParam);
        if(computedRunValue == null) {
          computedRunValue = new InstrumentRunValue();
          computedRunValue.setInstrumentParameter(computedParam);
          computedRunValue.setCaptureMethod(InstrumentParameterCaptureMethod.AUTOMATIC);
          currentRun.addInstrumentRunValue(computedRunValue);
        }

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
        if(computedRunValue.getDataType().equals(DataType.DECIMAL)) avgValue = avg;
        else if(computedRunValue.getDataType().equals(DataType.INTEGER)) avgValue = Math.round(avg);

        if(avgValue != null) computedRunValue.setData(new Data(computedRunValue.getDataType(), avgValue));

      }
    }
  }

  public InstrumentRunValue getOutputInstrumentRunValue(String parameterName) {
    if(currentRun == null) return null;

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
      // refresh
      currentRun = getPersistenceManager().get(InstrumentRun.class, currentRun.getId());
    }

    return outputParameterValue;
  }

  @Override
  public InstrumentRunValue getInputInstrumentRunValue(String parameterName) {
    if(currentRun == null) return null;
    
    InstrumentInputParameter instrumentInputParameter = new InstrumentInputParameter();
    instrumentInputParameter.setName(parameterName);
    instrumentInputParameter.setInstrument(currentRun.getInstrument());
    instrumentInputParameter = getPersistenceManager().matchOne(instrumentInputParameter);
    
    if (instrumentInputParameter == null) {
      throw new IllegalArgumentException("No such input parameter name for instrument " + currentRun.getInstrument().getName() + " :" + parameterName);
    }
    
    InstrumentRunValue valueTemplate = new InstrumentRunValue();
    valueTemplate.setInstrumentParameter(instrumentInputParameter);
    valueTemplate.setInstrumentRun(currentRun);

    InstrumentRunValue inputParameterValue = getPersistenceManager().matchOne(valueTemplate);
    
    if(inputParameterValue == null) {
      valueTemplate.setCaptureMethod(instrumentInputParameter.getCaptureMethod());
      inputParameterValue = getPersistenceManager().save(valueTemplate);
      // refresh
      currentRun = getPersistenceManager().get(InstrumentRun.class, currentRun.getId());
    }
    
    return inputParameterValue;
  }

}
