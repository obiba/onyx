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

import java.util.Calendar;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class ParticipantPropertyDataSourceTest {

  @Test
  public void testParticipantPropertyDataSource() {
    Participant participant = createParticipant();
    ParticipantPropertyDataSource participantPropertyDataSource = new ParticipantPropertyDataSource("firstName");
    Data data = participantPropertyDataSource.getData(participant);
    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals(participant.getFirstName(), data.getValue());

    participantPropertyDataSource = new ParticipantPropertyDataSource("fullName");
    data = participantPropertyDataSource.getData(participant);
    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals(participant.getFullName(), data.getValue());

    participantPropertyDataSource = new ParticipantPropertyDataSource("gender");
    data = participantPropertyDataSource.getData(participant);
    Assert.assertEquals(DataType.TEXT, data.getType());
    Assert.assertEquals(participant.getGender().toString(), data.getValueAsString());
  }

  @Test
  public void testParticipantPropertyDataSourceWithUnit() {
    Participant participant = createParticipant();
    ParticipantPropertyDataSource participantPropertyDataSource = new ParticipantPropertyDataSource("age", "years");
    Data data = participantPropertyDataSource.getData(participant);
    String unit = participantPropertyDataSource.getUnit();

    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals(participant.getAge(), data.getValue());
    Assert.assertEquals("years", unit);
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
