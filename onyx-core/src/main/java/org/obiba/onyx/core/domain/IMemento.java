/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
