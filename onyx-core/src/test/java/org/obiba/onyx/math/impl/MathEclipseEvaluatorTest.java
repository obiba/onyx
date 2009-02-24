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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.matheclipse.parser.client.eval.BooleanVariable;
import org.matheclipse.parser.client.eval.DoubleEvaluator;
import org.obiba.onyx.core.data.CurrentDateSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;

/**
 * 
 */
public class MathEclipseEvaluatorTest {

  @Test
  public void testEval001() {
    check("42", "42.0");
    check("1.5", "1.5");
    check("-42", "-42.0");
    check("+42", "42.0");
    check("-42.1", "-42.1");
    check("+42.2", "42.2");
    check("-3/4", "-0.75");
    check("+3/4", "0.75");
    check("3^3", "27.0");
    check("2+2*2", "6.0");
    check("2^9", "512.0");
    check("2^3^2", "512.0");
    check("(2^3)^2", "64.0");
    check("3+4*7", "31.0");
    check("3+4*7*3", "87.0");
    check("1+2+3+4*7*3", "90.0");
    // calculate in Radians
    check("Sin[Cos[3.2]]", "-0.8405484252742996");
    // Pi / 2
    check("90.0*Degree", "1.5707963267948966");
    check("Pi/2", "1.5707963267948966");
    check("Sin[Pi/2*Cos[Pi]]", "-1.0");

    check("Max[0,-42,Pi,12]", "12.0");
    check("Min[0,-42,Pi,12]", "-42.0");

    // check("Random[]", "-1.0");
  }

  @Test
  public void testEval002() {
    check("If[3/4<0.51, 1.1, -1.2]", "-1.2");
    check("If[True, 1.1, -1.2]", "1.1");
    check("If[3/4>0.51 && 3/8>0.1, 1.1, -1.2]", "1.1");
    check("If[3/4>0.51 || 3/8>0.1, 1.1, -1.2]", "1.1");
    check("If[!(3/4>0.51 || 3/8>0.1), 1.1, -1.2]", "-1.2");
    check("If[True && False, 1.1, -1.2]", "-1.2");
    check("If[(True && (False || True)), 1, 0]", "1");
    check("If[(False && (False || True)), 1, 0]", "0");
    check("If[(True && (False || False)), 1, 0]", "0");
    // cannot evaluate a boolean expression directly
    // see testEval008 instead
    // check("True && (False || False)", "0");
  }

  @Test
  public void testEval003() {
    try {
      Assert.assertEquals(-1.0, MathEclipseEvaluator.getInstance().evaluateDouble("Sin[Pi/2*Cos[Pi]]", null));
    } catch(Exception e) {
      e.printStackTrace();
      Assert.assertEquals("", e.getMessage());
    }
  }

  @Test
  public void testEval004() {
    try {
      String expression = "$1^2+3";
      List<Data> operands = Arrays.asList(new Data[] { DataBuilder.buildDecimal(3.0) });
      Assert.assertEquals(12.0, MathEclipseEvaluator.getInstance().evaluateDouble(expression, operands));

      operands = Arrays.asList(new Data[] { DataBuilder.buildDecimal(4.0) });
      Assert.assertEquals(19.0, MathEclipseEvaluator.getInstance().evaluateDouble(expression, operands));

    } catch(Exception e) {
      e.printStackTrace();
      Assert.assertEquals("", e.getMessage());
    }
  }

  @Test
  public void testEval005() {
    try {
      String expression = "x^2*x^2-1";
      List<Data> operands = Arrays.asList(new Data[] { DataBuilder.buildDecimal(3.0) });
      MathEclipseEvaluator.getInstance().evaluateDouble(expression, operands);
      Assert.fail();
    } catch(Exception e) {

    }
  }

  @Test
  public void testEval006() {
    try {
      String expression = "$1^2*$1^2-1";
      List<Data> operands = Arrays.asList(new Data[] { DataBuilder.buildDecimal(3.0) });
      Assert.assertEquals(80.0, MathEclipseEvaluator.getInstance().evaluateDouble(expression, operands));

      operands = Arrays.asList(new Data[] { DataBuilder.buildDecimal(4.0) });
      Assert.assertEquals(255.0, MathEclipseEvaluator.getInstance().evaluateDouble(expression, operands));
    } catch(Exception e) {
      e.printStackTrace();
      Assert.assertEquals("", e.getMessage());
    }
  }

