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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.easymock.LogicalOperator;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.domain.parser.IBarcodePartParser;
import org.obiba.onyx.ruby.core.domain.parser.impl.RegularExpressionBarcodePartParser;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

/**
 * Unit tests for <code>ActiveTubeRegistrationServiceImpl</code>
 */
public class ParticipantTubeRegistrationServiceImplTest {

  /**
   * Name of first tube registration configuration.
   */
  private static final String FIRST_TUBE_REGISTRATION_CONFIG_NAME = "bloodTubeRegistrationConfiguration";

  /**
   * Name of second tube registration configuration.
   */
  private static final String SECOND_TUBE_REGISTRATION_CONFIG_NAME = "urineTubeRegistrationConfiguration";

  private ParticipantTubeRegistrationServiceImpl service;

  private PersistenceManager persistenceManagerMock;

  @Autowired
  private PersistenceManager persistenceManager;

  private TubeRegistrationConfiguration firstTubeRegistrationConfig;

  private TubeRegistrationConfiguration secondTubeRegistrationConfig;

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

    Map<String, TubeRegistrationConfiguration> tubeRegistrationConfigMap = new HashMap<String, TubeRegistrationConfiguration>();
    firstTubeRegistrationConfig = createTubeRegistrationConfiguration();
    tubeRegistrationConfigMap.put(FIRST_TUBE_REGISTRATION_CONFIG_NAME, firstTubeRegistrationConfig);
    secondTubeRegistrationConfig = createTubeRegistrationConfiguration();
    tubeRegistrationConfigMap.put(SECOND_TUBE_REGISTRATION_CONFIG_NAME, secondTubeRegistrationConfig);

    service = new ParticipantTubeRegistrationServiceImpl();
    service.setPersistenceManager(persistenceManagerMock);
    service.setTubeRegistrationConfigurationMap(tubeRegistrationConfigMap);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStartWithNullParticipant() {
    service.start(null, FIRST_TUBE_REGISTRATION_CONFIG_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStartWithNullInterview() {
    participant.setInterview(null);
    service.start(participant, FIRST_TUBE_REGISTRATION_CONFIG_NAME);
  }

  @Test
  public void testResumeFirstTubeRegistrationStage() {
    ParticipantTubeRegistration resumedParticipantTubeRegistration = new ParticipantTubeRegistration();
    resumedParticipantTubeRegistration.setId(1l);

    expect(persistenceManagerMock.matchOne(isA(ParticipantTubeRegistration.class))).andReturn(resumedParticipantTubeRegistration);

    replay(persistenceManagerMock);

    service.resume(participant, FIRST_TUBE_REGISTRATION_CONFIG_NAME);

    verify(persistenceManagerMock);

    // Verify that the resumed ParticipantTubeRegistration has the expected TubeRegistrationConfiguration.
    Assert.assertEquals(firstTubeRegistrationConfig, resumedParticipantTubeRegistration.getTubeRegistrationConfig());
  }

  @Test
  public void testResumeSecondTubeRegistrationStage() {
    ParticipantTubeRegistration resumedParticipantTubeRegistration = new ParticipantTubeRegistration();
    resumedParticipantTubeRegistration.setId(1l);

    expect(persistenceManagerMock.matchOne(isA(ParticipantTubeRegistration.class))).andReturn(resumedParticipantTubeRegistration);

    replay(persistenceManagerMock);

    service.resume(participant, SECOND_TUBE_REGISTRATION_CONFIG_NAME);

    verify(persistenceManagerMock);

    // Verify that the resumed ParticipantTubeRegistration has the expected TubeRegistrationConfiguration.
    Assert.assertEquals(secondTubeRegistrationConfig, resumedParticipantTubeRegistration.getTubeRegistrationConfig());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testResumeWithNullParticipant() {
    service.resume(null, FIRST_TUBE_REGISTRATION_CONFIG_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testResumeWithNullInterview() {
    participant.setInterview(null);
    service.resume(participant, FIRST_TUBE_REGISTRATION_CONFIG_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testResumeWithNullTubeSetName() {
    service.resume(participant, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testResumeWithUnknownTubeSetName() {
    service.resume(participant, "bogusTubeSetName");
  }

  /**
   * Tests the case of an unexpected resume (i.e., no <code>ParticipantTubeRegistration</code> of the specified type
   * currently exists).
   */
  @Test(expected = IllegalStateException.class)
  public void testUnexpectedResume() {
    service.resume(participant, FIRST_TUBE_REGISTRATION_CONFIG_NAME);
  }

  @Test
  public void testDeleteParticipantTubeRegistration() {
    ParticipantTubeRegistration participantTubeRegistration = new ParticipantTubeRegistration();
    participantTubeRegistration.setId(1l);
    participantTubeRegistration.setTubeRegistrationConfig(firstTubeRegistrationConfig);
    participantTubeRegistration.setInterview(interview);
    participantTubeRegistration.setTubeSetName(FIRST_TUBE_REGISTRATION_CONFIG_NAME);
    participantTubeRegistration.setStartTime(new Date());

    expect(persistenceManagerMock.matchOne(isA(ParticipantTubeRegistration.class))).andReturn(participantTubeRegistration);
    persistenceManagerMock.delete(participantTubeRegistration);

    replay(persistenceManagerMock);

    service.deleteParticipantTubeRegistration(participant, FIRST_TUBE_REGISTRATION_CONFIG_NAME);

    verify(persistenceManagerMock);
  }

  @Test
  public void testDeleteAllParticipantTubeRegistrations() {

    ParticipantTubeRegistration returnValue = new ParticipantTubeRegistration();
    returnValue.setInterview(interview);

    ParticipantTubeRegistration expectedTemplate = new ParticipantTubeRegistration();
    expectedTemplate.setInterview(interview);

    // This expectation is used to match the template passed into the persistenceManager.match() method. It checks that
    // the passed in template is constructed as expected (with the correct interview)
    expect(persistenceManagerMock.match(EasyMock.cmp(expectedTemplate, new Comparator<ParticipantTubeRegistration>() {

      public int compare(ParticipantTubeRegistration o1, ParticipantTubeRegistration o2) {
        return o1.getInterview().equals(o2.getInterview()) ? 0 : 1;
      }

    }, LogicalOperator.EQUAL))).andReturn(Lists.newArrayList(returnValue, returnValue, returnValue));

    persistenceManagerMock.delete(returnValue);
    EasyMock.expectLastCall().times(3);

    replay(persistenceManagerMock);

    service.deleteAllParticipantTubeRegistrations(participant);

    verify(persistenceManagerMock);
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
