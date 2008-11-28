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
