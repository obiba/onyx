package org.obiba.onyx.webapp.participant.panel;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;

public abstract class CommentsModalPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  private ModalWindow commentsWindow;

  private FeedbackPanel feedback;

  private List<Action> commentList;

  public CommentsModalPanel(final ModalWindow commentsWindow) {

    super("content");
    this.commentsWindow = commentsWindow;
    commentList = activeInterviewService.getInterviewComments();    

    add(new ParticipantPanel("participant", activeInterviewService.getParticipant(),true));
    add(new CommentForm("commentForm"));

    add(feedback = new FeedbackPanel("feedback"));
    feedback.setOutputMarkupId(true);
    
    add(new CommentsDataView("comment-list", new CommentsDataProvider()));

    if(commentList.size() == 0) {
      add(new Label("noComments", new StringResourceModel("NoComments", this, null)));
    } else {
      add(new Label("noComments", ""));
    }

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

      final TextArea newComment = new TextArea("newComment", new PropertyModel(getModelObject(), "comment"));
      newComment.add(new RequiredFormFieldBehavior());
      newComment.setLabel(new StringResourceModel("NewComment", CommentsModalPanel.this, null));
      add(newComment);

      add(new AjaxButton("saveComment", this) {

        private static final long serialVersionUID = 1L;

        protected void onSubmit(AjaxRequestTarget target, Form form) {
          Action comment = (Action) CommentForm.this.getModelObject();
          comment.setActionType(ActionType.COMMENT);
          activeInterviewService.doAction(null, comment, activeInterviewService.getInterview().getUser());
          CommentsModalPanel.this.onAddComments(target);
          commentsWindow.close(target);
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
      kvPanel.addRow(new StringResourceModel("CommentTime", this, null), new PropertyModel(comment, "dateTime"));
      IModel stageModel;
      if(comment.getStage() != null) {
        stageModel = new PropertyModel(comment, "stage.description");
      } else {
        stageModel = new StringResourceModel("GeneralComment", this, null);
      }
      kvPanel.addRow(new StringResourceModel("Stage", this, null), stageModel);
      kvPanel.addRow(new StringResourceModel("MadeBy", this, null), new PropertyModel(comment, "user.name"));
      kvPanel.addRow(new StringResourceModel("Comment", this, null), new MultiLineLabel( KeyValueDataPanel.getRowValueId(), new PropertyModel(comment, "comment") ));
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
