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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.reusable.Dialog;
import org.obiba.onyx.core.reusable.DialogBuilder;
import org.obiba.onyx.core.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.core.reusable.Dialog.Status;
import org.obiba.onyx.core.reusable.Dialog.WindowClosedCallback;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.webapp.participant.page.InterviewPage;
import org.obiba.onyx.webapp.participant.panel.AddCommentPanel;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.util.DateModelUtils;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.springframework.context.support.DefaultMessageSourceResolvable;

/**
 * A list of actions (a log) associated with a particular interview. Provides the ability to filter by {@code Stage} as
 * well as a handler to add a new general comment.
 */
public class InterviewLogPanel extends Panel {

  private static final long serialVersionUID = -168983008420797758L;

  /** The {@code Dialog} holding this {@code Panel}. */
  private Dialog modalWindow;

  /** Component to hold the list of log items. */
  private Loop logItemLoop;

  /** The {@code Dialog} used to add a general comment. */
  private Dialog addCommentDialog;

  /** Reference allows us to update the comment count on this page when a new comment is added. */
  private InterviewPage interviewPage;

  /** Log items will be displayed for this stage. May be null when displaying items for all Stages. */
  private String stageName = null;

  /** Set to true to display all log items. */
  private boolean showAll = false;

  private List<Action> interviewLogList;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ModuleRegistry moduleRegistry;

  public InterviewLogPanel(String id) {
    super(id);
    addTableTitleComponents();

    addInterviewLogComponent();
  }

  private void addTableTitleComponents() {
    add(new Label("date-title", new StringResourceModel("Date", this, null)));
    add(new Label("stage-title", new StringResourceModel("Stage", this, null)));
    add(new Label("action-title", new StringResourceModel("Action", this, null)));
    add(new Label("author-title", new StringResourceModel("Author", this, null)));
    add(new Label("reason-title", new StringResourceModel("Reason", this, null)));
  }

  public void addInterviewLogComponent() {

    getFilteredSortedInterviewLogList();

    if(logItemLoop != null) {
      remove(logItemLoop);
    }
    logItemLoop = new Loop("table", interviewLogList.size()) {

      private static final long serialVersionUID = 5173436167390888581L;

      @Override
      protected void populateItem(LoopItem item) {
        if(interviewLogList.get(item.getIteration()).getComment() != null) {
          item.add(new CommentFragment("rows", "commentRow", InterviewLogPanel.this, item.getIteration()));
        } else {
          item.add(new RowFragment("rows", "tableRow", InterviewLogPanel.this, item.getIteration()));
        }
      }

    };

    addOrReplace(logItemLoop);
  }

  private void getFilteredSortedInterviewLogList() {

    interviewLogList = activeInterviewService.getInterviewActions();
    filterOut();

    Collections.sort(interviewLogList, new Comparator<Action>() {

      public int compare(Action o1, Action o2) {
        return o1.getDateTime().compareTo(o2.getDateTime());
      }

    });
  }

  /**
   * Filters out the comments not for this stage
   */
  private void filterOut() {
    if(stageName != null && !showAll) {
      List<Action> comments = new ArrayList<Action>();

      for(Action comment : interviewLogList) {
        if(stageName.equals(comment.getStage())) {
          comments.add(comment);
        }
      }
      interviewLogList = comments;
    }
  }

  public void setup(final Dialog modalWindow) {
    this.modalWindow = modalWindow;
    addDialogActionButtons(modalWindow);

    add(addCommentDialog = createAddCommentDialog());
  }

  public void setInterviewPage(InterviewPage interviewPage) {
    this.interviewPage = interviewPage;
  }

  public void setStageName(String stageName) {
    this.stageName = stageName;
    showStage();
    addInterviewLogComponent();
  }

  public class RowFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public RowFragment(String id, String markupId, MarkupContainer markupContainer, int iteration) {
      super(id, markupId, markupContainer);
      Action action = interviewLogList.get(iteration);
      WebMarkupContainer webMarkupContainer = new WebMarkupContainer("tableRowClass");
      add(webMarkupContainer);
      webMarkupContainer.add(new AttributeAppender("class", true, new Model(getOddEvenCssClass(iteration)), " "));

