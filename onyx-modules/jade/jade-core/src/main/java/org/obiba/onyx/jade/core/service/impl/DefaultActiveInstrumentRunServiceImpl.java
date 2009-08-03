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
import java.util.Map;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.data.InstrumentParameterDataSource;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.UnitParameterValueConverter;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheck;
import org.obiba.onyx.jade.core.domain.instrument.validation.IntegrityCheckType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.Measure;
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

  // Visible for testing.
  void reset() {
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

  // Visible for testing.
  boolean hasParameterWithWarning() {
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

        InstrumentRunValue runValue = getInstrumentRunValue(outputParameter, false);

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

  public void setInstrumentRunStatus(InstrumentRunStatus status) {
    InstrumentRun currentRun = getInstrumentRun();

    currentRun.setStatus(status);
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

    List<InstrumentOutputParameter> dependentComputedParameters = new ArrayList<InstrumentOutputParameter>();

    // TODO quick and dirty implementation, to be checked
    for(InstrumentOutputParameter computedParam : getInstrumentType().getOutputParameters(InstrumentParameterCaptureMethod.COMPUTED)) {
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

  public InstrumentRunValue getInstrumentRunValue(String parameterCode) {
    if(currentRunId == null) return null;
    return getInstrumentRunValue(getAndCheckInstrumentParameter(parameterCode), false);
  }

  public InstrumentRunValue getOrCreateInstrumentRunValue(String parameterCode) {
    if(currentRunId == null) return null;
    return getInstrumentRunValue(getAndCheckInstrumentParameter(parameterCode), true);
  }

  private InstrumentParameter getAndCheckInstrumentParameter(String name) {
    InstrumentParameter instrumentParameter = getInstrumentType().getInstrumentParameter(name);

    if(instrumentParameter == null) {
      InstrumentRun currentRun = getInstrumentRun();
      throw new IllegalArgumentException("No such parameter with code or vendore name for instrument " + currentRun.getInstrumentType() + " :" + name);
    }

    return instrumentParameter;
  }

  public InstrumentRunValue getOrCreateInstrumentRunValue(InstrumentParameter parameter) {
    return getInstrumentRunValue(parameter, true);
  }

  private InstrumentRunValue getInstrumentRunValue(InstrumentParameter parameter, boolean create) {
    if(parameter == null) throw new IllegalArgumentException("Cannot retrieve a run value from a null instrument parameter.");
    InstrumentRun instrumentRun = getInstrumentRun();
    if(instrumentRun == null) throw new IllegalArgumentException("Cannot retrieve a run value from a null instrument run.");

    InstrumentRunValue valueTemplate = new InstrumentRunValue();
    valueTemplate.setInstrumentParameter(parameter.getCode());
    valueTemplate.setInstrumentRun(instrumentRun);

    InstrumentRunValue runValue = getPersistenceManager().matchOne(valueTemplate);

    if(runValue == null && create) {
      valueTemplate.setCaptureMethod(parameter.getCaptureMethod());
      runValue = getPersistenceManager().save(valueTemplate);
    }

    return runValue;
  }

  public List<InstrumentRunValue> getInstrumentRunValues(String parameterCode) {
    InstrumentParameter parameter = getAndCheckInstrumentParameter(parameterCode);
    InstrumentRun instrumentRun = getInstrumentRun();
    if(instrumentRun == null) throw new IllegalArgumentException("Cannot retrieve a run value from a null instrument run.");

    List<InstrumentRunValue> runValues = new ArrayList<InstrumentRunValue>();

    if(!getInstrumentType().isRepeatable()) {
      InstrumentRunValue valueTemplate = new InstrumentRunValue();
      valueTemplate.setInstrumentParameter(parameter.getCode());
      valueTemplate.setInstrumentRun(instrumentRun);

      InstrumentRunValue runValue = getPersistenceManager().matchOne(valueTemplate);

      if(runValue != null) {
        runValues.add(runValue);
      }
    } else {
      for(Measure measure : instrumentRun.getMeasures()) {
        InstrumentRunValue valueTemplate = new InstrumentRunValue();
        valueTemplate.setInstrumentParameter(parameter.getCode());
        valueTemplate.setMeasure(measure);

        InstrumentRunValue runValue = getPersistenceManager().matchOne(valueTemplate);

        if(runValue != null) {
          runValues.add(runValue);
        }
      }
    }

    return runValues;
  }

  public void addMeasure(Map<String, Data> repeatableData) {
    addMeasure(repeatableData, null);
  }

  private void addManuallyCapturedMeasure(Map<String, Data> repeatableData) {
    addMeasure(repeatableData, InstrumentParameterCaptureMethod.MANUAL);
  }

  private void addMeasure(Map<String, Data> repeatableData, InstrumentParameterCaptureMethod captureMethod) {
    InstrumentRun instrumentRun = getInstrumentRun();
    Measure measure = new Measure();
    measure.setUser(userSessionService.getUser());
    measure.setTime(new Date());
    measure.setInstrumentBarcode(instrumentRun.getInstrument().getBarcode());
    measure.setInstrumentRun(instrumentRun);

    for(Map.Entry<String, Data> entry : repeatableData.entrySet()) {
      InstrumentParameter parameter = getAndCheckInstrumentParameter(entry.getKey());
      InstrumentRunValue runValue = new InstrumentRunValue();

      runValue.setMeasure(measure);
      runValue.setCaptureMethod(parameter.getCaptureMethod());
      runValue.setInstrumentParameter(parameter.getCode());
      runValue.setData(entry.getValue());
      runValue.setInstrumentRun(instrumentRun);

      if(captureMethod != null) {
        runValue.setCaptureMethod(captureMethod);
      } else {
        runValue.setCaptureMethod(parameter.getCaptureMethod());
      }
      measure.getInstrumentRunValues().add(runValue);

    }
    getPersistenceManager().save(measure);

  }

  public String updateReadOnlyInputParameterRunValue() {
    InstrumentType instrumentType = getInstrumentType();

    List<InstrumentInputParameter> inputParametersWithDataSource = instrumentType.getInstrumentParameters(InstrumentInputParameter.class, true);

    for(InstrumentInputParameter parameter : inputParametersWithDataSource) {
      Data data = parameter.getDataSource().getData(getParticipant());

      if(data != null) {
        final InstrumentRunValue runValue = getInstrumentRunValue(parameter, true);

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

    String instrumentTypeName = getInstrumentRun().getInstrumentType();
    for(IDataSource dataSource : computingDataSource.getDataSources()) {
      if(dataSource instanceof InstrumentParameterDataSource) {
        InstrumentParameterDataSource instrumentParameterDataSource = (InstrumentParameterDataSource) dataSource;
        // ONYX-584 we are only interested in dependencies between COMPUTED data sources
        // from the current instrument type.
        // COMPUTED outputs should not be linked by variable data sources.
        if(instrumentTypeName.equals(instrumentParameterDataSource.getInstrumentType())) {
          InstrumentParameter parameter = instrumentParameterDataSource.getInstrumentParameter();
          if(parameter.getCaptureMethod().equals(InstrumentParameterCaptureMethod.COMPUTED) && dataSource.getData(getParticipant()) == null) {
            return false;
          }
        }
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
    InstrumentRunValue computedRunValue = getOrCreateInstrumentRunValue(computedParam.getCode());

    IDataSource computedDataSource = computedParam.getDataSource();
    Data computedValue = computedDataSource.getData(getParticipant());
    computedRunValue.setData(computedValue);

    getPersistenceManager().save(computedRunValue);
    currentRun.addInstrumentRunValue(computedRunValue);
  }

  public int getCurrentMeasureCount() {
    InstrumentRun run = getInstrumentRun();
    InstrumentType type = getInstrumentType();
    if(type.isRepeatable()) {
      return run.getMeasureCount();
    } else {
      for(InstrumentRunValue runValue : run.getInstrumentRunValues()) {
        InstrumentParameter parameter = type.getInstrumentParameter(runValue.getInstrumentParameter());
        if(parameter instanceof InstrumentOutputParameter) {
          return 1;
        }
      }
      return 0;
    }
  }

  public void addOutputParameterValues(Map<String, Data> values) {
    addOutputParameterValues(values, null);
  }

  public void addManuallyCapturedOutputParameterValues(Map<String, Data> values) {
    addOutputParameterValues(values, InstrumentParameterCaptureMethod.MANUAL);
  }

  private void addOutputParameterValues(Map<String, Data> values, InstrumentParameterCaptureMethod captureMethod) {
    if(!getInstrumentType().isRepeatable()) {
      for(Map.Entry<String, Data> entry : values.entrySet()) {
        String paramName = entry.getKey();
        InstrumentParameter parameter = getInstrumentParameter(paramName);
        if(captureMethod != null && parameter.isManualCaptureAllowed()) parameter.setCaptureMethod(captureMethod);
        updateParameterValue(parameter, entry.getValue());
      }
    } else {
      if(captureMethod != null) {
        addManuallyCapturedMeasure(values);
      } else {
        addMeasure(values);
      }
    }
  }

  private InstrumentParameter getInstrumentParameter(String name) {
    InstrumentParameter parameter = getInstrumentType().getInstrumentParameter(name);

    // Test if no parameter found for the given name
    if(parameter == null || parameter instanceof InstrumentOutputParameter == false) {
      throw new IllegalArgumentException("No output parameter with name: " + name);
    }

    return parameter;
  }

  private void updateParameterValue(InstrumentParameter parameter, Data value) {
    InstrumentRunValue outputParameterValue = getOrCreateInstrumentRunValue(parameter);
    outputParameterValue.setData(value);
    update(outputParameterValue);
  }

  public void deleteMeasure(Measure measure) {
    getPersistenceManager().delete(measure);
  }

}