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
    InstrumentRunValue runValueTrue = new InstrumentRunValue();
    runValueTrue.setInstrumentParameter(instrumentParameter);
    runValueTrue.setInstrumentParameter(instrumentParameter);
    runValueTrue.setData(new Data(DataType.BOOLEAN, Boolean.TRUE));
    
    equalsValueCheck.setValue(Boolean.TRUE);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueTrue));
    
    // Test run value equals FALSE.
    InstrumentRunValue runValueFalse = new InstrumentRunValue();
    runValueFalse.setInstrumentParameter(instrumentParameter);
    runValueFalse.setInstrumentParameter(instrumentParameter);
    runValueFalse.setData(new Data(DataType.BOOLEAN, Boolean.FALSE));
    
    equalsValueCheck.setValue(Boolean.FALSE);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueFalse));
  }
  
  @Test
  public void testCheckParameterValueBooleanUnequal() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.BOOLEAN);
    
    // Test run value equals TRUE.
    InstrumentRunValue runValueTrue = new InstrumentRunValue();
    runValueTrue.setInstrumentParameter(instrumentParameter);
    runValueTrue.setInstrumentParameter(instrumentParameter);
    runValueTrue.setData(new Data(DataType.BOOLEAN, Boolean.TRUE));
    
    equalsValueCheck.setValue(Boolean.FALSE); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueTrue));
    
    // Test run value equals FALSE.
    InstrumentRunValue runValueFalse = new InstrumentRunValue();
    runValueFalse.setInstrumentParameter(instrumentParameter);
    runValueFalse.setInstrumentParameter(instrumentParameter);
    runValueFalse.setData(new Data(DataType.BOOLEAN, Boolean.FALSE));
    
    equalsValueCheck.setValue(Boolean.TRUE); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueFalse));
  }
  
  @Test
  public void testCheckParameterValueInteger() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.INTEGER);
    
    // Test run value equals 1.
    InstrumentRunValue runValueOne = new InstrumentRunValue();
    runValueOne.setInstrumentParameter(instrumentParameter);
    runValueOne.setInstrumentParameter(instrumentParameter);
    runValueOne.setData(new Data(DataType.INTEGER, Long.valueOf(1)));
    
    equalsValueCheck.setValue(Long.valueOf(1));
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueOne));
    
    // Test run value equals Long.MAX_VALUE.
    InstrumentRunValue runValueMaxInt = new InstrumentRunValue();
    runValueMaxInt.setInstrumentParameter(instrumentParameter);
    runValueMaxInt.setInstrumentParameter(instrumentParameter);
    runValueMaxInt.setData(new Data(DataType.INTEGER, Long.MAX_VALUE));
    
    equalsValueCheck.setValue(Long.MAX_VALUE);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueMaxInt));
  }
  
  @Test
  public void testCheckParameterValueIntegerUnequal() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.INTEGER);
    
    // Test run value equals 1.
    InstrumentRunValue runValueOne = new InstrumentRunValue();
    runValueOne.setInstrumentParameter(instrumentParameter);
    runValueOne.setInstrumentParameter(instrumentParameter);
    runValueOne.setData(new Data(DataType.INTEGER, Long.valueOf(1)));
    
    equalsValueCheck.setValue(Long.MAX_VALUE); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueOne));
    
    // Test run value equals Long.MAX_VALUE.
    InstrumentRunValue runValueMaxInt = new InstrumentRunValue();
    runValueMaxInt.setInstrumentParameter(instrumentParameter);
    runValueMaxInt.setInstrumentParameter(instrumentParameter);
    runValueMaxInt.setData(new Data(DataType.INTEGER, Long.MAX_VALUE));
    
    equalsValueCheck.setValue(Long.valueOf(1));
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueMaxInt));
  }
  
  @Test
  public void testCheckParameterValueDecimal() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.DECIMAL);
    
    // Test run value equals 1.5.
    InstrumentRunValue runValueOnePointFive = new InstrumentRunValue();
    runValueOnePointFive.setInstrumentParameter(instrumentParameter);
    runValueOnePointFive.setInstrumentParameter(instrumentParameter);
    runValueOnePointFive.setData(new Data(DataType.DECIMAL, Double.valueOf(1.5)));
    
    equalsValueCheck.setValue(Double.valueOf(1.5));
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueOnePointFive));
    
    // Test run value equals Double.MAX_VALUE.
    InstrumentRunValue runValueMaxDecimal = new InstrumentRunValue();
    runValueMaxDecimal.setInstrumentParameter(instrumentParameter);
    runValueMaxDecimal.setInstrumentParameter(instrumentParameter);
    runValueMaxDecimal.setData(new Data(DataType.DECIMAL, Double.MAX_VALUE));
    
    equalsValueCheck.setValue(Double.MAX_VALUE);
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueMaxDecimal));
  }
  
  @Test
  public void testCheckParameterValueDecimalUnequal() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.DECIMAL);
    
    // Test run value equals 1.5.
    InstrumentRunValue runValueOnePointFive = new InstrumentRunValue();
    runValueOnePointFive.setInstrumentParameter(instrumentParameter);
    runValueOnePointFive.setInstrumentParameter(instrumentParameter);
    runValueOnePointFive.setData(new Data(DataType.DECIMAL, Double.valueOf(1.5)));
    
    equalsValueCheck.setValue(Double.MAX_VALUE); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueOnePointFive));
    
    // Test run value equals Double.MAX_VALUE.
    InstrumentRunValue runValueMaxDecimal = new InstrumentRunValue();
    runValueMaxDecimal.setInstrumentParameter(instrumentParameter);
    runValueMaxDecimal.setInstrumentParameter(instrumentParameter);
    runValueMaxDecimal.setData(new Data(DataType.DECIMAL, Double.MAX_VALUE));
    
    equalsValueCheck.setValue(Double.valueOf(1.5)); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueMaxDecimal));
  }
  
  @Test
  public void testCheckParameterValueText() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.TEXT);
    
    // Test run value equals "Foo".
    InstrumentRunValue runValueFoo = new InstrumentRunValue();
    runValueFoo.setInstrumentParameter(instrumentParameter);
    runValueFoo.setInstrumentParameter(instrumentParameter);
    runValueFoo.setData(new Data(DataType.TEXT, "Foo"));
    
    equalsValueCheck.setValue("Foo");
    Assert.assertTrue(equalsValueCheck.checkParameterValue(runValueFoo));
  }
  
  @Test
  public void testCheckParameterValueTextUnequal() {
    InstrumentParameter instrumentParameter = new InstrumentInputParameter();
    instrumentParameter.setDataType(DataType.TEXT);
    
    // Test run value equals "Foo".
    InstrumentRunValue runValueFoo = new InstrumentRunValue();
    runValueFoo.setInstrumentParameter(instrumentParameter);
    runValueFoo.setInstrumentParameter(instrumentParameter);
    runValueFoo.setData(new Data(DataType.TEXT, "Foo"));
    
    equalsValueCheck.setValue("Bar"); // unequal
    Assert.assertFalse(equalsValueCheck.checkParameterValue(runValueFoo));
  }
}
