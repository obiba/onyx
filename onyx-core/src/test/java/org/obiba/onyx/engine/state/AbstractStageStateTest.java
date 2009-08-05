/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.state;

import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;

public class AbstractStageStateTest {

  private Stage stage;

  private InProgressState inProgressState;

  @Before
  public void setUp() throws Exception {
    stage = new Stage();
    inProgressState = new InProgressState();
    inProgressState.setStage(stage);
  }

  @Test
  public void testFinalState() {
    stage.setInterviewConclusion(true);
    Assert.assertTrue(inProgressState.isFinal());
  }

  @Test
  public void testNonFinalState() {
    stage.setInterviewConclusion(false);
    Assert.assertFalse(inProgressState.isFinal());
  }

  /**
   * A concrete implementation of the AbstractStageState for testing. This State has been completed.
   */
  private class InProgressState extends AbstractStageState {

    @Override
    protected void addUserActions(Set<ActionType> types) {
      types.add(ActionType.STOP);
      types.add(ActionType.COMMENT);
    }

    public String getName() {
      return "InProgress";
    }

    @Override
    public boolean isCompleted() {
      return true;
    }

  }

}
