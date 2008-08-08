package org.obiba.onyx.engine.state;

/**
 * A source of {@link TransitionEvent} can hold a list of {@link ITransitionListener} whishing to be informed about a
 * state transition event.
 * 
 * @author Yannick Marcon
 * 
 */
public interface ITransitionSource {

  /**
   * Add a {@link ITransitionListener}.
   * @param listener
   */
  public void addTransitionListener(ITransitionListener listener);

  /**
   * Remove the given {@link ITransitionListener}, if known (otherwise ignore it).
   * @param listener
   */
  public void removeTransitionListener(ITransitionListener listener);

}
