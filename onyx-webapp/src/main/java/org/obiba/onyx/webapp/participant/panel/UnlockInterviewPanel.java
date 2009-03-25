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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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

    AjaxLink noLink = new AjaxLink("no") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        ModalWindow.closeCurrent(target);
      }
    };
    add(noLink);

    AjaxLink cancelLink = new AjaxLink("cancel") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        ModalWindow.closeCurrent(target);
      }
    };
    add(cancelLink);

    AjaxLink yesLink = new AjaxLink("yes") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        interviewManager.overrideInterview(getParticipant());
        setResponsePage(InterviewPage.class);
      }
    };
    add(yesLink);
  }

  public Participant getParticipant() {
    return (Participant) getModelObject();
  }

  public User getInterviewer() {
    return interviewManager.getInterviewer(getParticipant());
  }

}
