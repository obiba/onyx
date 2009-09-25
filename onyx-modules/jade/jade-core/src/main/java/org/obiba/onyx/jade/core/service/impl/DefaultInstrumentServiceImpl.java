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
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
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

  public String getInstrumentInstallPath(InstrumentType type) {
    return instrumentsPath + "/" + type.getName();
  }

  public List<String> getWorkstationInstrumentTypes(String workstation) {
    List<String> instrumentTypes = new ArrayList<String>();
    Instrument template = new Instrument();
    template.setWorkstation(workstation);

    for(Instrument instrument : getPersistenceManager().match(template)) {
      instrumentTypes.add(instrument.getType());
    }

    return instrumentTypes;
  }

  public Instrument getInstrumentByBarcode(String barcode) {
    Instrument template = new Instrument();
    template.setBarcode(barcode);
    return getPersistenceManager().matchOne(template);
  }

  public void updateInstrument(Instrument instrument) {
    getPersistenceManager().save(instrument);
  }

}
