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
public class ConsentStageDependencyCondition implements StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(ConsentStageDependencyCondition.class);

  private String stageName;

  public ConsentStageDependencyCondition() {
  }

  public ConsentStageDependencyCondition(String name) {
    this.stageName = name;
  }

  /**
   * Returns a Boolean depending on the fact that the step is completed and also on its result Null if not completed
   * True if completed and consented False if completed and not consented
   */
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    IStageExecution consent = activeInterviewService.getStageExecution(stageName);
    if(consent != null) {
      if(!consent.isCompleted()) return null;
      else {
        Data consentData = consent.getData("Consent");
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

  @Override
  public String toString() {
    return "[" + getClass().getSimpleName() + ":" + stageName + "]";
  }

}
