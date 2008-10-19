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

  private String stageName;

  public PreviousStageDependencyCondition() {
  }

  public PreviousStageDependencyCondition(String name) {
    this.stageName = name;
  }

  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    if(!activeInterviewService.getStageExecution(stageName).isCompleted()) return null;
    else
      return true;
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
}
