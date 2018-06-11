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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
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
import org.obiba.onyx.jade.core.domain.run.MeasureStatus;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Transactional
public class DefaultActiveInstrumentRunServiceImpl extends PersistenceManagerAwareService implements ActiveInstrumentRunService {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(DefaultActiveInstrumentRunServiceImpl.class);

  //
  // Instance Variables
  //

  private ActiveInterviewService activeInterviewService;

  private InstrumentService instrumentService;

  private UserSessionService userSessionService;

  private Serializable currentRunId = null;

  //
  // ActiveInstrumentRunService Methods
  //

  public InstrumentRun start(Participant participant, Instrument instrument, InstrumentType instrumentType) {
    synchronized(getSessionMutex()) {
      if(participant == null) throw new IllegalArgumentException("Participant cannot be null.");
      if(instrument == null) throw new IllegalArgumentException("Instrument cannot be null.");
      if(instrumentType == null) throw new IllegalArgumentException("Instrument type cannot be null.");

      InstrumentRun currentRun = new InstrumentRun();
      // Instrument must not be null when InstrumentRun is persisted.
      currentRun.setInstrument(instrument);
      currentRun.setParticipant(participant);
      currentRun.setInstrumentType(instrumentType.getName());
      currentRun.setStatus(InstrumentRunStatus.IN_PROGRESS);
      currentRun.setTimeStart(new Date());
      currentRun.setUserName(userSessionService.getUserName());
      currentRun.setWorkstation(userSessionService.getWorkstation());
      getPersistenceManager().save(currentRun);
      currentRunId = currentRun.getId();

      return currentRun;
    }
  }

  // Visible for testing.
  void reset() {
    currentRunId = null;
  }

  public void end() {
    synchronized(getSessionMutex()) {
      if(currentRunId == null) return;

      InstrumentRun currentRun = getInstrumentRun();
      currentRun.setTimeEnd(new Date());

      log.debug("InstrumentRun id={} is ending with status {}", getInstrumentRun().getId(), currentRun.getStatus());
      getPersistenceManager().save(currentRun);
    }
  }

  public Participant getParticipant() {
    synchronized(getSessionMutex()) {
      if(currentRunId == null) return null;

      return getInstrumentRun().getParticipant();
    }
  }

  public InstrumentType getInstrumentType() {
    synchronized(getSessionMutex()) {
      String instrumentTypeName = getInstrumentRun().getInstrumentType();

      return instrumentService.getInstrumentType(instrumentTypeName);
    }
  }

  public void setInstrument(Instrument instrument) {
    synchronized(getSessionMutex()) {
      getInstrumentRun().setInstrument(instrument);
    }
  }

  public Instrument getInstrument() {
    return getInstrumentRun().getInstrument();
  }

  // Visible for testing.
  boolean hasParameterWithWarning() {
    return !getParametersWithWarning().isEmpty();
  }

