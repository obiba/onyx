package org.obiba.onyx.engine.state;

import java.util.List;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;

public interface IStageExecution {

  public List<ActionDefinition> getActions();

  public void execute(Action action);

  public void skip(Action action);

  public void stop(Action action);

  public void interrupt(Action action);

  public Component getWidget(String id);

  public boolean isInteractive();

  public boolean isCompleted();

  public boolean isFinal();

}
