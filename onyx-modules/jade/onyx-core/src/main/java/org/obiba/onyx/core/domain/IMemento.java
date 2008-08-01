package org.obiba.onyx.core.domain;

public interface IMemento {
  
  public Object saveToMemento();
  
  public void restoreFromMemento(Object memento);
  
}
