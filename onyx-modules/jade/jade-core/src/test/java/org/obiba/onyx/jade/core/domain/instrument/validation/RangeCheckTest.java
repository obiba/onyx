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
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

public class RangeCheckTest {

  private RangeCheck rangeCheck;

  private Participant participant;

  private InstrumentParameter instrumentParameter;

  private ActiveInstrumentRunService activeInstrumentRunServiceMock;

  @Before
  public void setUp() {
    rangeCheck = new RangeCheck();

    participant = new Participant();
    participant.setGender(Gender.MALE);

    instrumentParameter = new InstrumentInputParameter();

    activeInstrumentRunServiceMock = createMock(ActiveInstrumentRunService.class);
  }

  /**
   * Tests DataType.INTEGER value within range.
   */
  @Test
  public void testIntegerWithinRange() {
    instrumentParameter.setDataType(DataType.INTEGER);

    long minValue = 100l;
    long maxValue = 200l;

    rangeCheck.setIntegerMinValueMale(minValue);
    rangeCheck.setIntegerMaxValueMale(maxValue);

    // Test with run value in the middle of the range.
    Data middleData = DataBuilder.buildInteger(150l);

    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(rangeCheck.checkParameterValue(instrumentParameter, middleData, null, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);

    // Reset mocks.
    reset(activeInstrumentRunServiceMock);

    // Test with run value equal to the minimum.
    Data minData = DataBuilder.buildInteger(100l);

    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(rangeCheck.checkParameterValue(instrumentParameter, minData, null, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);

    // Reset mocks.
    reset(activeInstrumentRunServiceMock);

    // Test with run value equal to the maximum.
    Data maxData = DataBuilder.buildInteger(200l);

    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(rangeCheck.checkParameterValue(instrumentParameter, maxData, null, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }

  /**
   * Tests DataType.INTEGER value out of range.
   */
  @Test
  public void testIntegerOutOfRange() {
    instrumentParameter.setDataType(DataType.INTEGER);

    long minValue = 100l;
    long maxValue = 200l;

    rangeCheck.setIntegerMinValueMale(minValue);
    rangeCheck.setIntegerMaxValueMale(maxValue);

    // Test with run value less than the minimum.
    Data tooSmallData = DataBuilder.buildInteger(99l);

    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertFalse(rangeCheck.checkParameterValue(instrumentParameter, tooSmallData, null, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);

    // Reset mocks.
    reset(activeInstrumentRunServiceMock);

    // Test with run value greater than the maximum.
    Data tooBigData = DataBuilder.buildInteger(201l);

    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertFalse(rangeCheck.checkParameterValue(instrumentParameter, tooBigData, null, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }

  /**
   * Tests DataType.INTEGER value with a range that has a maximum but no minimum.
   */
  @Test
  public void testIntegerNoMinimum() {
    instrumentParameter.setDataType(DataType.INTEGER);

    rangeCheck.setIntegerMinValueMale(null);
    rangeCheck.setIntegerMaxValueMale(200l);

    // Test run value of Long.MIN_VALUE.
    Data minData = DataBuilder.buildInteger(Long.MIN_VALUE);

    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(rangeCheck.checkParameterValue(instrumentParameter, minData, null, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }

  /**
   * Tests DataType.INTEGER value with a range that has a minimum but no maximum.
   */
  @Test
  public void testIntegerNoMaximum() {
    instrumentParameter.setDataType(DataType.INTEGER);

    rangeCheck.setIntegerMinValueMale(100l);
    rangeCheck.setIntegerMaxValueMale(null);

    // Test run value of Long.MAX_VALUE.
    Data maxData = DataBuilder.buildInteger(Long.MAX_VALUE);

    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(rangeCheck.checkParameterValue(instrumentParameter, maxData, null, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }

  /**
   * Tests DataType.DECIMAL value within range.
   */
  @Test
  public void testDecimalWithinRange() {
    instrumentParameter.setDataType(DataType.DECIMAL);

    double minValue = 100.0;
    double maxValue = 200.0;

    rangeCheck.setDecimalMinValueMale(minValue);
    rangeCheck.setDecimalMaxValueMale(maxValue);

    // Test with run value in the middle of the range.
    Data middleData = DataBuilder.buildDecimal(150.0);

    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(rangeCheck.checkParameterValue(instrumentParameter, middleData, null, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);

    // Reset mocks.
    reset(activeInstrumentRunServiceMock);

    // Test with run value equal to the minimum.
    Data minData = DataBuilder.buildDecimal(100.0);

    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(rangeCheck.checkParameterValue(instrumentParameter, minData, null, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);

    // Reset mocks.
    reset(activeInstrumentRunServiceMock);

    // Test with run value equal to the maximum.
    Data maxData = DataBuilder.buildDecimal(200.0);

    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(rangeCheck.checkParameterValue(instrumentParameter, maxData, null, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }

  /**
   * Tests DataType.DECIMAL value out of range.
   */
  @Test
  public void testDecimalOutOfRange() {
    instrumentParameter.setDataType(DataType.DECIMAL);

    double minValue = 100.0;
    double maxValue = 200.0;

    rangeCheck.setDecimalMinValueMale(minValue);
    rangeCheck.setDecimalMaxValueMale(maxValue);

    // Test with run value less than the minimum.
    Data tooSmallData = DataBuilder.buildDecimal(99.0);

    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertFalse(rangeCheck.checkParameterValue(instrumentParameter, tooSmallData, null, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);

    // Reset mocks.
    reset(activeInstrumentRunServiceMock);

    // Test with run value greater than the maximum.
    Data tooBigData = DataBuilder.buildDecimal(201.0);

    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertFalse(rangeCheck.checkParameterValue(instrumentParameter, tooBigData, null, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }

  /**
   * Tests DataType.DECIMAL value with a range that has a maximum but no minimum.
   */
  @Test
  public void testDecimalNoMinimum() {
    instrumentParameter.setDataType(DataType.DECIMAL);

    rangeCheck.setDecimalMinValueMale(null);
    rangeCheck.setDecimalMaxValueMale(200.0);

    // Test run value of Double.MIN_VALUE.
    Data minData = DataBuilder.buildDecimal(Double.MIN_VALUE);

    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(rangeCheck.checkParameterValue(instrumentParameter, minData, null, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }

  /**
   * Tests DataType.DECIMAL value with a range that has a minimum but no maximum.
   */
  @Test
  public void testDecimalNoMaximum() {
    instrumentParameter.setDataType(DataType.DECIMAL);

    rangeCheck.setDecimalMinValueMale(100.0);
    rangeCheck.setDecimalMaxValueMale(null);

    // Test run value of Double.MAX_VALUE.
    Data maxData = DataBuilder.buildDecimal(Double.MAX_VALUE);

    expect(activeInstrumentRunServiceMock.getParticipant()).andReturn(participant);

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(rangeCheck.checkParameterValue(instrumentParameter, maxData, null, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }
}
