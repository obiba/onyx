/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.domain;

import org.junit.Test;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;

/**
 * Test the persistence with spring/hibernate environment
 */

public class ParticipantTubeRegistrationTest extends BaseDefaultSpringContextTestCase {

  // @Autowired(required = true)
  // ActiveTubeRegistrationService activeTubeRegistrationService;

  // @Autowired(required = true)
  // PersistenceManager persistenceManager;
  //
  // @Autowired(required = true)
  // TubeRegistrationConfiguration tubeRegistrationConfiguration;
  //
  // private Participant participant;
  //
  // private Interview interview;

  // /**
  // * @throws java.lang.Exception
  // */
  // @Before
  // public void setUp() throws Exception {
  // // Persist Participant and Interview object for the use of testing
  // participant = new Participant();
  // interview = new Interview();
  // interview.setStartDate(new Date());
  // persistenceManager.save(interview);
  // participant.setBarcode("10234");
  // participant.setInterview(interview);
  // persistenceManager.save(participant);
  // interview.setParticipant(participant);
  // persistenceManager.save(interview);
  // }
  //
  // @Test
  // public void testStartAndStopParticipantTubeRegistration() {
  // // Create and persist ParticipantTubeRegistration
  // ParticipantTubeRegistration tubeRegistration = activeTubeRegistrationService.start(participant);
  //
  // Assert.assertNotNull(tubeRegistration);
  //
  // // Get persisted ParticipantTubeRegistration
  // ParticipantTubeRegistration persistedRegistration = persistenceManager.get(ParticipantTubeRegistration.class,
  // tubeRegistration.getId());
  //
  // Assert.assertNotNull(persistedRegistration);
  // Assert.assertEquals(participant.getBarcode(), persistedRegistration.getInterview().getParticipant().getBarcode());
  // Assert.assertNotNull(persistedRegistration.getStartTime());
  // Assert.assertNull(persistedRegistration.getEndTime());
  //
  // // Stop tube registration
  // activeTubeRegistrationService.end();
  //
  // // Get persisted ParticipantTubeRegistration again
  // persistedRegistration = persistenceManager.get(ParticipantTubeRegistration.class, tubeRegistration.getId());
  //
  // // The end time should be set
  // Assert.assertNotNull(persistedRegistration.getEndTime());
  // }
  //
  // @Test
  // public void testRegisterAndUnregisterTube() {
  // String barcode = "1234500110";
  // RegisteredParticipantTube tube = new RegisteredParticipantTube();
  // tube.setBarcode(barcode);
  //
  // activeTubeRegistrationService.start(participant);
  //
  // // There should be no tubes before tube registering
  // Assert.assertEquals(0, activeTubeRegistrationService.getRegisteredTubeCount());
  //
  // List<MessageSourceResolvable> errors = activeTubeRegistrationService.registerTube(barcode);
  //
  // Assert.assertTrue(errors.isEmpty());
  //
  // // There should be 1 tube after tube registering
  // Assert.assertEquals(1, activeTubeRegistrationService.getRegisteredTubeCount());
  //
  // // The tube should be persisted
  // Assert.assertNotNull(persistenceManager.matchOne(tube));
  //
  // activeTubeRegistrationService.unregisterTube(barcode);
  //
  // // The tube is gone after unregistering
  // Assert.assertEquals(0, activeTubeRegistrationService.getRegisteredTubeCount());
  //
  // // The tube should not be persisted
  // Assert.assertNull(persistenceManager.matchOne(tube));
  // }
  //
  // @Test
  // public void testContraindications() {
  // // set up ParticipantTubeRegistration persistence
  // ParticipantTubeRegistration registration = new ParticipantTubeRegistration();
  // registration.setTubeRegistrationConfig(tubeRegistrationConfiguration);
  // registration.setInterview(interview);
  // registration.setStartTime(new Date());
  // registration.setOtherContraindication("other contraindication for test");
  // persistenceManager.save(registration);
  //
  // ParticipantTubeRegistration persistedRegistration = persistenceManager.get(ParticipantTubeRegistration.class,
  // registration.getId());
  // String cont = persistedRegistration.getOtherContraindication();
  //
  // // OtherContraindication should be persisted
  // Assert.assertEquals("other contraindication for test", cont);
  //
  // // This is because TubeRegistrationConfig is transient
  // persistedRegistration.setTubeRegistrationConfig(tubeRegistrationConfiguration);
  //
  // List<Contraindication> askedContraindications = persistedRegistration.getContraindications(Type.ASKED);
  //
  // // There should be 5 asked Contraindications
  // Assert.assertEquals(5, askedContraindications.size());
  //
  // List<Contraindication> observedContraindications = persistedRegistration.getContraindications(Type.OBSERVED);
  //
  // // There should be 10 observed Contraindications
  // Assert.assertEquals(10, observedContraindications.size());
  //
  // // It is not contraindicated
  // Assert.assertFalse(persistedRegistration.isContraindicated());
  //
  // // There should be both asked and observed Contraindications
  // Assert.assertTrue(persistedRegistration.hasContraindications(Type.ASKED));
  // Assert.assertTrue(persistedRegistration.hasContraindications(Type.OBSERVED));
  // }

  @Test
  public void testDummy() {

  }
}
