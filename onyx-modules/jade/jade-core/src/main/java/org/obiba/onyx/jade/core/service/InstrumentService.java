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

import org.obiba.onyx.jade.core.domain.instrument.InputSource;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;

public interface InstrumentService {
  
  /**
   * Create an instrument type.
   * @param name
   * @param description
   * @return
   */
  public InstrumentType createInstrumentType(String name, String description);
  
  /**
   * Get the instrument type by its name.
   * @param name
   * @return null if not found
   */
  public InstrumentType getInstrumentType(String name);
  
  /**
   * Add an depending instrument type to given instrument type.
   * @param type
   * @param dependency
   */
  public void addInstrumentTypeDependency(InstrumentType type, InstrumentType dependency);
  
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
   * Get the active instruments for given instrument type.
   * @param instrumentType
   * @return
   */
  public List<Instrument> getActiveInstruments(InstrumentType instrumentType);
  
  /**
   * Add an instrument to an instrument type.
   * @param instrumentType
   * @param instrument
   */
  public void addInstrument(InstrumentType instrumentType, Instrument instrument);
  
  /**
   * Shall we expect data from a remote instrument application ?
   * @param instrument
   * @return
   */
  public boolean isInteractiveInstrument(Instrument instrument);
  
  /**
   * Get the {@link InstrumentInputParameter}s that have a {@link InputSource} read only (or not).
   * @param instrument
   * @param readOnlySource
   * @return
   */
  public List<InstrumentInputParameter> getInstrumentInputParameter(Instrument instrument, boolean readOnlySource);
  
  /**
   * Count the {@link InstrumentInputParameter}s that have a {@link InputSource} read only (or not).
   * @param instrument
   * @param readOnlySource
   * @return
   */
  public int countInstrumentInputParameter(Instrument instrument, boolean readOnlySource);
}
