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

import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.StageDependencyCondition;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.jade.engine.JadeModule;

/**
 * Specific {@link StageDependencyCondition} for {@code Stage}s contributed by the {@link JadeModule}. For a completed
 * stage, this class will return true if there is a valid {@link InstrumentRun} with at least one
 * {@link InstrumentRunValue} otherwise it returns false. It returns null if the stage is not complete.
 * 
 */
public class JadeStageDependencyCondition implements StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private InstrumentRunService instrumentRunService;

  private InstrumentService instrumentService;

  private String stageName;

  public JadeStageDependencyCondition() {
  }

  public JadeStageDependencyCondition(String name) {
    this.stageName = name;
  }

  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    IStageExecution stageExecution = activeInterviewService.getStageExecution(stageName);

    if(!stageExecution.isCompleted()) {
      return null;
    } else {
      InstrumentRun instrumentRun = instrumentRunService.getLastCompletedInstrumentRun(activeInterviewService.getParticipant(), instrumentService.getInstrumentType(stageName));

      if(instrumentRun != null && instrumentRun.getInstrumentRunValues().size() > 0) {
        return true;
      } else {
        return false;
      }
    }
  }

  public boolean isDependentOn(String stageName) {
    return this.stageName.equals(stageName);
  }

  public String getStageName() {
    return stageName;
  }

  public void setStageName(String stageName) {
    this.stageName = stageName;
  }

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + ":" + stageName + "]";
  }
}
