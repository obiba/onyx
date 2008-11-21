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

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionDefinitionBuilder;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunRefusalReason;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.wicket.JadePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class JadeInProgressState extends AbstractStageState implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(JadeInProgressState.class);

  private ActiveInstrumentRunService activeInstrumentRunService;

  public void setActiveInstrumentRunService(ActiveInstrumentRunService activeInstrumentRunService) {
    this.activeInstrumentRunService = activeInstrumentRunService;
  }

  public void afterPropertiesSet() throws Exception {
    ActionDefinition def = ActionDefinitionBuilder.create(ActionType.STOP, "Cancel").setDescription("You may explain why you are cancelling this stage.").getActionDefinition();
    for(InstrumentRunRefusalReason reason : InstrumentRunRefusalReason.values()) {
      def.addReason(reason.toString());
      if(def.getDefaultReason() == null) def.setDefaultReason(reason.toString());
    }
    addAction(def);
    addSystemAction(ActionDefinitionBuilder.COMPLETE_ACTION);
  }

  public Component getWidget(String id) {
    return new JadePanel(id, getStage());
  }

  @Override
  public void stop(Action action) {
    log.info("Jade Stage {} is stopping", super.getStage().getName());
    // Invalidate current instrument run
    InstrumentRun run = activeInstrumentRunService.getInstrumentRun();
    if(run != null) {
      activeInstrumentRunService.setInstrumentRunStatus(InstrumentRunStatus.CANCELED);
      activeInstrumentRunService.end();
      activeInstrumentRunService.reset();
    }
    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public void complete(Action action) {
    log.info("Jade Stage {} is completing", super.getStage().getName());
    // Finish current instrument run
    InstrumentRunStatus runStatus = activeInstrumentRunService.getInstrumentRunStatus();
    activeInstrumentRunService.end();
    activeInstrumentRunService.reset();

    if(runStatus.equals(InstrumentRunStatus.CONTRA_INDICATED)) {
      castEvent(TransitionEvent.CONTRAINDICATED);
    } else
      castEvent(TransitionEvent.COMPLETE);
  }

  @Override
  public boolean isInteractive() {
    return true;
  }

  public String getName() {
    return "InProgress";
  }

}
