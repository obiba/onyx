/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.state.IStageExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple {@link StageDependencyCondition} for making a {@link Stage} a prerequisite of another.
 * <p>
 * Instances of this class are configured by setting the {@link #stageName} attribute to the pre-requisite stage.
 * <p>
 * The {@link #isDependencySatisfied(ActiveInterviewService)} method returns true when the pre-requisite stage is
 * complete. Otherwise null is returned. Note that this implementation never returns false.
 * @see IStageExecution#isCompleted()
 */
public class PreviousStageDependencyCondition implements StageDependencyCondition {

  private static final Logger log = LoggerFactory.getLogger(PreviousStageDependencyCondition.class);

  private String stageName;

  public PreviousStageDependencyCondition() {
  }

  public PreviousStageDependencyCondition(String name) {
    this.stageName = name;
  }

  public Boolean isDependencySatisfied(Stage stage, ActiveInterviewService activeInterviewService) {
    log.info("stageName={}", stageName);
    IStageExecution stageExecution = activeInterviewService.getStageExecution(stageName);
    if(stageExecution == null) {
      log.warn("Stage '{}' does not seem to be present. Dependent stages will consider it as 'complete'. Make sure your configuration files are using the correct stage names in their dependency declarations.", stageName);
      return true;
    }

    if(stageExecution.isInteractive()) {
      // ONYX-383 if slave is completed, it means that dependencies were already satisfied
      // then ignore the transition of the master that is being modified
      // and wait for the master to be in a stable state
      IStageExecution stageSlave = activeInterviewService.getStageExecution(stage);
      if(stageSlave.isCompleted()) {
        return true;
      } else {
        return null;
      }
    } else if(!stageExecution.isCompleted()) {
      return null;
    } else {
      return true;
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
