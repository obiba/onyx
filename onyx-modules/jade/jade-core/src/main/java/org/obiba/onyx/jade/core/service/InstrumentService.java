/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service;

import java.util.List;
import java.util.Map;

import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentMeasurementType;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;

public interface InstrumentService {

  /**
   * Get the instrument type by its name.
   * @param name
   * @return null if not found
   */
  public InstrumentType getInstrumentType(String name);

  /**
   * Returns the list of all {@code InstrumentType}s
   * @return all {@code InstrumentType}s
   */
  public Map<String, InstrumentType> getInstrumentTypes();

  /**
   * Get the instruments for the given instrument type name.
   * @param typeName
   * @return
   */
  public List<Instrument> getInstruments(String typeName);

  /**
   * Get the instruments for given instrument type.
   * @param instrumentType
   * @return
   */
  public List<Instrument> getInstruments(InstrumentType instrumentType);

  /**
   * Get the install path of the instrument type's specific code.
   * 
   * @param type the instrument
   * @return the context-relative path to the instrument type's code
   */
  public String getInstrumentInstallPath(InstrumentType type);

  /**
   * Get the instrument types for given workstation.
   * @param workstation
   * @return
   */
  // public List<String> getWorkstationInstrumentTypes(String workstation);
  /**
   * Get the instruments for given workstation.
   * @param workstation, paging, clauses
   * @return
   */
  public List<Instrument> getWorkstationInstruments(String workstation, PagingClause paging, SortingClause... clauses);

  /**
   * Gets the list of instruments of specific type assigned to the current workstation.
   * @param instrumentType The instrument type
   * @return A list of instrument
   */
  public List<Instrument> getActiveInstrumentsAssignedToCurrentWorkstation(InstrumentType instrumentType);

  /**
   * Gets the list of instruments available to the current workstation (both assigned and nomadic instruments).
   * @param instrumentType The instrument type
   * @return A list of instrument
   */
  public List<Instrument> getActiveInstrumentsForCurrentWorkstation(InstrumentType instrumentType);

  /**
   * Checks if a specific instrument is available to be used from current workstation.
   * @param instrument The instrument to check
   * @return True if available, false if available
   */
  public boolean isActiveInstrumentOfCurrentWorkstation(Instrument instrument, InstrumentType instrumentType);

  /**
   * Get the number of instruments for given workstation.
   * @param workstation
   * @return
   */
  public int countWorkstationInstruments(String workstation);

  /**
   * Get the instrument for given barcode.
   * @param barcode
   * @return
   */
  public Instrument getInstrumentByBarcode(String barcode);

  /**
   * Save the instrument.
   * @param instrument
   */
  public void updateInstrument(Instrument instrument);

  /**
   * Add a type to the instrument, ignore if type was already added.
   * @param instrument, type
   */
  public void addInstrumentMeasurementType(Instrument instrument, InstrumentMeasurementType type);

  /**
   * Update the instrument's status.
   * @param instrument, status
   */
  public void updateStatus(Instrument instrument, InstrumentStatus status);

  /**
   * Update the instrument's workstation.
   * @param instrument, workstation
   */
  public void updateWorkstation(Instrument instrument, String workstation);

  /**
   * Deletes the supplied {@link Instrument}.
   * @param instrument Instrument to delete.
   * @throws IllegalArgumentException if instrument is null.
   */
  public void deleteInstrument(Instrument instrument);

  /**
   * Delete the instrument measurement type association, and delete the instrument if there is no more instrument
   * measurement types.
   * @param type
   */
  public void deleteInstrumentMeasurementType(InstrumentMeasurementType type);

  /**
   * Get the instruments and types for given workstation.
   * @param workstation, paging, clauses
   * @return
   */
  public List<InstrumentMeasurementType> getWorkstationInstrumentMeasurementTypes(String workstation, PagingClause paging, SortingClause... clauses);

  /**
   * Get the number of instruments and types for given workstation.
   * @param workstation
   * @return
   */
  public int countWorkstationInstrumentMeasurementTypes(String workstation);

  /**
   * Get the instrument types associated to the given instrument.
   * @param instrument
   * @return
   */
  public List<InstrumentMeasurementType> getWorkstationInstrumentMeasurementTypes(Instrument instrument);

}
