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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.util.data.DataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 */
@Transactional
public class UpdateParticipantListenerTest extends BaseDefaultSpringContextTestCase {

  @Autowired(required = true)
  private PersistenceManager persistenceManager;

  @Autowired(required = true)
  private ParticipantService participantService;

  @Autowired(required = true)
  private ParticipantMetadata participantMetadata;

  // private UpdateParticipantListener updateParticipantListener;

  private Map<String, String> participantAttributesMap;

  @Before
  public void setUp() {
    participantAttributesMap = getParticipantAttributes();
    // updateParticipantListener = new UpdateParticipantListener("cag001", getUser(), participantService);
  }

  @Test
  @Dataset
  public void testOnParticipantRead() {
    int i = 1;
    for(Participant participant : getParticipants()) {
      // updateParticipantListener.onParticipantRead(i, participant);
      i++;
    }

    Assert.assertEquals(3l, persistenceManager.match(new Participant()).size());

    // test we can run same file multiple times without breaking the db
    i = 1;
    for(Participant participant : getParticipants()) {
      // updateParticipantListener.onParticipantRead(i, participant);
      i++;
    }

    Assert.assertEquals(3l, persistenceManager.match(new Participant()).size());
    Participant p = new Participant();
    p.setEnrollmentId("100001");
    p = persistenceManager.matchOne(p);
    Assert.assertNotNull("Cannot find participant", p);
    Assert.assertNotNull("Cannot find participant appointment", p.getAppointment());
    Assert.assertNotNull("Cannot find participant appointment date", p.getAppointment().getDate());

    // add a completed interview
    p = new Participant();
    p.setEnrollmentId("100003");
    p = persistenceManager.matchOne(p);
    Interview interview = new Interview();
    interview.setStatus(InterviewStatus.COMPLETED);
    interview.setStartDate(new Date());
    interview.setParticipant(p);
    persistenceManager.save(interview);
    p.setInterview(interview);
    persistenceManager.save(p);
    p = new Participant();
    p.setEnrollmentId("100003");
    p = persistenceManager.matchOne(p);
    Assert.assertNotNull("Cannot find participant", p);
    Assert.assertNotNull("Cannot find participant interview", p.getInterview());
    Assert.assertEquals("Cannot find participant completed interview", InterviewStatus.COMPLETED, p.getInterview().getStatus());

    i = 1;
    for(Participant participant : getParticipants()) {
      // updateParticipantListener.onParticipantRead(i, participant);
      i++;
    }

    Assert.assertEquals(3l, persistenceManager.match(new Participant()).size());
    p = new Participant();
    p.setEnrollmentId("100003");
    p = persistenceManager.matchOne(p);
    Assert.assertNotNull("Cannot find participant", p);
    Assert.assertNotNull("Cannot find participant appointment", p.getAppointment());
    Assert.assertNotNull("Cannot find participant appointment date", p.getAppointment().getDate());
    Assert.assertNotNull("Cannot find participant interview", p.getInterview());
    Assert.assertEquals("Cannot find participant completed interview", InterviewStatus.COMPLETED, p.getInterview().getStatus());

  }

  private User getUser() {
    User u = new User();
    u.setLastName("Onyx");
    u.setFirstName("Admin");
    return u;
  }

  private List<Participant> getParticipants() {
    List<Participant> participants = new ArrayList<Participant>();

    Participant p1 = new Participant();

    p1.setBirthDate(getDate(1964, 10, 1, 0, 0));
    p1.setEnrollmentId("100001");
    p1.setFirstName("Chantal");
    p1.setGender(Gender.FEMALE);
    p1.setLastName("Tremblay");
    p1.setSiteNo("cag001");

    Appointment a = new Appointment(p1, getDate(2009, 9, 1, 9, 0));
    a.setAppointmentCode("100001");
    p1.setAppointment(a);

    for(ParticipantAttribute configuredAttribute : participantMetadata.getConfiguredAttributes()) {
      if(configuredAttribute.isAssignableAtEnrollment()) {
        String value = participantAttributesMap.get("p1-" + configuredAttribute.getName().toUpperCase());
        p1.setConfiguredAttributeValue(configuredAttribute.getName(), DataBuilder.build(value));
      }
    }
    participants.add(p1);

    Participant p2 = new Participant();
    p2.setBirthDate(getDate(1964, 10, 2, 0, 0));
    p2.setEnrollmentId("100002");
    p2.setFirstName("Steve");
    p2.setGender(Gender.MALE);
    p2.setLastName("Smith");
    p2.setSiteNo("cag001");

    a = new Appointment(p2, getDate(2009, 9, 2, 9, 0));
    a.setAppointmentCode("100002");
    p2.setAppointment(a);

    for(ParticipantAttribute configuredAttribute : participantMetadata.getConfiguredAttributes()) {
      if(configuredAttribute.isAssignableAtEnrollment()) {
        String value = participantAttributesMap.get("p2-" + configuredAttribute.getName().toUpperCase());
        p2.setConfiguredAttributeValue(configuredAttribute.getName(), DataBuilder.build(value));
      }
    }
    participants.add(p2);

    Participant p3 = new Participant();
    p3.setBirthDate(getDate(1964, 10, 3, 0, 0));
    p3.setEnrollmentId("100003");
    p3.setFirstName("Suzan");
    p3.setGender(Gender.FEMALE);
    p3.setLastName("Casserly");
    p3.setSiteNo("cag001");

    a = new Appointment(p3, getDate(2009, 9, 3, 9, 0));
    a.setAppointmentCode("100003");
    p3.setAppointment(a);

    for(ParticipantAttribute configuredAttribute : participantMetadata.getConfiguredAttributes()) {
      if(configuredAttribute.isAssignableAtEnrollment()) {
        String value = participantAttributesMap.get("p3-" + configuredAttribute.getName().toUpperCase());
        p3.setConfiguredAttributeValue(configuredAttribute.getName(), DataBuilder.build(value));
      }
    }
    participants.add(p3);

    return participants;
  }

  private Map<String, String> getParticipantAttributes() {
    Map<String, String> map = new HashMap<String, String>();

    map.put("p1-STREET", "299, Avenue des Pins Ouest");
    map.put("p1-CITY", "Montréal");
    map.put("p1-PROVINCE", "QC");
    map.put("p1-COUNTRY", "Canada");
    map.put("p1-POSTAL CODE", "H1T2M4");
    map.put("p1-PHONE", "514-343-9898 ext 9494");

    map.put("p2-STREET", "309, C.P. , Suc.Centre-Ville");
    map.put("p2-CITY", "Vaudreuil Dorion");
    map.put("p2-PROVINCE", "QC");
    map.put("p2-COUNTRY", "Canada");
    map.put("p2-POSTAL CODE", "H3G 1A6");
    map.put("p2-PHONE", "514-343-9898 ext 9494");

    map.put("p3-STREET", "849, Blvd. Des Prairies");
    map.put("p3-CITY", "Vancouver");
    map.put("p3-PROVINCE", "QC");
    map.put("p3-COUNTRY", "Canada");
    map.put("p3-POSTAL CODE", "G1L 3L5");
    map.put("p3-PHONE", "514-343-9898 ext 9496");

    return map;
  }

  private Date getDate(int year, int month, int day, int hour, int minute) {
    Calendar c = Calendar.getInstance();

    c.set(Calendar.YEAR, year);
    c.set(Calendar.MONTH, month);
    c.set(Calendar.DAY_OF_MONTH, day);
    c.set(Calendar.HOUR_OF_DAY, hour);
    c.set(Calendar.MINUTE, minute);

    return c.getTime();
  }
}
