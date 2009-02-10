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
public class ModifiedDateSourceTest {

  private IDataSource iDataSourceMock = createMock(IDataSource.class);

  @Test
  public void testModifiedDateSource() {
    Participant participant = createParticipant();
    ModifiedDateSource modifiedDateSource = new ModifiedDateSource(iDataSourceMock, createDateModifierList());

    expect(iDataSourceMock.getData(participant)).andReturn(DataBuilder.buildDate(participant.getBirthDate()));

    replay(iDataSourceMock);
    Data data = modifiedDateSource.getData(participant);
    verify(iDataSourceMock);

    Calendar c = Calendar.getInstance();
    c.setTime(participant.getBirthDate());
    c.add(Calendar.YEAR, 5);
    c.add(Calendar.MONTH, 6);

    Assert.assertEquals(DataType.DATE, data.getType());
    Assert.assertEquals(c.getTime(), data.getValue());
  }

  @Test
  public void testNoDataModifiedDateSource() {
    Participant participant = createParticipant();
    ModifiedDateSource modifiedDateSource = new ModifiedDateSource(iDataSourceMock, createDateModifierList());

    expect(iDataSourceMock.getData(participant)).andReturn(null);

    replay(iDataSourceMock);
    Data data = modifiedDateSource.getData(participant);
    verify(iDataSourceMock);

    Assert.assertNull(data);
  }

  @Test
  public void testModifiedDateSourceWrongDataType() {
    Participant participant = createParticipant();
    ModifiedDateSource modifiedDateSource = new ModifiedDateSource(iDataSourceMock, createDateModifierList());

    expect(iDataSourceMock.getData(participant)).andReturn(DataBuilder.buildInteger(6));

    replay(iDataSourceMock);

    try {
      Data data = modifiedDateSource.getData(participant);
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
    c.set(1973, 1, 15);
    p.setBirthDate(c.getTime());
    return p;
  }

  private List<DateModifier> createDateModifierList() {
    List<DateModifier> modifiers = new ArrayList<DateModifier>();
    modifiers.add(new DateModifier(Calendar.YEAR, 5));
    modifiers.add(new DateModifier(Calendar.MONTH, 6));
    return modifiers;
  }
}
