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
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;

/**
 *
 */
public class ActiveTubeRegistrationServiceImplTest {

  private ActiveTubeRegistrationServiceImpl service;

  private ActiveInterviewService activeInterviewServiceMock;

  // private IContraindicatable contraindicatableMock;

  private PersistenceManager persistenceManagerMock;

  private TubeRegistrationConfiguration tubeRegistrationConfig;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
    // contraindicatableMock = createMock(IContraindicatable.class);
    persistenceManagerMock = createMock(PersistenceManager.class);
    tubeRegistrationConfig = new TubeRegistrationConfiguration();

    service = new ActiveTubeRegistrationServiceImpl();

    service.setActiveInterviewService(activeInterviewServiceMock);
    // service.setIContraindicatable(contraindicatableMock);
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
    Interview interview = new Interview();
    ParticipantTubeRegistration registration = new ParticipantTubeRegistration(tubeRegistrationConfig);
    registration.addRegisteredParticipantTube(new RegisteredParticipantTube());

    expect(activeInterviewServiceMock.getInterview()).andReturn(interview);
    expect(persistenceManagerMock.matchOne(isA(ParticipantTubeRegistration.class))).andReturn(registration);
    replay(activeInterviewServiceMock, persistenceManagerMock);

    int count = service.getRegisteredTubeCount();

    verify(activeInterviewServiceMock, persistenceManagerMock);
    Assert.assertEquals(1, count);

  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#registerTube(java.lang.String)}.
   */
  @Test
  public void testSucceedToRegisterTube() {
    /*
     * String barcode = "101234560108"; Interview interview = new Interview();
     * 
     * expect(activeInterviewServiceMock.getInterview()).andReturn(interview).times(2);
     * expect(contraindicatableMock.getContraindication()).andReturn(null);
     * expect(contraindicatableMock.getOtherContraindication()).andReturn(null);
     * 
     * replay(activeInterviewServiceMock, contraindicatableMock);
     * 
     * service.registerTube(barcode);
     */
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#setTubeComment(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public void testSetTubeComment() {

  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#setTubeRemark(java.lang.String, org.obiba.onyx.ruby.core.domain.Remark)}
   * .
   */
  @Test
  public void testSetTubeRemark() {

  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.service.impl.ActiveTubeRegistrationServiceImpl#unregisterTube(java.lang.String)}.
   */
  @Test
  public void testUnregisterTube() {

  }

}
