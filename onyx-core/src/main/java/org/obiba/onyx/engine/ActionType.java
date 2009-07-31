/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.TransitionEvent;

/**
 * Onyx has a fixed set of actions, that can be freely associated to different {@link TransitionEvent} through
 * {@link IStageExecution}.
 * @author Yannick Marcon
 * 
 */
public enum ActionType {
  /**
   * Does not not modify {@link IStageExecution} state.
   */
  COMMENT {
    @Override
    public void act(IStageExecution execution, Action action) {
    }
  },
  /**
   * {@link IStageExecution#complete(Action)}
   */
  COMPLETE {
    @Override
    public void act(IStageExecution execution, Action action) {
      execution.complete(action);
    }
  },
  /**
   * {@link IStageExecution#execute(Action)}
   */
  EXECUTE {
    @Override
    public void act(IStageExecution execution, Action action) {
      execution.execute(action);
    }
  },
  /**
   * {@link IStageExecution#interrupt(Action)}
   */
  INTERRUPT {
    @Override
    public void act(IStageExecution execution, Action action) {
      execution.interrupt(action);
      execution.setReason(action);
    }
  },
  /**
   * {@link IStageExecution#start(Action)}
   */
  START {
    @Override
    public void act(IStageExecution execution, Action action) {
      execution.start(action);
    }
  },
  /**
   * {@link IStageExecution#stop(Action)}
   */
  STOP {
    @Override
    public void act(IStageExecution execution, Action action) {
      execution.stop(action);
    }
  },
  /**
   * {@link IStageExecution#skip(Action)}
   */
  SKIP {
    @Override
    public void act(IStageExecution execution, Action action) {
      execution.skip(action);
      execution.setReason(action);
    }
  };

  /**
   * Ask the {@link IStageExecution} to perform {@link Action} according to its {@link ActionType}.
   * @param execution
   * @param action
   */
  public abstract void act(IStageExecution execution, Action action);
}
