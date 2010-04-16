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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.service.InterviewManager;
import org.obiba.onyx.engine.state.StageExecutionContext;

/**
 * Unit tests for {@link DefaultActiveInterviewServiceImpl}.
 */
public class DefaultActiveInterviewServiceImplTest {
  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() throws Exception {
  }

  //
  // Test Methods
  //

  @Test(expected = RuntimeException.class)
  public void testReinstateInterview_ThrowsExceptionIfInterviewIsInProgress() {
    DefaultActiveInterviewServiceImpl sut = createSUT(InterviewStatus.IN_PROGRESS);
    sut.reinstateInterview();
  }

  @Test(expected = RuntimeException.class)
  public void testReinstateInterview_ThrowsExceptionIfInterviewIsCompleted() {
    DefaultActiveInterviewServiceImpl sut = createSUT(InterviewStatus.COMPLETED);
    sut.reinstateInterview();
  }

  @Test
  public void testReinstate_ReinstatesClosedInterviewAsInProgress() {
    // Setup
    PersistenceManager mockPersistenceManager = createMock(PersistenceManager.class);
    Interview expectedInterview = new Interview();
    expectedInterview.setStatus(InterviewStatus.IN_PROGRESS); // should be reinstated as IN_PROGRESS
    expect(mockPersistenceManager.save(eqInterview(expectedInterview))).andReturn(expectedInterview).atLeastOnce();

    DefaultActiveInterviewServiceImpl sut = createSUT(InterviewStatus.CLOSED);
    sut.setPersistenceManager(mockPersistenceManager);

    replay(mockPersistenceManager);

    // Exercise
    sut.reinstateInterview();

    // Verify
    verify(mockPersistenceManager);
  }

  @Test
  public void testReinstate_ReinstatesCancelledInterviewWithFinalStageAsCompleted() {
    // Setup
    PersistenceManager mockPersistenceManager = createMock(PersistenceManager.class);
    Interview expectedInterview = new Interview();
    expectedInterview.setStatus(InterviewStatus.COMPLETED); // should be reinstated as COMPLETED
    expect(mockPersistenceManager.save(eqInterview(expectedInterview))).andReturn(expectedInterview).atLeastOnce();

    InterviewManager mockInterviewManager = createMock(InterviewManager.class);
    expect(mockInterviewManager.getStageContexts()).andReturn(createStageContexts(true)).atLeastOnce();

    DefaultActiveInterviewServiceImpl sut = createSUT(InterviewStatus.CANCELLED);
    sut.setPersistenceManager(mockPersistenceManager);
    sut.setInterviewManager(mockInterviewManager);

    replay(mockPersistenceManager, mockInterviewManager);

    // Exercise
    sut.reinstateInterview();

    // Verify
    verify(mockPersistenceManager, mockInterviewManager);
  }

  @Test
  public void testReinstate_ReinstatesCancelledInterviewWithoutFinalStageAsInProgress() {
    // Setup
    PersistenceManager mockPersistenceManager = createMock(PersistenceManager.class);
    Interview expectedInterview = new Interview();
    expectedInterview.setStatus(InterviewStatus.IN_PROGRESS); // should be reinstated as IN_PROGRESS
    expect(mockPersistenceManager.save(eqInterview(expectedInterview))).andReturn(expectedInterview).atLeastOnce();

    InterviewManager mockInterviewManager = createMock(InterviewManager.class);
    expect(mockInterviewManager.getStageContexts()).andReturn(createStageContexts(false)).atLeastOnce();

    DefaultActiveInterviewServiceImpl sut = createSUT(InterviewStatus.CANCELLED);
    sut.setPersistenceManager(mockPersistenceManager);
    sut.setInterviewManager(mockInterviewManager);

    replay(mockPersistenceManager, mockInterviewManager);

    // Exercise
    sut.reinstateInterview();

    // Verify
    verify(mockPersistenceManager, mockInterviewManager);
  }

  //
  // Helper Methods
  //

  private DefaultActiveInterviewServiceImpl createSUT(final InterviewStatus status) {
    DefaultActiveInterviewServiceImpl sut = new DefaultActiveInterviewServiceImpl() {
      @Override
      public Interview getInterview() {
        Interview interview = new Interview();
        interview.setStatus(status);
        return interview;
      }
    };

    return sut;
  }

  private Map<String, StageExecutionContext> createStageContexts(final boolean withFinal) {
    Map<String, StageExecutionContext> secMap = new HashMap<String, StageExecutionContext>();

    StageExecutionContext sec = new StageExecutionContext() {
      @Override
      public boolean isFinal() {
        return withFinal;
      }
    };
    secMap.put("sec", sec);

    return secMap;
  }

  //
  // Inner Classes
  //

  static class InterviewMatcher implements IArgumentMatcher {

    private Interview expected;

    public InterviewMatcher(Interview expected) {
      this.expected = expected;
    }

    public boolean matches(Object actual) {
      if(actual instanceof Interview) {
        return ((Interview) actual).getStatus().equals(expected.getStatus());
      } else {
        return false;
      }
    }

    public void appendTo(StringBuffer buffer) {
      buffer.append("eqInterview(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with status \"");
      buffer.append(expected.getStatus());
      buffer.append("\")");
    }
  }

  static Interview eqInterview(Interview in) {
    EasyMock.reportMatcher(new InterviewMatcher(in));
    return null;
  }
}
