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
    Data middleData = DataBuilder.buildInteger(150l);

    Assert.assertTrue(rangeCheck.checkParameterValue(middleData, null, null));

    // Test with run value equal to the minimum.
    Data minData = DataBuilder.buildInteger(100l);

    Assert.assertTrue(rangeCheck.checkParameterValue(minData, null, null));
    
    // Test with run value equal to the maximum.
    Data maxData = DataBuilder.buildInteger(200l);

    Assert.assertTrue(rangeCheck.checkParameterValue(maxData, null, null));
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
    Data tooSmallData = DataBuilder.buildInteger(99l);

    Assert.assertFalse(rangeCheck.checkParameterValue(tooSmallData, null, null));

    // Test with run value greater than the maximum.
    Data tooBigData = DataBuilder.buildInteger(201l);

    Assert.assertFalse(rangeCheck.checkParameterValue(tooBigData, null, null));
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
    Data minData = DataBuilder.buildInteger(Long.MIN_VALUE);
    
    Assert.assertTrue(rangeCheck.checkParameterValue(minData, null, null));
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
    Data maxData = DataBuilder.buildInteger(Long.MAX_VALUE);
    
    Assert.assertTrue(rangeCheck.checkParameterValue(maxData, null, null));
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
    Data middleData = DataBuilder.buildDecimal(150.0);

    Assert.assertTrue(rangeCheck.checkParameterValue(middleData, null, null));

    // Test with run value equal to the minimum.
    Data minData = DataBuilder.buildDecimal(100.0);
    
    Assert.assertTrue(rangeCheck.checkParameterValue(minData, null, null));
    
    // Test with run value equal to the maximum.
    Data maxData = DataBuilder.buildDecimal(200.0);

    Assert.assertTrue(rangeCheck.checkParameterValue(maxData, null, null));
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
    Data tooSmallData = DataBuilder.buildDecimal(99.0);

    Assert.assertFalse(rangeCheck.checkParameterValue(tooSmallData, null, null));

    // Test with run value greater than the maximum.
    Data tooBigData = DataBuilder.buildDecimal(201.0);

    Assert.assertFalse(rangeCheck.checkParameterValue(tooBigData, null, null));
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
    Data minData = DataBuilder.buildDecimal(Double.MIN_VALUE);
    
    Assert.assertTrue(rangeCheck.checkParameterValue(minData, null, null));
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
    Data maxData = DataBuilder.buildDecimal(Double.MAX_VALUE);
    
    Assert.assertTrue(rangeCheck.checkParameterValue(maxData, null, null));
  }
}