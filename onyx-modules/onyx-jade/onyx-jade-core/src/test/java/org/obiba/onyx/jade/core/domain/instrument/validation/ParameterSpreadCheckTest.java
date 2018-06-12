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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

public class ParameterSpreadCheckTest {

  private ParameterSpreadCheck parameterSpreadCheck;

  private Participant participant;

  private InstrumentRun instrumentRun;

  private InstrumentType instrumentType;

  private Instrument instrument;

  private InstrumentParameter checkedParameter;

  private InstrumentParameter otherParameter;

  private InstrumentRunService instrumentRunServiceMock;

  private ActiveInstrumentRunService activeInstrumentRunServiceMock;

  @Before
  public void setUp() {
    parameterSpreadCheck = new ParameterSpreadCheck();

    participant = new Participant();
    participant.setGender(Gender.MALE);

    instrumentType = new InstrumentType();

    instrument = new Instrument();
    instrument.addType(instrumentType.getName());

    instrumentRun = new InstrumentRun();
    instrumentRun.setParticipant(participant);
    instrumentRun.setInstrumentType(instrumentType.getName());

    checkedParameter = new InstrumentOutputParameter();
    otherParameter = new InstrumentInputParameter();
    otherParameter.setCode("otherParameterCode");

    parameterSpreadCheck.setParameterCode(otherParameter.getCode());

    instrumentRunServiceMock = createMock(InstrumentRunService.class);

    activeInstrumentRunServiceMock = createMock(ActiveInstrumentRunService.class);
  }

  /**
   * Tests DataType.INTEGER parameters with the same value.
   */
  @Test
  public void testIntegerParametersWithSameValue() {
    checkedParameter.setDataType(DataType.INTEGER);
    otherParameter.setDataType(DataType.INTEGER);

    // Set a spread of 5%.
    parameterSpreadCheck.setPercent(5);

    // Initialize checked parameter's run value.
    Data checkedData = DataBuilder.buildInteger(100l);

    // Test with other parameter's run value set to the SAME value.
    Data otherData = DataBuilder.buildInteger(100l);

    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter.getCode());
    otherRunValue.setData(otherData);

