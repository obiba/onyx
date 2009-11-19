/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service;

import java.util.List;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.stage.StageInstance;
import org.obiba.onyx.core.domain.stage.StageTransition;
import org.obiba.onyx.engine.Stage;

/**
 * 
 */
public interface InterviewService {

  /**
   * Returns a list of all the stage transitions for a given interview and stage.
   * 
   * @param interview the interview
   * @param stage the stage
   * @return the list of stage transitions, ordered by time of occurrence (earliest first)
   */
  public List<StageTransition> getStageTransitions(Interview interview, Stage stage);

  /**
   * Returns a list of all stage instances (all stages) for a given interview.
   * 
   * @param interview the interview
   * @return the list of stage instances, ordered by time of occurrence (earliest first)
   */
  public List<StageInstance> getStageInstances(Interview interview);
}
