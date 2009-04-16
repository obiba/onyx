/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.page;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.reusable.Dialog;
import org.obiba.onyx.core.reusable.DialogBuilder;
import org.obiba.onyx.core.reusable.Dialog.Status;
import org.obiba.onyx.core.reusable.Dialog.WindowClosedCallback;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionDefinitionConfiguration;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.panel.AddCommentPanel;
import org.obiba.onyx.webapp.participant.panel.CommentsModalPanel;
import org.obiba.onyx.webapp.participant.panel.InterviewMenuBar;
import org.obiba.onyx.webapp.participant.panel.ParticipantPanel;
import org.obiba.onyx.webapp.stage.panel.StageSelectionPanel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.util.DateModelUtils;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;

@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class InterviewPage extends BasePage {

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActionDefinitionConfiguration actionDefinitionConfiguration;

  @SpringBean
  private EntityQueryService queryService;

  private Dialog addCommentDialog;

  private Label commentsCount;

  AjaxLink viewComments;

  public InterviewPage() {
    super();

    if(activeInterviewService.getParticipant() == null || activeInterviewService.getInterview() == null) {
      setResponsePage(WebApplication.get().getHomePage());
    } else {

      addOrReplace(new EmptyPanel("header"));
      //
      // Modify menu bar.
      //
      remove("menuBar");
      add(new InterviewMenuBar("menuBar"));

      Participant participant = activeInterviewService.getParticipant();

      add(new ParticipantPanel("participant", new DetachableEntityModel(queryService, participant), true));

      final Dialog interviewLogsDialog = DialogBuilder.buildInfoDialog("interviewLogsDialog", "Interview Logs", new Label("content", "Interview logs not implemented yet")).getDialog();
      interviewLogsDialog.setInitialHeight(100);
      interviewLogsDialog.setInitialHeight(100);
      add(interviewLogsDialog);

      // Create modal comments window
      final ModalWindow commentsWindow;
      add(commentsWindow = new ModalWindow("addCommentsModal"));
      commentsWindow.setCssClassName("onyx");
      commentsWindow.setTitle(new StringResourceModel("CommentsWindow", this, null));
      commentsWindow.setInitialHeight(400);
      commentsWindow.setInitialWidth(600);

      commentsWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {

        private static final long serialVersionUID = 1L;

        public void onClose(AjaxRequestTarget target) {

        }
      });

      CommentsLink viewCommentsIconLink = new CommentsLink("viewCommentsIconLink", commentsWindow);
      viewCommentsIconLink.setMarkupId("viewCommentsIconLink");
      viewCommentsIconLink.setOutputMarkupId(true);
      add(viewCommentsIconLink);

      ContextImage commentIcon = new ContextImage("commentIcon", new Model("icons/note.png"));
      commentIcon.setMarkupId("commentIcon");
      commentIcon.setOutputMarkupId(true);
      viewCommentsIconLink.add(commentIcon);

      // Add view interview comments action
      add(viewComments = new CommentsLink("viewComments", commentsWindow));

      // Add create interview comments action
      add(addCommentDialog = createAddCommentDialog());
      add(new AjaxLink("addComment") {
        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          addCommentDialog.show(target);
        }
      });

      // Initialize comments counter
      updateCommentsCount();

      ActiveInterviewModel interviewModel = new ActiveInterviewModel();

      KeyValueDataPanel kvPanel = new KeyValueDataPanel("interview");
      kvPanel.addRow(new StringResourceModel("StartDate", this, null), new PropertyModel(interviewModel, "startDate"));
      kvPanel.addRow(new StringResourceModel("EndDate", this, null), new PropertyModel(interviewModel, "endDate"));
      kvPanel.addRow(new StringResourceModel("Status", this, null), new PropertyModel(interviewModel, "status"));
      add(kvPanel);

      // Interview cancellation
      final ActionDefinition cancelInterviewDef = actionDefinitionConfiguration.getActionDefinition(ActionType.STOP, "Interview", null, null);
      final ActionWindow interviewActionWindow = new ActionWindow("modal") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action) {
          activeInterviewService.setStatus(InterviewStatus.CANCELLED);
          setResponsePage(InterviewPage.class);
        }

      };
      add(interviewActionWindow);

      AjaxLink link = new AjaxLink("cancelInterview") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          interviewActionWindow.show(target, null, cancelInterviewDef);
        }

        @Override
        public boolean isVisible() {
          return activeInterviewService.getInterview().getStatus() != InterviewStatus.CANCELLED;
        }
      };
      add(link);

      add(new StageSelectionPanel("stage-list", getFeedbackWindow()) {

        private static final long serialVersionUID = 1L;

        @Override
        public void onViewComments(AjaxRequestTarget target, String stage) {

          commentsWindow.setContent(new CommentsModalPanel("content", commentsWindow, stage) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onAddComments(AjaxRequestTarget target) {
              InterviewPage.this.updateCommentsCount();
              target.addComponent(InterviewPage.this.commentsCount);
            }

          });
          commentsWindow.show(target);
        }

        @Override
        public void onViewLogs(AjaxRequestTarget target, String stage) {
          interviewLogsDialog.show(target);
        }

        @Override
        public void onActionPerformed(AjaxRequestTarget target, Stage stage, Action action) {
          InterviewPage.this.updateCommentsCount();
          target.addComponent(InterviewPage.this.commentsCount);
        }

      });
    }
  }

  public void updateCommentsCount() {
    viewComments.addOrReplace(commentsCount = new Label("commentsCount", String.valueOf(activeInterviewService.getInterviewComments().size())));
    commentsCount.setOutputMarkupId(true);
  }

  private Dialog createAddCommentDialog() {
    final AddCommentPanel dialogContent = new AddCommentPanel("content");
    DialogBuilder builder = DialogBuilder.buildDialog("addCommentDialog", new ResourceModel("AddComment"), dialogContent);
    builder.setOptions(Dialog.Option.OK_CANCEL_OPTION);

    Dialog dialog = builder.getDialog();
    dialog.setInitialHeight(534);
    dialog.setInitialWidth(490);
    dialog.setWindowClosedCallback(new WindowClosedCallback() {

      private static final long serialVersionUID = 1L;

      public void onClose(AjaxRequestTarget target, Status status) {
        if(status.equals(Status.SUCCESS)) {
          Action comment = (Action) dialogContent.getModelObject();
          activeInterviewService.doAction(null, comment);
          InterviewPage.this.updateCommentsCount();
          target.addComponent(InterviewPage.this.commentsCount);
        } else if(status.equals(Status.ERROR)) {
          // TODO: Display in a FeedbackWindow.
          System.err.println("<CommentValidationError>");
        }
      }
    });

    return dialog;
  }

  private class CommentsLink extends AjaxLink {

    private static final long serialVersionUID = 1L;

    private final ModalWindow commentsWindow;

    private CommentsLink(String id, ModalWindow commentsWindow) {
      super(id);
      this.commentsWindow = commentsWindow;
    }

    public void onClick(AjaxRequestTarget target) {
      commentsWindow.setContent(new CommentsModalPanel("content", commentsWindow, null) {
        private static final long serialVersionUID = 1L;

        @Override
        public void onAddComments(AjaxRequestTarget target) {
          InterviewPage.this.updateCommentsCount();
          target.addComponent(InterviewPage.this.commentsCount);
        }

      });
      commentsWindow.show(target);
    }
  }

  @SuppressWarnings("serial")
  private class ActiveInterviewModel implements Serializable {

    public String getStartDate() {
      DateFormat dateTimeFormat = userSessionService.getDateTimeFormat();
      Date date = activeInterviewService.getInterview().getStartDate();

      return date == null ? "" : DateModelUtils.getDateTimeModel(new Model(dateTimeFormat), new Model(date)).getObject().toString();
    }

    public String getEndDate() {
      DateFormat dateTimeFormat = userSessionService.getDateTimeFormat();
      Date date = activeInterviewService.getInterview().getEndDate();

      return date == null ? "" : DateModelUtils.getDateTimeModel(new Model(dateTimeFormat), new Model(date)).getObject().toString();
    }

    public String getStatus() {
      Action act = activeInterviewService.getStatusAction();

      // act.getStage() == null => action is on interview and not on a stage
      if(act != null && act.getStage() == null && act.getEventReason() != null) {
        String reason = (new SpringStringResourceModel(act.getEventReason())).getString();
        ValueMap map = new ValueMap("reason=" + reason);
        return (new StringResourceModel("InterviewStatus." + activeInterviewService.getInterview().getStatus() + ".WithReason", InterviewPage.this, new Model(map))).getString();
      } else {
        return (new StringResourceModel("InterviewStatus." + activeInterviewService.getInterview().getStatus(), InterviewPage.this, null)).getString();
      }
    }
  }

}
