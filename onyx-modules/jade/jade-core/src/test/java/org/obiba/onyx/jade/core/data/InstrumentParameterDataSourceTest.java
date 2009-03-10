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
public class InstrumentParameterDataSourceTest {

  private static final String INSTRUMENT_TYPE = "SittingHeight";

  private static final String PARAMETER_CODE = "RES_FIRST_SITTING_HEIGHT";

  private InstrumentRunService instrumentRunServiceMock = createMock(InstrumentRunService.class);

  private InstrumentService instrumentServiceMock = createMock(InstrumentService.class);

  @Test
  public void testInstrumentParameterDataSourceNoParticipant() {
    InstrumentParameterDataSource parameterDataSource = new InstrumentParameterDataSource(INSTRUMENT_TYPE, PARAMETER_CODE);
    Data data = parameterDataSource.getData(null);
    Assert.assertNull(data);
  }

  @Test
  public void testInstrumentParameterDataSourceNoRunValue() {
    Participant participant = createParticipant();
    InstrumentParameterDataSource parameterDataSource = initInstrumentParameterDataSource();

    InstrumentType instrumentType = new InstrumentType(INSTRUMENT_TYPE, "description");
    InstrumentOutputParameter param = new InstrumentOutputParameter();

    expect(instrumentServiceMock.getInstrumentType(INSTRUMENT_TYPE)).andReturn(instrumentType).anyTimes();
    expect(instrumentServiceMock.getParameterByCode(instrumentType, PARAMETER_CODE)).andReturn(param).anyTimes();
    expect(instrumentRunServiceMock.findInstrumentRunValueFromLastRun(participant, instrumentType, PARAMETER_CODE)).andReturn(null);

    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);
    Data data = parameterDataSource.getData(participant);
    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertNull(data);
  }

  @Test
  public void testInstrumentParameterDataSourceWithData() {
    Participant participant = createParticipant();
    InstrumentParameterDataSource parameterDataSource = initInstrumentParameterDataSource();

    InstrumentType instrumentType = new InstrumentType(INSTRUMENT_TYPE, "description");
    InstrumentOutputParameter param = new InstrumentOutputParameter();
    param.setDataType(DataType.INTEGER);

    expect(instrumentServiceMock.getInstrumentType(INSTRUMENT_TYPE)).andReturn(instrumentType).anyTimes();
    expect(instrumentServiceMock.getParameterByCode(instrumentType, PARAMETER_CODE)).andReturn(param).anyTimes();
    expect(instrumentRunServiceMock.findInstrumentRunValueFromLastRun((Participant) EasyMock.anyObject(), (InstrumentType) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(createRunValue());

    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);
    Data data = parameterDataSource.getData(participant);
    verify(instrumentServiceMock);
    verify(instrumentRunServiceMock);

    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals("165", data.getValueAsString());
  }

  @Test
  public void testInstrumentParameterDataSourceWithUnit() {
    InstrumentParameterDataSource parameterDataSource = initInstrumentParameterDataSource();

    InstrumentType instrumentType = new InstrumentType(INSTRUMENT_TYPE, "description");
    InstrumentOutputParameter param = new InstrumentOutputParameter();
    param.setMeasurementUnit("cm");

    expect(instrumentServiceMock.getInstrumentType(INSTRUMENT_TYPE)).andReturn(instrumentType).anyTimes();
    expect(instrumentServiceMock.getParameterByCode(instrumentType, PARAMETER_CODE)).andReturn(param).anyTimes();

    replay(instrumentServiceMock);
    replay(instrumentRunServiceMock);
    String unit = parameterDataSource.getUnit();
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

  private InstrumentParameterDataSource initInstrumentParameterDataSource() {
    InstrumentParameterDataSource parameterDataSource = new InstrumentParameterDataSource(INSTRUMENT_TYPE, PARAMETER_CODE);
    parameterDataSource.setInstrumentService(instrumentServiceMock);
    parameterDataSource.setIntrumentRunService(instrumentRunServiceMock);
    return parameterDataSource;
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
