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
