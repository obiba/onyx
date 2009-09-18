/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import java.util.List;

import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.stage.StageTransition;
import org.obiba.onyx.core.service.InterviewService;
import org.obiba.onyx.engine.Stage;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link org.obiba.onyx.core.service.InterviewService InterviewService}.
 */
@Transactional
public class DefaultInterviewServiceImpl extends PersistenceManagerAwareService implements InterviewService {
  //
  // InterviewService Methods
  //

  public List<StageTransition> getStageTransitions(Interview interview, Stage stage) {
    StageTransition template = new StageTransition();
    template.setInterview(interview);
    template.setStage(stage.getName());

    List<StageTransition> stageTransitions = getPersistenceManager().match(template, new SortingClause("action.dateTime"));

    return stageTransitions;
  }
}
