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

/**
 * Case of Inverse Stage Dependency Condition
 * @author acarey
 */
public class InverseStageDependencyCondition extends StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private StageDependencyCondition stageDependencyCondition;

  /**
   * Returns the inverse of the specified condition
   */
  @Override
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    if (stageDependencyCondition.isDependencySatisfied(activeInterviewService) == null)
      return false;
    return (!(stageDependencyCondition.isDependencySatisfied(activeInterviewService)));
  }

  @Override
  public boolean isDependentOn(String stageName) {
    return stageDependencyCondition.isDependentOn(stageName);
  }

  public void setStageDependencyCondition(StageDependencyCondition stageDependencyCondition) {
    this.stageDependencyCondition = stageDependencyCondition;
  }
}
