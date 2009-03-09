/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.InterviewManager;
import org.obiba.onyx.webapp.participant.page.InterviewPage;

/**
 *
 */
public class UnlockInterviewPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  InterviewManager interviewManager;

  /**
   * @param id
   */
  public UnlockInterviewPanel(String id, IModel participant) {
    super(id, participant);

    add(new MultiLineLabel("confirm", new StringResourceModel("ConfirmUnlockInterview", this, new Model(this))));

    AjaxLink cancelLink = new AjaxLink("cancel") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        onCancel(target);
      }
    };
    add(cancelLink);

    AjaxLink okLink = new AjaxLink("ok") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        interviewManager.overrideInterview(getParticipant());
        setResponsePage(InterviewPage.class);
      }
    };
    add(okLink);
  }

  public void onCancel(AjaxRequestTarget target) {

  }

  public Participant getParticipant() {
    return (Participant) getModelObject();
  }

  public User getInterviewer() {
    return interviewManager.getInterviewer(getParticipant());
  }

}
