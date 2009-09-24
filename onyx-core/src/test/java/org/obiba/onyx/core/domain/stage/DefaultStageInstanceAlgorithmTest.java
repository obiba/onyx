/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.stage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.state.StageState;

public class DefaultStageInstanceAlgorithmTest {

  //
  // Test Methods
  //

  @Test
  public void testSingleStageInstanceNoInterruptions() {
    DefaultStageInstanceAlgorithm algorithm = new DefaultStageInstanceAlgorithm();

    List<StageTransition> stageTransitions = new ArrayList<StageTransition>();
    stageTransitions.add(createStageTransition(StageState.Waiting, StageState.Ready, 0));
    stageTransitions.add(createStageTransition(StageState.Ready, StageState.InProgress, 1000));
    stageTransitions.add(createStageTransition(StageState.InProgress, StageState.Completed, 11000));

    // Execute test.
    List<StageInstance> stageInstances = algorithm.getStageInstances(stageTransitions);

    // Verify state.
    assertNotNull(stageInstances);
    assertEquals(1, stageInstances.size());
    StageInstance stageInstance = stageInstances.get(0);
    assertEquals(10, stageInstance.getDuration());
    assertEquals(0, stageInstance.getInterruptionCount());
    assertEquals(StageState.Completed, stageInstance.getLastState());
    assertTrue(stageInstance.isLast());
  }

  @Test
  public void testSingleStageInstanceWithInterruptions() {
    DefaultStageInstanceAlgorithm algorithm = new DefaultStageInstanceAlgorithm();

    List<StageTransition> stageTransitions = new ArrayList<StageTransition>();
    stageTransitions.add(createStageTransition(StageState.Waiting, StageState.Ready, 0));
    stageTransitions.add(createStageTransition(StageState.Ready, StageState.InProgress, 1000));
    stageTransitions.add(createStageTransition(StageState.InProgress, StageState.Interrupted, 2000));
    stageTransitions.add(createStageTransition(StageState.Interrupted, StageState.InProgress, 3000));
    stageTransitions.add(createStageTransition(StageState.InProgress, StageState.Interrupted, 4000));
    stageTransitions.add(createStageTransition(StageState.Interrupted, StageState.InProgress, 5000));
    stageTransitions.add(createStageTransition(StageState.InProgress, StageState.Completed, 6000));

    // Execute test.
    List<StageInstance> stageInstances = algorithm.getStageInstances(stageTransitions);

    // Verify state.
    assertNotNull(stageInstances);
    assertEquals(1, stageInstances.size());
    StageInstance stageInstance = stageInstances.get(0);
    assertEquals(3, stageInstance.getDuration());
    assertEquals(2, stageInstance.getInterruptionCount());
    assertEquals(StageState.Completed, stageInstance.getLastState());
    assertTrue(stageInstance.isLast());
  }

  @Test
  public void testSingleStageInstanceWithInitialWaitingReadyLoop() {
    DefaultStageInstanceAlgorithm algorithm = new DefaultStageInstanceAlgorithm();

    List<StageTransition> stageTransitions = new ArrayList<StageTransition>();
    stageTransitions.add(createStageTransition(StageState.Waiting, StageState.Ready, 0));
    stageTransitions.add(createStageTransition(StageState.Ready, StageState.Waiting, 1000));
    stageTransitions.add(createStageTransition(StageState.Waiting, StageState.Ready, 2000));
    stageTransitions.add(createStageTransition(StageState.Ready, StageState.Waiting, 3000));
    stageTransitions.add(createStageTransition(StageState.Waiting, StageState.Ready, 4000));
    stageTransitions.add(createStageTransition(StageState.Ready, StageState.InProgress, 5000));
    stageTransitions.add(createStageTransition(StageState.InProgress, StageState.Completed, 6000));

    // Execute test.
    List<StageInstance> stageInstances = algorithm.getStageInstances(stageTransitions);

    // Verify state.
    assertNotNull(stageInstances);
    assertEquals(1, stageInstances.size());
    StageInstance stageInstance = stageInstances.get(0);
    assertEquals(1, stageInstance.getDuration());
    assertEquals(0, stageInstance.getInterruptionCount());
    assertEquals(StageState.Completed, stageInstance.getLastState());
    assertTrue(stageInstance.isLast());
  }

  @Test
  public void testTwoStageInstancesFirstOneCancelledWhileInProgressSecondOneCompleted() {
    DefaultStageInstanceAlgorithm algorithm = new DefaultStageInstanceAlgorithm();

    List<StageTransition> stageTransitions = new ArrayList<StageTransition>();
    stageTransitions.add(createStageTransition(StageState.Waiting, StageState.Ready, 0));
    stageTransitions.add(createStageTransition(StageState.Ready, StageState.InProgress, 1000));
    stageTransitions.add(createStageTransition(StageState.InProgress, StageState.Ready, 2000));
    stageTransitions.add(createStageTransition(StageState.Ready, StageState.InProgress, 3000));
    stageTransitions.add(createStageTransition(StageState.InProgress, StageState.Completed, 4000));

    // Execute test.
    List<StageInstance> stageInstances = algorithm.getStageInstances(stageTransitions);

    // Verify state.
    assertNotNull(stageInstances);
    assertEquals(2, stageInstances.size());

    // Verify state of first StageInstance.
    StageInstance stageInstance = stageInstances.get(0);
    assertEquals(1, stageInstance.getDuration());
    assertEquals(0, stageInstance.getInterruptionCount());
    // assertEquals(StageState.InProgress, stageInstance.getLastState()); // Ready or InProgress?
    assertFalse(stageInstance.isLast());

    // Verify state of second StageInstance.
    stageInstance = stageInstances.get(1);
    assertEquals(1, stageInstance.getDuration());
    assertEquals(0, stageInstance.getInterruptionCount());
    assertEquals(StageState.Completed, stageInstance.getLastState());
    assertTrue(stageInstance.isLast());
  }

  //
  // Helper Methods
  //

  private StageTransition createStageTransition(StageState fromState, StageState toState, long timestamp) {
    StageTransition stageTransition = new StageTransition();

    Action action = new Action();
    action.setDateTime(new Date(timestamp));
    stageTransition.setAction(action);

    stageTransition.setFromState(fromState);
    stageTransition.setToState(toState);

    return stageTransition;
  }
}
