package org.obiba.onyx.core.domain;

public interface IMemento {
  
  public Object saveToMemento(Object memento);
  
  public void restoreFromMemento(Object memento);
  
}
