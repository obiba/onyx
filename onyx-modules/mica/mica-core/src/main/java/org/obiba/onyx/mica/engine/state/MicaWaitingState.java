/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.mica.engine.state;

import org.obiba.onyx.engine.state.ITransitionListener;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.springframework.beans.factory.InitializingBean;

/**
 * Jade Waiting State, goes there if a dependency is not satisfied.
 * @author Meryam Belhiah
 * 
 */
public class MicaWaitingState extends AbstractMicaStageState implements InitializingBean, ITransitionListener {

  public void afterPropertiesSet() throws Exception {
  }

  public String getName() {
    return "Mica.Waiting";
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.INVALID)) return false;
    else
      return true;
  }

}
