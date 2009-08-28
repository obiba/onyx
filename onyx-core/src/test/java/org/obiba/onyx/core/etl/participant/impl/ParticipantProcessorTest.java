/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantAttributeReader;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.util.data.DataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * 
 */
public class ParticipantProcessorTest extends BaseDefaultSpringContextTestCase {

  private static final String TEST_RESOURCES_DIR = ParticipantReaderTest.class.getSimpleName();

  private ParticipantReaderTest participantReaderTest = new ParticipantReaderTest();;

  private ParticipantProcessor processor = new ParticipantProcessor();

  private ApplicationConfiguration config;

  private ApplicationConfigurationService applicationConfigurationServiceMock;

  private ParticipantService participantServiceMock;

  @Autowired(required = true)
  private ParticipantMetadata participantMetadata;

  @Autowired(required = true)
  private PersistenceManager persistenceManager;

  private Map<String, String> participantAttributesMap;

  @Before
  public void setUp() throws Exception {
    applicationConfigurationServiceMock = createMock(ApplicationConfigurationService.class);
    participantServiceMock = createMock(ParticipantService.class);
    config = new ApplicationConfiguration();
    config.setSiteNo("cag001");

    processor.setParticipantMetadata(initParticipantMetadata());
    processor.setApplicationConfigurationService(applicationConfigurationServiceMock);
    processor.setParticipantService(participantServiceMock);

    participantAttributesMap = getParticipantAttributes();
  }

