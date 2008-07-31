package org.obiba.onyx.engine.state;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.transition.ExecutableState;
import org.obiba.onyx.engine.state.transition.SkippableState;

public abstract class ReadyState extends StageState implements ExecutableState, SkippableState {

  protected ReadyState() {
    addAction(new ActionDefinition(ActionType.EXECUTE));
    addAction(new ActionDefinition(ActionType.SKIP));
  }

  @Override
  public void execute(Action action) {
    onExecute(action);
    castEvent(EXECUTE);
  }

  @Override
  public void skip(Action action) {
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
