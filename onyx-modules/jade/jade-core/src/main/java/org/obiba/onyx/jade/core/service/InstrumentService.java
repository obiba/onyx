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

import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.jade.core.domain.instrument.InputSource;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
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
   * Returns the {@link InstrumentParameter} of the specified {@link InstrumentType} with the specified parameter code.
   * 
   * @param instrumentType instrument type
   * @param parameterCode parameter code
   * @return instrument parameter of the given instrument type and having the given parameter code
   */
  public InstrumentParameter getParameterByCode(InstrumentType instrumentType, String parameterCode);

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
   * Shall we expect data from a remote instrument application ?
   * @param instrument
   * @return
   */
  public boolean isInteractiveInstrument(InstrumentType instrument);

  /**
   * Get the {@link InstrumentInputParameter}s that have a {@link InputSource} read only (or not).
   * @param instrument
   * @param readOnlySource
   * @return
   */
  public List<InstrumentInputParameter> getInstrumentInputParameter(InstrumentType instrument, boolean readOnlySource);

  /**
   * Count the {@link InstrumentInputParameter}s that have a {@link InputSource} read only (or not).
   * @param instrument
   * @param readOnlySource
   * @return
   */
  public int countInstrumentInputParameter(InstrumentType instrument, boolean readOnlySource);

  /**
   * Given an {@link InstrumentType}, returns the type's {@link InstrumentOutputParameter}s with the specified
   * {@link InstrumentParameterCaptureMethod}.
   * 
   * @param instrumentType instrument type
   * @param captureMethod capture method
   * @return the type's instrument output parameters with the specified capture method (or an empty list if none)
   */
  public List<InstrumentOutputParameter> getOutputParameters(InstrumentType instrumentType, InstrumentParameterCaptureMethod captureMethod);

  /**
   * Given an {@link InstrumentType}, returns the type's {@link Contraindication} with the specified code.
   * 
   * @param instrumentType instrument type
   * @param contraindicationCode contraindication code
   * @return the type's contraindication witht he specified code
   */
  public Contraindication getContraindication(InstrumentType instrumentType, String contraindicationCode);

  /**
   * Get the install path of the instrument type's specific code.
   * 
   * @param type the instrument
   * @return the context-relative path to the instrument type's code
   */
  public String getInstrumentInstallPath(InstrumentType type);

  /**
   * Get the {@link InstrumentOutputParameter} from the parameterCode.
   * @param instrumentType
   * @param parameterCode
   * @return
   */
  public InstrumentOutputParameter getInstrumentOutputParameter(InstrumentType instrumentType, String parameterCode);

}
