/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * 
 */
package org.obiba.onyx.mica.engine.state;

import java.util.Set;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.mica.core.wicket.MicaPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicaInProgressState extends AbstractMicaStageState {

  private static final Logger log = LoggerFactory.getLogger(MicaInProgressState.class);

  // public void afterPropertiesSet() throws Exception {
  // addAction(ActionDefinitionBuilder.CANCEL_ACTION);
  // addSystemAction(ActionDefinitionBuilder.COMPLETE_ACTION);
  // }

  public String getName() {
    return "InProgress";
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.STOP);
  }

  @Override
  protected void addSystemActions(Set<ActionType> types) {
    types.add(ActionType.COMPLETE);
  }

  public Component getWidget(String id) {
    return new MicaPanel(id, getStage());
  }

  @Override
  public void complete(Action action) {
    log.debug("Mica Stage {} is completing", super.getStage().getName());
    // Finish current conclusion
    castEvent(TransitionEvent.COMPLETE);
  }

  @Override
  public void stop(Action action) {
    log.debug("Mica Stage {} is canceling", super.getStage().getName());
    castEvent(TransitionEvent.CANCEL);
  }

  @Override
  public boolean isInteractive() {
    return true;
  }

}
