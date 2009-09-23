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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.stage.StageTransition;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.StageState;

public class DefaultInterviewServiceImplTest {
  //
  // Constants
  //

  private static final String STAGE_TRANSITION_SORTING_CLAUSE = "action.dateTime";

  //
  // Instance Variables
  //

  private PersistenceManager persistenceManagerMock;

  private Interview interview;

  private Stage stage;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    persistenceManagerMock = createMock(PersistenceManager.class);

    interview = new Interview();
    interview.setId(1l);

    stage = new Stage();
    stage.setName("testStage");
  }

  //
  // Test Methods
  //

  @Test
  public void testGetStageTransitionsWhenNoneExists() {
    DefaultInterviewServiceImpl service = new DefaultInterviewServiceImpl();
    service.setPersistenceManager(persistenceManagerMock);

    // Create an empty list of stage transitions for the test.
    List<StageTransition> expectedStageTransitions = new ArrayList<StageTransition>();

    StageTransition stageTransition = createStageTransition(interview, stage);
    SortingClause sortingClause = new SortingClause(STAGE_TRANSITION_SORTING_CLAUSE);

    // Record expected behaviour.
    expect(persistenceManagerMock.match(stageTransition, sortingClause)).andReturn(expectedStageTransitions);
    replay(persistenceManagerMock);

    // Execute test.
    List<StageTransition> stageTransitions = service.getStageTransitions(interview, stage);

    // Verify expected behaviour.
    verify(persistenceManagerMock);

    // Verify expected state.
    assertNotNull(stageTransitions);
    Assert.assertTrue(stageTransitions.isEmpty());
  }

  @Test
  public void testGetStageTransitionsWhenThereAreSome() {
    DefaultInterviewServiceImpl service = new DefaultInterviewServiceImpl();
    service.setPersistenceManager(persistenceManagerMock);

    // Create a list of stage transitions for the test.
    List<StageTransition> expectedStageTransitions = new ArrayList<StageTransition>();
    expectedStageTransitions.add(createStageTransition(interview, stage, StageState.Waiting, StageState.Ready));
    expectedStageTransitions.add(createStageTransition(interview, stage, StageState.Ready, StageState.InProgress));

    StageTransition stageTransition = createStageTransition(interview, stage);
    SortingClause sortingClause = new SortingClause(STAGE_TRANSITION_SORTING_CLAUSE);

    // Record expected behaviour.
    expect(persistenceManagerMock.match(stageTransition, sortingClause)).andReturn(expectedStageTransitions);
    replay(persistenceManagerMock);

    // Execute test.
    List<StageTransition> stageTransitions = service.getStageTransitions(interview, stage);

    // Verify expected behaviour.
    verify(persistenceManagerMock);

    // Verify expected state.
    assertNotNull(stageTransitions);
    Assert.assertArrayEquals(expectedStageTransitions.toArray(), stageTransitions.toArray());
  }

  //
  // Helper Methods
  //

  private StageTransition createStageTransition(Interview interview, Stage stage) {
    StageTransition stageTransition = new StageTransition() {

      private static final long serialVersionUID = 1L;

      // Override equals method to treat as equal StageTransitions with
      // the same interview and stage. This allows us to match StageTransition arguments
      // in EasyMock.
      public boolean equals(Object o) {
        if(o instanceof StageTransition) {
          StageTransition otherStageTransition = (StageTransition) o;
          return getInterview().getId().equals(otherStageTransition.getInterview().getId()) && getStage().equals(otherStageTransition.getStage());
        }
        return false;
      }
    };
    stageTransition.setInterview(interview);
    stageTransition.setStage(stage.getName());

    return stageTransition;
  }

  private StageTransition createStageTransition(Interview interview, Stage stage, StageState fromState, StageState toState) {
    StageTransition stageTransition = createStageTransition(interview, stage);

    stageTransition.setFromState(fromState);
    stageTransition.setToState(toState);

    return stageTransition;
  }
}
