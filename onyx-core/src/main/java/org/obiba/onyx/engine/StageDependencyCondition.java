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

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.state.IStageExecution;

/**
 * Defines the contract for dependencies between {@link Stage}s in an {@link Interview}. When a {@link Stage} depends on
 * another (for whatever reason), it should have an instance of {@code StageDependencyCondition} that describes this
 * dependency.
 * <p>
 * The condition can be anything from a {@link Stage} being a pre-requisite of another to actually requiring data
 * capture by a previous {@link Stage}.
 */
public interface StageDependencyCondition {

  /**
   * Returns true if dependency is satisfied, false if it is not, null if it's undetermined.
   * <p>
   * True is returned when the associated {@link Stage} may be executed. False is returned when the associated
   * {@link Stage} cannot ever be executed (another {@link Stage} has invalidated this one). Otherwise, null is returned
   * when the {@link Stage} still cannot be executed, but may at one point become executable.
   * 
   * @param stage Stage to which the dependency is applied
   * @param activeInterviewService used to obtain {@link IStageExecution} instances of dependent {@link Stage}s
   * @return true, false or null as described above.
   */
  public Boolean isDependencySatisfied(Stage stage, ActiveInterviewService activeInterviewService);

  /**
   * Returns true if this instance has a dependency on the specified {@link Stage}.
   * 
   * @param stage Stage to which the dependency is applied
   * @param stageName the name of the stage to test.
   * @return true if this instance depends on the specified stage, false otherwise.
   */
  public boolean isDependentOn(Stage stage, String stageName);

}
