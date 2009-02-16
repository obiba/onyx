/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.math.impl;

import java.io.Serializable;
import java.util.List;

import org.matheclipse.parser.client.eval.DoubleEvaluator;
import org.matheclipse.parser.client.eval.DoubleVariable;
import org.obiba.onyx.math.AbstractAlgorithmEvaluator;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see http://matheclipse.org/en/MathEclipse_Parser
 */
public class MathEclipseEvaluator extends AbstractAlgorithmEvaluator {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(MathEclipseEvaluator.class);

  public static MathEclipseEvaluator getInstance() {
    return new MathEclipseEvaluator();
  }

  public boolean evaluateBoolean(String algorithm, List<Data> operands) {
    DoubleEvaluator engine = new DoubleEvaluator();
    double d = engine.evaluate("If[" + defineVariables(engine, algorithm, operands) + ", 1, 0]");
    return d == 1d;
  }

  public double evaluateDouble(String algorithm, List<Data> operands) {
    DoubleEvaluator engine = new DoubleEvaluator();
    double d = engine.evaluate(defineVariables(engine, algorithm, operands));
    return d;
  }

  private String defineVariables(DoubleEvaluator engine, String algorithm, List<Data> operands) {
    String newExpression = algorithm;
    if(operands != null) {
      for(int i = operands.size() - 1; i >= 0; i--) {
        Serializable value = convert(operands.get(i));
        if(Boolean.class.isInstance(value)) {
          newExpression = newExpression.replace(getVariableName(i), (Boolean) value ? "True" : "False");
        } else {
          engine.defineVariable(getVariableName(i), new DoubleVariable((Double) value));
        }
      }
    }
    log.debug(newExpression);
    return newExpression;
  }

  @Override
  protected boolean nullValueAllowed() {
    return false;
  }

}
