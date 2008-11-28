/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
