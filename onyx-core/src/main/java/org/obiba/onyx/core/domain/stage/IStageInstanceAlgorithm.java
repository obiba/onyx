/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.stage;

import java.util.List;

/**
 * Algorithm that derives a list of {@link StageInstance}s from a given list of {@link StageTransition}s.
 */
public interface IStageInstanceAlgorithm {

  /**
   * Returns the list {@link StageInstance}s corresponding to the given list of {@link StageTransition}s.
   * 
   * The list is returned in chronological order (earliest first).
   * 
   * @param stageTransitions list of stage transitions
   * @return list of stage instances
   */
  public List<StageInstance> getStageInstances(List<StageTransition> stageTransitions);
}
