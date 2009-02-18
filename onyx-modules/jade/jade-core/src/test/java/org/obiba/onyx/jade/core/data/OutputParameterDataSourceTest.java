/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.data;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Calendar;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class OutputParameterDataSourceTest {

  private static final String INSTRUMENT_TYPE = "SittingHeight";

  private static final String PARAMETER_CODE = "RES_FIRST_SITTING_HEIGHT";

  private InstrumentRunService instrumentRunServiceMock = createMock(InstrumentRunService.class);

  private InstrumentService instrumentServiceMock = createMock(InstrumentService.class);

  @Test
  public void testOutputParameterDataSourceNoParticipant() {
    OutputParameterDataSource outputParameterDataSource = new OutputParameterDataSource(INSTRUMENT_TYPE, PARAMETER_CODE);
    Data data = outputParameterDataSource.getData(null);
    Assert.assertNull(data);
  }

  @Test
  public void testOutputParameterDataSourceNoRunValue() {
    Participant participant = createParticipant();
    OutputParameterDataSource outputParameterDataSource = initOutputParameterDataSource();

    InstrumentType instrumentType = new InstrumentType(INSTRUMENT_TYPE, "description");
    InstrumentOutputParameter outputParam = new InstrumentOutputParameter();

    expect(instrumentServiceMock.getInstrumentType(INSTRUMENT_TYPE)).andReturn(instrumentType).anyTimes();
    expect(instrumentServiceMock.getInstrumentOutputParameter(instrumentType, PARAMETER_CODE)).andReturn(outputParam);
    expect(instrumentRunServiceMock.findInstrumentRunValueFromLastRun(participant, instrumentType, PARAMETER_CODE)).andReturn(null);

    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);
    Data data = outputParameterDataSource.getData(participant);
    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertNull(data);
  }

  @Test
  public void testOutputParameterDataSourceWithData() {
    Participant participant = createParticipant();
    OutputParameterDataSource outputParameterDataSource = initOutputParameterDataSource();

    InstrumentType instrumentType = new InstrumentType(INSTRUMENT_TYPE, "description");
    InstrumentOutputParameter outputParam = new InstrumentOutputParameter();
    outputParam.setDataType(DataType.INTEGER);

    expect(instrumentServiceMock.getInstrumentType(INSTRUMENT_TYPE)).andReturn(instrumentType).anyTimes();
    expect(instrumentServiceMock.getInstrumentOutputParameter(instrumentType, PARAMETER_CODE)).andReturn(outputParam);
    expect(instrumentRunServiceMock.findInstrumentRunValueFromLastRun((Participant) EasyMock.anyObject(), (InstrumentType) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(createRunValue());

    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);
    Data data = outputParameterDataSource.getData(participant);
    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals("165", data.getValueAsString());
  }

  @Test
  public void testOutputParameterDataSourceWithUnit() {
    OutputParameterDataSource outputParameterDataSource = initOutputParameterDataSource();

    expect(instrumentServiceMock.getInstrumentType(INSTRUMENT_TYPE)).andReturn(new InstrumentType(INSTRUMENT_TYPE, "description"));
    expect(instrumentServiceMock.getInstrumentOutputParameter((InstrumentType) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(createInstrumentOutputParameter());

    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);
    String unit = outputParameterDataSource.getUnit();
    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertEquals("cm", unit);
  }

  private InstrumentOutputParameter createInstrumentOutputParameter() {
    InstrumentOutputParameter instrumentOutputParameter = new InstrumentOutputParameter();
    instrumentOutputParameter.setCode(PARAMETER_CODE);
    instrumentOutputParameter.setDataType(DataType.INTEGER);
    instrumentOutputParameter.setMeasurementUnit("cm");
    return instrumentOutputParameter;
  }

  private InstrumentRunValue createRunValue() {
    InstrumentRunValue instrumentRunValue = new InstrumentRunValue();
    instrumentRunValue.setInstrumentParameter(createInstrumentOutputParameter().getCode());
    instrumentRunValue.setData(DataBuilder.buildInteger(165l));
    return instrumentRunValue;
  }

  private OutputParameterDataSource initOutputParameterDataSource() {
    OutputParameterDataSource outputParameterDataSource = new OutputParameterDataSource(INSTRUMENT_TYPE, PARAMETER_CODE);
    outputParameterDataSource.setInstrumentService(instrumentServiceMock);
    outputParameterDataSource.setIntrumentRunService(instrumentRunServiceMock);
    return outputParameterDataSource;
  }

  private Participant createParticipant() {
    Participant p = new Participant();
    p.setBarcode("1187432");
    p.setLastName("Tremblay");
    p.setFirstName("Patricia");
    p.setGender(Gender.FEMALE);
    Calendar c = Calendar.getInstance();
    c.set(1973, 1, 15);
    p.setBirthDate(c.getTime());
    return p;
  }
}
