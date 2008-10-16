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
 * A {@link TransitionEvent} listener, would want to be informed about a state transition that has occured in
 * {@link ITransitionSource}.
 * @author Yannick Marcon
 * 
 */
public interface ITransitionListener {

  /**
   * Called after a transition event has occured.
   * @param execution
   */
  public void onTransition(IStageExecution execution, TransitionEvent event);

  /**
   * Listen to transitions until it asks the contrary. Checked after transition has occured. If false, listener is
   * removed from listener list.
   */
  public boolean removeAfterTransition();

}
