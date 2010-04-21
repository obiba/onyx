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

import org.obiba.onyx.core.service.InterviewManager;
import org.obiba.onyx.core.service.UserSessionService;

/**
 * This bean is responsible for releasing the currently active interview when a sessions ends (by logout or timeout).
 * This bean contains a shutdown method that will perform the interview release when this session bean is destroyed.
 */
public class ActiveInterviewReleaseBean {

  private InterviewManager interviewManager;

  private String sessionId;

  public ActiveInterviewReleaseBean(UserSessionService userSessionService) {
    this.sessionId = userSessionService.getSessionId();
  }

  public void setInterviewManager(InterviewManager interviewManager) {
    this.interviewManager = interviewManager;
  }

  public void shutdown() {
    interviewManager.releaseInterview(sessionId);
  }
}
