/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.math;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.core.data.CurrentDateSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for sharing the conversion from {@link Data} to double or boolean value. Dates are turned into double.
 */
public abstract class AbstractAlgorithmEvaluator implements IAlgorithmEvaluator, Serializable {

  private static final Logger log = LoggerFactory.getLogger(AbstractAlgorithmEvaluator.class);

  public static final String CURRENT_DATE = "currentDate";

  public static final String CURRENT_YEAR = "currentYear";

  public static final String CURRENT_MONTH = "currentMonth";

  public static final String CURRENT_DAY = "currentDay";

  /**
   * Returns either a not null object of type Boolean or Double.
   * @param operand
   * @return
   */
  protected Serializable convert(String symbol, Data operand) {
    if(operand == null) {
      throw new IllegalArgumentException("Operand cannot be null: " + symbol);
      // log.warn("Operand cannot be null: " + symbol);
      // return new Double(0);
    }

    switch(operand.getType()) {
    case DATE:
      Date date = operand.getValue();
      return new Long(date.getTime()).doubleValue();

    case BOOLEAN:
      Boolean b = operand.getValue();
      if(b == null) {
        b = Boolean.FALSE;
      }
      return b;

    default:
      String value = operand.getValueAsString();
      if(value == null) {
        return new Double(0d);
      } else {
        try {
          Double d = DataBuilder.build(DataType.DECIMAL, value).getValue();
          return d;
        } catch(Exception e) {
          log.error("Cannot convert: " + operand, e);
        }
      }
      break;
    }

    throw new IllegalArgumentException("Cannot convert the operand (" + symbol + ") value neither as a double nor as a boolean: " + operand);
  }

  /**
   * Get the full default variables, based on data sources.
   * @return
   */
  protected Map<String, IDataSource> getDefaultVariables() {
    Map<String, IDataSource> defaultVariables = new HashMap<String, IDataSource>();

    defaultVariables.put(getVariableName(CURRENT_DATE), new CurrentDateSource());
    defaultVariables.put(getVariableName(CURRENT_YEAR), new CurrentDateSource(Calendar.YEAR));
    defaultVariables.put(getVariableName(CURRENT_MONTH), new CurrentDateSource(Calendar.MONTH));
    defaultVariables.put(getVariableName(CURRENT_DAY), new CurrentDateSource(Calendar.DATE));

    return defaultVariables;
  }

  /**
   * Get default variables based on Data, meaning ones that are Participant independent.
   * @return
   */
  protected Map<String, Data> getDefaultDoubleVariables() {
    Map<String, Data> defaultDoubleVariables = new HashMap<String, Data>();

    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    defaultDoubleVariables.put(getVariableName(CURRENT_DATE), DataBuilder.buildDate(cal.getTime()));
    defaultDoubleVariables.put(getVariableName(CURRENT_YEAR), DataBuilder.buildInteger(cal.get(Calendar.YEAR)));
    defaultDoubleVariables.put(getVariableName(CURRENT_MONTH), DataBuilder.buildInteger(cal.get(Calendar.MONTH)));
    defaultDoubleVariables.put(getVariableName(CURRENT_DAY), DataBuilder.buildInteger(cal.get(Calendar.DATE)));

    return defaultDoubleVariables;
  }

  /**
   * Given the index of the operand in the operands list, get the expected variable symbol.
   * @param index
   * @return
   */
  protected String getVariableName(int index) {
    return "$" + (index + 1);
  }

  /**
   * Given a variable name, built a variable symbol.
   * @param name
   * @return
   */
  protected String getVariableName(String name) {
    return "$" + name;
  }

}
