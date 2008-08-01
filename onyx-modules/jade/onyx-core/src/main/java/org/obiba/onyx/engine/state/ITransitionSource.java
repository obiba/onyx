package org.obiba.onyx.engine.state;

public interface ITransitionSource {
  
  public void addTransitionListener(ITransitionListener listener);
  
  public void removeTransitionListener(ITransitionListener listener);
  
  public void removeAllTransitionListener();
  
}
