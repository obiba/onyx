/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.service.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.Remark;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.springframework.context.MessageSourceResolvable;

/**
 *
 */
public class ActiveTubeRegistrationServiceImplTest {

  private ActiveTubeRegistrationServiceImpl service;

  private PersistenceManager persistenceManagerMock;

  private TubeRegistrationConfiguration tubeRegistrationConfig;

  private Participant participant;

  private Interview interview;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    participant = new Participant();
    interview = new Interview();
    participant.setBarcode("10234");
    participant.setInterview(interview);

    persistenceManagerMock = createMock(PersistenceManager.class);
    tubeRegistrationConfig = new TubeRegistrationConfiguration();

    service = new ActiveTubeRegistrationServiceImpl();

    service.setPersistenceManager(persistenceManagerMock);
    service.setTubeRegistrationConfig(tubeRegistrationConfig);
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#getExpectedTubeCount()}.
   */
  @Test
  public void testGetExpectedTubeCount() {
    tubeRegistrationConfig.setExpectedTubeCount(10);
    int count = service.getExpectedTubeCount();

    Assert.assertEquals(10, count);
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#getRegisteredTubeCount()}.
   */
  @Test
  public void testGetRegisteredTubeCount() {
    Serializable registrationId = "123";

    ParticipantTubeRegistration registration = new ParticipantTubeRegistration();
    registration.setTubeRegistrationConfig(tubeRegistrationConfig);
    registration.addRegisteredParticipantTube(new RegisteredParticipantTube());
    registration.setId(registrationId);

    expect(persistenceManagerMock.save(isA(ParticipantTubeRegistration.class))).andReturn(registration);
    expect(persistenceManagerMock.get(ParticipantTubeRegistration.class, registrationId)).andReturn(registration);

    replay(persistenceManagerMock);

    service.start(participant);
    int count = service.getRegisteredTubeCount();

    verify(persistenceManagerMock);
    Assert.assertEquals(1, count);

  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#registerTube(java.lang.String)}.
   */
  @Test
  public void testRegisterTubeWithRegistrationCreated() {

    String barcode = "101234560108";
    Serializable registrationId = "123";
    Interview interview = new Interview();

    ParticipantTubeRegistration registration = new ParticipantTubeRegistration();
    registration.setTubeRegistrationConfig(tubeRegistrationConfig);
    registration.setInterview(interview);
    registration.setStartTime(new Date());
    registration.setId(registrationId);

    RegisteredParticipantTube tube = new RegisteredParticipantTube();
    tube.setRegistrationTime(new Date());
    tube.setBarcode(barcode);

    expect(persistenceManagerMock.save(isA(ParticipantTubeRegistration.class))).andReturn(registration).times(2);
    expect(persistenceManagerMock.get(ParticipantTubeRegistration.class, registrationId)).andReturn(registration);
    expect(persistenceManagerMock.save(isA(RegisteredParticipantTube.class))).andReturn(tube);

    replay(persistenceManagerMock);

    service.start(participant);
    List<MessageSourceResolvable> errors = service.registerTube(barcode);

    verify(persistenceManagerMock);
    Assert.assertTrue(errors.isEmpty());

  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#setTubeComment(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testSetTubeComment() {
    String barcode = "101234560108";
    RegisteredParticipantTube tube = new RegisteredParticipantTube();
    tube.setRegistrationTime(new Date());
    tube.setBarcode(barcode);

    expect(persistenceManagerMock.matchOne(isA(RegisteredParticipantTube.class))).andReturn(tube).once();
    expect(persistenceManagerMock.save(isA(RegisteredParticipantTube.class))).andReturn(tube).once();

    replay(persistenceManagerMock);

    service.setTubeComment(barcode, "test comment");

    verify(persistenceManagerMock);
    Assert.assertEquals("test comment", tube.getComment());

  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#setTubeComment(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testFailToSetTubeComment() {
    String barcode = "101234560108";

    expect(persistenceManagerMock.matchOne(isA(RegisteredParticipantTube.class))).andReturn(null).once();

    replay(persistenceManagerMock);

    try {
      service.setTubeComment(barcode, "test comment");

    } catch(IllegalArgumentException e) {
      Assert.assertEquals("Couldn't find the the tube with code '101234560108'.", e.getMessage());
    }

    verify(persistenceManagerMock);

  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#setTubeRemark(java.lang.String, org.obiba.onyx.ruby.core.domain.Remark)}
   * .
   */
  @Test
  public void testSetTubeRemark() {
    String barcode = "101234560108";
    RegisteredParticipantTube tube = new RegisteredParticipantTube();
    tube.setRegistrationTime(new Date());
    tube.setBarcode(barcode);

    Remark remark = new Remark("123");

    expect(persistenceManagerMock.matchOne(isA(RegisteredParticipantTube.class))).andReturn(tube).once();
    expect(persistenceManagerMock.save(isA(RegisteredParticipantTube.class))).andReturn(tube).once();

    replay(persistenceManagerMock);

    service.setTubeRemark(barcode, remark);

    verify(persistenceManagerMock);
    Assert.assertEquals("123", tube.getRemarkCode());
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#unregisterTube(java.lang.String)}.
   */
  @Test
  public void testUnregisterTube() {
    String barcode = "101234560108";
    Serializable registrationId = "123";

    RegisteredParticipantTube tube = new RegisteredParticipantTube();
    tube.setRegistrationTime(new Date());
    tube.setBarcode(barcode);

    ParticipantTubeRegistration registration = new ParticipantTubeRegistration();
    registration.setTubeRegistrationConfig(tubeRegistrationConfig);
    registration.addRegisteredParticipantTube(tube);
    registration.setInterview(interview);
    registration.setStartTime(new Date());
    registration.setId(registrationId);

    expect(persistenceManagerMock.matchOne(isA(RegisteredParticipantTube.class))).andReturn(tube);
    expect(persistenceManagerMock.save(isA(ParticipantTubeRegistration.class))).andReturn(registration).times(2);
    expect(persistenceManagerMock.get(ParticipantTubeRegistration.class, registrationId)).andReturn(registration);

    persistenceManagerMock.delete(isA(RegisteredParticipantTube.class));

    replay(persistenceManagerMock);

    Assert.assertTrue(registration.getRegisteredParticipantTubes().contains(tube));

    service.start(participant);
    service.unregisterTube(barcode);

    verify(persistenceManagerMock);
    Assert.assertTrue(registration.getRegisteredParticipantTubes().isEmpty());

  }
}
