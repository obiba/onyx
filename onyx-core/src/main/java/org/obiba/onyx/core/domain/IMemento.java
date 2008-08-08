package org.obiba.onyx.core.domain;

import org.obiba.core.domain.IEntity;

/**
 * This interface must be implemented to support persistence by memeto pattern.
 * @author Yannick Marcon
 *
 */
public interface IMemento {
  
  /**
   * Save the state into the memento.
   * @param memento to be created if null, to be updated if not
   * @return the memto to persist
   */
  public IEntity saveToMemento(IEntity memento);
  
  /**
   * Given the memento, restore the persisted state.
   * @param memento
   */
  public void restoreFromMemento(IEntity memento);
  
}
