package org.obiba.onyx.engine.state;

import java.util.List;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.util.data.Data;

/**
 * State Machine interface, exposed by {@link StageExecutionContext}.
 * 
 * @see State Machine Design Pattern http://dotnet.zcu.cz/NET_2006/Papers_2006/short/B31-full.pdf
 * @author Yannick Marcon
 * 
 */
public interface IStageExecution {

  /**
   * Get the name identifying this stage execution.
   * @return
   */
  public String getName();

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
   * Get the action definitions, not exposed to normal user interface. It may be used by module UI.
   * @return
   */
  public List<ActionDefinition> getSystemActionDefinitions();

  /**
   * Get the system action definition for the given type or null if not found.
   * @param type
   * @return
   */
  public ActionDefinition getSystemActionDefinition(ActionType type);

  /**
   * Do execute.
   * @see ActionType
   * @param action
   */
  public void execute(Action action);

  /**
   * Do skip.
   * @see ActionType
   * @param action
   */
  public void skip(Action action);

  /**
   * Do stop.
   * @see ActionType
   * @param action
   */
  public void stop(Action action);

  /**
   * Do interrupt.
   * @see ActionType
   * @param action
   */
  public void interrupt(Action action);

  /**
   * Do complete.
   * @see ActionType
   * @param action
   */
  public void complete(Action action);

  /**
   * Get the {@link Component} to be displayed.
   * @see #isInteractive()
   * @param id
   * @return
   */
  public Component getWidget(String id);

  /**
   * Says if {@link #getWidget(String)} will return a non null value. This decides if current state should be displayed
   * in the stage page or the interview page.
   * @return
   */
  public boolean isInteractive();

  /**
   * Says if its a completed state, allowing stage dependencies resolution.
   * @return
   */
  public boolean isCompleted();

  /**
   * Says if it is a final state, ready for data export.
   * @return
   */
  public boolean isFinal();

  /**
   * Get a message indicating the current state information (may include reasons why the current state was reached).
   * @return
   */
  public String getMessage();

  /**
   * Get the {@link Data} corresponding to the given for stage execution dependency resolution.
   * @param key
   * @return the Data or null if not applicable
   */
  public Data getData(String key);

  /**
   * Sets the reason for the state (i.e., the action that caused the transition to the current state).
   * 
   * @param reason the reason for the state
   */
  public void setReason(Action reason);

  /**
   * Returns the reason for the state (i.e., the action that caused the transition to the current state).
   * 
   * @return the reason for the state
   */
  public Action getReason();

  /**
   * Returns the starting action type for the state (i.e., the first action that caused the transition to the current
   * state).
   * 
   * @return the actionType for the state
   */
  public ActionType getStartingActionType();
}