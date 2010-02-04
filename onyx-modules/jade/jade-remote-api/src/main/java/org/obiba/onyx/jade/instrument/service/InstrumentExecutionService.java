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

import org.obiba.onyx.util.data.Data;

/**
 * Defines the interface through which code running on local stations may communicate with the server.
 * <p>
 * This interface must be kept simple and should not exchange persisted objects, unless these have no relationship to
 * any other persisted entity.
 */
public interface InstrumentExecutionService {

  /**
   * Returns the name of the instrument's operator.
   * 
   * @return the instrument's operator name.
   */
  public String getInstrumentOperator();

  public String getInstrumentOperatorUsername();

  public Locale getInstrumentOperatorLocale();

  public String getParticipantID();

  public String getParticipantLastName();

  public String getParticipantFirstName();

  public Date getParticipantBirthDate();

  /**
   * Convert a date parameter to a String in the specified format.
   * @param parameter The input parameter from which the date will be retrieved from.
   * @param format The requested date format.
   * @return The formatted date.
   */
  public String getDateAsString(String parameter, SimpleDateFormat dateFormat);

  public String getParticipantGender();

  public int getExpectedMeasureCount();

  public int getCurrentMeasureCount();

  public Map<String, Data> getInputParametersValue(String... parameters);

  public Map<String, String> getInputParametersVendorNames(String... parameters);

  public Data getInputParameterValue(String parameter);

  public void addOutputParameterValues(Map<String, Data> values);

  public void instrumentRunnerError(Exception error);

}
