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
public class TypeConverterDataSourceTest {

  private IDataSource iDataSourceMock = createMock(IDataSource.class);

  @Test
  public void testTypeConverterDataSourceDecimalToText() {
    Participant participant = createParticipant();
    TypeConverterDataSource typeConverterDataSource = new TypeConverterDataSource(iDataSourceMock, DataType.TEXT);

    expect(iDataSourceMock.getData(participant)).andReturn(DataBuilder.buildDecimal(165.6));
    replay(iDataSourceMock);
    Data data = typeConverterDataSource.getData(participant);
    verify(iDataSourceMock);

    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals("165.6", data.getValue());
  }

  @Test
  public void testTypeConverterDataSourceDecimalToInteger() {
    Participant participant = createParticipant();
    TypeConverterDataSource typeConverterDataSource = new TypeConverterDataSource(iDataSourceMock, DataType.INTEGER);

    expect(iDataSourceMock.getData(participant)).andReturn(DataBuilder.buildDecimal(165.6));
    replay(iDataSourceMock);
    Data data = typeConverterDataSource.getData(participant);
    verify(iDataSourceMock);

    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals("166", data.getValueAsString());
  }

  @Test
  public void testTypeConverterDataSourceStringToDate() {
    Participant participant = createParticipant();
    TypeConverterDataSource typeConverterDataSource = new TypeConverterDataSource(iDataSourceMock, DataType.DATE);

    expect(iDataSourceMock.getData(participant)).andReturn(DataBuilder.buildText("1955-09-15"));
    replay(iDataSourceMock);
    Data data = typeConverterDataSource.getData(participant);
    verify(iDataSourceMock);

    Calendar c = Calendar.getInstance();
    c.set(1955, 8, 15, 0, 0, 0);

    Assert.assertEquals(DataType.DATE, data.getType());
    Assert.assertEquals(c.getTime().toString(), data.getValueAsString());

  }

  @Test
  public void testTypeConverterDataSourceStringToDateWrongFormat() {
    Participant participant = createParticipant();
    TypeConverterDataSource typeConverterDataSource = new TypeConverterDataSource(iDataSourceMock, DataType.DATE);

    expect(iDataSourceMock.getData(participant)).andReturn(DataBuilder.buildText("1955/09/15"));
    replay(iDataSourceMock);
    try {
      typeConverterDataSource.getData(participant);
      fail("Should get Exception.");
    } catch(Exception e) {
      Assert.assertEquals("Data cannot be parsed in type DATE: wrong format 1955/09/15", e.getMessage());
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
