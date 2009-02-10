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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
public class FirstNotNullDataSourceTest {

  private IDataSource iDataSourceMock0 = createMock(IDataSource.class);

  private IDataSource iDataSourceMock1 = createMock(IDataSource.class);

  private IDataSource iDataSourceMock2 = createMock(IDataSource.class);

  @Test
  public void testFirstNotNullDataSource() {
    Participant participant = createParticipant();

    expect(iDataSourceMock0.getData(participant)).andReturn(null);
    expect(iDataSourceMock1.getData(participant)).andReturn(DataBuilder.buildInteger(65));
    expect(iDataSourceMock1.getUnit()).andReturn("kg");

    replay(iDataSourceMock0);
    replay(iDataSourceMock1);
    replay(iDataSourceMock2);

    FirstNotNullDataSource firstNotNullDataSource = new FirstNotNullDataSource(createIDataSourceList());
    Data data = firstNotNullDataSource.getData(participant);
    String unit = firstNotNullDataSource.getUnit();

    verify(iDataSourceMock0);
    verify(iDataSourceMock1);
    verify(iDataSourceMock2);

    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals(65, data.getValue());
    Assert.assertEquals("kg", unit);
  }

  @Test
  public void testNoResultFirstNotNullDataSource() {
    Participant participant = createParticipant();

    expect(iDataSourceMock0.getData(participant)).andReturn(null);
    expect(iDataSourceMock1.getData(participant)).andReturn(null);
    expect(iDataSourceMock2.getData(participant)).andReturn(null);

    replay(iDataSourceMock0);
    replay(iDataSourceMock1);
    replay(iDataSourceMock2);

    FirstNotNullDataSource firstNotNullDataSource = new FirstNotNullDataSource(createIDataSourceList());
    Data data = firstNotNullDataSource.getData(participant);
    String unit = firstNotNullDataSource.getUnit();

    verify(iDataSourceMock0);
    verify(iDataSourceMock1);
    verify(iDataSourceMock2);

    Assert.assertNull(data);
    Assert.assertNull(unit);
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

  private List<IDataSource> createIDataSourceList() {
    List<IDataSource> iDataSources = new ArrayList<IDataSource>();
    iDataSources.add(iDataSourceMock0);
    iDataSources.add(iDataSourceMock1);
    iDataSources.add(iDataSourceMock2);
    return iDataSources;
  }
}
