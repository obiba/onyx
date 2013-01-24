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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.obiba.onyx.util.data.Data;

/**
 * Defines the interface through which code running on local stations may communicate with the server.
 */
public interface InstrumentExecutionService {

  // Developer note: This interface must be kept simple and should not exchange persisted objects,
  // unless these have no relationship to any other persisted entity.

  //
  // Operator methods
  //

  /**
   * Get the instrument's operator full name.
   */
  String getInstrumentOperator();

  /**
   * Get the instrument's operator login name.
   */
  String getInstrumentOperatorUsername();

  /**
   * Get the instrument's operator user interface language.
   */
  Locale getInstrumentOperatorLocale();

  //
  // Participant methods
  //

  /**
   * Get the participant's identifier in Onyx.
   */
  String getParticipantID();

  /**
   * Get the participant's last name.
   */
  String getParticipantLastName();

  /**
   * Get the participant's first name.
   */
  String getParticipantFirstName();

  /**
   * Get the participant's birth date.
   */
  Date getParticipantBirthDate();

  /**
   * Get the participant's gender.
   */
  String getParticipantGender();

  /**
   * Convert a date parameter to a String in the specified format.
   * @param parameter The input parameter from which the date will be retrieved from.
   * @param format The requested date format.
   * @return The formatted date.
   */
  String getDateAsString(String parameter, SimpleDateFormat dateFormat);

  //
  // Instrument configuration
  //

  /**
   * Get the number of expected measures: can be a calculated (for instance more measures could be required to
   * increase the quality of data already collected).
   */
  int getExpectedMeasureCount();

  /**
   * Current number of measures already sent to the server.
   */
  int getCurrentMeasureCount();

  /**
   * Multiple measures are expected (default is false).
   */
  boolean isRepeatableMeasure();

  /**
   * Check that the input parameter with the given name is part of the instrument configuration. This is to be
   * checked before getting data for this parameter.
   * @param parameter
   * @return
   */
  boolean hasInputParameter(String parameter);

  /**
   * Get input data for the list of input parameter names.
   * @param parameters
   * @return
   */
  Map<String, Data> getInputParametersValue(String... parameters);

  /**
   * Get the vendor names of given input parameter names (the name of parameter can be different in Onyx and in the
   * instrument).
   * @param parameters
   * @return
   */
  Map<String, String> getInputParametersVendorNames(String... parameters);

  /**
   * Get a specific input parameter data.
   * @param parameter
   * @return
   */
  Data getInputParameterValue(String parameter);

  /**
   * Check that the output parameter with the given name is part of the instrument configuration. This is to be
   * checked before sending data for this parameter.
   * @param parameter
   * @return
   */
  boolean hasOutputParameter(String parameter);

  /**
   * Get a set of expected output parameters using their vendor name.
   */
  Set<String> getExpectedOutputParameterVendorNames();

  /**
   * Sends the data collected for each output parameter name.
   * @param values
   */
  void addOutputParameterValues(Map<String, Data> values);


  /**
   * To be called in case of a fatal error occurs when interacting with the instrument.
   * @param error
   */
  void instrumentRunnerError(Exception error);

}
