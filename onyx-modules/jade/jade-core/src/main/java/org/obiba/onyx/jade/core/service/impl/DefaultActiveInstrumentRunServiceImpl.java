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
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
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
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultActiveInstrumentRunServiceImpl extends PersistenceManagerAwareService implements ActiveInstrumentRunService {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(DefaultActiveInstrumentRunServiceImpl.class);

  //
  // Instance Variables
  //

  private InstrumentService instrumentService;

  private UserSessionService userSessionService;

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

          if(!check.checkParameterValue(outputParameter, runValue.getData(outputParameter.getDataType()), null, this)) {
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

    List<InstrumentOutputParameter> dependentComputedParameters = new ArrayList<InstrumentOutputParameter>();

    // TODO quick and dirty implementation, to be checked
    for(InstrumentOutputParameter computedParam : getOutputParameters(InstrumentParameterCaptureMethod.COMPUTED)) {
      if(isReadyToCompute(computedParam)) {
        // First, compute the parameters not depend on other computed parameters
        calculateAndPersistValue(currentRun, computedParam);
      } else {
        dependentComputedParameters.add(computedParam);
      }
    }
    // Then, compute the parameters that depend on other computed parameters
    short maxRecur = 3; // Maximum dependence level
    while(!dependentComputedParameters.isEmpty() && maxRecur > 0) {

      List<InstrumentOutputParameter> parameters = new ArrayList<InstrumentOutputParameter>();
      for(InstrumentOutputParameter computedParam : dependentComputedParameters) {
        if(isReadyToCompute(computedParam)) {
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
    InstrumentType instrumentType = getInstrumentType();

    List<InstrumentInputParameter> inputParametersWithDataSource = instrumentType.getInstrumentParameters(InstrumentInputParameter.class, true);

    for(InstrumentInputParameter parameter : inputParametersWithDataSource) {
      Data data = parameter.getDataSource().getData(getParticipant());

      if(data != null) {
        final InstrumentRunValue runValue = getInstrumentRunValue(parameter);

        if(!data.getType().equals(parameter.getDataType())) {
          UnitParameterValueConverter converter = new UnitParameterValueConverter();
          converter.convert(parameter, runValue, data);
        } else {
          runValue.setData(data);
        }
        update(runValue);
      } else {
        log.error("The value for instrument parameter {} comes from an InputSource, but this source has not produced a value. Please correct stage dependencies or your instrument-descriptor.xml file for this instrument.", parameter.getCode());
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

  /**
   * Indicates whether the specified computed output parameter is ready to be computed (i.e., if all its component data
   * sources have data).
   * 
   * @param computedParam computed output parameter
   * @return <code>true</code> if the computed output parameter is ready to be computed
   */
  private boolean isReadyToCompute(InstrumentOutputParameter computedParam) {
    ComputingDataSource computingDataSource = (ComputingDataSource) computedParam.getDataSource();

    for(IDataSource dataSource : computingDataSource.getDataSources()) {
      if(dataSource.getData(getParticipant()) == null) {
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
  private void calculateAndPersistValue(InstrumentRun currentRun, InstrumentOutputParameter computedParam) {
    InstrumentRunValue computedRunValue = getOutputInstrumentRunValue(computedParam.getCode());

    IDataSource computedDataSource = computedParam.getDataSource();
    Data computedValue = computedDataSource.getData(getParticipant());
    computedRunValue.setData(computedValue);

    getPersistenceManager().save(computedRunValue);
    currentRun.addInstrumentRunValue(computedRunValue);
  }
}