    expect(activeInstrumentRunServiceMock.getInstrumentRunValue(otherParameter.getCode())).andReturn(otherRunValue);
    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(parameterSpreadCheck.checkParameterValue(checkedParameter, checkedData, instrumentRunServiceMock, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }

  /**
   * Tests DataType.INTEGER parameters within the required spread.
   */
  @Test
  public void testIntegerParametersWithinSpread() {
    checkedParameter.setDataType(DataType.INTEGER);
    otherParameter.setDataType(DataType.INTEGER);

    // Set a spread of 5%.
    parameterSpreadCheck.setPercent(5);

    // Initialize other parameter's run value.
    Data otherData = DataBuilder.buildInteger(100l);

    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter.getCode());
    otherRunValue.setData(otherData);

    // Test with checked parameter's run value set to MINIMUM value within spread.
    Data checkedDataMin = DataBuilder.buildInteger(95l);

    expect(activeInstrumentRunServiceMock.getInstrumentRunValue(otherParameter.getCode())).andReturn(otherRunValue);
    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(parameterSpreadCheck.checkParameterValue(checkedParameter, checkedDataMin, instrumentRunServiceMock, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);

    // Reset mocks.
    reset(activeInstrumentRunServiceMock);

    // Test with checked parameter's run value set to MAXIMUM value within spread.
    Data checkedDataMax = DataBuilder.buildInteger(105l);

    expect(activeInstrumentRunServiceMock.getInstrumentRunValue(otherParameter.getCode())).andReturn(otherRunValue);
    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(parameterSpreadCheck.checkParameterValue(checkedParameter, checkedDataMax, instrumentRunServiceMock, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }

  /**
   * Tests DataType.INTEGER parameters outside the required spread.
   */
  @Test
  public void testIntegerParametersOutsideSpread() {
    checkedParameter.setDataType(DataType.INTEGER);
    otherParameter.setDataType(DataType.INTEGER);

    // Set a spread of 5%.
    parameterSpreadCheck.setPercent(5);

    // Initialize other parameter's run value.
    Data otherData = DataBuilder.buildInteger(100l);

    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter.getCode());
    otherRunValue.setData(otherData);

    // Test with checked parameter's run value set to LESS than the minimum value in spread.
    Data checkedDataMin = DataBuilder.buildInteger(94l);

    expect(activeInstrumentRunServiceMock.getInstrumentRunValue(otherParameter.getCode())).andReturn(otherRunValue);
    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertFalse(parameterSpreadCheck.checkParameterValue(checkedParameter, checkedDataMin, instrumentRunServiceMock, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);

    // Reset mocks.
    reset(activeInstrumentRunServiceMock);

    // Test with checked parameter's run value set to MORE than the maximum value in spread.
    Data checkedDataMax = DataBuilder.buildInteger(106l);

    expect(activeInstrumentRunServiceMock.getInstrumentRunValue(otherParameter.getCode())).andReturn(otherRunValue);
    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertFalse(parameterSpreadCheck.checkParameterValue(checkedParameter, checkedDataMax, instrumentRunServiceMock, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }

  /**
   * Tests DataType.DECIMAL parameters with the same value.
   */
  @Test
  public void testDecimalParametersWithSameValue() {
    checkedParameter.setDataType(DataType.DECIMAL);
    otherParameter.setDataType(DataType.DECIMAL);

    // Set a spread of 5%.
    parameterSpreadCheck.setPercent(5);

    // Initialize checked parameter's run value.
    Data checkedData = DataBuilder.buildDecimal(100.0);

    // Test with other parameter's run value set to the SAME value.
    Data otherData = DataBuilder.buildDecimal(100.0);

    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter.getCode());
    otherRunValue.setData(otherData);

    expect(activeInstrumentRunServiceMock.getInstrumentRunValue(otherParameter.getCode())).andReturn(otherRunValue);
    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(parameterSpreadCheck.checkParameterValue(checkedParameter, checkedData, instrumentRunServiceMock, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }

  /**
   * Tests DataType.DECIMAL parameters within the required spread.
   */
  @Test
  public void testDecimalParametersWithinSpread() {
    checkedParameter.setDataType(DataType.DECIMAL);
    otherParameter.setDataType(DataType.DECIMAL);

    // Set a spread of 5%.
    parameterSpreadCheck.setPercent(5);

    // Initialize other parameter's run value.
    Data otherData = DataBuilder.buildDecimal(100.0);

    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter.getCode());
    otherRunValue.setData(otherData);

    // Test with checked parameter's run value set to MINIMUM value within spread.
    Data checkedDataMin = DataBuilder.buildDecimal(95.0);

    expect(activeInstrumentRunServiceMock.getInstrumentRunValue(otherParameter.getCode())).andReturn(otherRunValue);
    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(parameterSpreadCheck.checkParameterValue(checkedParameter, checkedDataMin, instrumentRunServiceMock, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);

    // Reset mocks.
    reset(activeInstrumentRunServiceMock);

    // Test with checked parameter's run value set to MAXIMUM value within spread.
    Data checkedDataMax = DataBuilder.buildDecimal(105.0);

    expect(activeInstrumentRunServiceMock.getInstrumentRunValue(otherParameter.getCode())).andReturn(otherRunValue);
    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(parameterSpreadCheck.checkParameterValue(checkedParameter, checkedDataMax, instrumentRunServiceMock, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }

  /**
   * Tests DataType.DECIMAL parameters outside the required spread.
   */
  @Test
  public void testDecimalParametersOutsideSpread() {
    checkedParameter.setDataType(DataType.DECIMAL);
    otherParameter.setDataType(DataType.DECIMAL);

    // Set a spread of 5%.
    parameterSpreadCheck.setPercent(5);

    // Initialize other parameter's run value.
    Data otherData = DataBuilder.buildDecimal(100.0);

    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter.getCode());
    otherRunValue.setData(otherData);

    // Test with checked parameter's run value set to LESS than the minimum value in spread.
    Data checkedDataMin = DataBuilder.buildDecimal(94.0);

    expect(activeInstrumentRunServiceMock.getInstrumentRunValue(otherParameter.getCode())).andReturn(otherRunValue);
    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertFalse(parameterSpreadCheck.checkParameterValue(checkedParameter, checkedDataMin, instrumentRunServiceMock, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);

    // Reset mocks.
    reset(activeInstrumentRunServiceMock);

    // Test with checked parameter's run value set to MORE than the maximum value in spread.
    Data checkedDataMax = DataBuilder.buildDecimal(106.0);

    expect(activeInstrumentRunServiceMock.getInstrumentRunValue(otherParameter.getCode())).andReturn(otherRunValue);
    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertFalse(parameterSpreadCheck.checkParameterValue(checkedParameter, checkedDataMax, instrumentRunServiceMock, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }
}
