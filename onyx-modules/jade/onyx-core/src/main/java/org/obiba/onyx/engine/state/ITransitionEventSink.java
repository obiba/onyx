package org.obiba.onyx.engine.state;

public interface ITransitionEventSink {
  
  public void castEvent(TransitionEvent event);
  
}
