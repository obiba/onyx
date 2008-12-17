/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.instrument.validation;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

public class EqualsValueCheckTest {

  private EqualsValueCheck equalsValueCheck;

  private InstrumentParameter instrumentParameter;

  @Before
  public void setUp() {
    equalsValueCheck = new EqualsValueCheck();

    instrumentParameter = new InstrumentInputParameter();
    equalsValueCheck.setTargetParameter(instrumentParameter);
  }

  /**
   * Tests equal DataType.BOOLEAN values.
   */
  @Test
  public void testCheckParameterValueBoolean() {
    instrumentParameter.setDataType(DataType.BOOLEAN);

    // Test with run value TRUE.
    Data trueData = DataBuilder.buildBoolean(Boolean.TRUE);

    equalsValueCheck.setBooleanValue(Boolean.TRUE);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(trueData, null, null));

    // Test with run value FALSE.
    Data falseData = DataBuilder.buildBoolean(Boolean.FALSE);

    equalsValueCheck.setBooleanValue(Boolean.FALSE);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(falseData, null, null));
  }

  /**
   * Tests unequal DataType.BOOLEAN values.
   */
  @Test
  public void testCheckParameterValueBooleanUnequal() {
    instrumentParameter.setDataType(DataType.BOOLEAN);

    // Test with run value TRUE.
    Data trueData = DataBuilder.buildBoolean(Boolean.TRUE);

    equalsValueCheck.setBooleanValue(false); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(trueData, null, null));

    // Test with run value FALSE.
    Data falseData = DataBuilder.buildBoolean(Boolean.FALSE);

    equalsValueCheck.setBooleanValue(true); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(falseData, null, null));
  }

  /**
   * Tests equal DataType.INTEGER values.
   */
  @Test
  public void testCheckParameterValueInteger() {
    instrumentParameter.setDataType(DataType.INTEGER);

    // Test run value equals 1.
    Data oneData = DataBuilder.buildInteger(Long.valueOf(1));

    equalsValueCheck.setIntegerValue(1l);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(oneData, null, null));

    // Test run value equals Long.MAX_VALUE.
    Data maxIntData = DataBuilder.buildInteger(Long.MAX_VALUE);

    equalsValueCheck.setIntegerValue(Long.MAX_VALUE);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(maxIntData, null, null));
  }

  /**
   * Tests unequal DataType.INTEGER values.
   */
  @Test
  public void testCheckParameterValueIntegerUnequal() {
    instrumentParameter.setDataType(DataType.INTEGER);

    // Test run value equals 1.
    Data oneData = DataBuilder.buildInteger(Long.valueOf(1));

    equalsValueCheck.setData(DataBuilder.buildInteger(Long.MAX_VALUE)); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(oneData, null, null));

    // Test run value equals Long.MAX_VALUE.
    Data maxIntData = DataBuilder.buildInteger(Long.MAX_VALUE);

    equalsValueCheck.setData(DataBuilder.buildInteger(Long.valueOf(1)));
    Assert.assertFalse(equalsValueCheck.checkParameterValue(maxIntData, null, null));
  }

  /**
   * Tests equal DataType.DECIMAL values.
   */
  @Test
  public void testCheckParameterValueDecimal() {
    instrumentParameter.setDataType(DataType.DECIMAL);

    // Test run value equals 1.5.
    Data onePointFiveData = DataBuilder.buildDecimal(Double.valueOf(1.5));

    equalsValueCheck.setData(onePointFiveData);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(onePointFiveData, null, null));

    // Test run value equals Double.MAX_VALUE.
    Data maxDecimalData = DataBuilder.buildDecimal(Double.MAX_VALUE);

    equalsValueCheck.setData(maxDecimalData);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(maxDecimalData, null, null));
  }

  /**
   * Tests unequal DataType.DECIMAL values.
   */
  @Test
  public void testCheckParameterValueDecimalUnequal() {
    instrumentParameter.setDataType(DataType.DECIMAL);

    // Test run value equals 1.5.
    Data onePointFiveData = DataBuilder.buildDecimal(Double.valueOf(1.5));

    equalsValueCheck.setData(DataBuilder.buildDecimal(Double.MIN_VALUE)); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(onePointFiveData, null, null));

    // Test run value equals Double.MAX_VALUE.
    Data maxDecimalData = DataBuilder.buildDecimal(Double.MAX_VALUE);

    equalsValueCheck.setData(DataBuilder.buildDecimal(Double.valueOf(1.5))); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(maxDecimalData, null, null));
  }

  /**
   * Tests equal DataType.TEXT values.
   */
  @Test
  public void testCheckParameterValueText() {
    instrumentParameter.setDataType(DataType.TEXT);

    // Test run value equals "Foo".
    Data fooData = DataBuilder.buildText("Foo");

    equalsValueCheck.setData(fooData);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(fooData, null, null));
  }

  /**
   * Tests unequal DataType.TEXT values.
   */
  @Test
  public void testCheckParameterValueTextUnequal() {
    instrumentParameter.setDataType(DataType.TEXT);

    // Test run value equals "Foo".
    Data fooData = DataBuilder.buildText("Foo");

    equalsValueCheck.setData(new Data(DataType.TEXT, "Bar")); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(fooData, null, null));
  }

  /**
   * Tests equal null values (i.e., run value is null, so is check value).
   */
  @Test
  public void testCheckParameterValueNull() {
    // Any type will do for this test, since the value will be null.
    instrumentParameter.setDataType(DataType.TEXT);

    // Test run value is null.
    Data nullData = DataBuilder.buildText(null);

    equalsValueCheck.setData(new Data(DataType.TEXT, null));
    Assert.assertTrue(equalsValueCheck.checkParameterValue(nullData, null, null));
  }

  /**
   * Tests run value null, check value not null.
   */
  @Test
  public void testRunValueNullCheckValueNotNull() {
    // Any type will do for this test, since the value will be null.
    instrumentParameter.setDataType(DataType.TEXT);

    // Test run value is null.
    Data nullData = DataBuilder.buildText(null);

    equalsValueCheck.setData(new Data(DataType.TEXT, "Foo")); // not null
    Assert.assertFalse(equalsValueCheck.checkParameterValue(nullData, null, null));
  }

  /**
   * Tests run value not null, check value null.
   */
  @Test
  public void testRunValueNotNullCheckValueNull() {
    // Any type will do for this test, since the value will be null.
    instrumentParameter.setDataType(DataType.TEXT);

    // Test run value is not null.
    Data fooData = DataBuilder.buildText("Foo");

    equalsValueCheck.setData(new Data(DataType.TEXT, null)); // null
    Assert.assertFalse(equalsValueCheck.checkParameterValue(fooData, null, null));
  }

  /**
   * Tests DataType.BOOLEAN values using operators.
   */
  @Test
  public void testCheckParameterValueBooleanWithOperators() {
    instrumentParameter.setDataType(DataType.BOOLEAN);

    // Test run value equals true.
    Data runValue = DataBuilder.buildBoolean(true);

    // Check value = false.
    equalsValueCheck.setData(DataBuilder.buildBoolean(false));
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value = true.
    equalsValueCheck.setData(DataBuilder.buildBoolean(true));
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Operator should be ignored for booleans.
    equalsValueCheck.setOperator(ComparisonOperator.LESSER);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));
    equalsValueCheck.setOperator(ComparisonOperator.GREATER);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));
    equalsValueCheck.setOperator(ComparisonOperator.GREATER_EQUALS);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

  }

  /**
   * Tests DataType.TEXT values using operators.
   */
  @Test
  public void testCheckParameterValueTextWithOperators() {
    instrumentParameter.setDataType(DataType.TEXT);

    // Test run value equals "genomic".
    Data runValue = DataBuilder.buildText("genomic");

    // Check value = "genomic" should be true.
    equalsValueCheck.setData(DataBuilder.buildText("genomic"));
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value = "blood" should be false.
    equalsValueCheck.setData(DataBuilder.buildText("blood"));
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value > "blood".
    equalsValueCheck.setData(DataBuilder.buildText("blood"));
    equalsValueCheck.setOperator(ComparisonOperator.GREATER);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

  }

  /**
   * Tests DataType.INTEGER values using operators.
   */
  @Test
  public void testCheckParameterValueIntegerWithOperators() {
    instrumentParameter.setDataType(DataType.INTEGER);

    // Test run value equals 100.
    Data runValue = DataBuilder.buildInteger(100l);

    // Check value < 1 should be false.
    equalsValueCheck.setData(DataBuilder.buildInteger(1l));
    equalsValueCheck.setOperator(ComparisonOperator.LESSER);
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value > 1 should be true.
    equalsValueCheck.setOperator(ComparisonOperator.GREATER);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value < 100 should be false.
    equalsValueCheck.setOperator(ComparisonOperator.LESSER);
    equalsValueCheck.setData(DataBuilder.buildInteger(100l));
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value <= 100 should be true.
    equalsValueCheck.setOperator(ComparisonOperator.LESSER_EQUALS);
    equalsValueCheck.setData(DataBuilder.buildInteger(100l));
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value >= 100 should be true.
    equalsValueCheck.setOperator(ComparisonOperator.GREATER_EQUALS);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value = 100 should be true.
    equalsValueCheck.setOperator(ComparisonOperator.EQUALS);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value < 101 should be true.
    equalsValueCheck.setOperator(ComparisonOperator.LESSER);
    equalsValueCheck.setData(DataBuilder.buildInteger(101l));
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

  }

  /**
   * Tests DataType.DECIMAL values using operators.
   */
  @Test
  public void testCheckParameterValueDecimalWithOperators() {
    instrumentParameter.setDataType(DataType.DECIMAL);

    // Test run value equals 1.5
    Data runValue = DataBuilder.buildDecimal(1.5d);

    // Check value = 1.5 should be true.
    equalsValueCheck.setData(DataBuilder.buildDecimal(1.5d));
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value >= 1.5 should be true.
    equalsValueCheck.setOperator(ComparisonOperator.GREATER_EQUALS);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value <= 1.5 should be true.
    equalsValueCheck.setOperator(ComparisonOperator.LESSER_EQUALS);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value > 1.5 should be false.
    equalsValueCheck.setOperator(ComparisonOperator.GREATER);
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value > 1.499 should be true.
    equalsValueCheck.setOperator(ComparisonOperator.GREATER);
    equalsValueCheck.setData(DataBuilder.buildDecimal(1.499d));
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value < 1.501 should be true.
    equalsValueCheck.setOperator(ComparisonOperator.LESSER);
    equalsValueCheck.setData(DataBuilder.buildDecimal(1.501d));
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value = 1.500 should be true.
    equalsValueCheck.setOperator(ComparisonOperator.EQUALS);
    equalsValueCheck.setData(DataBuilder.buildDecimal(1.500d));
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value = 1.501 should be false.
    equalsValueCheck.setOperator(ComparisonOperator.EQUALS);
    equalsValueCheck.setData(DataBuilder.buildDecimal(1.501d));
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValue, null, null));

  }

  /**
   * Tests DataType.DATE values using operators.
   */
  @Test
  public void testCheckParameterValueDateWithOperators() {
    instrumentParameter.setDataType(DataType.DATE);

    // Test run value equals to current time.
    long currentTime = System.currentTimeMillis();
    Data runValue = DataBuilder.buildDate(new Date(currentTime));

    // Check value = current time should be true.
    equalsValueCheck.setData(DataBuilder.buildDate(new Date(currentTime)));
    equalsValueCheck.setOperator(ComparisonOperator.EQUALS);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value < an hour ago should be false.
    equalsValueCheck.setData(DataBuilder.buildDate(new Date(currentTime - 3600 * 1000)));
    equalsValueCheck.setOperator(ComparisonOperator.LESSER);
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValue, null, null));

    // Check value > an hour ago should be true.
    equalsValueCheck.setData(DataBuilder.buildDate(new Date(currentTime - 3600 * 1000)));
    equalsValueCheck.setOperator(ComparisonOperator.GREATER);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValue, null, null));

  }

}
