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

import java.util.Map;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.engine.state.StageExecutionContext;

/**
 *
 */
public interface InterviewManager {

  public Participant getInterviewedParticipant();

  public Map<String, StageExecutionContext> getStageContexts();

  public boolean isInterviewAvailable(Participant participant);

  public User getInterviewer(Participant participant);

  public Interview obtainInterview(Participant participant);

  public void releaseInterview();

  public void releaseInterview(String sessionId);

  public Interview overrideInterview(Participant participant);

}
