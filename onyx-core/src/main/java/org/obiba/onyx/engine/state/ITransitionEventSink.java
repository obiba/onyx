package org.obiba.onyx.engine.state;

/**
 * Defines the operation of performing the state transition given the {@link TransitionEvent}.
 * 
 * @see State Machine Design Pattern http://dotnet.zcu.cz/NET_2006/Papers_2006/short/B31-full.pdf
 * @author Yannick Marcon
 * 
 */
public interface ITransitionEventSink {

  /**
   * Perform the state transition.
   * @param event
   */
  public void castEvent(TransitionEvent event);

}
