package org.obiba.onyx.engine;

import org.obiba.onyx.engine.state.IStageExecution;

public enum ActionType {
  COMMENT {
    @Override
    public void act(IStageExecution execution, Action instance) {
    }
  },
  EXECUTE {
    @Override
    public void act(IStageExecution execution, Action instance) {
      execution.execute(instance);
    }
  },
  INTERRUPT {
    @Override
    public void act(IStageExecution execution, Action instance) {
      execution.interrupt(instance);
    }
  },
  STOP {
    @Override
    public void act(IStageExecution execution, Action instance) {
      execution.stop(instance);
    }
  },
  SKIP {
    @Override
    public void act(IStageExecution execution, Action instance) {
      execution.skip(instance);
    }
  };

  public abstract void act(IStageExecution execution, Action instance);
}
