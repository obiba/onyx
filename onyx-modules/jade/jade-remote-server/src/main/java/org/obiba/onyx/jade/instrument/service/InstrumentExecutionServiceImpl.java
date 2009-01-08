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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class InstrumentExecutionServiceImpl implements InstrumentExecutionService {

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
    return (getInstrumentRun().getParticipant().getFirstName());
  }

  public String getParticipantLastName() {
    return (getInstrumentRun().getParticipant().getLastName());
  }

  public Date getParticipantBirthDate() {
    return (getInstrumentRun().getParticipant().getBirthDate());
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
      InstrumentRunValue inputParameterValue = activeInstrumentRunService.getInputInstrumentRunValue(parameterCode);
      inputParametersValue.put(inputParameterValue.getInstrumentParameter().getCode(), inputParameterValue.getData());
    }
    log.info("getInputParametersValue({})={}", parameters, inputParametersValue);
    return (inputParametersValue);
  }

  public Data getInputParameterValue(String parameterCode) {
    return activeInstrumentRunService.getInputInstrumentRunValue(parameterCode).getData();
  }

  public void addOutputParameterValues(Map<String, Data> values) {
    for(Map.Entry<String, Data> entry : values.entrySet()) {
      addOutputParameterValue(entry.getKey(), entry.getValue());
    }
  }

  public void addOutputParameterValue(String name, Data value) {
    log.info("addOutputParameterValue({}, {})", name, value);

    // Lookup the paramater using it's code
    InstrumentParameter parameter = activeInstrumentRunService.getParameterByCode(name);
    if(parameter == null || parameter instanceof InstrumentOutputParameter == false) {
      // Lookup the paramater using it's vendor name
      parameter = activeInstrumentRunService.getParameterByVendorName(name);
    }

    // Test if no parameter found for the given name
    if(parameter == null || parameter instanceof InstrumentOutputParameter == false) {
      throw new IllegalArgumentException("No output parameter with name: " + name);
    }
    InstrumentRunValue outputParameterValue = activeInstrumentRunService.getInstrumentRunValue(parameter);
    outputParameterValue.setData(value);
    activeInstrumentRunService.update(outputParameterValue);
  }

  public void instrumentRunnerError(Exception error) {
    activeInstrumentRunService.setInstrumentRunStatus(InstrumentRunStatus.IN_ERROR);
  }

}
