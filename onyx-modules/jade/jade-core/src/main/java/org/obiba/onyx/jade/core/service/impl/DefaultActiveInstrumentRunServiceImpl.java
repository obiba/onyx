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
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentComputedOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameterAlgorithm;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantInteractionType;
import org.obiba.onyx.jade.core.domain.instrument.UnitParameterValueConverter;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheckType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InputDataSourceVisitor;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultActiveInstrumentRunServiceImpl extends PersistenceManagerAwareService implements ActiveInstrumentRunService {

  private static final Logger log = LoggerFactory.getLogger(DefaultActiveInstrumentRunServiceImpl.class);

  private InstrumentService instrumentService;

  private UserSessionService userSessionService;

  private InputDataSourceVisitor inputDataSourceVisitor;

  private Serializable currentRunId = null;

  //
  // ActiveInstrumentRunService Methods
  //

  public InstrumentRun start(Participant participant, InstrumentType instrumentType) {
    if(participant == null) throw new IllegalArgumentException("Participant cannot be null.");
    if(instrumentType == null) throw new IllegalArgumentException("Instrument type cannot be null.");

    InstrumentRun currentRun = new InstrumentRun();
    currentRun.setParticipant(participant);
    currentRun.setInstrumentType(instrumentType.getName());
    currentRun.setStatus(InstrumentRunStatus.IN_PROGRESS);
    currentRun.setTimeStart(new Date());
    currentRun.setUser(userSessionService.getUser());
    getPersistenceManager().save(currentRun);
    currentRunId = currentRun.getId();

    return currentRun;
  }

  public void reset() {
    currentRunId = null;
  }

  public void end() {
    if(currentRunId == null) return;

    InstrumentRun currentRun = getInstrumentRun();
    currentRun.setTimeEnd(new Date());

    log.debug("InstrumentRun id={} is ending with status {}", getInstrumentRun().getId(), currentRun.getStatus());
    getPersistenceManager().save(currentRun);
  }

  public Participant getParticipant() {
    if(currentRunId == null) return null;

    return getInstrumentRun().getParticipant();
  }

  public InstrumentType getInstrumentType() {
    String instrumentTypeName = getInstrumentRun().getInstrumentType();

    return instrumentService.getInstrumentType(instrumentTypeName);
  }

  public void setInstrument(Instrument instrument) {
    getInstrumentRun().setInstrument(instrument);
  }

  public Instrument getInstrument() {
    return getInstrumentRun().getInstrument();
  }

  public InstrumentParameter getParameterByCode(String code) {
    return instrumentService.getParameterByCode(getInstrumentType(), code);
  }

  public InstrumentParameter getParameterByVendorName(String vendorName) {
    if(vendorName == null) throw new IllegalArgumentException("name cannot be null.");
    for(InstrumentParameter parameter : getInstrumentType().getInstrumentParameters()) {
      if(vendorName.equals(parameter.getVendorName()) == true) {
        return parameter;
      }
    }
    return null;
  }

  public boolean hasInterpretativeParameter(ParticipantInteractionType type) {
    return !getInterpretativeParameters(type).isEmpty();
  }

  public List<InterpretativeParameter> getInterpretativeParameters(ParticipantInteractionType type) {
    List<InterpretativeParameter> interpretativeParameters = new ArrayList<InterpretativeParameter>();

    InstrumentType instrumentType = getInstrumentType();

    for(InstrumentParameter parameter : instrumentType.getInstrumentParameters()) {
      if(parameter instanceof InterpretativeParameter) {
        InterpretativeParameter interpretativeParameter = (InterpretativeParameter) parameter;

        if(type == null || interpretativeParameter.getType().equals(type)) {
          interpretativeParameters.add(interpretativeParameter);
        }
      }
    }

    return interpretativeParameters;
  }

  public boolean hasInterpretativeParameter() {
    return !getInterpretativeParameters(null).isEmpty();
  }

  public List<InterpretativeParameter> getInterpretativeParameters() {
    return getInterpretativeParameters(null);
  }

  public boolean hasInputParameter(boolean readOnly) {
    return !getInputParameters(readOnly).isEmpty();
  }

  public List<InstrumentInputParameter> getInputParameters(boolean readOnly) {
    return instrumentService.getInstrumentInputParameter(getInstrumentType(), readOnly);
  }

  public boolean hasInputParameter(InstrumentParameterCaptureMethod captureMethod) {
    return !getInputParameters(captureMethod).isEmpty();
  }

  public List<InstrumentInputParameter> getInputParameters(InstrumentParameterCaptureMethod captureMethod) {
    List<InstrumentInputParameter> inputParameters = new ArrayList<InstrumentInputParameter>();

    InstrumentType instrumentType = getInstrumentType();

    for(InstrumentParameter parameter : instrumentType.getInstrumentParameters()) {
      if(parameter instanceof InstrumentInputParameter) {
        InstrumentInputParameter inputParameter = (InstrumentInputParameter) parameter;
        InstrumentParameterCaptureMethod inputParameterCaptureMethod = inputParameter.getCaptureMethod();

        if(inputParameterCaptureMethod.equals(captureMethod)) {
          inputParameters.add(inputParameter);
        }
      }
    }

    return inputParameters;
  }

  public boolean hasInputParameter() {
    return !getInputParameters().isEmpty();
  }

  public List<InstrumentInputParameter> getInputParameters() {
    List<InstrumentInputParameter> inputParameters = new ArrayList<InstrumentInputParameter>();

    InstrumentType instrumentType = getInstrumentType();

    for(InstrumentParameter parameter : instrumentType.getInstrumentParameters()) {
      if(parameter instanceof InstrumentInputParameter) {
        InstrumentInputParameter inputParameter = (InstrumentInputParameter) parameter;
        inputParameters.add(inputParameter);
      }
    }

    return inputParameters;
  }

  public boolean hasOutputParameter(InstrumentParameterCaptureMethod captureMethod) {
    return !getOutputParameters(captureMethod).isEmpty();
  }

  public List<InstrumentOutputParameter> getOutputParameters(InstrumentParameterCaptureMethod captureMethod) {
    return instrumentService.getOutputParameters(getInstrumentType(), captureMethod);
  }

  public boolean hasOutputParameter(boolean automatic) {
    return !getOutputParameters(automatic).isEmpty();
  }

  public List<InstrumentOutputParameter> getOutputParameters(boolean automatic) {
    List<InstrumentOutputParameter> outputParameters = new ArrayList<InstrumentOutputParameter>();

    if(automatic) {
      outputParameters = getOutputParameters(InstrumentParameterCaptureMethod.AUTOMATIC);
    } else {
      for(InstrumentParameterCaptureMethod captureMethod : InstrumentParameterCaptureMethod.values()) {
        if(!captureMethod.equals(InstrumentParameterCaptureMethod.AUTOMATIC)) {
          outputParameters.addAll(getOutputParameters(captureMethod));
        }
      }
    }

    return outputParameters;
  }

  public boolean hasOutputParameter() {
    return !getOutputParameters().isEmpty();
  }

  public List<InstrumentOutputParameter> getOutputParameters() {
    List<InstrumentOutputParameter> outputParameters = new ArrayList<InstrumentOutputParameter>();

    InstrumentType instrumentType = getInstrumentType();

    for(InstrumentParameter parameter : instrumentType.getInstrumentParameters()) {
      if(parameter instanceof InstrumentOutputParameter) {
        InstrumentOutputParameter outputParameter = (InstrumentOutputParameter) parameter;
        outputParameters.add(outputParameter);
      }
    }

    return outputParameters;
  }

  public boolean hasParameterWithWarning() {
    return !getParametersWithWarning().isEmpty();
  }

  public List<InstrumentOutputParameter> getParametersWithWarning() {
    List<InstrumentOutputParameter> paramsWithWarnings = new ArrayList<InstrumentOutputParameter>();

    InstrumentType instrumentType = getInstrumentType();

    for(InstrumentParameter parameter : instrumentType.getInstrumentParameters()) {
      if(parameter instanceof InstrumentOutputParameter) {
        InstrumentOutputParameter outputParameter = (InstrumentOutputParameter) parameter;

        // Don't include parameters with a non-MANUAL capture method.
        if(!outputParameter.getCaptureMethod().equals(InstrumentParameterCaptureMethod.MANUAL)) {
          continue;
        }

        InstrumentRunValue runValue = getInstrumentRunValue(outputParameter);

        // Don't include parameters that haven't been assigned a value.
        if(runValue == null || runValue.getData(outputParameter.getDataType()) == null || runValue.getData(outputParameter.getDataType()).getValue() == null) {
          continue;
        }

        for(IntegrityCheck check : outputParameter.getIntegrityChecks()) {
          // Skip non-warning checks.
          if(!check.getType().equals(IntegrityCheckType.WARNING)) {
            continue;
          }

          if(!check.checkParameterValue(runValue.getData(outputParameter.getDataType()), null, this)) {
            paramsWithWarnings.add(outputParameter);
          }
        }
      }
    }

    return paramsWithWarnings;
  }

  public void setInstrumentRun(InstrumentRun instrumentRun) {
    if(instrumentRun != null) {
      currentRunId = instrumentRun.getId();
    }
  }

  public InstrumentRun getInstrumentRun() {
    log.debug("currentRunId={}", currentRunId);
    if(currentRunId == null) return null;

    return getPersistenceManager().get(InstrumentRun.class, currentRunId);
  }

  public void persistRun() {
    log.debug("ActiveInstrumentRunService is persisting InstrumentRun");
    getPersistenceManager().save(getInstrumentRun());
  }

  public void setInstrumentRunStatus(InstrumentRunStatus status) {
    InstrumentRun currentRun = getInstrumentRun();

    currentRun.setStatus(status);
    getPersistenceManager().save(currentRun);
  }

  public InstrumentRunStatus getInstrumentRunStatus() {
    return getInstrumentRun().getStatus();
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

    List<InstrumentComputedOutputParameter> dependentComputedParameters = new ArrayList<InstrumentComputedOutputParameter>();

    // TODO quick and dirty implementation, to be checked
    for(InstrumentOutputParameter param : getOutputParameters(InstrumentParameterCaptureMethod.COMPUTED)) {
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
      log.warn("Computed Parameters depend on others not ready to calculate. Instrument: {}", currentRun.getInstrumentType());
    }
  }

  public InstrumentRunValue getOutputInstrumentRunValue(String parameterCode) {
    if(currentRunId == null) return null;

    InstrumentRun currentRun = getInstrumentRun();

    InstrumentParameter instrumentOutputParameter = getParameterByCode(parameterCode);

    if(instrumentOutputParameter == null) {
      throw new IllegalArgumentException("No such output parameter code for instrument " + currentRun.getInstrumentType() + " :" + parameterCode);
    }

    return getInstrumentRunValue(instrumentOutputParameter);
  }

  public InstrumentRunValue getOutputInstrumentRunValueByVendorName(String parameterVendorName) {
    if(currentRunId == null) return null;

    InstrumentRun currentRun = getInstrumentRun();

    InstrumentParameter instrumentOutputParameter = getParameterByVendorName(parameterVendorName);

    if(instrumentOutputParameter == null) {
      throw new IllegalArgumentException("No such output parameter vendor name for instrument " + currentRun.getInstrumentType() + " :" + parameterVendorName);
    }

    return getInstrumentRunValue(instrumentOutputParameter);
  }

  public InstrumentRunValue getInputInstrumentRunValue(String parameterCode) {
    if(currentRunId == null) return null;

    InstrumentRun currentRun = getInstrumentRun();

    InstrumentParameter instrumentInputParameter = getParameterByCode(parameterCode);

    if(instrumentInputParameter == null) {
      throw new IllegalArgumentException("No such input parameter code for instrument " + currentRun.getInstrumentType() + " :" + parameterCode);
    }

    return getInstrumentRunValue(instrumentInputParameter);
  }

  public InstrumentRunValue getInterpretativeInstrumentRunValue(String parameterCode) {
    if(currentRunId == null) return null;

    InstrumentRun currentRun = getInstrumentRun();

    InstrumentParameter instrumentInterpretativeParameter = getParameterByCode(parameterCode);

    if(instrumentInterpretativeParameter == null) {
      throw new IllegalArgumentException("No such interpretative parameter code for instrument " + currentRun.getInstrumentType() + " :" + parameterCode);
    }

    return getInstrumentRunValue(instrumentInterpretativeParameter);
  }

  public InstrumentRunValue getInstrumentRunValue(InstrumentParameter parameter) {
    if(parameter == null) throw new IllegalArgumentException("Cannot retrieve a run value from a null instrument parameter.");
    InstrumentRun instrumentRun = getInstrumentRun();
    if(instrumentRun == null) throw new IllegalArgumentException("Cannot retrieve a run value from a null instrument run.");

    InstrumentRunValue valueTemplate = new InstrumentRunValue();
    valueTemplate.setInstrumentParameter(parameter.getCode());
    valueTemplate.setInstrumentRun(instrumentRun);

    InstrumentRunValue parameterValue = getPersistenceManager().matchOne(valueTemplate);

    if(parameterValue == null) {
      valueTemplate.setCaptureMethod(parameter.getCaptureMethod());
      parameterValue = getPersistenceManager().save(valueTemplate);
    }

    return parameterValue;
  }

  public String updateReadOnlyInputParameterRunValue() {

    // get the data from not read-only input parameters sources
    for(InstrumentInputParameter param : getInputParameters(true)) {
      final InstrumentRunValue runValue = getInstrumentRunValue(param);
      Data data = inputDataSourceVisitor.getData(getParticipant(), param);
      if(data != null) {
        if(!data.getType().equals(param.getDataType())) {
          UnitParameterValueConverter converter = new UnitParameterValueConverter();
          converter.convert(param, runValue, data);
        } else {
          runValue.setData(data);
        }
        update(runValue);
      } else {
        log.error("The value for instrument parameter {} comes from an InputSource, but this source has not produced a value. Please correct stage dependencies or your instrument-descriptor.xml file for this instrument.", param.getCode());
        return ("An unexpected problem occurred while setting up this instrument's run. Please contact support.");
      }
    }
    return null;

  }

  //
  // IContraindicatable Methods
  //

  public List<Contraindication> getContraindications(Contraindication.Type type) {
    List<Contraindication> contraindications = new ArrayList<Contraindication>();

    for(Contraindication contraindication : getInstrumentType().getContraindications()) {
      if(contraindication.getType().equals(type)) {
        contraindications.add(contraindication);
      }
    }

    return contraindications;
  }

  public boolean hasContraindications(Contraindication.Type type) {
    return !getContraindications(type).isEmpty();
  }

  public Contraindication getContraindication() {
    InstrumentType instrumentType = getInstrumentType();
    InstrumentRun instrumentRun = getInstrumentRun();

    for(Contraindication ci : instrumentType.getContraindications()) {
      if(ci.getCode().equals(instrumentRun.getContraindication())) return ci;
    }
    return null;
  }

  public boolean isContraindicated() {
    return getInstrumentRun().getContraindication() != null;
  }

  public void setContraindication(Contraindication contraindication) {
    getInstrumentRun().setContraindication(contraindication);
  }

  public void setOtherContraindication(String other) {
    getInstrumentRun().setOtherContraindication(other);
  }

  public String getOtherContraindication() {
    return getInstrumentRun().getOtherContraindication();
  }

  //
  // Methods
  //

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }

  public void setInputDataSourceVisitor(InputDataSourceVisitor inputDataSourceVisitor) {
    this.inputDataSourceVisitor = inputDataSourceVisitor;
  }

  /**
   * 
   * @param currentRun
   * @param computedParam
   * @return
   */
  private boolean isReadyToCompute(InstrumentRun currentRun, InstrumentComputedOutputParameter computedParam) {
    for(InstrumentParameter p : computedParam.getInstrumentParameters()) {
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
    } else if(computedParam.getAlgorithm().equals(InstrumentOutputParameterAlgorithm.DIFFERENCE)) {
      computedRunValue = calculateDifferenceValue(currentRun, computedParam);
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
    InstrumentRunValue computedRunValue = getOutputInstrumentRunValue(computedParam.getCode());

    double sum = 0;
    int count = 0;
    for(InstrumentParameter p : computedParam.getInstrumentParameters()) {
      count++;
      InstrumentRunValue runValue = currentRun.getInstrumentRunValue(p);
      if(p.getDataType().equals(DataType.DECIMAL)) {
        Double value = runValue.getValue(p.getDataType());
        sum += value;
      } else if(p.getDataType().equals(DataType.INTEGER)) {
        Long value = runValue.getValue(p.getDataType());
        sum += value.doubleValue();
      }
    }
    double avg = sum / count;

    Serializable avgValue = null;
    if(computedParam.getDataType().equals(DataType.DECIMAL)) {
      long avgInt = Math.round(avg * 100);
      avgValue = (double) avgInt / 100;
    } else if(computedParam.getDataType().equals(DataType.INTEGER)) {
      avgValue = Math.round(avg);
    }

    if(avgValue != null) {
      computedRunValue.setData(new Data(computedParam.getDataType(), avgValue));
    }

    return computedRunValue;
  }

  /**
   * @param currentRun
   * @param computedParam
   */
  private InstrumentRunValue calculateDifferenceValue(InstrumentRun currentRun, InstrumentComputedOutputParameter computedParam) {
    InstrumentRunValue computedRunValue = getOutputInstrumentRunValue(computedParam.getCode());

    List<InstrumentParameter> parameters = computedParam.getInstrumentParameters();

    if(parameters == null || parameters.size() != 2) {
      throw new IllegalArgumentException("The number of parameters provided is invalid, two parameters are needed.");
    }

    InstrumentRunValue runValue0 = currentRun.getInstrumentRunValue(parameters.get(0));
    InstrumentRunValue runValue1 = currentRun.getInstrumentRunValue(parameters.get(1));
    Double value0 = 0.0;
    Double value1 = 0.0;

    if(parameters.get(0).getDataType().equals(DataType.DECIMAL)) {
      value0 = runValue0.getValue(DataType.DECIMAL);
    } else if(parameters.get(0).getDataType().equals(DataType.INTEGER)) {
      Long valueInt = runValue0.getValue(DataType.INTEGER);
      value0 = Double.valueOf(valueInt);
    }
    if(parameters.get(1).getDataType().equals(DataType.DECIMAL)) {
      value1 = runValue1.getValue(DataType.DECIMAL);
    } else if(parameters.get(1).getDataType().equals(DataType.INTEGER)) {
      Long valueInt = runValue1.getValue(DataType.INTEGER);
      value1 = Double.valueOf(valueInt);
    }
    double diff = value0 - value1;

    Serializable diffValue = null;
    if(computedParam.getDataType().equals(DataType.DECIMAL)) {
      long diffInt = Math.round(diff * 100);
      diffValue = (double) diffInt / 100;
    } else if(computedParam.getDataType().equals(DataType.INTEGER)) {
      diffValue = Math.round(diff);
    }

    if(diffValue != null) {
      computedRunValue.setData(new Data(computedParam.getDataType(), diffValue));
    }

    return computedRunValue;
  }

  /**
   * @param currentRun
   * @param computedParam
   */
  private InstrumentRunValue calculateRatioValue(InstrumentRun currentRun, InstrumentComputedOutputParameter computedParam) {
    InstrumentRunValue computedRunValue = getOutputInstrumentRunValue(computedParam.getCode());

    List<InstrumentParameter> parameters = computedParam.getInstrumentParameters();
    if(parameters == null || parameters.size() != 2) {
      throw new IllegalArgumentException("The number of parameters provided is invalid, two parameters are needed.");
    }

    InstrumentRunValue runValue0 = currentRun.getInstrumentRunValue(parameters.get(0));
    InstrumentRunValue runValue1 = currentRun.getInstrumentRunValue(parameters.get(1));
    Double value0 = 0.0;
    Double value1 = 0.0;

    if(parameters.get(0).getDataType().equals(DataType.DECIMAL)) {
      value0 = runValue0.getValue(DataType.DECIMAL);
    } else if(parameters.get(0).getDataType().equals(DataType.INTEGER)) {
      Long valueInt = runValue0.getValue(DataType.INTEGER);
      value0 = Double.valueOf(valueInt);
    }
    if(parameters.get(1).getDataType().equals(DataType.DECIMAL)) {
      value1 = runValue1.getValue(DataType.DECIMAL);
    } else if(parameters.get(1).getDataType().equals(DataType.INTEGER)) {
      Long valueInt = runValue1.getValue(DataType.INTEGER);
      value1 = Double.valueOf(valueInt);
    }

    if(value1 == 0.0) {
      throw new IllegalArgumentException("The second parameter must not be zero in Ratio parameter calculation.");
    }
    double ratio = value0 / value1;

    Serializable ratioValue = null;
    if(computedParam.getDataType().equals(DataType.DECIMAL)) {
      long ratioInt = Math.round(ratio * 100);
      ratioValue = (double) ratioInt / 100;
    } else if(computedParam.getDataType().equals(DataType.INTEGER)) {
      ratioValue = Math.round(ratio);
    }

    if(ratioValue != null) {
      computedRunValue.setData(new Data(computedParam.getDataType(), ratioValue));
    }

    return computedRunValue;
  }
}
