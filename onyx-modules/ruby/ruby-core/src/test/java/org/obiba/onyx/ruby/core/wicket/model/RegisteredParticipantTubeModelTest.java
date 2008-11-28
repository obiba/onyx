/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.model;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;

public class RegisteredParticipantTubeModelTest {
  //
  // Instance Variables
  //

  private ExtendedApplicationContextMock applicationContextMock;

  private ActiveTubeRegistrationService activeTubeRegistrationServiceMock;

  private ParticipantTubeRegistration participantTubeRegistration;

  private RegisteredParticipantTube registeredParticipantTube1;

  private RegisteredParticipantTube registeredParticipantTube2;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() throws Exception {
    initDomainObjects();
    initApplicationContext();
  }

  //
  // Test Methods
  //

  @Test
  public void testGetModelObject() {
    expect(activeTubeRegistrationServiceMock.getParticipantTubeRegistration()).andReturn(participantTubeRegistration);

    replay(activeTubeRegistrationServiceMock);

    RegisteredParticipantTubeModel registeredParticipantTubeModel = new RegisteredParticipantTubeModel(registeredParticipantTube2);
    Object modelObject = registeredParticipantTubeModel.getObject();

    verify(activeTubeRegistrationServiceMock);

    Assert.assertEquals(modelObject, registeredParticipantTubeModel.getObject());
  }

  //
  // Helper Methods
  //

  private void initDomainObjects() {
    registeredParticipantTube1 = new RegisteredParticipantTube();
    registeredParticipantTube1.setBarcode("1234567011");

    registeredParticipantTube2 = new RegisteredParticipantTube();
    registeredParticipantTube2.setBarcode("1234567012");

    participantTubeRegistration = new ParticipantTubeRegistration();
    participantTubeRegistration.addRegisteredParticipantTube(registeredParticipantTube1);
    participantTubeRegistration.addRegisteredParticipantTube(registeredParticipantTube2);
  }

  private void initApplicationContext() {
    applicationContextMock = new ExtendedApplicationContextMock();

    activeTubeRegistrationServiceMock = createMock(ActiveTubeRegistrationService.class);
    applicationContextMock.putBean("activeTubeRegistrationService", activeTubeRegistrationServiceMock);

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(applicationContextMock);

    new WicketTester(application);
  }
}
