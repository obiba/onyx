/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.engine.state;

import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageDependencyCondition;
import org.obiba.onyx.engine.state.IStageExecution;

/**
 * Specific {@link StageDependencyCondition} for {@link Stage}s contributed by {@link Quartz}. For a completed stage,
 * this class returns true otherwise it returns null.
 */
public class QuartzStageDependencyCondition implements StageDependencyCondition {

  private String stageName;

  public QuartzStageDependencyCondition() {
  }

  public QuartzStageDependencyCondition(String name) {
    this.stageName = name;
  }

  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    IStageExecution stageExecution = activeInterviewService.getStageExecution(stageName);

    if(!stageExecution.isCompleted()) return null;
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