  public List<InstrumentOutputParameter> getParametersWithWarning() {
    synchronized(getSessionMutex()) {
      List<InstrumentOutputParameter> paramsWithWarnings = new ArrayList<InstrumentOutputParameter>();

      InstrumentType instrumentType = getInstrumentType();

      for(InstrumentOutputParameter outputParameter : instrumentType.getInstrumentParameters(InstrumentOutputParameter.class)) {

        // Don't include parameters with a non-MANUAL capture method.
        if(!outputParameter.getCaptureMethod().equals(InstrumentParameterCaptureMethod.MANUAL)) {
          continue;
        }

        if(!instrumentType.isRepeatable(outputParameter)) {
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
        // TODO check repeated measures individually
      }

      return paramsWithWarnings;
    }
  }

  public void setInstrumentRun(InstrumentRun instrumentRun) {
    synchronized(getSessionMutex()) {
      if(instrumentRun != null) {
        currentRunId = instrumentRun.getId();
      }
    }
  }

  public InstrumentRun getInstrumentRun() {
    synchronized(getSessionMutex()) {
      // ONYX-961: Calling this method will throw a NoSuchInterviewException
      // if the current session does not have a lock on an interview.
      activeInterviewService.getParticipant();

      log.debug("currentRunId={}", currentRunId);
      if(currentRunId == null) return null;

      return getPersistenceManager().get(InstrumentRun.class, currentRunId);
    }
  }

  public void setInstrumentRunStatus(InstrumentRunStatus status) {
    synchronized(getSessionMutex()) {
      InstrumentRun currentRun = getInstrumentRun();

      currentRun.setStatus(status);
      getPersistenceManager().save(currentRun);
    }
  }

  public void update(InstrumentRunValue currentRunValue) {
    synchronized(getSessionMutex()) {
      if(currentRunId == null) return;
      if(currentRunValue.getInstrumentRun() == null) throw new IllegalArgumentException("Current instrument run cannot be null");
      if(!currentRunId.equals(currentRunValue.getInstrumentRun().getId())) throw new IllegalArgumentException("Unexpected given current instrument run");

      getPersistenceManager().save(currentRunValue);
    }
  }

  public void computeOutputParameters() {
    synchronized(getSessionMutex()) {
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
  }

  public InstrumentRunValue getInstrumentRunValue(String parameterCode) {
    synchronized(getSessionMutex()) {
      if(currentRunId == null) return null;
      return getInstrumentRunValue(getAndCheckInstrumentParameter(parameterCode), false);
    }
  }

  public InstrumentRunValue getOrCreateInstrumentRunValue(String parameterCode) {
    synchronized(getSessionMutex()) {
      if(currentRunId == null) return null;
      return getInstrumentRunValue(getAndCheckInstrumentParameter(parameterCode), true);
    }
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
    synchronized(getSessionMutex()) {
      return getInstrumentRunValue(parameter, true);
    }
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

  public InstrumentRunValue getInstrumentRunValue(String parameterCode, Measure measure) {
    synchronized(getSessionMutex()) {
      InstrumentRunValue valueTemplate = new InstrumentRunValue();
      valueTemplate.setInstrumentParameter(parameterCode);
      valueTemplate.setMeasure(measure);

      InstrumentRunValue runValue = getPersistenceManager().matchOne(valueTemplate);

      return runValue;
    }
  }

  public List<InstrumentRunValue> getInstrumentRunValues(String parameterCode) {
    synchronized(getSessionMutex()) {
      InstrumentParameter parameter = getAndCheckInstrumentParameter(parameterCode);
      InstrumentRun instrumentRun = getInstrumentRun();
      if(instrumentRun == null) throw new IllegalArgumentException("Cannot retrieve a run value from a null instrument run.");

      List<InstrumentRunValue> runValues = new ArrayList<InstrumentRunValue>();

      if(!getInstrumentType().isRepeatable(parameter)) {
        InstrumentRunValue valueTemplate = new InstrumentRunValue();
        valueTemplate.setInstrumentParameter(parameter.getCode());
        valueTemplate.setInstrumentRun(instrumentRun);

        InstrumentRunValue runValue = getPersistenceManager().matchOne(valueTemplate);

        if(runValue != null) {
          runValues.add(runValue);
        }
      } else {
        for(Measure measure : instrumentRun.getMeasures()) {

          InstrumentRunValue runValue = getInstrumentRunValue(parameter.getCode(), measure);

          if(runValue != null) {
            runValues.add(runValue);
          }
        }
      }

      return runValues;
    }
  }

  public Measure addMeasure(Map<String, Data> repeatableData) {
    synchronized(getSessionMutex()) {
      return addMeasure(repeatableData, null);
    }
  }

  private void addManuallyCapturedMeasure(Map<String, Data> repeatableData) {
    addMeasure(repeatableData, InstrumentParameterCaptureMethod.MANUAL);
  }

  private Measure addMeasure(Map<String, Data> repeatableData, InstrumentParameterCaptureMethod captureMethod) {
    InstrumentRun instrumentRun = getInstrumentRun();
    Measure measure = new Measure();
    measure.setUserName(userSessionService.getUserName());
    measure.setTime(new Date());
    measure.setInstrumentBarcode(instrumentRun.getInstrument().getBarcode());
    measure.setInstrumentRun(instrumentRun);
    measure.setStatus(MeasureStatus.VALID);
    measure.setWorkstation(userSessionService.getWorkstation());

    for(Map.Entry<String, Data> entry : repeatableData.entrySet()) {
      InstrumentParameter parameter = getAndCheckInstrumentParameter(entry.getKey());
      InstrumentRunValue runValue = new InstrumentRunValue();

      runValue.setMeasure(measure);
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
    // The relationship is driven by Measure.instrumentRun. This should make the InstrumentRun instance reflect the
    // change
    getPersistenceManager().refresh(instrumentRun);

    return measure;

  }

  public String updateReadOnlyInputParameterRunValue() {
    synchronized(getSessionMutex()) {
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
  }

  //
  // IContraindicatable Methods
  //

  public List<Contraindication> getContraindications(Contraindication.Type type) {
    synchronized(getSessionMutex()) {
      List<Contraindication> contraindications = new ArrayList<Contraindication>();

      for(Contraindication contraindication : getInstrumentType().getContraindications()) {
        if(contraindication.getType().equals(type)) {
          contraindications.add(contraindication);
        }
      }

      return contraindications;
    }
  }

  public boolean hasContraindications(Contraindication.Type type) {
    synchronized(getSessionMutex()) {
      return !getContraindications(type).isEmpty();
    }
  }

  public Contraindication getContraindication() {
    synchronized(getSessionMutex()) {
      InstrumentType instrumentType = getInstrumentType();
      InstrumentRun instrumentRun = getInstrumentRun();

      for(Contraindication ci : instrumentType.getContraindications()) {
        if(ci.getCode().equals(instrumentRun.getContraindication())) return ci;
      }
      return null;
    }
  }

  public boolean isContraindicated() {
    synchronized(getSessionMutex()) {
      return getInstrumentRun().getContraindication() != null;
    }
  }

  public void setContraindication(Contraindication contraindication) {
    synchronized(getSessionMutex()) {
      getInstrumentRun().setContraindication(contraindication);
    }
  }

  public void setOtherContraindication(String other) {
    synchronized(getSessionMutex()) {
      getInstrumentRun().setOtherContraindication(other);
    }
  }

  public String getOtherContraindication() {
    synchronized(getSessionMutex()) {
      return getInstrumentRun().getOtherContraindication();
    }
  }

  //
  // Methods
  //

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

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
    IDataSource ds = computedParam.getDataSource();

    if(ds instanceof ComputingDataSource) {
      ComputingDataSource computingDataSource = (ComputingDataSource) ds;

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
    synchronized(getSessionMutex()) {
      InstrumentRun run = getInstrumentRun();
      InstrumentType type = getInstrumentType();
      if(type.isRepeatable()) {
        return run.getValidMeasureCount();
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
  }

  public synchronized void addOutputParameterValues(Map<String, Data> values) {
    synchronized(getSessionMutex()) {
      addOutputParameterValues(values, null);
    }
  }

  public synchronized void addManuallyCapturedOutputParameterValues(Map<String, Data> values) {
    synchronized(getSessionMutex()) {
      addOutputParameterValues(values, InstrumentParameterCaptureMethod.MANUAL);
    }
  }

  private void addOutputParameterValues(Map<String, Data> values, InstrumentParameterCaptureMethod captureMethod) {
    values = deleteNullOutputParameterValues(values); // Avoid saving parameters with null values.

    // filter out the repeatable parameters
    // and persist non-repeatable data
    InstrumentType type = getInstrumentType();
    Map<String, Data> repeatableData = new HashMap<String, Data>();
    for(Map.Entry<String, Data> entry : values.entrySet()) {
      InstrumentParameter parameter = getInstrumentParameter(entry.getKey());
      if(!type.isRepeatable(parameter)) {
        updateParameterValue(parameter, entry.getValue(), captureMethod);
      } else {
        repeatableData.put(entry.getKey(), entry.getValue());
      }
    }

    // persist repeatable data if any
    if(repeatableData.size() > 0) {
      if(captureMethod != null) {
        addManuallyCapturedMeasure(repeatableData);
      } else {
        addMeasure(repeatableData);
      }
    }
  }

  /**
   * Returns a map with null output parameters removed. Additionally the null output parameters are also removed from
   * the database. We do not want to save null values to the database.
   * @param values Map of output parameters to values
   * @return A Map with the null output parameters removed
   */
  private Map<String, Data> deleteNullOutputParameterValues(Map<String, Data> values) {
    Map<String, Data> result = new HashMap<String, Data>();
    for(Map.Entry<String, Data> entry : values.entrySet()) {
      if(entry.getValue() != null && entry.getValue().getValue() != null) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }

  private InstrumentParameter getInstrumentParameter(String name) {
    InstrumentParameter parameter = getInstrumentType().getInstrumentParameter(name);

    // Test if no parameter found for the given name
    if(parameter == null || parameter instanceof InstrumentOutputParameter == false) {
      throw new IllegalArgumentException("No output parameter with name: " + name);
    }

    return parameter;
  }

  private void updateParameterValue(InstrumentParameter parameter, Data value, InstrumentParameterCaptureMethod captureMethod) {
    // ONYX-1562 building the run value by modifying the parameter original capture method
    // is very ugly. Patch the problem by restoring the original one.
    InstrumentParameterCaptureMethod originalCaptureMethod = parameter.getCaptureMethod();
    if(captureMethod != null && parameter.isManualCaptureAllowed()) {
      parameter.setCaptureMethod(captureMethod);
    }
    updateParameterValue(parameter, value);
    parameter.setCaptureMethod(originalCaptureMethod);
  }

  private void updateParameterValue(InstrumentParameter parameter, Data value) {
    InstrumentRunValue outputParameterValue = getOrCreateInstrumentRunValue(parameter);
    outputParameterValue.setData(value);
    update(outputParameterValue);
  }

  public void deleteMeasure(Measure measure) {
    synchronized(getSessionMutex()) {
      getPersistenceManager().delete(measure);
    }
  }

  public void setSkipRemainingMeasuresCommentFromInstrumentRun(String comment) {
    synchronized(getSessionMutex()) {
      if(comment == null) throw new IllegalArgumentException("Cannot add a null comment on the instrumentRun");

      InstrumentRun currentRun = getInstrumentRun();
      currentRun.setSkipComment(comment);
      getPersistenceManager().save(currentRun);
    }
  }

  public void removeSkipRemainingMeasuresCommentFromInstrumentRun() {
    synchronized(getSessionMutex()) {
      InstrumentRun currentRun = getInstrumentRun();
      currentRun.setSkipComment(null);
      getPersistenceManager().save(currentRun);
    }
  }

  public Map<IntegrityCheck, InstrumentOutputParameter> checkIntegrity(List<InstrumentOutputParameter> outputParams) {
    synchronized(getSessionMutex()) {
      return checkIntegrity(outputParams, null);
    }
  }

  public Map<IntegrityCheck, InstrumentOutputParameter> checkIntegrity(List<InstrumentOutputParameter> outputParams, Measure measure) {
    synchronized(getSessionMutex()) {
      HashMap<IntegrityCheck, InstrumentOutputParameter> failedChecks = new HashMap<IntegrityCheck, InstrumentOutputParameter>();

      for(InstrumentOutputParameter param : outputParams) {
        List<IntegrityCheck> integrityChecks = param.getIntegrityChecks();

        for(IntegrityCheck integrityCheck : integrityChecks) {

          // Skip non-ERROR type checks.
          if(!integrityCheck.getType().equals(IntegrityCheckType.ERROR)) {
            continue;
          }

          boolean checkFailed = false;
          List<InstrumentRunValue> runValues = new ArrayList<InstrumentRunValue>();
          if(measure == null || getInstrumentType().isRepeatable(param) == false) {
            runValues.addAll(getInstrumentRunValues(param.getCode()));
          } else {
            runValues.add(getInstrumentRunValue(param.getCode(), measure));
          }

          for(InstrumentRunValue runValue : runValues) {

            Data paramData = (runValue != null) ? runValue.getData(param.getDataType()) : null;

            if(!integrityCheck.checkParameterValue(param, paramData, null, this)) {
              failedChecks.put(integrityCheck, param);
              checkFailed = true;
            }
          }

          if(checkFailed) {
            break; // stop checking parameter after first failure (but continue checking other parameters!)
          }
        }
      }

      return failedChecks;
    }
  }

  public synchronized void removeInvalidMeasuresFromInstrumentRun() {
    synchronized(getSessionMutex()) {
      log.info("removing invalid measures");
      List<Measure> invalidMeasures = getInstrumentRun().getMeasures(MeasureStatus.INVALID);
      for(Measure measure : invalidMeasures) {
        deleteMeasure(measure);
      }
    }
  }

  public synchronized void deleteInstrumentRunValue(InstrumentRunValue instrumentRunValue) {
    synchronized(getSessionMutex()) {
      getPersistenceManager().delete(instrumentRunValue);
    }
  }

  /**
   * Get the best session mutex, to protect from concurrent accesses (instrument controller and web application).
   * @return
   */
  private Object getSessionMutex() {
    // ONYX-1611
    Object mutex = this;
    RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
    if(attrs != null) {
      mutex = attrs.getSessionMutex();
      log.trace("BEAN {} MUTEX {} SESSION {}", new Object[] { this, mutex, attrs.getSessionId() });
    }
    return mutex;
  }

  @Override
  public void updateMeasureStatus(Measure measure, MeasureStatus status) {
    measure.setStatus(status);
    getPersistenceManager().save(measure);
  }
}
