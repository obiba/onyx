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

import java.util.Calendar;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class ComparingDataSourceTest {

  private IDataSource iDataSourceMock0 = createMock(IDataSource.class);

  private IDataSource iDataSourceMock1 = createMock(IDataSource.class);

  @Test
  public void testComparingDataSource01() {
    Participant participant = createParticipant();

    expect(iDataSourceMock0.getData(participant)).andReturn(DataBuilder.buildInteger(65));
    expect(iDataSourceMock1.getData(participant)).andReturn(DataBuilder.buildInteger(65));

    replay(iDataSourceMock0);
    replay(iDataSourceMock1);

    ComparingDataSource dataSource = new ComparingDataSource(iDataSourceMock0, ComparisonOperator.eq, iDataSourceMock1);
    Data data = dataSource.getData(participant);

    verify(iDataSourceMock0);
    verify(iDataSourceMock1);

    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals(true, data.getValue());
  }

  @Test
  public void testComparingDataSource02() {
    Participant participant = createParticipant();

    expect(iDataSourceMock0.getData(participant)).andReturn(DataBuilder.buildInteger(65));
    expect(iDataSourceMock1.getData(participant)).andReturn(DataBuilder.buildInteger(65));

    replay(iDataSourceMock0);
    replay(iDataSourceMock1);

    ComparingDataSource dataSource = new ComparingDataSource(iDataSourceMock0, ComparisonOperator.ne, iDataSourceMock1);
    Data data = dataSource.getData(participant);

    verify(iDataSourceMock0);
    verify(iDataSourceMock1);

    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals(false, data.getValue());
  }

  @Test
  public void testComparingDataSource03() {
    Participant participant = createParticipant();

    expect(iDataSourceMock0.getData(participant)).andReturn(DataBuilder.buildInteger(65));
    expect(iDataSourceMock1.getData(participant)).andReturn(DataBuilder.buildInteger(65));

    replay(iDataSourceMock0);
    replay(iDataSourceMock1);

    ComparingDataSource dataSource = new ComparingDataSource(iDataSourceMock0, ComparisonOperator.ge, iDataSourceMock1);
    Data data = dataSource.getData(participant);

    verify(iDataSourceMock0);
    verify(iDataSourceMock1);

    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals(true, data.getValue());
  }

  @Test
  public void testComparingDataSource04() {
    Participant participant = createParticipant();

    expect(iDataSourceMock0.getData(participant)).andReturn(DataBuilder.buildInteger(66));
    expect(iDataSourceMock1.getData(participant)).andReturn(DataBuilder.buildInteger(65));

    replay(iDataSourceMock0);
    replay(iDataSourceMock1);

    ComparingDataSource dataSource = new ComparingDataSource(iDataSourceMock0, ComparisonOperator.ge, iDataSourceMock1);
    Data data = dataSource.getData(participant);

    verify(iDataSourceMock0);
    verify(iDataSourceMock1);

    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals(true, data.getValue());
  }

  @Test
  public void testComparingDataSource05() {
    Participant participant = createParticipant();

    expect(iDataSourceMock0.getData(participant)).andReturn(DataBuilder.buildInteger(65));
    expect(iDataSourceMock1.getData(participant)).andReturn(DataBuilder.buildInteger(65));

    replay(iDataSourceMock0);
    replay(iDataSourceMock1);

    ComparingDataSource dataSource = new ComparingDataSource(iDataSourceMock0, ComparisonOperator.le, iDataSourceMock1);
    Data data = dataSource.getData(participant);

    verify(iDataSourceMock0);
    verify(iDataSourceMock1);

    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals(true, data.getValue());
  }

  @Test
  public void testComparingDataSource06() {
    Participant participant = createParticipant();

    expect(iDataSourceMock0.getData(participant)).andReturn(DataBuilder.buildInteger(66));
    expect(iDataSourceMock1.getData(participant)).andReturn(DataBuilder.buildInteger(65));

    replay(iDataSourceMock0);
    replay(iDataSourceMock1);

    ComparingDataSource dataSource = new ComparingDataSource(iDataSourceMock0, ComparisonOperator.le, iDataSourceMock1);
    Data data = dataSource.getData(participant);

    verify(iDataSourceMock0);
    verify(iDataSourceMock1);

    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals(false, data.getValue());
  }

  @Test
  public void testComparingDataSource07() {
    Participant participant = createParticipant();

    expect(iDataSourceMock0.getData(participant)).andReturn(null);
    expect(iDataSourceMock1.getData(participant)).andReturn(null);

    replay(iDataSourceMock0);
    replay(iDataSourceMock1);

    ComparingDataSource dataSource = new ComparingDataSource(iDataSourceMock0, ComparisonOperator.eq, iDataSourceMock1);
    Data data = dataSource.getData(participant);

    verify(iDataSourceMock0);
    verify(iDataSourceMock1);

    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals(true, data.getValue());
  }

  @Test
  public void testComparingDataSource08() {
    Participant participant = createParticipant();

    expect(iDataSourceMock0.getData(participant)).andReturn(null);
    expect(iDataSourceMock1.getData(participant)).andReturn(null);

    replay(iDataSourceMock0);
    replay(iDataSourceMock1);

    ComparingDataSource dataSource = new ComparingDataSource(iDataSourceMock0, ComparisonOperator.ne, iDataSourceMock1);
    Data data = dataSource.getData(participant);

    verify(iDataSourceMock0);
    verify(iDataSourceMock1);

    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals(false, data.getValue());
  }

  @Test
  public void testComparingDataSource09() {
    Participant participant = createParticipant();

    expect(iDataSourceMock0.getData(participant)).andReturn(DataBuilder.buildInteger(66));
    expect(iDataSourceMock1.getData(participant)).andReturn(null);

    replay(iDataSourceMock0);
    replay(iDataSourceMock1);

    ComparingDataSource dataSource = new ComparingDataSource(iDataSourceMock0, ComparisonOperator.eq, iDataSourceMock1);
    Data data = dataSource.getData(participant);

    verify(iDataSourceMock0);
    verify(iDataSourceMock1);

    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals(false, data.getValue());
  }

  @Test
  public void testComparingDataSource10() {
    Participant participant = createParticipant();

    expect(iDataSourceMock0.getData(participant)).andReturn(null);
    expect(iDataSourceMock1.getData(participant)).andReturn(DataBuilder.buildInteger(66));

    replay(iDataSourceMock0);
    replay(iDataSourceMock1);

    ComparingDataSource dataSource = new ComparingDataSource(iDataSourceMock0, ComparisonOperator.eq, iDataSourceMock1);
    Data data = dataSource.getData(participant);

    verify(iDataSourceMock0);
    verify(iDataSourceMock1);

    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals(false, data.getValue());
  }

  @Test
  public void testComparingDataSource11() {
    Participant participant = createParticipant();

    expect(iDataSourceMock0.getData(participant)).andReturn(DataBuilder.buildInteger(64));
    expect(iDataSourceMock1.getData(participant)).andReturn(DataBuilder.buildInteger(65));

    replay(iDataSourceMock0);
    replay(iDataSourceMock1);

    ComparingDataSource dataSource = new ComparingDataSource(iDataSourceMock0, ComparisonOperator.lt, iDataSourceMock1);
    Data data = dataSource.getData(participant);

    verify(iDataSourceMock0);
    verify(iDataSourceMock1);

    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals(true, data.getValue());
  }

  @Test
  public void testComparingDataSource12() {
    Participant participant = createParticipant();

    expect(iDataSourceMock0.getData(participant)).andReturn(DataBuilder.buildInteger(66));
    expect(iDataSourceMock1.getData(participant)).andReturn(DataBuilder.buildInteger(65));

    replay(iDataSourceMock0);
    replay(iDataSourceMock1);

    ComparingDataSource dataSource = new ComparingDataSource(iDataSourceMock0, ComparisonOperator.gt, iDataSourceMock1);
    Data data = dataSource.getData(participant);

    verify(iDataSourceMock0);
    verify(iDataSourceMock1);

    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals(true, data.getValue());
  }

  @Test
  public void testComparingDataSource13() {

    ComparingDataSource dataSource = new ComparingDataSource(iDataSourceMock0, ComparisonOperator.gt, iDataSourceMock1);
    try {
      dataSource.addDataSource(iDataSourceMock0);
      Assert.assertFalse(true);
    } catch(Exception e) {
    }
  }

  private Participant createParticipant() {
    Participant p = new Participant();
    p.setBarcode("1187432");
    p.setLastName("Tremblay");
    p.setFirstName("Patricia");
    p.setGender(Gender.FEMALE);
    Calendar c = Calendar.getInstance();
    c.set(1973, 5, 15);
    p.setBirthDate(c.getTime());
    return p;
  }

}
