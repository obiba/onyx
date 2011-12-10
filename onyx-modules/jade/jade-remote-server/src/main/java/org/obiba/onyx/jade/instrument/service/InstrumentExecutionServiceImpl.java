/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.domain.run.MeasureStatus;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class InstrumentExecutionServiceImpl implements InstrumentExecutionService {

  private static final Logger log = LoggerFactory.getLogger(InstrumentExecutionServiceImpl.class);

  private InstrumentRunService instrumentRunService;

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

  public String getInstrumentOperatorUsername() {
    return getInstrumentRun().getUser().getLogin();
  }

  public Locale getInstrumentOperatorLocale() {
    return getInstrumentRun().getUser().getLanguage();
  }

  public String getParticipantFirstName() {
    return (getInstrumentRun().getParticipant().getFirstName());
  }

  public String getParticipantLastName() {
    return (getInstrumentRun().getParticipant().getLastName());
  }

  public Date getParticipantBirthDate() {
    return (getInstrumentRun().getParticipant().getBirthDate());
  }

  public String getDateAsString(String parameter, SimpleDateFormat dateFormat) {
    return dateFormat.format(getInputParameterValue(parameter).getValue());
  }

  public String getParticipantGender() {
    return (getInstrumentRun().getParticipant().getGender().toString());
  }

  public String getParticipantID() {
    return (getInstrumentRun().getParticipant().getBarcode());
  }

  public Map<String, Data> getInputParametersValue(String... parameters) {
    Map<String, Data> inputParametersValue = new HashMap<String, Data>();
    for(String parameterCode : parameters) {
      InstrumentParameter inputParameter = activeInstrumentRunService.getInstrumentType().getInstrumentParameter(parameterCode);
      if(inputParameter != null) {
        InstrumentRunValue inputParameterValue = activeInstrumentRunService.getInstrumentRunValue(parameterCode);
        if(inputParameterValue != null) {
          inputParametersValue.put(parameterCode, inputParameterValue.getData(inputParameter.getDataType()));
        } else {
          log.warn("Run value for input parameter with code {} in {} is null.", parameterCode, activeInstrumentRunService.getInstrumentType());
        }
      } else {
        log.warn("Parameter with code {} unknown in {}.", parameterCode, activeInstrumentRunService.getInstrumentType());
      }
    }
    log.info("getInputParametersValue({})={}", parameters, inputParametersValue);
    return (inputParametersValue);
  }

  @Override
  public boolean hasInputParameter(String parameterCode) {
    InstrumentType type = activeInstrumentRunService.getInstrumentType();
    return type.getInstrumentParameter(InstrumentInputParameter.class, parameterCode) != null;
  }

  @Override
  public boolean hasOutputParameter(String parameterCode) {
    InstrumentType type = activeInstrumentRunService.getInstrumentType();
    return type.getInstrumentParameter(InstrumentOutputParameter.class, parameterCode) != null;
  }

  public Map<String, String> getInputParametersVendorNames(String... parameters) {
    Map<String, String> inputParametersVendorName = new HashMap<String, String>();
    for(String parameterCode : parameters) {
      InstrumentParameter parameter = activeInstrumentRunService.getInstrumentType().getInstrumentParameter(parameterCode);
      inputParametersVendorName.put(parameterCode, parameter.getVendorName());
    }
    log.info("getInputParametersVendorNames({})={}", parameters, inputParametersVendorName);
    return (inputParametersVendorName);
  }

  public Data getInputParameterValue(String parameterCode) {
    InstrumentParameter parameter = activeInstrumentRunService.getInstrumentType().getInstrumentParameter(parameterCode);
    return activeInstrumentRunService.getInstrumentRunValue(parameterCode).getData(parameter.getDataType());
  }

  @Override
  public Set<String> getExpectedOutputParameterVendorNames() {
    Set<String> names = new LinkedHashSet<String>();
    for(InstrumentParameter ip : activeInstrumentRunService.getInstrumentType().getOutputParameters(true)) {
      names.add(ip.getVendorName());
    }
    return names;
  }

  public void addOutputParameterValues(Map<String, Data> values) {
    InstrumentType type = activeInstrumentRunService.getInstrumentType();

    // filter out the repeatable parameters
    // and persist non-repeatable data
    Map<String, Data> repeatableData = new HashMap<String, Data>();
    for(Map.Entry<String, Data> entry : values.entrySet()) {
      InstrumentParameter parameter = getInstrumentOutputParameter(entry.getKey());
      if(!type.isRepeatable(parameter)) {
        updateParameterValue(parameter, entry.getValue());
      } else {
        repeatableData.put(entry.getKey(), entry.getValue());
      }
    }

    // persist repeatable data if any in a measure
    if(repeatableData.size() > 0) {
      Measure measure = activeInstrumentRunService.addMeasure(repeatableData);
      List<InstrumentOutputParameter> outputParams = type.getOutputParameters(InstrumentParameterCaptureMethod.AUTOMATIC);
      MeasureStatus status = activeInstrumentRunService.checkIntegrity(outputParams, measure).isEmpty() ? MeasureStatus.VALID : MeasureStatus.INVALID;
      instrumentRunService.updateMeasureStatus(measure, status);
    }
  }

  private void updateParameterValue(InstrumentParameter parameter, Data value) {
    InstrumentRunValue outputParameterValue = activeInstrumentRunService.getOrCreateInstrumentRunValue(parameter);
    outputParameterValue.setData(value);
    activeInstrumentRunService.update(outputParameterValue);
  }

  private InstrumentOutputParameter getInstrumentOutputParameter(String name) {
    InstrumentOutputParameter parameter = activeInstrumentRunService.getInstrumentType().getInstrumentParameter(InstrumentOutputParameter.class, name);

    // Test if no parameter found for the given name
    if(parameter == null) {
      throw new IllegalArgumentException("No output parameter with name: " + name);
    }

    return parameter;
  }

  public void instrumentRunnerError(Exception error) {
    activeInstrumentRunService.setInstrumentRunStatus(InstrumentRunStatus.IN_ERROR);
  }

  public int getCurrentMeasureCount() {
    return activeInstrumentRunService.getCurrentMeasureCount();
  }

  public int getExpectedMeasureCount() {
    return activeInstrumentRunService.getInstrumentType().getExpectedMeasureCount(activeInstrumentRunService.getParticipant());
  }

  public InstrumentRunService getInstrumentRunService() {
    return instrumentRunService;
  }

  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

}
