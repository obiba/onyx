/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.answer;

import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Arithmetic operands, resolved with Doubles.
 */
public enum ArithmeticOperator {

  plus {
    @Override
    protected Double resolveInternal(Double left, Double right) {
      return left + right;
    }
  },

  minus {
    @Override
    protected Double resolveInternal(Double left, Double right) {
      return left - right;
    }
  },

  multiply {
    @Override
    protected Double resolveInternal(Double left, Double right) {
      return left * right;
    }
  },

  divide {
    @Override
    protected Double resolveInternal(Double left, Double right) {
      return left / right;
    }
  };

  /**
   * Resolve the arithmetic operation, given operands.
   * @param left
   * @param right
   * @return
   */
  protected abstract Double resolveInternal(Double left, Double right);

  /**
   * Resolve the arithmetic operation, given operands.
   * @param left
   * @param right
   * @return
   */
  public Double resolve(Data left, Data right) {
    return resolveInternal(convertToDouble(left), convertToDouble(right));
  }

  /**
   * Resolve the arithmetic operation, given operands.
   * @param left
   * @param right
   * @param dataType type of the resulting data (can be null, if so it is guessed with the operand data types)
   * @return
   */
  public Data resolve(Data left, Data right, DataType dataType) {
    Double result = resolve(left, right);

    Data data;
    if(dataType == null) {
      dataType = DataType.DECIMAL;
      // try to guess the data type
      if(left != null) {
        dataType = left.getType();
        if(right != null && right.getType().equals(DataType.DECIMAL)) {
          dataType = right.getType();
        }
      } else if(right != null) {
        dataType = right.getType();
      }
    }

    if(dataType.equals(DataType.INTEGER)) {
      data = DataBuilder.buildInteger(Math.round(result));
    } else if(dataType.equals(DataType.TEXT)) {
      data = DataBuilder.buildText(result.toString());
    } else {
      data = DataBuilder.buildDecimal(result);
    }

    return data;
  }

  /**
   * Convert the data value as a double for operation resolution.
   * @param data
   * @return
   */
  private Double convertToDouble(Data data) {
    if(data == null) {
      data = DataBuilder.buildDecimal(0d);
    } else {
      if(data.getType().equals(DataType.TEXT)) {
        data = DataBuilder.buildDecimal(data.getValueAsString());
      } else if(!data.getType().equals(DataType.INTEGER) && !data.getType().equals(DataType.DECIMAL)) {
        throw new IllegalArgumentException("Wrong DataType for right operand: INTEGER or DECIMAL expected, " + data.getType() + " found.");
      }
    }

    return Double.valueOf(data.getValueAsString());
  }
}
