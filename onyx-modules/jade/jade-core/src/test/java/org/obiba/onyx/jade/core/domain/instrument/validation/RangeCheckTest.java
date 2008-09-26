package org.obiba.onyx.jade.core.domain.instrument.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

public class RangeCheckTest {

  private RangeCheck rangeCheck;

  private InstrumentParameter instrumentParameter;

  @Before
  public void setUp() {
    rangeCheck = new RangeCheck();

    instrumentParameter = new InstrumentInputParameter();
    rangeCheck.setTargetParameter(instrumentParameter);
  }

  /**
   * Tests DataType.INTEGER value within range.
   */
  @Test
  public void testIntegerWithinRange() {
    instrumentParameter.setDataType(DataType.INTEGER);

    long minValue = 100l;
    long maxValue = 200l;
    
    rangeCheck.setIntegerMinValue(minValue);
    rangeCheck.setIntegerMaxValue(maxValue);
    
    // Test with run value in the middle of the range.
    InstrumentRunValue runValueMiddle = new InstrumentRunValue();
    runValueMiddle.setInstrumentParameter(instrumentParameter);
    runValueMiddle.setData(DataBuilder.buildInteger(150l));

    Assert.assertTrue(rangeCheck.checkParameterValue(runValueMiddle, null));

    // Test with run value equal to the minimum.
    InstrumentRunValue runValueMin = new InstrumentRunValue();
    runValueMin.setInstrumentParameter(instrumentParameter);
    runValueMin.setData(DataBuilder.buildInteger(100l));

    Assert.assertTrue(rangeCheck.checkParameterValue(runValueMin, null));
    
    // Test with run value equal to the maximum.
    InstrumentRunValue runValueMax = new InstrumentRunValue();
    runValueMax.setInstrumentParameter(instrumentParameter);
    runValueMax.setData(DataBuilder.buildInteger(200l));

    Assert.assertTrue(rangeCheck.checkParameterValue(runValueMax, null));
  }
  
  /**
   * Tests DataType.INTEGER value out of range.
   */
  @Test
  public void testIntegerOutOfRange() {
    instrumentParameter.setDataType(DataType.INTEGER);

    long minValue = 100l;
    long maxValue = 200l;
    
    rangeCheck.setIntegerMinValue(minValue);
    rangeCheck.setIntegerMaxValue(maxValue);
    
    // Test with run value less than the minimum.
    InstrumentRunValue runValueMiddle = new InstrumentRunValue();
    runValueMiddle.setInstrumentParameter(instrumentParameter);
    runValueMiddle.setData(DataBuilder.buildInteger(99l));

    Assert.assertFalse(rangeCheck.checkParameterValue(runValueMiddle, null));

    // Test with run value greater than the maximum.
    InstrumentRunValue runValueMin = new InstrumentRunValue();
    runValueMin.setInstrumentParameter(instrumentParameter);
    runValueMin.setData(DataBuilder.buildInteger(201l));

    Assert.assertFalse(rangeCheck.checkParameterValue(runValueMin, null));
  }
  
  /**
   * Tests DataType.INTEGER value with a range that has a maximum but no
   * minimum.
   */
  @Test
  public void testIntegerNoMinimum() {
    instrumentParameter.setDataType(DataType.INTEGER);
    
    rangeCheck.setIntegerMinValue(null);
    rangeCheck.setIntegerMaxValue(200l);
    
    // Test run value of Long.MIN_VALUE.
    InstrumentRunValue runValueMin = new InstrumentRunValue();
    runValueMin.setInstrumentParameter(instrumentParameter);
    runValueMin.setData(DataBuilder.buildInteger(Long.MIN_VALUE));
    
    Assert.assertTrue(rangeCheck.checkParameterValue(runValueMin, null));
  }
  
  /**
   * Tests DataType.INTEGER value with a range that has a minimum but no
   * maximum.
   */
  @Test
  public void testIntegerNoMaximum() {
    instrumentParameter.setDataType(DataType.INTEGER);
    
    rangeCheck.setIntegerMinValue(100l);
    rangeCheck.setIntegerMaxValue(null);
    
    // Test run value of Long.MAX_VALUE.
    InstrumentRunValue runValueMax = new InstrumentRunValue();
    runValueMax.setInstrumentParameter(instrumentParameter);
    runValueMax.setData(DataBuilder.buildInteger(Long.MAX_VALUE));
    
    Assert.assertTrue(rangeCheck.checkParameterValue(runValueMax, null));
  }
  
