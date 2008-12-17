/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.contraindication.Contraindication.Type;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.UserSessionService;
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
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultActiveInstrumentRunServiceImpl extends PersistenceManagerAwareService implements ActiveInstrumentRunService {

  private static final Logger log = LoggerFactory.getLogger(DefaultActiveInstrumentRunServiceImpl.class);

  private UserSessionService userSessionService;

  private Serializable currentRunId = null;

  public InstrumentRun start(Participant participant, InstrumentType instrumentType) {
    if(participant == null) throw new IllegalArgumentException("participant cannot be null");
    if(instrumentType == null) throw new IllegalArgumentException("instrumentType cannot be null");

    if(currentRunId != null) {
      InstrumentRun currentRun = getInstrumentRun();
      if(currentRun.getStatus() == InstrumentRunStatus.IN_PROGRESS) {
        cancel();
      }
      currentRun = null;
    }

    InstrumentRun currentRun = new InstrumentRun();
    currentRun.setParticipant(participant);
    currentRun.setInstrumentType(instrumentType);
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

    log.debug("InstrumentRun id={} is ending with status {}", getInstrumentRun().getId(), currentRun.getStatus());
    getPersistenceManager().save(currentRun);
  }

  private void end(InstrumentRunStatus status) {
    if(status == null) throw new IllegalArgumentException("status cannot be null");
    if(currentRunId == null) return;

    InstrumentRun currentRun = getInstrumentRun();
    currentRun.setTimeEnd(new Date());
    currentRun.setStatus(status);

    log.debug("InstrumentRun id={} is ending with status {}", getInstrumentRun().getId(), status);
    getPersistenceManager().save(currentRun);
  }

  public InstrumentRun getInstrumentRun() {
    if(currentRunId == null) return null;

    return getPersistenceManager().get(InstrumentRun.class, currentRunId);
  }

  public Participant getParticipant() {
    if(currentRunId == null) return null;

    return getInstrumentRun().getParticipant();
  }

  public void reset() {
    currentRunId = null;
  }

  public void persistRun() {
    log.info("ActiveInstrumentRunService is persisting InstrumentRun");
    getPersistenceManager().save(getInstrumentRun());
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
    template.setInstrumentType(currentRun.getInstrumentType());
    template.setCaptureMethod(InstrumentParameterCaptureMethod.COMPUTED);

    List<InstrumentComputedOutputParameter> dependentComputedParameters = new ArrayList<InstrumentComputedOutputParameter>();

    // TODO quick and dirty implementation, to be checked
    for(InstrumentOutputParameter param : getPersistenceManager().match(template)) {
      InstrumentComputedOutputParameter computedParam = (InstrumentComputedOutputParameter) param;

      if(isReadyToCompute(currentRun, computedParam)) {
        // First, compute the parameters not depend on other computed parameters
        calculateAndPersistValue(currentRun, computedParam);
      } else {
        dependentComputedParameters.add(computedParam);
      }
    }
    // Then, compute the parameters that depend on other computed parameters
    short maxRecur = 3; // Maximum dependence level
    while(!dependentComputedParameters.isEmpty() && maxRecur > 0) {

      List<InstrumentComputedOutputParameter> parameters = new ArrayList<InstrumentComputedOutputParameter>();
      for(InstrumentComputedOutputParameter computedParam : dependentComputedParameters) {
        if(isReadyToCompute(currentRun, computedParam)) {
          calculateAndPersistValue(currentRun, computedParam);
        } else {
          parameters.add(computedParam);
        }
      }
      dependentComputedParameters = parameters;
      maxRecur--;
    }
    if(!dependentComputedParameters.isEmpty()) {
      log.warn("Computed Parameters depend on others not ready to calculate. Instrument: {}", currentRun.getInstrumentType().getName());
    }
  }

  /**
   * 
   * @param currentRun
   * @param computedParam
   * @return
   */
  private boolean isReadyToCompute(InstrumentRun currentRun, InstrumentComputedOutputParameter computedParam) {
    for(InstrumentOutputParameter p : computedParam.getInstrumentOutputParameters()) {
      if(p.getCaptureMethod().equals(InstrumentParameterCaptureMethod.COMPUTED) && currentRun.getInstrumentRunValue(p) == null) {
        return false;
      }
    }
    return true;
  }

  /**
   * 
   * @param currentRun
   * @param computedParam
   */
  private void calculateAndPersistValue(InstrumentRun currentRun, InstrumentComputedOutputParameter computedParam) {
    InstrumentRunValue computedRunValue = null;

    if(computedParam.getAlgorithm().equals(InstrumentOutputParameterAlgorithm.AVERAGE)) {
      computedRunValue = calculateAverageValue(currentRun, computedParam);
    } else if(computedParam.getAlgorithm().equals(InstrumentOutputParameterAlgorithm.RATIO)) {
      computedRunValue = calculateRatioValue(currentRun, computedParam);
    }
    if(computedRunValue != null) {
      getPersistenceManager().save(computedRunValue);
      currentRun.addInstrumentRunValue(computedRunValue);
    }
  }

  /**
   * @param currentRun
   * @param computedParam
   */
  private InstrumentRunValue calculateAverageValue(InstrumentRun currentRun, InstrumentComputedOutputParameter computedParam) {
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

    return computedRunValue;
  }

  /**
   * @param currentRun
   * @param computedParam
   */
  private InstrumentRunValue calculateRatioValue(InstrumentRun currentRun, InstrumentComputedOutputParameter computedParam) {
    InstrumentRunValue computedRunValue = getOutputInstrumentRunValue(computedParam.getName());

    List<InstrumentOutputParameter> parameters = computedParam.getInstrumentOutputParameters();
    if(parameters == null || parameters.size() != 2) {
      throw new IllegalArgumentException("The number of parameters provided is invalid, two parameters are needed.");
    }

    InstrumentRunValue runValue0 = currentRun.getInstrumentRunValue(parameters.get(0));
    InstrumentRunValue runValue1 = currentRun.getInstrumentRunValue(parameters.get(1));
    Double value0 = 0.0;
    Double value1 = 0.0;

    if(runValue0.getDataType().equals(DataType.DECIMAL)) {
      value0 = runValue0.getValue();
    } else if(runValue0.getDataType().equals(DataType.INTEGER)) {
      Long valueInt = runValue0.getValue();
      value0 = Double.valueOf(valueInt);
    }
    if(runValue1.getDataType().equals(DataType.DECIMAL)) {
      value1 = runValue1.getValue();
    } else if(runValue1.getDataType().equals(DataType.INTEGER)) {
      Long valueInt = runValue1.getValue();
      value1 = Double.valueOf(valueInt);
    }

    if(value1 == 0.0) {
      throw new IllegalArgumentException("The second parameter must not be zero in Ratio parameter calculation.");
    }
    double ratio = value0 / value1;

    Serializable ratioValue = null;
    if(computedRunValue.getDataType().equals(DataType.DECIMAL)) {
      long ratioInt = Math.round(ratio * 100);
      ratioValue = (double) ratioInt / 100;
    } else if(computedRunValue.getDataType().equals(DataType.INTEGER)) {
      ratioValue = Math.round(ratio);
    }

    if(ratioValue != null) {
      computedRunValue.setData(new Data(computedRunValue.getDataType(), ratioValue));
    }

    return computedRunValue;
  }

  public InstrumentRunValue getOutputInstrumentRunValue(String parameterName) {
    if(currentRunId == null) return null;

    InstrumentRun currentRun = getInstrumentRun();

    InstrumentOutputParameter instrumentOutputParameter = new InstrumentOutputParameter();
    instrumentOutputParameter.setName(parameterName);
    instrumentOutputParameter.setInstrumentType(currentRun.getInstrumentType());
    instrumentOutputParameter = getPersistenceManager().matchOne(instrumentOutputParameter);

    if(instrumentOutputParameter == null) {
      throw new IllegalArgumentException("No such output parameter name for instrument " + currentRun.getInstrumentType().getName() + " :" + parameterName);
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
    instrumentInputParameter.setInstrumentType(currentRun.getInstrumentType());
    instrumentInputParameter = getPersistenceManager().matchOne(instrumentInputParameter);

    if(instrumentInputParameter == null) {
      throw new IllegalArgumentException("No such input parameter name for instrument " + currentRun.getInstrumentType().getName() + " :" + parameterName);
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
    instrumentInterpretativeParameter.setInstrumentType(currentRun.getInstrumentType());
    instrumentInterpretativeParameter = getPersistenceManager().matchOne(instrumentInterpretativeParameter);

    if(instrumentInterpretativeParameter == null) {
      throw new IllegalArgumentException("No such interpretative parameter name for instrument " + currentRun.getInstrumentType().getName() + " :" + parameterName);
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
    return getInstrumentRun().getInstrumentType();
  }

  public void setInstrument(Instrument instrument) {
    getInstrumentRun().setInstrument(instrument);
  }

  public Contraindication getContraindication() {
    return getInstrumentRun().getContraindication();
  }

  public boolean hasContraindications(Type type) {
    return getInstrumentRun().hasContraindications(type);
  }

  public InstrumentRunStatus getInstrumentRunStatus() {
    return getInstrumentRun().getStatus();
  }

  public void setInstrumentRunStatus(InstrumentRunStatus status) {
    InstrumentRun currentRun = getInstrumentRun();

    currentRun.setStatus(status);
    getPersistenceManager().save(currentRun);
  }

  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }

  public Instrument getInstrument() {
    return getInstrumentRun().getInstrument();
  }

}
