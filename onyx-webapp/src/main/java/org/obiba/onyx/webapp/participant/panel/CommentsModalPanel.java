package org.obiba.onyx.webapp.participant.panel;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.util.DateModelUtils;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;

public abstract class CommentsModalPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  private ModalWindow commentsWindow;

  private FeedbackPanel feedback;

  private List<Action> commentList;

  WebMarkupContainer previousComments;

  public CommentsModalPanel(final ModalWindow commentsWindow) {

    super("content");
    this.commentsWindow = commentsWindow;
    commentList = activeInterviewService.getInterviewComments();
    setOutputMarkupId(true);

    add(new ParticipantPanel("participant", activeInterviewService.getParticipant(), true));
    add(new CommentForm("commentForm"));

    add(feedback = new FeedbackPanel("feedback"));
    feedback.setOutputMarkupId(true);

    // The WebMarkupContainer is needed to allow the DataView update through Ajax. The DataView cannot be update directly.
    add(previousComments = new WebMarkupContainer("previousComments"));
    previousComments.add(new CommentsDataView("comment-list", new CommentsDataProvider()));
    previousComments.setOutputMarkupId(true);

    // No comment message is only visible when there is no comment for the current interview.
    previousComments.add(new Label("noComments", new StringResourceModel("NoComments", this, null)) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isVisible() {
        if(commentList.size() == 0) {
          return true;
        }
        return false;
      }
    });

    commentsWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {

      private static final long serialVersionUID = 1L;

      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        return true;
      }
    });

  }

  public abstract void onAddComments(AjaxRequestTarget target);

  private class CommentForm extends Form {

    private static final long serialVersionUID = 1L;

    public CommentForm(String id) {
      super(id);

      setModel(new Model(new Action()));

      final TextArea newComment = new TextArea("newComment", new PropertyModel(getModel(), "comment"));
      newComment.add(new RequiredFormFieldBehavior());
      newComment.setOutputMarkupId(true);
      add(newComment);

      // Save a new comment.
      add(new AjaxButton("saveComment", this) {

        private static final long serialVersionUID = 1L;

        protected void onSubmit(AjaxRequestTarget target, Form form) {

          // Add new comment to interview.
          Action comment = (Action) CommentForm.this.getModelObject();
          comment.setActionType(ActionType.COMMENT);
          activeInterviewService.doAction(null, comment, activeInterviewService.getInterview().getUser());
          CommentsModalPanel.this.onAddComments(target);

          // Refresh previous comments list.
          commentList = activeInterviewService.getInterviewComments();
          target.addComponent(previousComments);

          // Reset new comment form.
          CommentForm.this.getModel().setObject(new Action());
          target.addComponent(newComment);

          // Display a message confirming that the comment was saved.
          info(new StringResourceModel("NewCommentAddedConfirmation", this, null, new Object[] { DateUtils.getDateTimeModel(new Model(new Date(System.currentTimeMillis()))) }).getString());
          target.addComponent(feedback);
        }

        protected void onError(AjaxRequestTarget target, Form form) {
          target.addComponent(feedback);
        }

      });

      add(new AjaxLink("cancelComment") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          commentsWindow.close(target);
        }

      });

    }

  }

  private class CommentsDataView extends DataView {

    private static final long serialVersionUID = 1L;

    public CommentsDataView(String id, IDataProvider dataProvider) {
      super(id, dataProvider);
    }

    @Override
    protected void populateItem(Item item) {
      Action comment = (Action) item.getModelObject();

      KeyValueDataPanel kvPanel = new KeyValueDataPanel("comment-panel");
      kvPanel.addRow(new StringResourceModel("CommentTime", this, null), DateModelUtils.getDateTimeModel(new PropertyModel(comment, "dateTime")));
      IModel stageModel;
      if(comment.getStage() != null) {
        stageModel = new PropertyModel(comment, "stage.description");
      } else {
        stageModel = new StringResourceModel("GeneralComment", this, null);
      }
      kvPanel.addRow(new StringResourceModel("Stage", this, null), stageModel);
      User currentUser = comment.getUser();
      kvPanel.addRow(new StringResourceModel("MadeBy", this, null), new Label(KeyValueDataPanel.getRowValueId(), currentUser.getFirstName() + " " + currentUser.getLastName()));
      kvPanel.addRow(new StringResourceModel("Comment", this, null), new MultiLineLabel(KeyValueDataPanel.getRowValueId(), new PropertyModel(comment, "comment")));
      item.add(kvPanel);

    }

  }

  private class CommentsDataProvider implements IDataProvider {

    private static final long serialVersionUID = 1L;

    Action template;

    public Iterator iterator(int first, int count) {
      return commentList.iterator();
    }

    public IModel model(Object object) {
      return new Model((Action) object);
    }

    public int size() {
      return commentList.size();
    }

    public void detach() {
    }

  }

}