  /**
   * Tests DataType.DECIMAL value within range.
   */
  @Test
  public void testDecimalWithinRange() {
    instrumentParameter.setDataType(DataType.DECIMAL);

    double minValue = 100.0;
    double maxValue = 200.0;
    
    rangeCheck.setDecimalMinValue(minValue);
    rangeCheck.setDecimalMaxValue(maxValue);
    
    // Test with run value in the middle of the range.
    InstrumentRunValue runValueMiddle = new InstrumentRunValue();
    runValueMiddle.setInstrumentParameter(instrumentParameter);
    runValueMiddle.setData(DataBuilder.buildDecimal(150.0));

    Assert.assertTrue(rangeCheck.checkParameterValue(runValueMiddle, null));

    // Test with run value equal to the minimum.
    InstrumentRunValue runValueMin = new InstrumentRunValue();
    runValueMin.setInstrumentParameter(instrumentParameter);
    runValueMin.setData(DataBuilder.buildDecimal(100.0));
    
    Assert.assertTrue(rangeCheck.checkParameterValue(runValueMin, null));
    
    // Test with run value equal to the maximum.
    InstrumentRunValue runValueMax = new InstrumentRunValue();
    runValueMax.setInstrumentParameter(instrumentParameter);
    runValueMax.setData(DataBuilder.buildDecimal(200.0));

    Assert.assertTrue(rangeCheck.checkParameterValue(runValueMax, null));
  }
  
  /**
   * Tests DataType.DECIMAL value out of range.
   */
  @Test
  public void testDecimalOutOfRange() {
    instrumentParameter.setDataType(DataType.DECIMAL);

    double minValue = 100.0;
    double maxValue = 200.0;
    
    rangeCheck.setDecimalMinValue(minValue);
    rangeCheck.setDecimalMaxValue(maxValue);
    
    // Test with run value less than the minimum.
    InstrumentRunValue runValueMiddle = new InstrumentRunValue();
    runValueMiddle.setInstrumentParameter(instrumentParameter);
    runValueMiddle.setData(DataBuilder.buildDecimal(99.0));

    Assert.assertFalse(rangeCheck.checkParameterValue(runValueMiddle, null));

    // Test with run value greater than the maximum.
    InstrumentRunValue runValueMin = new InstrumentRunValue();
    runValueMin.setInstrumentParameter(instrumentParameter);
    runValueMin.setData(DataBuilder.buildDecimal(201.0));

    Assert.assertFalse(rangeCheck.checkParameterValue(runValueMin, null));
  }
  
  /**
   * Tests DataType.DECIMAL value with a range that has a maximum but no
   * minimum.
   */
  @Test
  public void testDecimalNoMinimum() {
    instrumentParameter.setDataType(DataType.DECIMAL);
    
    rangeCheck.setDecimalMinValue(null);
    rangeCheck.setDecimalMaxValue(200.0);
    
    // Test run value of Double.MIN_VALUE.
    InstrumentRunValue runValueMin = new InstrumentRunValue();
    runValueMin.setInstrumentParameter(instrumentParameter);
    runValueMin.setData(DataBuilder.buildDecimal(Double.MIN_VALUE));
    
    Assert.assertTrue(rangeCheck.checkParameterValue(runValueMin, null));
  }
  
  /**
   * Tests DataType.DECIMAL value with a range that has a minimum but no
   * maximum.
   */
  @Test
  public void testDecimalNoMaximum() {
    instrumentParameter.setDataType(DataType.DECIMAL);
    
    rangeCheck.setDecimalMinValue(100.0);
    rangeCheck.setDecimalMaxValue(null);
    
    // Test run value of Double.MAX_VALUE.
    InstrumentRunValue runValueMax = new InstrumentRunValue();
    runValueMax.setInstrumentParameter(instrumentParameter);
    runValueMax.setData(DataBuilder.buildDecimal(Double.MAX_VALUE));
    
    Assert.assertTrue(rangeCheck.checkParameterValue(runValueMax, null));
  }
}