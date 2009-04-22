/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.home.page;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.reusable.Dialog;
import org.obiba.onyx.core.reusable.Dialog.Status;
import org.obiba.onyx.core.service.InterviewManager;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.page.InterviewPage;
import org.obiba.onyx.webapp.participant.panel.UnlockInterviewPanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;

@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class HomePage extends BasePage {

  @SpringBean
  private ParticipantService participantService;

  @SpringBean
  private InterviewManager interviewManager;

  @SpringBean
  private UserSessionService userSessionService;

  private Dialog unlockInterviewWindow;

  private UnlockInterviewPanel content;

  private static final int DEFAULT_INITIAL_HEIGHT = 146;

  private static final int DEFAULT_INITIAL_WIDTH = 400;

  public HomePage() {
    super();

    add(new ParticipantSearchForm("searchForm"));

    unlockInterviewWindow = new Dialog("unlockInterview");
    unlockInterviewWindow.setTitle(new ResourceModel("UnlockInterview"));
    unlockInterviewWindow.setOptions(Dialog.Option.YES_NO_CANCEL_OPTION);
    unlockInterviewWindow.setInitialHeight(DEFAULT_INITIAL_HEIGHT);
    unlockInterviewWindow.setInitialWidth(DEFAULT_INITIAL_WIDTH);

    unlockInterviewWindow.setCloseButtonCallback(new Dialog.CloseButtonCallback() {
      private static final long serialVersionUID = 1L;

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {

        if(status != null && status.equals(Dialog.Status.YES)) {
          interviewManager.overrideInterview(content.getParticipant());
          setResponsePage(InterviewPage.class);
        }

        return true;
      }
    });

    add(unlockInterviewWindow);
  }

  private class ParticipantSearchForm extends Form {

    private static final long serialVersionUID = 1L;

    public ParticipantSearchForm(String id) {
      super(id);

      setModel(new Model(new Participant()));

      TextField barCode = new TextField("barCode", new PropertyModel(getModel(), "barcode"));
      barCode.add(new RequiredFormFieldBehavior());
      barCode.setLabel(new StringResourceModel("ParticipantCode", HomePage.this, null));
      add(barCode);

      add(new AjaxButton("submit") {

        private static final long serialVersionUID = 1L;

        @Override
        protected void onSubmit(AjaxRequestTarget target, Form form) {
          Participant participant = getParticipant();
          // Participant found, display interview page.
          if(participant != null) {
            if(interviewManager.isInterviewAvailable(participant)) {
              interviewManager.obtainInterview(participant);
              setResponsePage(InterviewPage.class);
            }
            content = new UnlockInterviewPanel(unlockInterviewWindow.getContentId(), new PropertyModel(ParticipantSearchForm.this, "participant"));
            content.add(new AttributeModifier("class", true, new Model("obiba-content unlockInterview-panel-content")));
            unlockInterviewWindow.setContent(content);
            target.appendJavascript("Wicket.Window.unloadConfirmation = false;");

            if(userSessionService.getUser().getRoles().contains(Role.PARTICIPANT_MANAGER)) {
              unlockInterviewWindow.show(target);
            } else {
              error((new StringResourceModel("InterviewLocked", this, ParticipantSearchForm.this.getModel())).getString());
              getFeedbackWindow().setContent(new FeedbackPanel("content"));
              getFeedbackWindow().show(target);
            }
          } else {
            // Not found, display error message in feedback panel.
            error((new StringResourceModel("ParticipantNotFound", this, ParticipantSearchForm.this.getModel())).getString());
            getFeedbackWindow().setContent(new FeedbackPanel("content"));
            getFeedbackWindow().show(target);
          }
        }
      });

    }

    public Participant getParticipant() {
      Participant template = (Participant) ParticipantSearchForm.this.getModelObject();
      return participantService.getParticipant(template);
    }
  }

}
