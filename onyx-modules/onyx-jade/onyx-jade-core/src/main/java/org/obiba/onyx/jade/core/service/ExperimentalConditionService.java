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

import java.util.Date;
import java.util.List;

import org.obiba.onyx.core.domain.Attribute;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLog;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;

/**
 * This service provides methods to register and retrieve {@link ExperimentalConditionLog}s and
 * {@link InstrumentCalibration}s as well as access the recorded calibration values themselves.
 */
public interface ExperimentalConditionService {

  /** Instrument barcode attribute name. */
  public final static String INSTRUMENT_BARCODE = "INSTRUMENT_BARCODE";

  /**
   * Initializes the service. In particular, it registers the configured experimental conditions.
   */
  public void init();

  /**
   * Persists an {@link ExperimentalCondition} and all its associated {@link ExperimentalConditionValues}.
   * @param experimentalCondition ExperimentalCondition to persist.
   */
  public void save(ExperimentalCondition experimentalCondition);

  /**
   * Returns a list all all the available {@link ExperimentalConditionLog}s.
   * @return List of all ExperimentalConditionLogs.
   */
  public List<ExperimentalConditionLog> getExperimentalConditionLog();

  /**
   * Register an {@link ExperimentalConditionLog} in order to make it available via this service.
   * @param log The ExperimentalConditionLog to be registered.
   */
  public void register(ExperimentalConditionLog log);

  /**
   * Returns a list of {@link ExperimentalCondition}s. If a null template is provided, then all ExperimentalConditions
   * will be returned.
   * @param template The template will be matched on the following attributes: "id", "name", "workstation" as well as an
   * {@link ExperimentalConditionValue} with an attributeName equal to "INSTRUMENT_BARCODE".
   * @return A list of ExperimentalConditions matching the supplied template, in chronological order (earlier first)
   */
  public List<ExperimentalCondition> getExperimentalConditions(ExperimentalCondition template);

  /**
   * Returns the {@link ExperimentalConditionLog} with the provided name.
   * @param name The name of the ExperimentalConditionLog.
   * @return
   * @throws IllegalStateException If no ExperimentalLogCondition exists with the provided name.
   */
  public ExperimentalConditionLog getExperimentalConditionLogByName(String name);

  /**
   * Returns "non-instrument-related" conditions ({@link ExperimentalCondition}s) for the specified workstation.
   * @param workstationId the workstation's indentifier
   * @return "non-instrument-related" conditions for the specified workstation, in chronological order (earlier first)
   */
  public List<ExperimentalCondition> getNonInstrumentRelatedConditions(String workstationId);

  /**
   * Returns the instrument calibrations ({@link ExperimentalCondition}s) for the specified instrument.
   * @param instrumentBarcode the instrument's barcode
   * @return instrument calibrations for the specified instrument, in chronological order (earlier first)
   */
  public List<ExperimentalCondition> getInstrumentCalibrations(String instrumentBarcode);

  /**
   * Returns true in an {@link InstrumentCalibration} exists for the given instrumentType.
   * @param instrumentType
   * @return True if an InstrumentCalibration exists for the instrumentType.
   */
  public boolean instrumentCalibrationExists(String instrumentType);

  /**
   * Returns a list of {@link InstrumentCalibration}s for the given instrumentType.
   * @param instrumentType
   * @return A list of InstrumentCalibrations.
   */
  public List<InstrumentCalibration> getInstrumentCalibrationsByType(String instrumentType);

  /**
   * Returns an {@link Attribute} for the given experimentalConditionValue.
   * @param experimentalConditionValue
   * @return
   * @throws IllegalStateException If the attribute does not exist.
   */
  public Attribute getAttribute(ExperimentalConditionValue experimentalConditionValue);

  /**
   * Deletes the supplied {@link ExperimentalCondition} and all it's children.
   * @param experimentalCondition ExperimentalCondition to delete.
   * @throws IllegalArgumentException if experimentalCondition is null;
   */
  public void deleteExperimentalCondition(ExperimentalCondition experimentalCondition);

  /**
   * Returns a list of "non-instrument-related" conditions ({@link ExperimentalCondition}s) for the specified
   * workstation, recorded after the specified date.
   * 
   * @param workstationId workstation id
   * @param date date
   * @return list of "non-instrument-related" conditions for the specified workstation, recorded after the specified
   * date, in chronological order (earlier first)
   */
  public List<ExperimentalCondition> getNonInstrumentRelatedConditionsRecordedAfter(String workstationId, Date date);

  /**
   * Returns a list of instrument calibrations ({@link ExperimentalCondition}s) for the specified instrument, recorded
   * after the specified date.
   * 
   * @param insturmentBarcode the instrument's barcode
   * @param date date
   * @return list of instrument calibrations for the specified instrument, recorded after the specified date, in
   * chronological order (earlier first)
   */
  public List<ExperimentalCondition> getInstrumentCalibrationsRecordedAfter(String instrumentBarcode, Date date);
}
