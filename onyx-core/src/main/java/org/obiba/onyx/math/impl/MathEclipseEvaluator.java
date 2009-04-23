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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matheclipse.parser.client.ast.ASTNode;
import org.matheclipse.parser.client.ast.FunctionNode;
import org.matheclipse.parser.client.eval.DoubleEvaluator;
import org.matheclipse.parser.client.eval.DoubleVariable;
import org.matheclipse.parser.client.eval.IDouble1Function;
import org.matheclipse.parser.client.eval.IDouble2Function;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.math.AbstractAlgorithmEvaluator;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see http://matheclipse.org/en/MathEclipse_Parser
 */
public class MathEclipseEvaluator extends AbstractAlgorithmEvaluator {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(MathEclipseEvaluator.class);

  public static MathEclipseEvaluator getInstance() {
    return new MathEclipseEvaluator();
  }

  public boolean evaluateBoolean(String algorithm, List<Data> operands) {
    DoubleEvaluator engine = createEvaluator();
    double d = engine.evaluate("If[" + defineVariables(engine, algorithm, operands) + ", 1, 0]");
    return d == 1d;
  }

  public double evaluateDouble(String algorithm, List<Data> operands) {
    DoubleEvaluator engine = createEvaluator();
    double d = engine.evaluate(defineVariables(engine, algorithm, operands));
    return d;
  }

  /**
   * Add default variables definition to current evaluation engine.
   * @param engine
   * @param algorithm
   * @param operands
   * @return the new expression (boolean variables are not replaced correctly)
   */
  private String defineVariables(DoubleEvaluator engine, String algorithm, List<Data> operands) {
    String newExpression = algorithm;
    if(operands != null) {
      for(int i = operands.size() - 1; i >= 0; i--) {
        String symbol = getVariableName(i);
        Serializable value = convert(algorithm, symbol, operands.get(i));
        if(Boolean.class.isInstance(value)) {
          newExpression = newExpression.replace(symbol, (Boolean) value ? "True" : "False");
        } else {
          engine.defineVariable(symbol, new DoubleVariable((Double) value));
        }
      }
    }
    for(Map.Entry<String, Data> entry : getDefaultDoubleVariables().entrySet()) {
      engine.defineVariable(entry.getKey(), new DoubleVariable((Double) convert(algorithm, entry.getKey(), entry.getValue())));
    }

    log.debug(newExpression);
    return newExpression;
  }

  /**
   * Add default variables definition to current evaluation engine.
   * @param engine
   * @param algorithm
   * @param participant
   * @return the new expression (boolean variables are not replaced correctly)
   */
  private String defineDefaultVariables(DoubleEvaluator engine, String algorithm, Participant participant) {
    String newExpression = algorithm;
    for(Map.Entry<String, IDataSource> entry : getDefaultVariables().entrySet()) {
      Serializable value = convert(algorithm, entry.getKey(), entry.getValue().getData(participant));
      if(Boolean.class.isInstance(value)) {
        newExpression = newExpression.replace(entry.getKey(), (Boolean) value ? "True" : "False");
      } else {
        engine.defineVariable(entry.getKey(), new DoubleVariable((Double) value));
      }
    }

    log.debug(newExpression);
    return newExpression;
  }

  public boolean evaluateBoolean(String algorithm, Participant participant, List<IDataSource> operands) {
    List<Data> datas = getDatas(participant, operands);

    DoubleEvaluator engine = createEvaluator();
    String newExpression = defineDefaultVariables(engine, algorithm, participant);
    newExpression = defineVariables(engine, newExpression, datas);

    double d = engine.evaluate("If[" + newExpression + ", 1, 0]");
    return d == 1d;
  }

  public double evaluateDouble(String algorithm, Participant participant, List<IDataSource> operands) {
    List<Data> datas = getDatas(participant, operands);

    DoubleEvaluator engine = createEvaluator();
    String newExpression = defineDefaultVariables(engine, algorithm, participant);
    newExpression = defineVariables(engine, newExpression, datas);

    return engine.evaluate(newExpression);
  }

  protected DoubleEvaluator createEvaluator() {
    return new ExtendedDoubleEvaluator();
  }

  /**
   * Get the data from the data sources.
   * @param participant
   * @param operands
   * @return
   */
  private List<Data> getDatas(Participant participant, List<IDataSource> operands) {
    List<Data> datas = null;
    if(operands != null) {
      datas = new ArrayList<Data>();
      for(IDataSource source : operands) {
        datas.add(source.getData(participant));
      }
    }
    return datas;
  }

  private static class ExtendedDoubleEvaluator extends DoubleEvaluator {

    private Map<String, Object> functions = new HashMap<String, Object>();

    public ExtendedDoubleEvaluator() {
      functions.put("Abs", new IDouble1Function() {
        public double evaluate(double arg1) {
          return Math.abs(arg1);
        }
      });
      functions.put("Round", new IDouble2Function() {
        public double evaluate(double arg1, double arg2) {
          return new BigDecimal(arg2).setScale((int) arg1, RoundingMode.HALF_UP).doubleValue();
        }
      });
    }

    @Override
    public double evaluateFunction(FunctionNode functionNode) {
      String symbol = functionNode.getNode(0).toString();
      if(functions.get(symbol) != null) {
        if(functionNode.size() == 2 && functions.get(symbol) instanceof IDouble1Function) {
          return ((IDouble1Function) functions.get(symbol)).evaluate(evaluateNode((ASTNode) functionNode.getNode(1)));
        } else if(functionNode.size() == 3 && functions.get(symbol) instanceof IDouble2Function) {
          return ((IDouble2Function) functions.get(symbol)).evaluate(evaluateNode((ASTNode) functionNode.getNode(1)), evaluateNode((ASTNode) functionNode.getNode(2)));
        }
      }
      return super.evaluateFunction(functionNode);
    }
  }
}
