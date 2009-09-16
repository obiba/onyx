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

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Ruby states.
 */
public abstract class AbstractRubyStageState extends AbstractStageState {
  //
  // Constants
  //

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractRubyStageState.class);

  //
  // Instance Variables
  //

  protected ActiveTubeRegistrationService activeTubeRegistrationService;

  //
  // AbstractStageState Methods
  //

  @Override
  public void onTransition(IStageExecution execution, StageState fromState, TransitionEvent event) {
    Boolean var = areDependenciesCompleted();

    if(var == null) {
      // ONYX-428
      if(!execution.isInteractive() && wantTransitionEvent(TransitionEvent.INVALID)) {
        castEvent(TransitionEvent.INVALID);
      }
    } else if(var == true && wantTransitionEvent(TransitionEvent.VALID)) {
      castEvent(TransitionEvent.VALID);
    } else if(var == false && wantTransitionEvent(TransitionEvent.NOTAPPLICABLE)) {
      castEvent(TransitionEvent.NOTAPPLICABLE);
    }
  }

  //
  // Methods
  //

  public void setActiveTubeRegistrationService(ActiveTubeRegistrationService activeTubeRegistrationService) {
    this.activeTubeRegistrationService = activeTubeRegistrationService;
  }
}
