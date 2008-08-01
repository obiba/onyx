package org.obiba.onyx.engine.state;

import java.util.List;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;

public interface IStageExecution {

  /**
   * Get the action definitions, exposed to user by Onyx.
   * @return
   */
  public List<ActionDefinition> getActionDefinitions();
  
  /**
   * Get the action definition for the given type or null if not found.
   * @param type
   * @return
   */
  public ActionDefinition getActionDefinition(ActionType type);
  
  /**
   * Get the action definitions, not exposed to normal user interface. It may be used 
   * by module UI.
   * @return
   */
  public List<ActionDefinition> getSystemActionDefinitions();
  
  /**
   * Get the system action definition for the given type or null if not found.
   * @param type
   * @return
   */
  public ActionDefinition getSystemActionDefinition(ActionType type);

  public void execute(Action action);

  public void skip(Action action);

  public void stop(Action action);

  public void interrupt(Action action);

  public void complete(Action action);

  public Component getWidget(String id);

  public boolean isInteractive();

  public boolean isCompleted();

  public boolean isFinal();
  
  public String getMessage();

}