  /**
   * Tests processing of an appointment list that contains no configured attributes (i.e., essential attributes only).
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithNoConfiguredAttributes() throws IOException {
    expect(applicationConfigurationServiceMock.getApplicationConfiguration()).andReturn(config);
    expectLastCall().times(3);

    expect(participantServiceMock.getParticipant((Participant) EasyMock.anyObject())).andReturn(null);
    expectLastCall().times(3);

    List<Participant> participants = participantReaderTest.getParticipantList(false, TEST_RESOURCES_DIR + "/appointmentList_noConfiguredAttributes.xls");
    List<Participant> processedParticipants = new ArrayList<Participant>();

    replay(applicationConfigurationServiceMock);
    replay(participantServiceMock);

    try {
      for(Participant participantItem : participants) {
        Participant participant = processor.process(participantItem);
        if(participant != null) processedParticipants.add(participant);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    verify(applicationConfigurationServiceMock);
    verify(participantServiceMock);

    Assert.assertEquals(3, processedParticipants.size());
    Assert.assertEquals("Tremblay", processedParticipants.get(0).getLastName());
    Assert.assertEquals("Casserly", processedParticipants.get(2).getLastName());
  }

  /**
   * Tests processing of an appointment list where a mandatory attribute (Enrollment ID, at line 4) has not been
   * assigned a value. The processor should not return this participant
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithMissingMandatoryAttributeValue() throws IOException {
    expect(applicationConfigurationServiceMock.getApplicationConfiguration()).andReturn(config);
    expectLastCall().times(2);

    expect(participantServiceMock.getParticipant((Participant) EasyMock.anyObject())).andReturn(null);
    expectLastCall().times(2);

    List<Participant> participants = participantReaderTest.getParticipantList(false, TEST_RESOURCES_DIR + "/appointmentList_missingMandatoryAttributeValue.xls");
    List<Participant> processedParticipants = new ArrayList<Participant>();

    replay(applicationConfigurationServiceMock);
    replay(participantServiceMock);

    try {
      for(Participant participantItem : participants) {
        Participant participant = processor.process(participantItem);
        if(participant != null) processedParticipants.add(participant);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    verify(applicationConfigurationServiceMock);
    verify(participantServiceMock);

    Assert.assertEquals(2, processedParticipants.size());
    Assert.assertEquals("Tremblay", processedParticipants.get(0).getLastName());
    Assert.assertEquals("100001", processedParticipants.get(0).getEnrollmentId());
    Assert.assertEquals("Casserly", processedParticipants.get(1).getLastName());
    Assert.assertEquals("100003", processedParticipants.get(1).getEnrollmentId());
  }

  /**
   * Tests processing of an appointment list where an attribute (Gender, line 3) has been assigned value that is not
   * allowed (i.e., is not with the attribute's "allowed value" list). The processor should not return this participant
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithNotAllowedAttributeValue() throws IOException {
    expect(applicationConfigurationServiceMock.getApplicationConfiguration()).andReturn(config);
    expectLastCall().times(2);

    expect(participantServiceMock.getParticipant((Participant) EasyMock.anyObject())).andReturn(null);
    expectLastCall().times(2);

    List<Participant> participants = participantReaderTest.getParticipantList(false, TEST_RESOURCES_DIR + "/appointmentList_notAllowedAttributeValue.xls");
    List<Participant> processedParticipants = new ArrayList<Participant>();

    replay(applicationConfigurationServiceMock);
    replay(participantServiceMock);

    try {
      for(Participant participantItem : participants) {
        Participant participant = processor.process(participantItem);
        if(participant != null) processedParticipants.add(participant);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    verify(applicationConfigurationServiceMock);
    verify(participantServiceMock);

    Assert.assertEquals(2, processedParticipants.size());
    Assert.assertEquals("Smith", processedParticipants.get(0).getLastName());
    Assert.assertEquals(Gender.MALE, processedParticipants.get(0).getGender());
    Assert.assertEquals("Casserly", processedParticipants.get(1).getLastName());
    Assert.assertEquals(Gender.FEMALE, processedParticipants.get(1).getGender());
  }

  @Test
  public void testProcessWithConfiguredAttributes() throws IOException {
    Calendar cal = Calendar.getInstance();

    cal.clear();
    cal.set(2008, 9 - 1, 1, 9, 0);
    final Date expectedAppointmentTime = cal.getTime();

    cal.clear();
    cal.set(1964, 10 - 1, 1);
    final Date expectedBirthDate = cal.getTime();

    expect(applicationConfigurationServiceMock.getApplicationConfiguration()).andReturn(config);
    expectLastCall().times(1);

    expect(participantServiceMock.getParticipant((Participant) EasyMock.anyObject())).andReturn(null);
    expectLastCall().times(1);

    List<Participant> participants = participantReaderTest.getParticipantList(true, TEST_RESOURCES_DIR + "/appointmentList_includesConfiguredAttributes.xls");
    List<Participant> processedParticipants = new ArrayList<Participant>();

    replay(applicationConfigurationServiceMock);
    replay(participantServiceMock);

    try {
      for(Participant participantItem : participants) {
        Participant participant = processor.process(participantItem);
        if(participant != null) processedParticipants.add(participant);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    verify(applicationConfigurationServiceMock);
    verify(participantServiceMock);

    Assert.assertEquals(1, participants.size());
    Assert.assertEquals(expectedAppointmentTime, participants.get(0).getAppointment().getDate());
    Assert.assertEquals("cag001", participants.get(0).getSiteNo());
    Assert.assertEquals("100001", participants.get(0).getEnrollmentId());
    Assert.assertEquals("Tremblay", participants.get(0).getLastName());
    Assert.assertEquals("Chantal", participants.get(0).getFirstName());
    Assert.assertEquals("F", (participants.get(0).getGender().equals(Gender.FEMALE) ? "F" : "M"));
    Assert.assertEquals(expectedBirthDate, participants.get(0).getBirthDate());

    // Verify that the participant's configured attributes have been assigned the correct values.
    Assert.assertEquals("299, Avenue des Pins Ouest", participants.get(0).getConfiguredAttributeValue("Street").getValue());
    Assert.assertEquals("Montr\u00e9al", participants.get(0).getConfiguredAttributeValue("City").getValue());
    Assert.assertEquals("QC", participants.get(0).getConfiguredAttributeValue("Province").getValue());
    Assert.assertEquals("Canada", participants.get(0).getConfiguredAttributeValue("Country").getValue());
    Assert.assertEquals("H1T 2M4", participants.get(0).getConfiguredAttributeValue("Postal Code").getValue());
    Assert.assertEquals("514-343-9898 ext 9494", participants.get(0).getConfiguredAttributeValue("Phone").getValue());
  }

  /**
   * Tests processing of an appointment list containing a duplicate enrollment id. The processor should not return this
   * participant
   * 
   * @throws IOException if the appointment list could not be read
   */
  @Test
  public void testProcessWithDuplicateDuplicateEnrollmentId() throws IOException {
    expect(applicationConfigurationServiceMock.getApplicationConfiguration()).andReturn(config);
    expectLastCall().times(2);

    expect(participantServiceMock.getParticipant((Participant) EasyMock.anyObject())).andReturn(null);
    expectLastCall().times(2);

    List<Participant> participants = participantReaderTest.getParticipantList(true, TEST_RESOURCES_DIR + "/appointmentList_duplicateEnrollmentId.xls");
    List<Participant> processedParticipants = new ArrayList<Participant>();

    replay(applicationConfigurationServiceMock);
    replay(participantServiceMock);

    try {
      for(Participant participantItem : participants) {
        Participant participant = processor.process(participantItem);
        if(participant != null) processedParticipants.add(participant);
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    verify(applicationConfigurationServiceMock);
    verify(participantServiceMock);

    Assert.assertEquals(2, processedParticipants.size());
    Assert.assertEquals("Tremblay", processedParticipants.get(0).getLastName());
    Assert.assertEquals("100001", processedParticipants.get(0).getEnrollmentId());
    Assert.assertEquals("Smith", processedParticipants.get(1).getLastName());
    Assert.assertEquals("100002", processedParticipants.get(1).getEnrollmentId());
  }

  @Test
  @Dataset
  public void testProcessParticipantAlreadyExist() {
    expect(applicationConfigurationServiceMock.getApplicationConfiguration()).andReturn(config);
    expectLastCall().times(3);

    Participant p = new Participant();
    p.setEnrollmentId("100001");
    expect(participantServiceMock.getParticipant((Participant) EasyMock.anyObject())).andReturn(persistenceManager.matchOne(p));
    Assert.assertEquals(1l, persistenceManager.matchOne(p).getId());
    Assert.assertEquals("Suzan", persistenceManager.matchOne(p).getFirstName());
    Assert.assertEquals("2008-09-01", getDateFormat().format(persistenceManager.matchOne(p).getAppointment().getDate()));

    p.setEnrollmentId("100002");
    expect(participantServiceMock.getParticipant((Participant) EasyMock.anyObject())).andReturn(persistenceManager.matchOne(p));
    p.setEnrollmentId("100003");
    expect(participantServiceMock.getParticipant((Participant) EasyMock.anyObject())).andReturn(persistenceManager.matchOne(p));

    List<Participant> processedParticipants = new ArrayList<Participant>();

    replay(applicationConfigurationServiceMock);
    replay(participantServiceMock);

    int i = 1;

    try {
      for(Participant participantItem : getParticipants()) {
        Participant participant = processor.process(participantItem);
        if(participant != null) processedParticipants.add(participant);
        i++;
      }
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    verify(applicationConfigurationServiceMock);
    verify(participantServiceMock);

    // Appointment date is the only data that is allowed to change here
    Assert.assertEquals(2, processedParticipants.size());
    Assert.assertEquals(1l, processedParticipants.get(0).getId());
    Assert.assertEquals("Suzan", processedParticipants.get(0).getFirstName());
    Assert.assertEquals("2009-09-01", getDateFormat().format(processedParticipants.get(0).getAppointment().getDate()));

    // Participant 100002 is ignored because his interview is completed
    p = new Participant();
    p.setEnrollmentId("100002");
    p = persistenceManager.matchOne(p);
    Assert.assertNotNull("Cannot find participant", p);
    Assert.assertNotNull("Cannot find participant interview", p.getInterview());
    Assert.assertEquals("Cannot find participant completed interview", InterviewStatus.COMPLETED, p.getInterview().getStatus());
    Assert.assertEquals("100003", processedParticipants.get(1).getEnrollmentId());
  }

  //
  // Helper Methods
  //
  private ParticipantMetadata initParticipantMetadata() throws IOException {
    ParticipantMetadata participantMetadata = new ParticipantMetadata();

    ParticipantAttributeReader attributeReader = new ParticipantAttributeReader();

    attributeReader.setResources(new Resource[] { new ClassPathResource(TEST_RESOURCES_DIR + "/essential-attributes.xml") });
    List<ParticipantAttribute> essentialAttributes = attributeReader.read();
    participantMetadata.setEssentialAttributes(essentialAttributes);

    attributeReader.setResources(new Resource[] { new ClassPathResource(TEST_RESOURCES_DIR + "/configured-attributes.xml") });
    List<ParticipantAttribute> configuredAttributes = attributeReader.read();
    participantMetadata.setConfiguredAttributes(configuredAttributes);

    return participantMetadata;
  }

  //  
  // 
  //
  // @Autowired(required = true)
  // private ParticipantService participantService;
  //
  //
  // // private UpdateParticipantListener updateParticipantListener;
  //
  // 
  //
  // 
  //

  //
  // 
  //
  // }
  //
  // private User getUser() {
  // User u = new User();
  // u.setLastName("Onyx");
  // u.setFirstName("Admin");
  // return u;
  // }
  //
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
    c.set(Calendar.MONTH, month - 1);
    c.set(Calendar.DAY_OF_MONTH, day);
    c.set(Calendar.HOUR_OF_DAY, hour);
    c.set(Calendar.MINUTE, minute);

    return c.getTime();
  }

  private SimpleDateFormat getDateFormat() {
    return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
  }
}
