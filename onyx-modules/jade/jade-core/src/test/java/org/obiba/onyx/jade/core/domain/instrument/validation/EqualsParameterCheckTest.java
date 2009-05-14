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
import static org.easymock.EasyMock.verify;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

public class EqualsParameterCheckTest {

  private EqualsParameterCheck equalsParameterCheck;

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
    equalsParameterCheck = new EqualsParameterCheck();

    instrumentType = new InstrumentType();

    instrument = new Instrument();
    instrument.setType(instrumentType.getName());

    instrumentRun = new InstrumentRun();
    instrumentRun.setParticipant(participant);
    instrumentRun.setInstrumentType(instrumentType.getName());

    checkedParameter = new InstrumentOutputParameter();
    checkedParameter.setCode("checkedParamName");
    instrumentType.addInstrumentParameter(checkedParameter);

    otherParameter = new InstrumentInputParameter();
    otherParameter.setCode("otherParamName");
    instrumentType.addInstrumentParameter(otherParameter);

    equalsParameterCheck.setParameterCode(otherParameter.getCode());

    instrumentRunServiceMock = createMock(InstrumentRunService.class);

    activeInstrumentRunServiceMock = createMock(ActiveInstrumentRunService.class);
  }

  /**
   * Tests equal DataType.INTEGER parameters.
   */
  @Test
  public void testIntegerParametersEqual() {
    checkedParameter.setDataType(DataType.INTEGER);
    otherParameter.setDataType(DataType.INTEGER);

    // Initialize checked parameter's run value.
    Data checkedData = DataBuilder.buildInteger(100l);

    // Initialize other parameter's run value.
    Data otherData = DataBuilder.buildInteger(100l);

    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter.getCode());
    otherRunValue.setData(otherData);

    expect(activeInstrumentRunServiceMock.getInstrumentRunValue(otherParameter.getCode())).andReturn(otherRunValue);
    expect(activeInstrumentRunServiceMock.getInstrumentType()).andReturn(instrumentType).atLeastOnce();

    replay(activeInstrumentRunServiceMock);

    Assert.assertTrue(equalsParameterCheck.checkParameterValue(checkedParameter, checkedData, instrumentRunServiceMock, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }

  /**
   * Tests unequal DataType.INTEGER parameters.
   */
  @Test
  public void testIntegerParametersUnequal() {
    checkedParameter.setDataType(DataType.INTEGER);
    otherParameter.setDataType(DataType.INTEGER);

    // Initialize checked parameter's run value.
    Data checkedData = DataBuilder.buildInteger(100l);

    // Initialize other parameter's run value.
    Data otherData = DataBuilder.buildInteger(200l);

    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter.getCode());
    otherRunValue.setData(otherData);

    expect(activeInstrumentRunServiceMock.getInstrumentRunValue(otherParameter.getCode())).andReturn(otherRunValue);
    expect(activeInstrumentRunServiceMock.getInstrumentType()).andReturn(instrumentType).atLeastOnce();

    replay(activeInstrumentRunServiceMock);

    Assert.assertFalse(equalsParameterCheck.checkParameterValue(checkedParameter, checkedData, instrumentRunServiceMock, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
  }

  @Test
  public void testParametersWithOperator() {
    checkedParameter.setDataType(DataType.INTEGER);
    otherParameter.setDataType(DataType.INTEGER);

    // Initialize checked parameter's run value.
    Data checkedData = DataBuilder.buildInteger(9l);

    // Initialize other parameter's run value.
    Data otherData = DataBuilder.buildInteger(100l);

    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter.getCode());
    otherRunValue.setData(otherData);

    expect(activeInstrumentRunServiceMock.getInstrumentRunValue(otherParameter.getCode())).andReturn(otherRunValue);
    expect(activeInstrumentRunServiceMock.getInstrumentType()).andReturn(instrumentType).atLeastOnce();

    replay(activeInstrumentRunServiceMock);
    equalsParameterCheck.setOperator(ComparisonOperator.LESSER);
    Assert.assertTrue(equalsParameterCheck.checkParameterValue(checkedParameter, checkedData, instrumentRunServiceMock, activeInstrumentRunServiceMock));

  }
}
