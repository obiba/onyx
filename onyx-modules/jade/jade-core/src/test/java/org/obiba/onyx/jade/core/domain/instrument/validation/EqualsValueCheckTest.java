package org.obiba.onyx.jade.core.domain.instrument.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
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

    InstrumentRunValue runValueTrue = new InstrumentRunValue();
    runValueTrue.setInstrumentParameter(instrumentParameter);
    runValueTrue.setData(trueData);

    equalsValueCheck.setBooleanValue(Boolean.TRUE);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueTrue, null));

    // Test with run value FALSE.
    Data falseData = DataBuilder.buildBoolean(Boolean.FALSE);

    InstrumentRunValue runValueFalse = new InstrumentRunValue();
    runValueFalse.setInstrumentParameter(instrumentParameter);
    runValueFalse.setData(falseData);

    equalsValueCheck.setBooleanValue(Boolean.FALSE);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueFalse, null));
  }

  /**
   * Tests unequal DataType.BOOLEAN values.
   */
  @Test
  public void testCheckParameterValueBooleanUnequal() {
    instrumentParameter.setDataType(DataType.BOOLEAN);

    // Test with run value TRUE.
    Data trueData = DataBuilder.buildBoolean(Boolean.TRUE);

    InstrumentRunValue runValueTrue = new InstrumentRunValue();
    runValueTrue.setInstrumentParameter(instrumentParameter);
    runValueTrue.setData(trueData);

    equalsValueCheck.setBooleanValue(false); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueTrue, null));

    // Test with run value FALSE.
    Data falseData = DataBuilder.buildBoolean(Boolean.FALSE);

    InstrumentRunValue runValueFalse = new InstrumentRunValue();
    runValueFalse.setInstrumentParameter(instrumentParameter);
    runValueFalse.setData(falseData);

    equalsValueCheck.setBooleanValue(true); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueFalse, null));
  }

  /**
   * Tests equal DataType.INTEGER values.
   */
  @Test
  public void testCheckParameterValueInteger() {
    instrumentParameter.setDataType(DataType.INTEGER);

    // Test run value equals 1.
    Data oneData = DataBuilder.buildInteger(Long.valueOf(1));

    InstrumentRunValue runValueOne = new InstrumentRunValue();
    runValueOne.setInstrumentParameter(instrumentParameter);
    runValueOne.setData(oneData);

    equalsValueCheck.setIntegerValue(1l);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueOne, null));

    // Test run value equals Long.MAX_VALUE.
    Data maxIntData = DataBuilder.buildInteger(Long.MAX_VALUE);

    InstrumentRunValue runValueMaxInt = new InstrumentRunValue();
    runValueMaxInt.setInstrumentParameter(instrumentParameter);
    runValueMaxInt.setData(maxIntData);

    equalsValueCheck.setIntegerValue(Long.MAX_VALUE);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueMaxInt, null));
  }

  /**
   * Tests unequal DataType.INTEGER values.
   */
  @Test
  public void testCheckParameterValueIntegerUnequal() {
    instrumentParameter.setDataType(DataType.INTEGER);

    // Test run value equals 1.
    Data oneData = DataBuilder.buildInteger(Long.valueOf(1));

    InstrumentRunValue runValueOne = new InstrumentRunValue();
    runValueOne.setInstrumentParameter(instrumentParameter);
    runValueOne.setData(oneData);

    equalsValueCheck.setData(DataBuilder.buildInteger(Long.MAX_VALUE)); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueOne, null));

    // Test run value equals Long.MAX_VALUE.
    Data maxIntData = DataBuilder.buildInteger(Long.MAX_VALUE);

    InstrumentRunValue runValueMaxInt = new InstrumentRunValue();
    runValueMaxInt.setInstrumentParameter(instrumentParameter);
    runValueMaxInt.setData(maxIntData);

    equalsValueCheck.setData(DataBuilder.buildInteger(Long.valueOf(1)));
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueMaxInt, null));
  }

  /**
   * Tests equal DataType.DECIMAL values.
   */
  @Test
  public void testCheckParameterValueDecimal() {
    instrumentParameter.setDataType(DataType.DECIMAL);

    // Test run value equals 1.5.
    Data onePointFiveData = DataBuilder.buildDecimal(Double.valueOf(1.5));

    InstrumentRunValue runValueOnePointFive = new InstrumentRunValue();
    runValueOnePointFive.setInstrumentParameter(instrumentParameter);
    runValueOnePointFive.setData(onePointFiveData);

    equalsValueCheck.setData(onePointFiveData);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueOnePointFive, null));

    // Test run value equals Double.MAX_VALUE.
    Data maxDecimalData = DataBuilder.buildDecimal(Double.MAX_VALUE);

    InstrumentRunValue runValueMaxDecimal = new InstrumentRunValue();
    runValueMaxDecimal.setInstrumentParameter(instrumentParameter);
    runValueMaxDecimal.setData(maxDecimalData);

    equalsValueCheck.setData(maxDecimalData);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueMaxDecimal, null));
  }

  /**
   * Tests unequal DataType.DECIMAL values.
   */
  @Test
  public void testCheckParameterValueDecimalUnequal() {
    instrumentParameter.setDataType(DataType.DECIMAL);

    // Test run value equals 1.5.
    Data onePointFiveData = DataBuilder.buildDecimal(Double.valueOf(1.5));

    InstrumentRunValue runValueOnePointFive = new InstrumentRunValue();
    runValueOnePointFive.setInstrumentParameter(instrumentParameter);
    runValueOnePointFive.setData(onePointFiveData);

    equalsValueCheck.setData(DataBuilder.buildDecimal(Double.MIN_VALUE)); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueOnePointFive, null));

    // Test run value equals Double.MAX_VALUE.
    Data maxDecimalData = DataBuilder.buildDecimal(Double.MAX_VALUE);

    InstrumentRunValue runValueMaxDecimal = new InstrumentRunValue();
    runValueMaxDecimal.setInstrumentParameter(instrumentParameter);
    runValueMaxDecimal.setData(maxDecimalData);

    equalsValueCheck.setData(DataBuilder.buildDecimal(Double.valueOf(1.5))); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueMaxDecimal, null));
  }

  /**
   * Tests equal DataType.TEXT values.
   */
  @Test
  public void testCheckParameterValueText() {
    instrumentParameter.setDataType(DataType.TEXT);

    // Test run value equals "Foo".
    Data fooData = DataBuilder.buildText("Foo");

    InstrumentRunValue runValueFoo = new InstrumentRunValue();
    runValueFoo.setInstrumentParameter(instrumentParameter);
    runValueFoo.setData(fooData);

    equalsValueCheck.setData(fooData);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueFoo, null));
  }

  /**
   * Tests unequal DataType.TEXT values.
   */
  @Test
  public void testCheckParameterValueTextUnequal() {
    instrumentParameter.setDataType(DataType.TEXT);

    // Test run value equals "Foo".
    Data fooData = DataBuilder.buildText("Foo");

    InstrumentRunValue runValueFoo = new InstrumentRunValue();
    runValueFoo.setInstrumentParameter(instrumentParameter);
    runValueFoo.setData(fooData);

    equalsValueCheck.setData(new Data(DataType.TEXT, "Bar")); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueFoo, null));
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

    InstrumentRunValue runValueNull = new InstrumentRunValue();
    runValueNull.setInstrumentParameter(instrumentParameter);
    runValueNull.setData(nullData);

    equalsValueCheck.setData(new Data(DataType.TEXT, null));
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueNull, null));
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

    InstrumentRunValue runValueNull = new InstrumentRunValue();
    runValueNull.setInstrumentParameter(instrumentParameter);
    runValueNull.setData(nullData);

    equalsValueCheck.setData(new Data(DataType.TEXT, "Foo")); // not null
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueNull, null));
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

    InstrumentRunValue runValueFoo = new InstrumentRunValue();
    runValueFoo.setInstrumentParameter(instrumentParameter);
    runValueFoo.setData(fooData);

    equalsValueCheck.setData(new Data(DataType.TEXT, null)); // null
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueFoo, null));
  }
}