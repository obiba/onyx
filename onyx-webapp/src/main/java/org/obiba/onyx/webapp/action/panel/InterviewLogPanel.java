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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.Strings;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.webapp.participant.panel.AddCommentPanel;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.behavior.ScrollToBottomBehaviour;
import org.obiba.onyx.wicket.behavior.ScrollableTableBodyBehaviour;
import org.obiba.onyx.wicket.reusable.AddCommentWindow;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.wicket.reusable.Dialog.OptionSide;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.reusable.Dialog.WindowClosedCallback;
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

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ModuleRegistry moduleRegistry;

  public InterviewLogPanel(String id, final int tableBodyHeight, IModel model) {
    super(id, model);
    add(new ScrollableTableBodyBehaviour(tableBodyHeight));

    addInterviewLogComponent();
  }

  @SuppressWarnings("unchecked")
  public void addInterviewLogComponent() {

    final List<Action> interviewLogList = (List<Action>) getModelObject();

    if(logItemLoop != null) {
      remove(logItemLoop);
    }
    logItemLoop = new Loop("table", interviewLogList.size()) {

      private static final long serialVersionUID = 5173436167390888581L;

      @Override
      protected void populateItem(LoopItem item) {
        item.setRenderBodyOnly(true);
        Action action = interviewLogList.get(item.getIteration());
        item.add(new CommentFragment("rows", "interviewLogRow", InterviewLogPanel.this, new Model(action), item.getIteration()));
      }
    };

    addOrReplace(logItemLoop);
  }

  public void setup(final Dialog modalWindow) {
    this.modalWindow = modalWindow;
    addDialogActionButtons(modalWindow);

    add(addCommentDialog = createAddCommentDialog());
  }

  public void setStageName(String stageName) {
    ((LoadableInterviewLogModel) getModel()).showLogEntriesForStage(stageName);
    addInterviewLogComponent();
  }

  public class CommentFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    public CommentFragment(String id, String markupId, MarkupContainer markupContainer, IModel model, int iteration) {
      super(id, markupId, markupContainer, model);
      setRenderBodyOnly(true);
      Action action = (Action) getModelObject();
      WebMarkupContainer webMarkupContainer = new WebMarkupContainer("logEntryRow");
      add(webMarkupContainer);
      webMarkupContainer.add(new AttributeAppender("class", true, new Model(getOddEvenCssClass(iteration)), " "));

      webMarkupContainer = addCommonFragmentComponents(webMarkupContainer, action);

      WebMarkupContainer webMarkupContainerTwo = new WebMarkupContainer("logCommentRow");
      add(webMarkupContainerTwo);
      String commentText = "";
      if(action.getComment() != null) {
        commentText = action.getComment();
      } else {
        webMarkupContainerTwo.setVisible(false);
      }
      webMarkupContainerTwo.add(new AttributeAppender("class", true, new Model(getOddEvenCssClass(iteration)), " "));
      CharSequence escapedComment = Strings.escapeMarkup(commentText);
      Label comment = new Label("comment", textToMultiLineHtml(escapedComment.toString()));
      comment.setEscapeModelStrings(false);
      comment.add(new AttributeAppender("class", true, new Model("log-comment"), " "));
      webMarkupContainerTwo.add(comment);
    }
  }

  private String textToMultiLineHtml(String text) {
    Pattern pattern = Pattern.compile("[\\r\\n]");
    Matcher matcher = pattern.matcher(text);
    String output = matcher.replaceAll("<br/>");
    return output;
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
    return new ResourceModel("GeneralComment");
  }

  private IModel getActionModel(Action action) {
    IModel actionModel;
    if(action.getActionType() != null) {
      actionModel = new MessageSourceResolvableStringModel(new DefaultMessageSourceResolvable("action." + action.getActionType()));
    } else {
      actionModel = new Model("");
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

  @SuppressWarnings("serial")
  private void addDialogActionButtons(final Dialog modalWindow) {

    modalWindow.addOption("ShowAll", OptionSide.LEFT, new AjaxLink("showAll") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        modalWindow.setStatus(Status.OTHER);
        // Redraw, but show all log entries.
        ((LoadableInterviewLogModel) InterviewLogPanel.this.getModel()).showAllLogEntries();
        addInterviewLogComponent();
        // Disable Show All Button
        target.appendJavascript("$('[name=showAll]').attr('disabled','true');$('[name=showAll]').css('color','rgba(0, 0, 0, 0.2)');$('[name=showAll]').css('border-color','rgba(0, 0, 0, 0.2)');");
        target.addComponent(InterviewLogPanel.this);
      }

    }, "showAll");

    modalWindow.addOption("Add", OptionSide.RIGHT, new AjaxLink("add") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        modalWindow.setStatus(Status.OTHER);
        addCommentDialog.show(target);
      }

    });
  }

  private AddCommentWindow createAddCommentDialog() {

    AddCommentWindow dialog = new AddCommentWindow("addCommentDialog");
    final AddCommentPanel dialogContent = new AddCommentPanel("content");
    dialog.setContent(dialogContent);

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

          // Scroll to bottom of log to make the recently added comment visible.
          InterviewLogPanel.this.add(new ScrollToBottomBehaviour("#interviewLogPanel tbody"));
          // Disable Show All Button
          target.appendJavascript("$('[name=showAll]').attr('disabled','true');$('[name=showAll]').css('color','rgba(0, 0, 0, 0.2)');$('[name=showAll]').css('border-color','rgba(0, 0, 0, 0.2)');");

          ((LoadableInterviewLogModel) InterviewLogPanel.this.getModel()).showAllLogEntries();
          InterviewLogPanel.this.addInterviewLogComponent();
          target.addComponent(InterviewLogPanel.this);
        }

        return true;
      }

    });

    return dialog;
  }

}
