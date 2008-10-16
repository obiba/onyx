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
 * Base class for Stage Dependency Conditions.
 * @author acarey
 * 
 */

public abstract class StageDependencyCondition {

  /**
   * Returns true if dependency is satisfied, false if it is not,
   * null if it's impossible to know whether it's right or wrong (step not done yet)
   * @param activeInterviewService
   * @return
   */
  public abstract Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService);

  /**
   * returns true if this stageDependencyCondition depends on the specified stage 
   * @param stageName
   * @return
   */
  public abstract boolean isDependentOn(String stageName);

}