      webMarkupContainer = addCommonFragmentComponents(webMarkupContainer, action);
    }
  }

  public class CommentFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public CommentFragment(String id, String markupId, MarkupContainer markupContainer, int iteration) {
      super(id, markupId, markupContainer);
      Action action = interviewLogList.get(iteration);
      WebMarkupContainer webMarkupContainer = new WebMarkupContainer("commentRowClass");
      add(webMarkupContainer);
      webMarkupContainer.add(new AttributeAppender("class", true, new Model(getOddEvenCssClass(iteration)), " "));

      webMarkupContainer = addCommonFragmentComponents(webMarkupContainer, action);

      WebMarkupContainer webMarkupContainerTwo = new WebMarkupContainer("commentRowClassTwo");
      add(webMarkupContainerTwo);
      webMarkupContainerTwo.add(new AttributeAppender("class", true, new Model(getOddEvenCssClass(iteration)), " "));
      webMarkupContainerTwo.add(new Label("comment", action.getComment()));

      ContextImage commentIcon = new ContextImage("commentIcon", new Model("icons/note.png"));
      commentIcon.setMarkupId("commentIcon");
      commentIcon.setOutputMarkupId(true);
      webMarkupContainerTwo.add(commentIcon);

    }
  }

  private String getOddEvenCssClass(int row) {
    return row % 2 == 1 ? "odd" : "even";
  }

  private WebMarkupContainer addCommonFragmentComponents(WebMarkupContainer webMarkupContainer, Action action) {
    webMarkupContainer.add(new Label("dateTime", DateModelUtils.getDateTimeModel(new PropertyModel(action, "dateTime"))));
    webMarkupContainer.add(new Label("stage", getStageModel(action)));
    webMarkupContainer.add(new Label("action", getActionModel(action)));
    webMarkupContainer.add(new Label("author", action.getUser().getFullName()));
    webMarkupContainer.add(new Label("reason", getReasonModel(action)));
    return webMarkupContainer;
  }

  private IModel getStageModel(Action action) {
    IModel stageModel;
    if(action.getStage() != null) {
      stageModel = new PropertyModel(new StageModel(moduleRegistry, action.getStage()), "description");
      if(stageModel != null && stageModel.getObject() != null) {
        return new MessageSourceResolvableStringModel(stageModel);
      }
    }
    return new StringResourceModel("GeneralComment", this, null);
  }

  private IModel getActionModel(Action action) {
    IModel actionModel;
    if(action.getActionType() != null) {
      actionModel = new MessageSourceResolvableStringModel(new DefaultMessageSourceResolvable("action." + action.getActionType()));
    } else {
      actionModel = new StringResourceModel("GeneralComment", this, null);
    }
    return actionModel;
  }

  private IModel getReasonModel(Action action) {
    IModel reasonModel;
    if(action.getEventReason() != null && !action.getEventReason().equals("")) {
      reasonModel = new MessageSourceResolvableStringModel(new DefaultMessageSourceResolvable(action.getEventReason()));
    } else {
      reasonModel = new Model("");
    }
    return reasonModel;
  }

  private void showAll() {
    showAll = true;
  }

  private void showStage() {
    showAll = false;
  }

  @SuppressWarnings("serial")
  private void addDialogActionButtons(final Dialog modalWindow) {

    modalWindow.setCloseButtonCallback(new CloseButtonCallback() {

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {
        if(status.equals(Status.WINDOW_CLOSED)) {
          return true;
        } else if(status.equals(Status.YES)) {
          // Redraw, but show all log entries.
          showAll();
          addInterviewLogComponent();
          target.addComponent(InterviewLogPanel.this);
          modalWindow.resetStatus();
          return false;
        } else if(status.equals(Status.NO)) {
          // Add a new comment.
          addCommentDialog.show(target);
          return false;
        }
        return true;
      }

    });

  }

  private Dialog createAddCommentDialog() {
    final AddCommentPanel dialogContent = new AddCommentPanel("content");
    dialogContent.add(new AttributeModifier("class", true, new Model("obiba-content add-comment-panel-content")));

    DialogBuilder builder = DialogBuilder.buildDialog("addCommentDialog", new ResourceModel("AddComment"), dialogContent);
    builder.setOptions(Dialog.Option.OK_CANCEL_OPTION);

    Dialog dialog = builder.getDialog();
    dialog.setInitialHeight(420);
    dialog.setInitialWidth(375);

    dialog.setWindowClosedCallback(new WindowClosedCallback() {

      private static final long serialVersionUID = 1L;

      public void onClose(AjaxRequestTarget target, Status status) {
        if(status.equals(Status.WINDOW_CLOSED) || status.equals(Status.SUCCESS) || status.equals(Status.CANCELLED)) {
          dialogContent.clearCommentField();
        }

        modalWindow.resetStatus();
      }

    });

    dialog.setCloseButtonCallback(new CloseButtonCallback() {

      private static final long serialVersionUID = -2156016038938151812L;

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {
        if(status.equals(Status.SUCCESS)) {
          Action comment = (Action) dialogContent.getModelObject();
          activeInterviewService.doAction(null, comment);
          interviewPage.updateCommentCount(target);
          InterviewLogPanel.this.showAll();
          InterviewLogPanel.this.addInterviewLogComponent();
          target.addComponent(InterviewLogPanel.this);
          // Scroll to bottom of log to make the recently added comment visible.
          target.appendJavascript("$('#interviewLogPanel').attr({ scrollTop: $('#interviewLogPanel').attr('scrollHeight') });");
        }

        return true;
      }

    });

    return dialog;
  }
}
