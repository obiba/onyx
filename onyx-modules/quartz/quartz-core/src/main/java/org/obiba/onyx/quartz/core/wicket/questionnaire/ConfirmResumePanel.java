/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.questionnaire;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.core.service.ActiveInterviewService;

public class ConfirmResumePanel extends Panel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean(name = "activeInterviewService")
  private transient ActiveInterviewService activeInterviewService;

  //
  // Constructors
  //

  @SuppressWarnings("serial")
  public ConfirmResumePanel(String id, String questionNumberToResumeAt) {
    super(id);

    add(new Label("participant", activeInterviewService.getParticipant().getFullName()));
    add(new Label("user", activeInterviewService.getInterview().getUser().getFullName()));
    add(new Label("interviewInProgress", new StringResourceModel("InterviewInProgress", this, null)));

    if(questionNumberToResumeAt != null) {
      add(new Label("interviewWillResumeAt", new StringResourceModel("InterviewWillResumeAt", this, null)));
      add(new Label("questionToResumeAt", new StringResourceModel("QuestionToResumeAt", this, new Model(new ValueMap("questionNumber=" + questionNumberToResumeAt)))));
    } else {
      add(new Label("interviewWillResumeAt", new StringResourceModel("InterviewWillResumeAtPointOfInterruption", this, null)));
      add(new Label("questionToResumeAt", ""));
    }
  }
}