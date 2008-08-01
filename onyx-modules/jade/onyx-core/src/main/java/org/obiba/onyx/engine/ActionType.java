package org.obiba.onyx.engine;

import org.obiba.onyx.engine.state.IStageExecution;

public enum ActionType {
  COMMENT {
    @Override
    public void act(IStageExecution execution, Action action) {
    }
  },
  COMPLETE {
    @Override
    public void act(IStageExecution execution, Action action) {
      execution.complete(action);
    }
  },
  EXECUTE {
    @Override
    public void act(IStageExecution execution, Action action) {
      execution.execute(action);
    }
  },
  INTERRUPT {
    @Override
    public void act(IStageExecution execution, Action action) {
      execution.interrupt(action);
    }
  },
  STOP {
    @Override
    public void act(IStageExecution execution, Action action) {
      execution.stop(action);
    }
  },
  SKIP {
    @Override
    public void act(IStageExecution execution, Action action) {
      execution.skip(action);
    }
  };

  public abstract void act(IStageExecution execution, Action action);
}
