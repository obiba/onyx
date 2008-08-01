package org.obiba.onyx.engine.state;

public interface ITransitionListener {
  
  /**
   * Called when a transition event has occured.
   * @param execution
   */
  public void onTransition(IStageExecution execution, TransitionEvent event);
  
  /**
   * Listen to transitions until it asks the contrary. Checked after transition has occured. 
   * If false, listener is removed from listener list.
   */
  public boolean isListening();
  
}
