/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.engine.state;

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.springframework.beans.factory.InitializingBean;

public class JadeNotApplicableState extends AbstractStageState implements InitializingBean {

  public void afterPropertiesSet() throws Exception {
    // TODO Auto-generated method stub

  }
  
  @Override
  public boolean isCompleted() {
    return true;
  }

  public String getName() {
    return "Jade.NotApplicable";
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.NOTAPPLICABLE)) return false;
    else
      return true;
  }
}
