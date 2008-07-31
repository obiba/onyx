package org.obiba.onyx.engine.state;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionInstance;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.transition.SkippableState;
import org.obiba.onyx.engine.state.transition.ExecutableState;

public abstract class ReadyState extends StageState implements ExecutableState, SkippableState {

  protected ReadyState() {
    addAction(new Action(ActionType.EXECUTE));
    addAction(new Action(ActionType.SKIP));
  }

  @Override
  public void execute(ActionInstance action) {
    onExecute(action);
    castEvent(EXECUTE);
  }

  @Override
  public void skip(ActionInstance action) {
    onSkip(action);
    castEvent(SKIP);
  }

  public Component getWidget(String id) {
    return null;
  }

  public boolean isInteractive() {
    return false;
  }

  public boolean isFinal() {
    return false;
  }

  public boolean isCompleted() {
    return false;
  }

}
