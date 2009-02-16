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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class CurrentDateSourceTest {

  @Test
  public void testCurrentDateSource() {
    Participant participant = createParticipant();
    CurrentDateSource currentDateSource = new CurrentDateSource();
    Data data = currentDateSource.getData(participant);

    Date current = new Date();
    Assert.assertEquals(DataType.DATE, data.getType());
    Assert.assertEquals(current.toString(), data.getValueAsString());
  }

  @Test
  public void testCurrentMonthSource() {
    Participant participant = createParticipant();
    CurrentDateSource currentDateSource = new CurrentDateSource(Calendar.MONTH);
    Data data = currentDateSource.getData(participant);

    Calendar current = Calendar.getInstance();
    current.setTime(new Date());

    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals(current.get(Calendar.MONTH), data.getValue());
  }

  @Test
  public void testModifiedDateSource() {
    Participant participant = createParticipant();
    CurrentDateSource currentDateSource = new CurrentDateSource(createDateModifierList());
    Data data = currentDateSource.getData(participant);

    Calendar current = Calendar.getInstance();
    current.setTime(new Date());
    current.add(Calendar.YEAR, -20);
    current.add(Calendar.MONTH, 6);

    Assert.assertEquals(DataType.DATE, data.getType());
    Date date = data.getValue();
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);

    Assert.assertEquals(current.get(Calendar.YEAR), cal.get(Calendar.YEAR));
    Assert.assertEquals(current.get(Calendar.MONTH), cal.get(Calendar.MONTH));
    Assert.assertEquals(current.get(Calendar.DATE), cal.get(Calendar.DATE));
  }

  @Test
  public void testModifiedYearSource() {
    Participant participant = createParticipant();
    CurrentDateSource currentDateSource = new CurrentDateSource(Calendar.YEAR, createDateModifierList());
    Data data = currentDateSource.getData(participant);

    Calendar current = Calendar.getInstance();
    current.setTime(new Date());
    current.add(Calendar.YEAR, -20);
    current.add(Calendar.MONTH, 6);

    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals(current.get(Calendar.YEAR), data.getValue());
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
    modifiers.add(new DateModifier(Calendar.YEAR, -20));
    modifiers.add(new DateModifier(Calendar.MONTH, 6));
    return modifiers;
  }
}
