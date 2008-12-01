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
package org.obiba.onyx.jade.engine.state;

import java.util.Set;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JadeCompletedState extends AbstractJadeStageState {

  private static final Logger log = LoggerFactory.getLogger(JadeCompletedState.class);

  public String getName() {
    return "Completed";
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.STOP);
  }

  @Override
  public void stop(Action action) {
    super.execute(action);
    log.info("Jade Stage {} is cancelling", super.getStage().getName());
    cancelInstrumentRun();
    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.NOTAPPLICABLE)) {
      cancelInstrumentRun();
    }
    return super.wantTransitionEvent(transitionEvent);
  }

  private void cancelInstrumentRun() {
    InstrumentType instrumentType = instrumentService.getInstrumentType(getStage().getName());
    Participant participant = activeInterviewService.getParticipant();
    instrumentRunService.setInstrumentRunStatus(instrumentRunService.getLastCompletedInstrumentRun(participant, instrumentType), InstrumentRunStatus.CANCELED);
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  @Override
  public ActionType getStartingActionType() {
    return ActionType.EXECUTE;
  }
}
