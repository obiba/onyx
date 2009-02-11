/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.data;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;

import java.util.Calendar;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class UnitConverterDataSourceTest {

  private IDataSource iDataSourceMock = createMock(IDataSource.class);

  @Test
  public void testUnitConverterDataSourceDecimalToInteger() {
    Participant participant = createParticipant();
    UnitConverterDataSource unitConverterDataSource = new UnitConverterDataSource(iDataSourceMock, "kg");

    expect(iDataSourceMock.getData(participant)).andReturn(DataBuilder.buildInteger(56420));
    expect(iDataSourceMock.getUnit()).andReturn("g").times(2);
    replay(iDataSourceMock);
    Data data = unitConverterDataSource.getData(participant);
    String unit = unitConverterDataSource.getUnit();
    verify(iDataSourceMock);

    Assert.assertEquals("56", data.getValueAsString());
    Assert.assertEquals("kg", unit);
  }

  @Test
  public void testUnitConverterDataSourceMonthToYear() {
    Participant participant = createParticipant();
    UnitConverterDataSource unitConverterDataSource = new UnitConverterDataSource(iDataSourceMock, "year");

    expect(iDataSourceMock.getData(participant)).andReturn(DataBuilder.buildInteger(491));
    expect(iDataSourceMock.getUnit()).andReturn("month").times(2);
    replay(iDataSourceMock);
    Data data = unitConverterDataSource.getData(participant);
    String unit = unitConverterDataSource.getUnit();
    verify(iDataSourceMock);

    Assert.assertEquals("40", data.getValueAsString());
    Assert.assertEquals("year", unit);
  }

  @Test
  public void testUnitConverterDataSourceMilliSecondsToYear() {
    Participant participant = createParticipant();
    UnitConverterDataSource unitConverterDataSource = new UnitConverterDataSource(iDataSourceMock, "year");

    expect(iDataSourceMock.getData(participant)).andReturn(DataBuilder.buildInteger(1280361600000l));
    expect(iDataSourceMock.getUnit()).andReturn("ms").times(2);
    replay(iDataSourceMock);
    Data data = unitConverterDataSource.getData(participant);
    String unit = unitConverterDataSource.getUnit();
    verify(iDataSourceMock);

    Assert.assertEquals("40", data.getValueAsString());
    Assert.assertEquals("year", unit);
  }

  @Test
  public void testUnitConverterDataSourceWrongDataType() {
    Participant participant = createParticipant();
    UnitConverterDataSource unitConverterDataSource = new UnitConverterDataSource(iDataSourceMock, "kg");

    expect(iDataSourceMock.getData(participant)).andReturn(DataBuilder.buildText("test not number type"));
    replay(iDataSourceMock);
    try {
      unitConverterDataSource.getData(participant);
      fail("Should get Exception.");
    } catch(Exception e) {
      Assert.assertEquals("DataType of number kind expected, " + DataType.TEXT + " received.", e.getMessage());
    }
    verify(iDataSourceMock);
  }

  @Test
  public void testUnitConverterDataSourceNoTargetUnit() {
    try {
      UnitConverterDataSource unitConverterDataSource = new UnitConverterDataSource(iDataSourceMock, null);
      fail("Should get Exception.");
    } catch(Exception e) {
      Assert.assertEquals("Target unit cannot be null.", e.getMessage());
    }
  }

  @Test
  public void testUnitConverterDataSourceNoSourceUnit() {
    Participant participant = createParticipant();
    UnitConverterDataSource unitConverterDataSource = new UnitConverterDataSource(iDataSourceMock, "kg");

    expect(iDataSourceMock.getData(participant)).andReturn(DataBuilder.buildInteger(56420));
    expect(iDataSourceMock.getUnit()).andReturn(null);
    replay(iDataSourceMock);
    try {
      Data data = unitConverterDataSource.getData(participant);
      String unit = unitConverterDataSource.getUnit();
      fail("Should get Exception.");
    } catch(Exception e) {
      Assert.assertEquals("Unit source cannot be null.", e.getMessage());
    }
    verify(iDataSourceMock);
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
