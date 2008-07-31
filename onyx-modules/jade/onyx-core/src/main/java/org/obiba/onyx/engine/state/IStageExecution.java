package org.obiba.onyx.engine.state;

import java.util.List;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionInstance;

public interface IStageExecution {

  public List<Action> getActions();

  public void execute(ActionInstance action);

  public void skip(ActionInstance action);

  public void stop(ActionInstance action);

  public void interrupt(ActionInstance action);

  public Component getWidget(String id);

  public boolean isInteractive();

  public boolean isCompleted();

  public boolean isFinal();

}
