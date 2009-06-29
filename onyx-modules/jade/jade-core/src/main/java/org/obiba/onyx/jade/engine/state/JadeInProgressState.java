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

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.wicket.JadePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JadeInProgressState extends AbstractJadeStageState {

  private static final Logger log = LoggerFactory.getLogger(JadeInProgressState.class);

  private boolean resuming;

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.STOP);
  }

  @Override
  protected void addSystemActions(Set<ActionType> types) {
    types.add(ActionType.COMPLETE);
  }

  public Component getWidget(String id) {
    return new JadePanel(id, getStage(), isResuming());
  }

  @Override
  public void stop(Action action) {
    log.debug("Jade Stage {} is stopping", super.getStage().getName());
    cancelInstrumentRuns();
    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public void complete(Action action) {
    log.debug("Jade Stage {} is completing", super.getStage().getName());
    // Finish current instrument run
    InstrumentRun run = getLastInstrumentRun();

    InstrumentRunStatus runStatus = run.getStatus();
    if(!runStatus.equals(InstrumentRunStatus.CONTRA_INDICATED)) {
      run.setStatus(InstrumentRunStatus.COMPLETED);
      activeInstrumentRunService.setInstrumentRun(run);
      runStatus = InstrumentRunStatus.COMPLETED;
    }
    activeInstrumentRunService.end();

    if(runStatus.equals(InstrumentRunStatus.CONTRA_INDICATED)) {
      castEvent(TransitionEvent.CONTRAINDICATED);
    } else {
      castEvent(TransitionEvent.COMPLETE);
    }
  }

  private InstrumentRun getLastInstrumentRun() {
    return instrumentRunService.getInstrumentRun(activeInterviewService.getParticipant(), getStage().getName());
  }

  @Override
  public boolean isInteractive() {
    return true;
  }

  @Override
  public void interrupt(Action action) {
    castEvent(TransitionEvent.INTERRUPT);
  }

  public String getName() {
    return "InProgress";
  }

  private boolean isResuming() {
    return resuming;
  }

  @Override
  public void onEntry(TransitionEvent event) {
    if(event.equals(TransitionEvent.RESUME)) {
      activeInstrumentRunService.setInstrumentRun(getLastInstrumentRun());
      resuming = true;
    } else {
      // ONYX-181: Set the current InstrumentRun on the ActiveInstrumentRunService. This particular
      // instance of the service may not have had its start method called, in which case it will have
      // a null InstrumentRun.
      activeInstrumentRunService.start(activeInterviewService.getParticipant(), instrumentService.getInstrumentType(getStage().getName()));
      resuming = false;
    }
  }

}
