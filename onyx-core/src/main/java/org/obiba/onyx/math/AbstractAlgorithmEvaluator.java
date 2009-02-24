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

/**
 * Base class for sharing the conversion from {@link Data} to double or boolean value. Dates are turned into double.
 */
public abstract class AbstractAlgorithmEvaluator implements IAlgorithmEvaluator {

  public static final String CURRENT_DATE = "currentDate";

  public static final String CURRENT_YEAR = "currentYear";

  public static final String CURRENT_MONTH = "currentMonth";

  public static final String CURRENT_DAY = "currentDay";

  private Map<String, IDataSource> defaultVariables;

  /**
   * Returns either a not null object of type Boolean or Double.
   * @param operand
   * @return
   */
  protected Serializable convert(Data operand) {
    if(!nullValueAllowed()) {
      if(operand == null) {
        throw new IllegalArgumentException("Operand cannot be null.");
      }

      if(operand.getValue() == null) {
        throw new IllegalArgumentException("Operand value cannot be null.");
      }
    } else {
      return null;
    }

    if(operand.getType().equals(DataType.DATE)) {
      Date date = operand.getValue();
      return new Long(date.getTime()).doubleValue();
    } else {
      String value = operand.getValueAsString();

      try {
        Double d = DataBuilder.build(DataType.DECIMAL, value).getValue();
        return d;
      } catch(Exception e) {
        try {
          Boolean b = DataBuilder.build(DataType.BOOLEAN, value).getValue();
          return b;
        } catch(Exception e2) {

        }
      }
    }

    throw new IllegalArgumentException("Cannot convert the operand value neither as a double nor as a boolean: " + operand);
  }

  /**
   * Get the full default variables, based on data sources.
   * @return
   */
  protected Map<String, IDataSource> getDefaultVariables() {
    if(defaultVariables == null) {
      defaultVariables = new HashMap<String, IDataSource>();

      defaultVariables.put(getVariableName(CURRENT_DATE), new CurrentDateSource());
      defaultVariables.put(getVariableName(CURRENT_YEAR), new CurrentDateSource(Calendar.YEAR));
      defaultVariables.put(getVariableName(CURRENT_MONTH), new CurrentDateSource(Calendar.MONTH));
      defaultVariables.put(getVariableName(CURRENT_DAY), new CurrentDateSource(Calendar.DATE));
    }

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
   * Allow null values in expression (especially for booleans).
   * @return
   */
  protected abstract boolean nullValueAllowed();

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
