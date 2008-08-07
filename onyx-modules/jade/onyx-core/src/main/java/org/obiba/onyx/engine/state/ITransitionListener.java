package org.obiba.onyx.engine.state;

/**
 * A {@link TransitionEvent} listener, would want to be informed about a state transition that has occured in
 * {@link ITransitionSource}.
 * @author Yannick Marcon
 * 
 */
public interface ITransitionListener {

  /**
   * Called after a transition event has occured.
   * @param execution
   */
  public void onTransition(IStageExecution execution, TransitionEvent event);

  /**
   * Listen to transitions until it asks the contrary. Checked after transition has occured. If false, listener is
   * removed from listener list.
   */
  public boolean removeAfterTransition();

}
