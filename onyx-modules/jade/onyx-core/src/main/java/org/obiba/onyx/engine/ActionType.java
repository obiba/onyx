package org.obiba.onyx.engine;

import org.obiba.onyx.engine.state.IStageExecution;

public enum ActionType {
  EXECUTE {
    @Override
    public void act(IStageExecution execution, ActionInstance instance) {
      execution.execute(instance);
    }
  },
  STOP {
    @Override
    public void act(IStageExecution execution, ActionInstance instance) {
      execution.stop(instance);
    }
  },
  INTERRUPT {
    @Override
    public void act(IStageExecution execution, ActionInstance instance) {
      execution.interrupt(instance);
    }
  },
  SKIP {
    @Override
    public void act(IStageExecution execution, ActionInstance instance) {
      execution.skip(instance);
    }
  };

  public abstract void act(IStageExecution execution, ActionInstance instance);
}
