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

import java.util.Set;

import org.apache.wicket.Component;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.wicket.RubyPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ruby IN PROGRESS state.
 */
public class RubyInProgressState extends AbstractRubyStageState {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(RubyInProgressState.class);

  //
  // Instance Variables
  //

  private boolean resuming;

  //
  // AbstractRubyStageState Methods
  //

  public String getName() {
    return "InProgress";
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.STOP);
    types.add(ActionType.INTERRUPT);
  }

  @Override
  protected void addSystemActions(Set<ActionType> types) {
    types.add(ActionType.COMPLETE);
  }

  public Component getWidget(String id) {
    return new RubyPanel(id, getStage(), isResuming());
  }

  @Override
  public void stop(Action action) {
    log.info("Ruby Stage {} is stopping", super.getStage().getName());

    activeTubeRegistrationService.deleteParticipantTubeRegistration();

    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public void complete(Action action) {
    log.info("Ruby Stage {} is completing", super.getStage().getName());

    ParticipantTubeRegistration participantTubeRegistration = activeTubeRegistrationService.getParticipantTubeRegistration();
    boolean contraIndicated = (participantTubeRegistration.getContraindication() != null);

    activeTubeRegistrationService.end();

    if(contraIndicated) {
      castEvent(TransitionEvent.CONTRAINDICATED);
    } else {
      castEvent(TransitionEvent.COMPLETE);
    }
  }

  @Override
  public boolean isInteractive() {
    return true;
  }

  @Override
  public void interrupt(Action action) {
    log.info("Ruby Stage {} is interrupting", super.getStage().getName());
    castEvent(TransitionEvent.INTERRUPT);
  }

  @Override
  public void onEntry(TransitionEvent event) {
    Participant participant = activeInterviewService.getParticipant();

    if(event.equals(TransitionEvent.RESUME)) {
      resuming = true;
      activeTubeRegistrationService.resume(participant);
    } else {
      resuming = false;
      activeTubeRegistrationService.start(participant);
    }
  }

  //
  // Methods
  //

  private boolean isResuming() {
    return resuming;
  }
}
