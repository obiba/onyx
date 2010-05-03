/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import java.util.Calendar;
import java.util.Date;

import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.core.service.ParticipantRegistry;
import org.obiba.onyx.util.data.DataBuilder;

/**
 * This implementation of the {@link ParticipantRegistry} returns one of four fixed {@link Participant}s and is intended
 * for use during development. Providing the unique ids 1-4 will return one of the {@code Particpant}s. Id 5 will
 * produce a {@link ParticipantRegistryLookupException}. Anything else will produce a {@link NoSuchParticipantException}
 * .
 */
public class FixedParticipantRegistry implements ParticipantRegistry {

  public Participant lookupParticipant(String uniqueId) throws NoSuchParticipantException, ParticipantRegistryLookupException {
    int id = getUniqueIdAsInteger(uniqueId);
    if(id >= 1 && id <= 4) {
      return getParticipant(id);
    } else if(id == 5) {
      throw new ParticipantRegistryLookupException();
    } else {
      throw new NoSuchParticipantException(uniqueId);
    }
  }

  /**
   * @param uniqueId
   * @return
   */
  private int getUniqueIdAsInteger(String uniqueId) {
    try {
      return Integer.valueOf(uniqueId).intValue();
    } catch(NumberFormatException e) {
      throw new NoSuchParticipantException(uniqueId);
    }
  }

  private Participant getParticipant(int id) {
    switch(id) {
    case 1:
      return participantOne();
    case 2:
      return participantTwo();
    case 3:
      return participantThree();
    case 4:
      return participantFour();
    default:
      throw new NoSuchParticipantException(Integer.toString(id));
    }
  }

  private Participant participantOne() {
    Participant p = new Participant();
    p.setFirstName("Bodvild");
    p.setLastName("Nidudsdatter");
    p.setGender(Gender.FEMALE);
    p.setBirthDate(DateBuilder.newBuilder().year(1960).month(1).day(23).build());
    p.setRecruitmentType(RecruitmentType.VOLUNTEER);
    p.setConfiguredAttributeValue("Street", DataBuilder.buildText("392 Iduns Gate"));
    p.setConfiguredAttributeValue("City", DataBuilder.buildText("Oslo"));
    return p;
  }

  private Participant participantTwo() {
    Participant p = new Participant();
    p.setFirstName("Herdis");
    p.setLastName("Ketilsdottir");
    p.setGender(Gender.FEMALE);
    p.setBirthDate(DateBuilder.newBuilder().year(1955).month(5).day(15).build());
    p.setRecruitmentType(RecruitmentType.VOLUNTEER);
    p.setConfiguredAttributeValue("Street", DataBuilder.buildText("12 Ingerthas vei"));
    p.setConfiguredAttributeValue("City", DataBuilder.buildText("Kristiansand"));
    return p;
  }

  private Participant participantThree() {
    Participant p = new Participant();
    p.setFirstName("Anir");
    p.setLastName("Bowswayer");
    p.setGender(Gender.MALE);
    p.setBirthDate(DateBuilder.newBuilder().year(1950).month(7).day(14).build());
    p.setRecruitmentType(RecruitmentType.VOLUNTEER);
    p.setConfiguredAttributeValue("Street", DataBuilder.buildText("782 Grooseveien"));
    p.setConfiguredAttributeValue("City", DataBuilder.buildText("Grimstad"));
    return p;
  }

  private Participant participantFour() {
    Participant p = new Participant();
    p.setFirstName("Jokull");
    p.setLastName("Ingimundson");
    p.setGender(Gender.MALE);
    p.setBirthDate(DateBuilder.newBuilder().year(1945).month(9).day(19).build());
    p.setRecruitmentType(RecruitmentType.VOLUNTEER);
    p.setConfiguredAttributeValue("Street", DataBuilder.buildText("33 Skarkoll"));
    p.setConfiguredAttributeValue("City", DataBuilder.buildText("Froland Verk"));
    return p;
  }

  private static class DateBuilder {
    private int year;

    private int month;

    private int day;

    public static DateBuilder newBuilder() {
      return new DateBuilder();
    }

    public DateBuilder year(int year) {
      this.year = year;
      return this;
    }

    public DateBuilder month(int month) {
      this.month = month - 1; // zero-based
      return this;
    }

    public DateBuilder day(int day) {
      this.day = day;
      return this;
    }

    public Date build() {
      Calendar c = Calendar.getInstance();
      c.set(year, month, day);
      return c.getTime();
    }
  }
}
