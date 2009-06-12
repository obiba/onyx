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
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.Remark;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.domain.parser.IBarcodePartParser;
import org.obiba.onyx.ruby.core.domain.parser.impl.RegularExpressionBarcodePartParser;
import org.springframework.context.MessageSourceResolvable;

/**
 * Unit tests for <code>ActiveTubeRegistrationServiceImpl</code>
 */
public class ActiveTubeRegistrationServiceImplTest {

  private static final String TUBE_REGISTRATION_CONFIG_NAME = "tubeRegistrationConfiguration";

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

    tubeRegistrationConfig = createTubeRegistrationConfiguration();

    service = new ActiveTubeRegistrationServiceImpl();

    service.setPersistenceManager(persistenceManagerMock);

    Map<String, TubeRegistrationConfiguration> tubeRegistrationConfigMap = new HashMap<String, TubeRegistrationConfiguration>();
    tubeRegistrationConfigMap.put(TUBE_REGISTRATION_CONFIG_NAME, tubeRegistrationConfig);
    service.setTubeRegistrationConfigurationMap(tubeRegistrationConfigMap);
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#getExpectedTubeCount()}.
   */
  @Test
  public void testGetExpectedTubeCount() {
    Serializable registrationId = 1l;

    ParticipantTubeRegistration registration = new ParticipantTubeRegistration();
    registration.setId(registrationId);
    registration.setTubeSetName(TUBE_REGISTRATION_CONFIG_NAME);
    registration.setTubeRegistrationConfig(tubeRegistrationConfig);

    expect(persistenceManagerMock.save(isA(ParticipantTubeRegistration.class))).andReturn(registration);
    expect(persistenceManagerMock.get(ParticipantTubeRegistration.class, registrationId)).andReturn(registration);

    replay(persistenceManagerMock);

    service.start(participant, TUBE_REGISTRATION_CONFIG_NAME);

    tubeRegistrationConfig.setExpectedTubeCount(10);
    int count = service.getExpectedTubeCount();

    verify(persistenceManagerMock);

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
    registration.setId(registrationId);
    registration.setTubeSetName(TUBE_REGISTRATION_CONFIG_NAME);
    registration.setTubeRegistrationConfig(tubeRegistrationConfig);
    registration.addRegisteredParticipantTube(new RegisteredParticipantTube());

    expect(persistenceManagerMock.save(isA(ParticipantTubeRegistration.class))).andReturn(registration);
    expect(persistenceManagerMock.get(ParticipantTubeRegistration.class, registrationId)).andReturn(registration);

    replay(persistenceManagerMock);

    service.start(participant, TUBE_REGISTRATION_CONFIG_NAME);
    int count = service.getRegisteredTubeCount();

    verify(persistenceManagerMock);

    // Should get exactly one Registered Tube
    Assert.assertEquals(1, count);

  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#registerTube(java.lang.String)}.
   */
  @Test
  public void testRegisterTubeWithRegistrationCreated() {

    String barcode = "1012345601";
    Serializable registrationId = "123";
    Interview interview = new Interview();

    // Set up the Tube Registration object need to be created
    ParticipantTubeRegistration registration = new ParticipantTubeRegistration();
    registration.setTubeSetName(TUBE_REGISTRATION_CONFIG_NAME);
    registration.setTubeRegistrationConfig(tubeRegistrationConfig);
    registration.setInterview(interview);
    registration.setStartTime(new Date());
    registration.setId(registrationId);

    RegisteredParticipantTube tube = new RegisteredParticipantTube();
    tube.setRegistrationTime(new Date());
    tube.setBarcode(barcode);

    // Set expectations
    expect(persistenceManagerMock.save(isA(ParticipantTubeRegistration.class))).andReturn(registration).anyTimes();
    expect(persistenceManagerMock.get(ParticipantTubeRegistration.class, registrationId)).andReturn(registration).anyTimes();
    expect(persistenceManagerMock.save(isA(RegisteredParticipantTube.class))).andReturn(tube).anyTimes();
    expect(persistenceManagerMock.matchOne(isA(RegisteredParticipantTube.class))).andReturn(null).anyTimes();

    replay(persistenceManagerMock);

    service.start(participant, TUBE_REGISTRATION_CONFIG_NAME);
    List<MessageSourceResolvable> errors = service.registerTube(barcode);

    verify(persistenceManagerMock);

    // Should get no errors
    Assert.assertTrue(errors.isEmpty());

  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#setTubeComment(java.lang.String, java.lang.String)} .
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

    // The comment should be set
    Assert.assertEquals("test comment", tube.getComment());

  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#setTubeComment(java.lang.String, java.lang.String)} .
   */
  @Test
  public void testFailToSetTubeComment() {
    String barcode = "1012345601";

    expect(persistenceManagerMock.matchOne(isA(RegisteredParticipantTube.class))).andReturn(null).once();

    replay(persistenceManagerMock);

    try {
      service.setTubeComment(barcode, "test comment");
      fail("Should get IllegalArgumentException.");
    } catch(IllegalArgumentException e) {
      // Hope to get IllegalArgumentException
      Assert.assertEquals("No tube with barcode '" + barcode + "' has been registered", e.getMessage());
    }

    verify(persistenceManagerMock);

  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#setTubeRemark(java.lang.String, org.obiba.onyx.ruby.core.domain.Remark)} .
   */
  @Test
  public void testSetTubeRemark() {
    String barcode = "101234560108";
    RegisteredParticipantTube tube = new RegisteredParticipantTube();
    tube.setRegistrationTime(new Date());
    tube.setBarcode(barcode);

    Set<String> remarkCodes = new HashSet<String>();
    // Add a code that should be absent after the service method call
    remarkCodes.add("Removed");
    tube.setRemarks(remarkCodes);

    List<Remark> remarks = new ArrayList<Remark>();
    remarks.add(new Remark("123"));
    remarks.add(new Remark("321"));

    expect(persistenceManagerMock.matchOne(isA(RegisteredParticipantTube.class))).andReturn(tube).once();
    expect(persistenceManagerMock.save(tube)).andReturn(tube).once();

    replay(persistenceManagerMock);

    service.setTubeRemark(barcode, remarks);

    verify(persistenceManagerMock);

    Assert.assertTrue(remarkCodes.contains("123"));
    Assert.assertTrue(remarkCodes.contains("321"));
    Assert.assertFalse(remarkCodes.contains("Removed"));
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#unregisterTube(java.lang.String)}.
   */
  @Test
  public void testUnregisterTube() {
    String barcode = "101234560108";
    Serializable registrationId = "123";

    // create the tube need to be unregistered
    RegisteredParticipantTube tube = new RegisteredParticipantTube();
    tube.setRegistrationTime(new Date());
    tube.setBarcode(barcode);

    // create another tube which is not to be removed from registration
    RegisteredParticipantTube tube1 = new RegisteredParticipantTube();
    tube1.setBarcode("111");

    ParticipantTubeRegistration registration = new ParticipantTubeRegistration();
    registration.setTubeRegistrationConfig(tubeRegistrationConfig);
    registration.addRegisteredParticipantTube(tube);
    registration.addRegisteredParticipantTube(tube1);
    registration.setInterview(interview);
    registration.setStartTime(new Date());
    registration.setId(registrationId);

    expect(persistenceManagerMock.matchOne(isA(RegisteredParticipantTube.class))).andReturn(tube);
    expect(persistenceManagerMock.save(isA(ParticipantTubeRegistration.class))).andReturn(registration).times(2);
    expect(persistenceManagerMock.get(ParticipantTubeRegistration.class, registrationId)).andReturn(registration);

    persistenceManagerMock.delete(isA(RegisteredParticipantTube.class));

    replay(persistenceManagerMock);

    // The tube need to be unregistered should be in the registration
    // before executing the unregisterTube()
    Assert.assertTrue(registration.getRegisteredParticipantTubes().contains(tube));

    service.start(participant, TUBE_REGISTRATION_CONFIG_NAME);
    service.unregisterTube(barcode);

    verify(persistenceManagerMock);

    // The tube is gone after executing the unregisterTube()
    Assert.assertFalse(registration.getRegisteredParticipantTubes().contains(tube));

    // But another tube is still there
    Assert.assertTrue(registration.getRegisteredParticipantTubes().contains(tube1));
  }

  private TubeRegistrationConfiguration createTubeRegistrationConfiguration() {
    TubeRegistrationConfiguration tubeRegistrationConfiguration = new TubeRegistrationConfiguration();

    RegularExpressionBarcodePartParser parser = new RegularExpressionBarcodePartParser();
    parser.setSize(10);
    parser.setExpression(".*");

    List<IBarcodePartParser> parserList = new ArrayList<IBarcodePartParser>();
    parserList.add(parser);

    BarcodeStructure barcodeStructure = new BarcodeStructure();
    barcodeStructure.setParsers(parserList);

    tubeRegistrationConfiguration.setBarcodeStructure(barcodeStructure);

    return tubeRegistrationConfiguration;
  }
}
