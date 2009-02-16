/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.data;

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.math.IAlgorithmEvaluator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * <p>
 * Given an algorithm (boolean, arithmetic, conditional etc. expressions) and operands provided by a list of IDataSource
 * (this list is optional), the equation is evaluated. Only {@link DataType} BOOLEAN, INTEGER and DECIMAL are supported
 * as a result of the expression evaluation. Operands are data source which data value can be turned either in DECIMAL
 * or BOOLEAN. Expression variable syntax is $n as the nth operand.
 * </p>
 * 
 * Examples:
 * <ul>
 * <li>($1 + $2 + $3)/3 returns the mean value</li>
 * <li>$1^2*$2^2-1 returns the double value for this polynomial expression</li>
 * <li>$1 <= ($2 - $3) returns a boolean comparing the two statements</li>
 * <li>$1 && ($2 || $3) returns a boolean value</li>
 * <li>If[$1>2, 23, $2] returns a double value conditionally to a boolean statement</li>
 * <li>usual mathematical functions are supported ...
 * <li>
 * </ul>
 * 
 * @see http://matheclipse.org/en/Using_MathEclipse#Syntax_Overview
 */
public class ComputingDataSource extends AbstractMultipleDataSource {

  private static final long serialVersionUID = 1L;

  private IAlgorithmEvaluator algorithmEvaluator;

  private String expression;

  private DataType type;

  private String unit;

  /**
   * 
   * @return null if type is not one of BOOLEAN, INTEGER, DECIMAL
   */
  public Data getData(Participant participant) {
    List<Data> operands = new ArrayList<Data>();
    for(IDataSource source : getDataSources()) {
      operands.add(source.getData(participant));
    }

    if(type.equals(DataType.BOOLEAN)) {
      return DataBuilder.buildBoolean(algorithmEvaluator.evaluateBoolean(expression, operands));
    } else if(type.isNumberType()) {
      double d = algorithmEvaluator.evaluateDouble(expression, operands);
      if(type.equals(DataType.DECIMAL)) {
        return DataBuilder.buildDecimal(d);
      } else {
        return DataBuilder.buildInteger(Long.valueOf(Math.round(d)));
      }
    }

    return null;
  }

  public String getUnit() {
    return unit;
  }

  public ComputingDataSource(DataType type, String expression) {
    super();
    this.type = type;
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public DataType getType() {
    return type;
  }

  public void setType(DataType type) {
    this.type = type;
  }

  public void setAlgorithmEvaluator(IAlgorithmEvaluator algorithmEvaluator) {
    this.algorithmEvaluator = algorithmEvaluator;
  }

}
