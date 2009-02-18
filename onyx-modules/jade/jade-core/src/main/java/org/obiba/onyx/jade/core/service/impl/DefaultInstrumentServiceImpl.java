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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultInstrumentServiceImpl extends PersistenceManagerAwareService implements InstrumentService {

  @SuppressWarnings("unused")
  private final Logger log = LoggerFactory.getLogger(getClass());

  private String instrumentsPath;

  private Map<String, InstrumentType> instrumentTypes;

  public void setInstrumentTypes(Map<String, InstrumentType> instrumentTypes) {
    this.instrumentTypes = instrumentTypes;
  }

  public void setInstrumentsPath(String instrumentsPath) {
    this.instrumentsPath = instrumentsPath;
  }

  public InstrumentType getInstrumentType(String name) {
    return instrumentTypes.get(name);
  }

  public Map<String, InstrumentType> getInstrumentTypes() {
    return instrumentTypes;
  }

  public InstrumentParameter getParameterByCode(InstrumentType instrumentType, String parameterCode) {
    for(InstrumentParameter parameter : instrumentType.getInstrumentParameters()) {
      if(parameter.getCode().equals(parameterCode)) {
        return parameter;
      }
    }
    return null;
  }

  public List<Instrument> getInstruments(String typeName) {
    return getInstruments(getInstrumentType(typeName));
  }

  public List<Instrument> getInstruments(InstrumentType instrumentType) {
    Instrument template = new Instrument();
    template.setType(instrumentType.getName());

    return getPersistenceManager().match(template);
  }

  public List<Instrument> getActiveInstruments(InstrumentType instrumentType) {
    Instrument template = new Instrument();
    template.setType(instrumentType.getName());
    template.setStatus(InstrumentStatus.ACTIVE);

    return getPersistenceManager().match(template);
  }

  public boolean isInteractiveInstrument(InstrumentType instrumentType) {
    if(instrumentType == null) return false;

    return !getOutputParameters(instrumentType, InstrumentParameterCaptureMethod.AUTOMATIC).isEmpty();
  }

  public int countInstrumentInputParameter(InstrumentType instrument, boolean readOnlySource) {
    return getInstrumentInputParameter(instrument, readOnlySource).size();
  }

  public List<InstrumentInputParameter> getInstrumentInputParameter(InstrumentType instrumentType, boolean readOnlySource) {
    return instrumentType.getInstrumentParameters(InstrumentInputParameter.class, readOnlySource);
  }

  public List<InstrumentOutputParameter> getOutputParameters(InstrumentType instrumentType, InstrumentParameterCaptureMethod captureMethod) {
    List<InstrumentOutputParameter> outputParameters = new ArrayList<InstrumentOutputParameter>();

    for(InstrumentParameter parameter : instrumentType.getInstrumentParameters()) {
      if(parameter instanceof InstrumentOutputParameter) {
        InstrumentOutputParameter outputParameter = (InstrumentOutputParameter) parameter;
        InstrumentParameterCaptureMethod outputParameterCaptureMethod = outputParameter.getCaptureMethod();

        if(outputParameterCaptureMethod.equals(captureMethod)) {
          outputParameters.add(outputParameter);
        }
      }
    }

    return outputParameters;
  }

  public InstrumentOutputParameter getInstrumentOutputParameter(InstrumentType instrumentType, String parameterCode) {
    for(InstrumentParameter parameter : instrumentType.getInstrumentParameters()) {
      if(parameter instanceof InstrumentOutputParameter) {
        if(parameter.getCode().equals(parameterCode)) {
          return (InstrumentOutputParameter) parameter;
        }
      }
    }
    return null;
  }

  public Contraindication getContraindication(InstrumentType instrumentType, String contraindicationCode) {
    for(Contraindication ci : instrumentType.getContraindications()) {
      if(ci.getCode().equals(contraindicationCode)) return ci;
    }
    return null;
  }

  public String getInstrumentInstallPath(InstrumentType type) {
    return instrumentsPath + "/" + type.getName();
  }
}