  // does not work, see testEval008 instead
  // boolean variables are not substituted
  // DoubleEvaluator, line 457, fix is:
  // BooleanVariable v = fBooleanVariables.get(node.toString());
  // @Test
  public void testEval007() {
    try {
      DoubleEvaluator engine = new DoubleEvaluator();

      BooleanVariable vb = new BooleanVariable(true);
      engine.defineVariable("$1", vb);
      BooleanVariable vb2 = new BooleanVariable(true);
      engine.defineVariable("$2", vb2);
      double d = engine.evaluate("If[$1 && $2, 1, 0]");
      Assert.assertEquals(d, 1d);
      vb.setValue(false);
      d = engine.evaluate();
      Assert.assertEquals(d, 0d);
    } catch(Exception e) {
      e.printStackTrace();
      Assert.assertEquals("", e.getMessage());
    }
  }

  @Test
  public void testEval008() {
    try {
      String expression = "$1 && ($2 || $3)";
      List<Data> operands = Arrays.asList(new Data[] { DataBuilder.buildBoolean(true), DataBuilder.buildBoolean(false), DataBuilder.buildBoolean(true) });
      Assert.assertEquals(true, MathEclipseEvaluator.getInstance().evaluateBoolean(expression, operands));

      operands = Arrays.asList(new Data[] { DataBuilder.buildBoolean(true), DataBuilder.buildBoolean(false), DataBuilder.buildBoolean(false) });
      Assert.assertEquals(false, MathEclipseEvaluator.getInstance().evaluateBoolean(expression, operands));
    } catch(Exception e) {
      e.printStackTrace();
      Assert.assertEquals("", e.getMessage());
    }
  }

  @Test
  public void testEval009() {
    try {
      String expression = "$1 && ($2>3 || $3)";
      List<Data> operands = Arrays.asList(new Data[] { DataBuilder.buildBoolean(true), DataBuilder.buildDecimal(0d), DataBuilder.buildBoolean(true) });
      Assert.assertEquals(true, MathEclipseEvaluator.getInstance().evaluateBoolean(expression, operands));
    } catch(Exception e) {
      e.printStackTrace();
      Assert.assertEquals("", e.getMessage());
    }
  }

  // does not work, same as testEval007
  // @Test
  public void testEval010() {
    try {
      BooleanVariable vd = new BooleanVariable(true);
      DoubleEvaluator engine = new DoubleEvaluator();
      engine.defineVariable("x", vd);
      double d = engine.evaluate("(If[x,3.0,0])^2+3");
      Assert.assertEquals(Double.valueOf(d).toString(), "12.0");
    } catch(Exception e) {
      e.printStackTrace();
      Assert.assertEquals("", e.getMessage());
    }
  }

  @Test
  public void testEvalCurrentYear() {
    try {
      String expression = "$currentYear-$1";

      // as a data source
      List<IDataSource> operands = Arrays.asList(new IDataSource[] { new CurrentDateSource(Calendar.YEAR) });
      Assert.assertEquals(0d, MathEclipseEvaluator.getInstance().evaluateDouble(expression, null, operands));

      // as a data
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      List<Data> datas = Arrays.asList(new Data[] { DataBuilder.buildInteger(cal.get(Calendar.YEAR)) });
      Assert.assertEquals(0d, MathEclipseEvaluator.getInstance().evaluateDouble(expression, datas));

    } catch(Exception e) {
      e.printStackTrace();
      Assert.assertEquals("", e.getMessage());
    }
  }

  @Test
  public void testEvalCurrentDate() {
    try {
      String expression = "$currentDate-1>0";

      // as data source
      Assert.assertEquals(true, MathEclipseEvaluator.getInstance().evaluateBoolean(expression, null, null));

      // as data
      Assert.assertEquals(true, MathEclipseEvaluator.getInstance().evaluateBoolean(expression, null));

    } catch(Exception e) {
      e.printStackTrace();
      Assert.assertEquals("", e.getMessage());
    }
  }

  private void check(String in, String compareWith) {
    try {
      double d = MathEclipseEvaluator.getInstance().evaluateDouble(in, null);
      Assert.assertEquals(d, Double.parseDouble(compareWith));
    } catch(Exception e) {
      e.printStackTrace();
      Assert.assertEquals("", e.getMessage());
    }
  }
}
