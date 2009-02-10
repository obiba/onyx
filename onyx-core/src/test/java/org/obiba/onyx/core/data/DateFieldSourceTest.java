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
public class DateFieldSourceTest {

  private IDataSource iDataSourceMock = createMock(IDataSource.class);

  @Test
  public void testDateFieldSource() {
    Participant participant = createParticipant();

    expect(iDataSourceMock.getData(participant)).andReturn(DataBuilder.buildDate(participant.getBirthDate()));

    replay(iDataSourceMock);
    DateFieldSource dateFieldSource = new DateFieldSource(iDataSourceMock, Calendar.MONTH);
    Data data = dateFieldSource.getData(participant);
    verify(iDataSourceMock);

    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals(participant.getBirthMonth(), data.getValue());
  }

  @Test
  public void testDateFieldSourceWrongDataType() {
    Participant participant = createParticipant();

    expect(iDataSourceMock.getData(participant)).andReturn(DataBuilder.buildInteger(6));

    replay(iDataSourceMock);
    DateFieldSource dateFieldSource = new DateFieldSource(iDataSourceMock, Calendar.MONTH);
    try {
      Data data = dateFieldSource.getData(participant);
      fail("Should get IllegalArgumentException.");
    } catch(IllegalArgumentException e) {
      Assert.assertEquals("DataType " + DataType.DATE + " expected, " + DataType.INTEGER + " received.", e.getMessage());
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
    c.set(1973, 5, 15);
    p.setBirthDate(c.getTime());
    return p;
  }
}
