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

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mica Ready State, goes there each time a state is cancelled.
 * 
 * @author Meryam Belhiah
 * 
 */
public class MicaReadyState extends AbstractMicaStageState {

  private static final Logger log = LoggerFactory.getLogger(MicaReadyState.class);

  @SuppressWarnings("unused")
  private ActiveConclusionService activeConclusionService;

  public void setActiveConsentService(ActiveConclusionService activeConsentService) {
    this.activeConclusionService = activeConsentService;
  }

  // public void afterPropertiesSet() throws Exception {
  // addAction(ActionDefinitionBuilder.START_ACTION);
  // }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.EXECUTE);
  }

  @Override
  public void execute(Action action) {
    super.execute(action);
    log.info("Mica Stage {} is starting", super.getStage().getName());
    castEvent(TransitionEvent.START);
  }

  public String getName() {
    return "Ready";
  }

}
