package org.obiba.onyx.jade.core.domain.instrument.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

public class EqualsValueCheckTest {

  private EqualsValueCheck equalsValueCheck;
  
  @Before
  public void setUp() {
    equalsValueCheck = new EqualsValueCheck();
  }
  
  @Test
  public void testCheckParameterValueBoolean() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.BOOLEAN);
    
    // Test run value equals TRUE.
    Data trueData = new Data(DataType.BOOLEAN, Boolean.TRUE);
    
    InstrumentRunValue runValueTrue = new InstrumentRunValue();
    runValueTrue.setInstrumentParameter(instrumentParameter);
    runValueTrue.setData(trueData);
    
    equalsValueCheck.setValue(trueData);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueTrue));
    
    // Test run value equals FALSE.
    Data falseData = new Data(DataType.BOOLEAN, Boolean.FALSE);
      
    InstrumentRunValue runValueFalse = new InstrumentRunValue();
    runValueFalse.setInstrumentParameter(instrumentParameter);
    runValueFalse.setData(falseData);
    
    equalsValueCheck.setValue(falseData);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueFalse));
  }
  
  @Test
  public void testCheckParameterValueBooleanUnequal() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.BOOLEAN);
    
    // Test run value equals TRUE.
    Data trueData = new Data(DataType.BOOLEAN, Boolean.TRUE);
      
    InstrumentRunValue runValueTrue = new InstrumentRunValue();
    runValueTrue.setInstrumentParameter(instrumentParameter);
    runValueTrue.setData(trueData);
    
    equalsValueCheck.setValue(new Data(DataType.BOOLEAN, Boolean.FALSE)); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueTrue));
    
    // Test run value equals FALSE.
    Data falseData = new Data(DataType.BOOLEAN, Boolean.FALSE);
    
    InstrumentRunValue runValueFalse = new InstrumentRunValue();
    runValueFalse.setInstrumentParameter(instrumentParameter);
    runValueFalse.setData(falseData);
    
    equalsValueCheck.setValue(new Data(DataType.BOOLEAN, Boolean.TRUE)); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueFalse));
  }
  
  @Test
  public void testCheckParameterValueInteger() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.INTEGER);
    
    // Test run value equals 1.
    Data oneData = new Data(DataType.INTEGER, Long.valueOf(1));
      
    InstrumentRunValue runValueOne = new InstrumentRunValue();
    runValueOne.setInstrumentParameter(instrumentParameter);
    runValueOne.setData(oneData);
    
    equalsValueCheck.setValue(oneData);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueOne));
    
    // Test run value equals Long.MAX_VALUE.
    Data maxIntData = new Data(DataType.INTEGER, Long.MAX_VALUE);
      
    InstrumentRunValue runValueMaxInt = new InstrumentRunValue();
    runValueMaxInt.setInstrumentParameter(instrumentParameter);
    runValueMaxInt.setData(maxIntData);
    
    equalsValueCheck.setValue(maxIntData);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueMaxInt));
  }
  
  @Test
  public void testCheckParameterValueIntegerUnequal() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.INTEGER);
    
    // Test run value equals 1.
    Data oneData = new Data(DataType.INTEGER, Long.valueOf(1));
      
    InstrumentRunValue runValueOne = new InstrumentRunValue();
    runValueOne.setInstrumentParameter(instrumentParameter);
    runValueOne.setData(oneData);
    
    equalsValueCheck.setValue(new Data(DataType.INTEGER, Long.MAX_VALUE)); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueOne));
    
    // Test run value equals Long.MAX_VALUE.
    Data maxIntData = new Data(DataType.INTEGER, Long.MAX_VALUE);
      
    InstrumentRunValue runValueMaxInt = new InstrumentRunValue();
    runValueMaxInt.setInstrumentParameter(instrumentParameter);
    runValueMaxInt.setData(maxIntData);
    
    equalsValueCheck.setValue(new Data(DataType.INTEGER, Long.valueOf(1)));
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueMaxInt));
  }
  
  @Test
  public void testCheckParameterValueDecimal() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.DECIMAL);
    
    // Test run value equals 1.5.
    Data onePointFiveData = new Data(DataType.DECIMAL, Double.valueOf(1.5));
      
    InstrumentRunValue runValueOnePointFive = new InstrumentRunValue();
    runValueOnePointFive.setInstrumentParameter(instrumentParameter);
    runValueOnePointFive.setData(onePointFiveData);
    
    equalsValueCheck.setValue(onePointFiveData);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueOnePointFive));
    
    // Test run value equals Double.MAX_VALUE.
    Data maxDecimalData = new Data(DataType.DECIMAL, Double.MAX_VALUE);
      
    InstrumentRunValue runValueMaxDecimal = new InstrumentRunValue();
    runValueMaxDecimal.setInstrumentParameter(instrumentParameter);
    runValueMaxDecimal.setData(maxDecimalData);
    
    equalsValueCheck.setValue(maxDecimalData);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueMaxDecimal));
  }
  
  @Test
  public void testCheckParameterValueDecimalUnequal() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.DECIMAL);
    
    // Test run value equals 1.5.
    Data onePointFiveData = new Data(DataType.DECIMAL, Double.valueOf(1.5));
      
    InstrumentRunValue runValueOnePointFive = new InstrumentRunValue();
    runValueOnePointFive.setInstrumentParameter(instrumentParameter);
    runValueOnePointFive.setData(onePointFiveData);
    
    equalsValueCheck.setValue(new Data(DataType.DECIMAL, Double.MIN_VALUE)); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueOnePointFive));
    
    // Test run value equals Double.MAX_VALUE.
    Data maxDecimalData = new Data(DataType.DECIMAL, Double.MAX_VALUE);
      
    InstrumentRunValue runValueMaxDecimal = new InstrumentRunValue();
    runValueMaxDecimal.setInstrumentParameter(instrumentParameter);
    runValueMaxDecimal.setData(maxDecimalData);
    
    equalsValueCheck.setValue(new Data(DataType.DECIMAL, Double.valueOf(1.5))); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueMaxDecimal));
  }
  
  @Test
  public void testCheckParameterValueText() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.TEXT);
    
    // Test run value equals "Foo".
    Data fooData = new Data(DataType.TEXT, "Foo");
    
    InstrumentRunValue runValueFoo = new InstrumentRunValue();
    runValueFoo.setInstrumentParameter(instrumentParameter);
    runValueFoo.setData(fooData);
    
    equalsValueCheck.setValue(fooData);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueFoo));
  }
  
  @Test
  public void testCheckParameterValueTextUnequal() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.TEXT);
    
    // Test run value equals "Foo".
    Data fooData = new Data(DataType.TEXT, "Foo");
    
    InstrumentRunValue runValueFoo = new InstrumentRunValue();
    runValueFoo.setInstrumentParameter(instrumentParameter);
    runValueFoo.setData(fooData);
    
    equalsValueCheck.setValue(new Data(DataType.TEXT, "Bar")); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueFoo));
  }
}