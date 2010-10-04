/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.action.panel;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionAscendingComparator;
import org.obiba.onyx.wicket.model.SpringDetachableModel;

/**
 * Returns a {@code List<Action>} object representing the interview log for the current participant. By default the
 * entire interview log for all stages will be returned. The log may be filtered for a particular stage by calling
 * {@code setStageName(String stageName)}. Calling {@code clearStageName()} will cause the complete list to be
 * retrieved. The returned values are sorted in ascending order.
 */
public class LoadableInterviewLogModel extends SpringDetachableModel {

  private static final long serialVersionUID = 1L;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  private String stageName;

  private boolean commentsOnly;

  @Override
  protected Object load() {
    List<Action> interviewLogList;
    if(stageName != null) {
      if(commentsOnly()) {
        interviewLogList = activeInterviewService.getInterviewComments(stageName); // Get stage comments.
      } else {
        interviewLogList = activeInterviewService.getInterviewActions(stageName); // Get stage actions.
      }
    } else {
      if(commentsOnly()) {
        interviewLogList = activeInterviewService.getInterviewComments(); // Get all comments.
      } else {
        interviewLogList = activeInterviewService.getInterviewActions(); // Get all actions.
      }
    }

    Collections.sort(interviewLogList, new ActionAscendingComparator());
    return interviewLogList;
  }

  /**
   * Sets the stage name. The interview log returned will include log entries only for the specified stage name.
   * @param stageName return interview log entries only for this stage name.
   */
  public void setStage(String stageName) {
    this.stageName = stageName;
  }

  /**
   * Clears the stage name. The InterviewLog returned will include log entries for all stages.
   */
  public void showAllLogEntries() {
    this.stageName = null;
  }

  public void setCommentsOnly(boolean commentsOnly) {
    this.commentsOnly = commentsOnly;
  }

  public boolean commentsOnly() {
    return this.commentsOnly;
  }
}
