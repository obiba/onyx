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

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.UserSessionService;

/**
 * 
 */
public class DefaultInterviewManagerImplTest {

  private DefaultInterviewManagerImpl interviewManagerImpl = new DefaultInterviewManagerImpl();

  private UserSessionService mockUserSessionService;

  private PersistenceManager mockPersistenceManager;

  private Participant firstParticipant;

  private Participant secondParticipant;

  private User firstUser;

  private User secondUser;

  @Before
  public void setup() {
    mockUserSessionService = EasyMock.createMock(UserSessionService.class);
    mockPersistenceManager = EasyMock.createMock(PersistenceManager.class);

    interviewManagerImpl.setPersistenceManager(mockPersistenceManager);
    interviewManagerImpl.setUserSessionService(mockUserSessionService);

    firstParticipant = new Participant();
    firstParticipant.setId(new Long(1));

    secondParticipant = new Participant();
    secondParticipant.setId(new Long(2));

    firstUser = new User();
    firstUser.setId(new Long(1));

    secondUser = new User();
    secondUser.setId(new Long(2));
  }

  @Test
  public void testInterviewAvailable() {
    Assert.assertTrue(interviewManagerImpl.isInterviewAvailable(firstParticipant));
    Assert.assertTrue(interviewManagerImpl.isInterviewAvailable(secondParticipant));
  }

  @Test
  public void testObtainInterview() {
    Assert.assertTrue(interviewManagerImpl.isInterviewAvailable(firstParticipant));

    expectObtainInterview(firstUser, firstParticipant);

    EasyMock.replay(mockUserSessionService, mockPersistenceManager);

    Interview interview = interviewManagerImpl.obtainInterview(firstParticipant);
    Assert.assertNotNull(interview);

    EasyMock.verify(mockUserSessionService, mockPersistenceManager);

    // The interview should no longer be available.
    Assert.assertFalse(interviewManagerImpl.isInterviewAvailable(firstParticipant));
  }

  @Test
  public void testReleaseInterviewWhenLocked() {
    Assert.assertTrue(interviewManagerImpl.isInterviewAvailable(firstParticipant));

    expectObtainInterview(firstUser, firstParticipant);

    EasyMock.expect(mockPersistenceManager.get(User.class, new Long(1))).andReturn(firstUser);
    EasyMock.expect(mockPersistenceManager.get(Participant.class, new Long(1))).andReturn(firstParticipant);
    EasyMock.replay(mockUserSessionService, mockPersistenceManager);

    Interview interview = interviewManagerImpl.obtainInterview(firstParticipant);
    Assert.assertNotNull(interview);

    // The interview should no longer be available.
    Assert.assertFalse(interviewManagerImpl.isInterviewAvailable(firstParticipant));

    interviewManagerImpl.releaseInterview();

    EasyMock.verify(mockUserSessionService, mockPersistenceManager);

    // The interview should be available again.
    Assert.assertTrue(interviewManagerImpl.isInterviewAvailable(firstParticipant));
  }

  @Test
  public void testReleaseInterviewWithoutLock() {
    Assert.assertTrue(interviewManagerImpl.isInterviewAvailable(firstParticipant));

    EasyMock.expect(mockUserSessionService.getSessionId()).andReturn("firstSession").once();
    EasyMock.replay(mockUserSessionService, mockPersistenceManager);

    // The interview should be available.
    Assert.assertTrue(interviewManagerImpl.isInterviewAvailable(firstParticipant));

    interviewManagerImpl.releaseInterview();

    // The interview should still be available.
    Assert.assertTrue(interviewManagerImpl.isInterviewAvailable(firstParticipant));

    EasyMock.verify(mockUserSessionService, mockPersistenceManager);
  }

  @Test
  public void testCurrentParticipantIsValid() {
    Assert.assertTrue(interviewManagerImpl.isInterviewAvailable(firstParticipant));

    expectObtainInterview(firstUser, firstParticipant);

    EasyMock.expect(mockPersistenceManager.get(Participant.class, new Long(1))).andReturn(firstParticipant);
    EasyMock.replay(mockUserSessionService, mockPersistenceManager);

    Interview interview = interviewManagerImpl.obtainInterview(firstParticipant);
    Assert.assertNotNull(interview);
    Participant currentParticipant = interviewManagerImpl.getInterviewedParticipant();
    Assert.assertNotNull(currentParticipant);
    Assert.assertEquals(firstParticipant, currentParticipant);

    EasyMock.verify(mockUserSessionService, mockPersistenceManager);
  }

  @Test
  public void testCurrentInterviewerIsValid() {
    Assert.assertTrue(interviewManagerImpl.isInterviewAvailable(firstParticipant));

    expectObtainInterview(firstUser, firstParticipant);

    EasyMock.expect(mockPersistenceManager.get(User.class, new Long(1))).andReturn(firstUser);
    EasyMock.replay(mockUserSessionService, mockPersistenceManager);

    Interview interview = interviewManagerImpl.obtainInterview(firstParticipant);
    Assert.assertNotNull(interview);

    User interviewer = interviewManagerImpl.getInterviewer(firstParticipant);
    Assert.assertNotNull(interviewer);
    Assert.assertEquals(firstUser, interviewer);

    EasyMock.verify(mockUserSessionService, mockPersistenceManager);
  }

  /**
   * Adds expectations when obtainInterview is called on the InterviewManager. It creates the expected behaviour of the
   * service's dependencies (primarily, the PersistenceManager).
   * 
   * @param participant
   * @return
   */
  private Capture<Interview> expectObtainInterview(User user, final Participant participant) {
    EasyMock.expect(mockUserSessionService.getSessionId()).andReturn("firstSession").anyTimes();
    EasyMock.expect(mockUserSessionService.getUser()).andReturn(user).anyTimes();

    final Capture<Interview> interviewCapture = new Capture<Interview>();

    // The DefaultInterviewManagerImpl will create the Interview instance. We'll capture it here so we can re-use it
    // elsewhere in the test.
    EasyMock.expect(mockPersistenceManager.save(EasyMock.capture(interviewCapture))).andAnswer(new IAnswer<Interview>() {
      public Interview answer() throws Throwable {
        return interviewCapture.getValue();
      }
    });

    // The refresh method would reload the participant instance from the database, but this time with it's interview
    // relationship.
    EasyMock.expect(mockPersistenceManager.refresh(participant)).andAnswer(new IAnswer<Participant>() {
      public Participant answer() throws Throwable {
        participant.setInterview(interviewCapture.getValue());
        return firstParticipant;
      }
    });
    return interviewCapture;
  }

}
