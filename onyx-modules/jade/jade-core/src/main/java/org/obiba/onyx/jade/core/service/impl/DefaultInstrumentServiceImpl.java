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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultInstrumentServiceImpl extends PersistenceManagerAwareService implements InstrumentService, InitializingBean {

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
    InstrumentType template = new InstrumentType(name, null);
    return getPersistenceManager().matchOne(template);
  }

  public Map<String,InstrumentType> getInstrumentTypes() {
    Map<String, InstrumentType> instrumentTypes = new HashMap<String, InstrumentType>();
    for(InstrumentType instrumentType : getPersistenceManager().list(InstrumentType.class)) {
      instrumentTypes.put(instrumentType.getName(), instrumentType);
    }
    return instrumentTypes;
  }

  public List<Instrument> getInstruments(String typeName) {
    return getInstruments(getInstrumentType(typeName));
  }

  public List<Instrument> getInstruments(InstrumentType instrumentType) {
    Instrument template = new Instrument();
    template.setInstrumentType(instrumentType);

    return getPersistenceManager().match(template);
  }

  public List<Instrument> getActiveInstruments(InstrumentType instrumentType) {
    Instrument template = new Instrument();
    template.setInstrumentType(instrumentType);
    template.setStatus(InstrumentStatus.ACTIVE);

    return getPersistenceManager().match(template);
  }

  public boolean isInteractiveInstrument(InstrumentType instrumentType) {
    if(instrumentType == null) return false;

    InstrumentOutputParameter template = new InstrumentOutputParameter();
    template.setInstrumentType(instrumentType);
    template.setCaptureMethod(InstrumentParameterCaptureMethod.AUTOMATIC);

    return getPersistenceManager().count(template) > 0;
  }

  public int countInstrumentInputParameter(InstrumentType instrument, boolean readOnlySource) {
    return getInstrumentInputParameter(instrument, readOnlySource).size();
  }

  public List<InstrumentInputParameter> getInstrumentInputParameter(InstrumentType instrumentType, boolean readOnlySource) {
    List<InstrumentInputParameter> list = new ArrayList<InstrumentInputParameter>();
    InstrumentInputParameter template = new InstrumentInputParameter();
    template.setInstrumentType(instrumentType);

    for(InstrumentInputParameter param : getPersistenceManager().match(template)) {
      if(param.getInputSource() != null && param.getInputSource().isReadOnly() == readOnlySource) {
        list.add(param);
      }
    }

    return list;
  }

  public String getInstrumentInstallPath(InstrumentType type) {
    return instrumentsPath + "/" + type.getName();
  }

  public void afterPropertiesSet() throws Exception {

    if(instrumentTypes != null && persistenceManager.list(InstrumentType.class).size() == 0) {

      log.debug("Persisting InstrumentTypes injected by InstrumentTypeFactory...");
      for(InstrumentType instrumentType : instrumentTypes.values()) {
        log.debug(instrumentType.getName());

        persistenceManager.save(instrumentType);

        persistenceManager.list(InstrumentType.class).size();

      }
      log.debug("Completed persisting InstrumentTypes.");

    }

  }

}
