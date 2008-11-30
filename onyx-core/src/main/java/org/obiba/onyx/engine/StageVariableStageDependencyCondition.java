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
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class StageVariableStageDependencyCondition implements StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(StageVariableStageDependencyCondition.class);

  private String stageName;

  private String variableName;

  public StageVariableStageDependencyCondition() {
  }

  public StageVariableStageDependencyCondition(String name) {
    this.stageName = name;
  }

  /**
   * Returns a Boolean depending on the fact that the step is completed and also on its result. Returns null if
   * dependent stage is not completed, other the method returns the value of the dependent stage's data.
   */
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    IStageExecution stage = activeInterviewService.getStageExecution(stageName);
    if(stage != null) {
      if(stage.isCompleted() == false) {
        return null;
      } else {
        Data consentData = stage.getData(variableName);
        if(consentData != null) {
          return consentData.getValue();
        } else
          return false;
      }
    }
    return null;
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

  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  public String getVariableName() {
    return variableName;
  }

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + ":" + stageName + "." + variableName + "]";
  }

}
