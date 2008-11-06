/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Ruby INTERRUPTED state.
 */
public class RubyInterruptedState extends AbstractRubyStageState implements InitializingBean {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(RubyInterruptedState.class);

  //
  // InitializingBean Methods
  //

  public void afterPropertiesSet() throws Exception {
    addAction(ActionDefinitionBuilder.create(ActionType.STOP, "Cancel").setDescription("You may explain why you are cancelling this stage.").setAskParticipantId(true).setAskPassword(true).getActionDefinition());
    addAction(ActionDefinitionBuilder.create(ActionType.EXECUTE, "Resume").setAskParticipantId(true).setAskPassword(true).getActionDefinition());
  }

  //
  // AbstractRubyStageState Methods
  //

  public String getName() {
    return "Ruby.Interrupted";
  }

  @Override
  public void stop(Action action) {
    log.info("Ruby Stage {} is canceling", super.getStage().getName());

    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public void execute(Action action) {
    log.info("Ruby Stage {} is resuming", super.getStage().getName());
    castEvent(TransitionEvent.RESUME);
  }
}
