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
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class DefaultInstrumentServiceImpl extends PersistenceManagerAwareService implements InstrumentService {

  @SuppressWarnings("unused")
  private final Logger log = LoggerFactory.getLogger(getClass());

  private String instrumentsPath;

  private Map<String, InstrumentType> instrumentTypes;

  private UserSessionService userSessionService;

  public void setInstrumentTypes(Map<String, InstrumentType> instrumentTypes) {
    this.instrumentTypes = instrumentTypes;
  }

  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
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
    Instrument persitedIntrument = (instrument.getId() != null) ? getPersistenceManager().get(Instrument.class, instrument.getId()) : instrument;

    if(instrument.getId() != null) {
      persitedIntrument.setName(instrument.getName());
      persitedIntrument.setVendor(instrument.getVendor());
      persitedIntrument.setModel(instrument.getModel());
      persitedIntrument.setSerialNumber(instrument.getSerialNumber());
      persitedIntrument.setWorkstation(instrument.getWorkstation());
    }

    getPersistenceManager().save(persitedIntrument);
  }

  public void updateStatus(Instrument instrument, InstrumentStatus status) {
    Instrument persistedInstrument = getPersistenceManager().get(Instrument.class, instrument.getId());
    persistedInstrument.setStatus(status);

    // ONYX-913: Automatically release instrument when de-activated.
    if(status.equals(InstrumentStatus.INACTIVE)) {
      persistedInstrument.setWorkstation(null);
    }

    getPersistenceManager().save(persistedInstrument);
  }

  public void updateWorkstation(Instrument instrument, String workstation) {
    // ONYX-913: Throw an exception if an attempt is made to assign an inactive instrument
    // to a workstation. Note: The UI should not allow this, but we want to enforce this at
    // the service layer as well.
    if(instrument.getStatus().equals(InstrumentStatus.INACTIVE) && workstation != null) {
      throw new RuntimeException("Inactive instrument cannot be assigned to a workstation (instrument: " + instrument + ", workstation: " + workstation + ")");
    }

    Instrument persistedInstrument = getPersistenceManager().get(Instrument.class, instrument.getId());
    persistedInstrument.setWorkstation(workstation);
    getPersistenceManager().save(persistedInstrument);
  }

  public List<Instrument> getActiveInstrumentsAssignedToCurrentWorkstation(InstrumentType instrumentType) {
    List<Instrument> activeInstrumentsAssignedToCurrentWorkstation = new ArrayList<Instrument>();
    List<Instrument> activeInstruments = getActiveInstruments(instrumentType);
    for(Instrument instrument : activeInstruments) {
      if(instrument.getWorkstation() != null && instrument.getWorkstation().equals(userSessionService.getWorkstation())) {
        activeInstrumentsAssignedToCurrentWorkstation.add(instrument);
      }
    }
    return activeInstrumentsAssignedToCurrentWorkstation;
  }

  public List<Instrument> getActiveInstrumentsForCurrentWorkstation(InstrumentType instrumentType) {
    List<Instrument> activeInstrumentsForCurrentWorkstation = new ArrayList<Instrument>();
    List<Instrument> activeInstruments = getActiveInstruments(instrumentType);
    for(Instrument instrument : activeInstruments) {
      if(instrument.getWorkstation() == null || instrument.getWorkstation().equals(userSessionService.getWorkstation())) {
        activeInstrumentsForCurrentWorkstation.add(instrument);
      }
    }
    return activeInstrumentsForCurrentWorkstation;
  }

  public boolean isActiveInstrumentOfCurrentWorkstation(Instrument instrument) {
    List<Instrument> activeInstruments = getActiveInstrumentsForCurrentWorkstation(getInstrumentType(instrument.getType()));
    for(Instrument activeInstrument : activeInstruments) {
      if(activeInstrument.getBarcode() == instrument.getBarcode()) {
        return true;
      }
    }
    return false;
  }

  public void deleteInstrument(Instrument instrument) {
    if(instrument == null) throw new IllegalArgumentException("The instrument must not be null.");
    getPersistenceManager().delete(instrument);
  }

}
