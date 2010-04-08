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
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.exception.NonUniqueParticipantException;
import org.obiba.onyx.core.service.InterviewManager;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.page.InterviewPage;
import org.obiba.onyx.webapp.participant.page.ParticipantReceptionPage;
import org.obiba.onyx.webapp.participant.panel.UnlockInterviewPanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class HomePage extends BasePage {

  @SpringBean
  private ParticipantService participantService;

  @SpringBean
  private InterviewManager interviewManager;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  @SpringBean
  private EntityQueryService queryService;

  private Dialog unlockInterviewWindow;

  private UnlockInterviewPanel content;

  private static final int DEFAULT_INITIAL_HEIGHT = 13;

  private static final int DEFAULT_INITIAL_WIDTH = 34;

  public HomePage() {
    super();

    add(new ParticipantSearchForm("searchForm"));

    unlockInterviewWindow = new Dialog("unlockInterview");
    unlockInterviewWindow.setTitle(new ResourceModel("UnlockInterview"));
    unlockInterviewWindow.setOptions(Dialog.Option.YES_NO_CANCEL_OPTION);
    unlockInterviewWindow.setHeightUnit("em");
    unlockInterviewWindow.setWidthUnit("em");
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

    private String participantIdentifier;

    public ParticipantSearchForm(String id) {
      super(id);

      setModel(new Model(new Participant()));

      final TextField participantIdentifier = new TextField("participantIdentifier", new PropertyModel(ParticipantSearchForm.this, "participantIdentifier"));
      participantIdentifier.add(new RequiredFormFieldBehavior());
      participantIdentifier.setLabel(new StringResourceModel("ParticipantCode", HomePage.this, null));
      add(participantIdentifier);

      add(new AjaxButton("submit") {

        private static final long serialVersionUID = 1L;

        @Override
        protected void onSubmit(AjaxRequestTarget target, Form form) {

          Participant participant = null;
          try {

            // Lookup for a Participant matching the identifier.
            participant = participantService.getParticipant(getParticipantIdentifier());

            // One Participant was found.
            if(participant != null) {

              // Redirect to the reception page if the matched Participant has not been received yet (no barcode).
              if(participant.getBarcode() == null) {
                setParticipantIdentifier(null);
                setResponsePage(new ParticipantReceptionPage(new DetachableEntityModel(queryService, participant), HomePage.this));

                // If the Participant has been received (barcode exist), display the interview page.
              } else {
                displayInterviewPage(target, participant);
              }

              // No Participant was found for the specified identifier, display error message in feedback panel.
            } else {
              error((new StringResourceModel("ParticipantNotFound", this, new Model(ParticipantSearchForm.this))).getString());
              getFeedbackWindow().setContent(new FeedbackPanel("content"));
              getFeedbackWindow().show(target);
            }

            // Multiple participants were found for the specified identifier
            // (case where ParticipantA.barcode == PArticipantB.enrollmentId).
            // Display an error message informing the user of that particular situation.
          } catch(NonUniqueParticipantException e) {
            error((new StringResourceModel("MultipleParticipantWereFound", this, new Model(ParticipantSearchForm.this))).getString());
            getFeedbackWindow().setContent(new FeedbackPanel("content"));
            getFeedbackWindow().show(target);
          }
        }

        private void displayInterviewPage(AjaxRequestTarget target, Participant participant) {
          if(interviewManager.isInterviewAvailable(participant)) {
            interviewManager.obtainInterview(participant);
            setResponsePage(InterviewPage.class);
          } else {
            content = new UnlockInterviewPanel(unlockInterviewWindow.getContentId(), new DetachableEntityModel(queryService, participant));
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
          }
        }
      });

    }

    public String getParticipantIdentifier() {
      return participantIdentifier;
    }

    public void setParticipantIdentifier(String participantIdentifier) {
      this.participantIdentifier = participantIdentifier;
    }

  }

}
