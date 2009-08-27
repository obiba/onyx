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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantAttributeReader;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.obiba.onyx.core.service.ParticipantService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 *
 */
public class ParticipantProcessorTest {

  private static final String TEST_RESOURCES_DIR = ParticipantReaderTest.class.getSimpleName();

  private ParticipantReaderTest participantReaderTest = new ParticipantReaderTest();;

  private ParticipantProcessor processor = new ParticipantProcessor();

  private ApplicationConfiguration config;

  private ApplicationConfigurationService applicationConfigurationServiceMock;

  private ParticipantService participantServiceMock;

  @Before
  public void setUp() throws Exception {
    applicationConfigurationServiceMock = createMock(ApplicationConfigurationService.class);
    participantServiceMock = createMock(ParticipantService.class);
    config = new ApplicationConfiguration();
    config.setSiteNo("cag001");

    processor.setParticipantMetadata(initParticipantMetadata());
    processor.setApplicationConfigurationService(applicationConfigurationServiceMock);
    processor.setParticipantService(participantServiceMock);
  }

  /**
   * Tests processing of an appointment list that contains no configured attributes (i.e., essential attributes only).
   * 
   * @throws IOException if the appointment list could not be read
   */
  // @Test
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
  // @Test
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
  // @Test
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

  // @Test
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

  // TODO: tests presently in UpdatePArticipantListener

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

}
