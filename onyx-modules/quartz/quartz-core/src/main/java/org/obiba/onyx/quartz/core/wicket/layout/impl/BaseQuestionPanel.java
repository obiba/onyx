/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.wicket.reusable.AddCommentWindow;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.toggle.ToggleLink;

/**
 * Base class for implementing question UIs: it defines the question header including label, help, instructions, comment
 * etc and content place holder.
 */
public abstract class BaseQuestionPanel extends QuestionPanel {

  private static final long serialVersionUID = -391676180175754335L;

  private static final String BOILERPLATE_CLASS = "obiba-quartz-boilerplate";

  public BaseQuestionPanel(String id, IModel<Question> questionModel) {
    super(id, questionModel);

    setOutputMarkupId(true);

    Question question = (Question) getDefaultModelObject();
    if(question.getNumber() != null) {
      add(new Label("number", question.getNumber() + ") "));
    } else {
      add(new Label("number"));
    }
    add(new Label("label", new QuestionnaireStringResourceModel(question, "label")).setEscapeModelStrings(false));

    addCommentModalWindow(question);

    // help toggle
    QuestionnaireStringResourceModel helpModel = new QuestionnaireStringResourceModel(question, "help");
    if(helpModel.getString() != null && !helpModel.getString().trim().equals("")) {
      Label helpContent = new Label("help", helpModel);
      // help resource can contain html formatting
      helpContent.setEscapeModelStrings(false);
      add(helpContent);

      // toggle has background image defined by css
      ToggleLink toggleLink = new ToggleLink("helpToggle", new Model<String>("&nbsp;&nbsp;&nbsp;&nbsp;"), new Model<String>("&nbsp;&nbsp;&nbsp;&nbsp;"), helpContent);
      toggleLink.setLabelEscapeModelStrings(false);
      add(toggleLink);
    } else {
      // dummy content
      add(new EmptyPanel("helpToggle").setVisible(false));
      add(new EmptyPanel("help").setVisible(false));
    }

    QuestionnaireStringResourceModel stringModel = new QuestionnaireStringResourceModel(question, "instructions");
    add(new Label("instructions", stringModel).setEscapeModelStrings(false).setVisible(!isEmptyString(stringModel.getString())));

    stringModel = new QuestionnaireStringResourceModel(question, "caption");
    add(new Label("caption", stringModel).setEscapeModelStrings(false).setVisible(!isEmptyString(stringModel.getString())));

    stringModel = new QuestionnaireStringResourceModel(question, "specifications");
    add(new Label("specifications", stringModel).setEscapeModelStrings(false).setVisible(!isEmptyString(stringModel.getString())));

    // change the css rendering in case of a boiler plate
    if(question.isBoilerPlate()) {
      add(new AttributeModifier("class", new Model<String>(BOILERPLATE_CLASS)));
      add(new EmptyPanel("content").setVisible(false));
    } else {
      setContent("content");
    }

  }

  private boolean isEmptyString(String str) {
    return str == null || str.trim().equals("");
  }

  public void setCommentVisible(boolean visible) {
    get("addCommentModal").setVisible(visible);
  }

  @SuppressWarnings("serial")
  private void addCommentModalWindow(Question question) {

    // Create modal comments window
    final AddCommentWindow commentWindow;
    add(commentWindow = new AddCommentWindow("addCommentModal"));

    final IModel<String> commentWindowTitleModel = new LoadableDetachableModel<String>() {

      @SuppressWarnings("unchecked")
      @Override
      protected String load() {
        String title = (new StringResourceModel("CommentsWindow", BaseQuestionPanel.this, null)).getString() + " - " + new QuestionnaireStringResourceModel((IModel<Question>) BaseQuestionPanel.this.getDefaultModel(), "label").getString();

        // Question label is truncated if too long for Modal Window title bar.
        if(title.length() > 50) {
          title = title.substring(0, 50) + "...";
        }

        return title;
      }

    };

    final WebMarkupContainer imageLink = new WebMarkupContainer("comment-action");
    imageLink.setOutputMarkupId(true);
    add(imageLink);

    // Boiler Plate questions should not have comments.
    if(!question.isBoilerPlate()) {

      // Display a different link if a comment already exist for question.
      imageLink.add(new AbstractBehavior() {
        @Override
        public void onComponentTag(Component component, ComponentTag tag) {
          super.onComponentTag(component, tag);
          String comment = activeQuestionnaireAdministrationService.getComment((Question) BaseQuestionPanel.this.getDefaultModel().getObject());

          if(comment != null) {
            String cssClass = "comment-edit";
            if(tag.getAttributes().containsKey("class")) {
              cssClass += " " + tag.getAttributes().getString("class");
            }
            tag.getAttributes().put("class", cssClass);
          }
        }
      });

      // Add comment action link.
      imageLink.add(new AjaxLink("addComment") {
        public void onClick(AjaxRequestTarget target) {
          final QuestionCommentModalPanel contentPanel = new QuestionCommentModalPanel("content", commentWindow, BaseQuestionPanel.this.getDefaultModel()) {

            protected void onAddComment(AjaxRequestTarget target1) {
              target1.addComponent(imageLink);
            }

          };

          commentWindow.setCloseButtonCallback(new Dialog.CloseButtonCallback() {
            public boolean onCloseButtonClicked(AjaxRequestTarget target1, Status status) {

              if(status.equals(Status.SUCCESS)) {
                activeQuestionnaireAdministrationService.setComment((Question) contentPanel.getDefaultModelObject(), contentPanel.getComment());
                contentPanel.onAddComment(target1);
                commentWindow.close(target1);
              } else if(status.equals(Status.ERROR)) {
                contentPanel.displayFeedback(target1);
                return false;
              }
              return true;
            }
          });

          commentWindow.setContent(contentPanel);
          commentWindow.setTitle(commentWindowTitleModel);
          commentWindow.show(target);
        }
      });

    } else {
      imageLink.add(new WebMarkupContainer("addComment"));
      imageLink.setVisible(false);
    }

  }

  protected void setContent(String id) {
    Question question = (Question) getDefaultModelObject();
    Panel content;
    if(!question.hasSubQuestions()) {
      content = createCategoriesPanel(id, getModel());
    } else if(!question.hasCategories()) {
      content = createQuetionListPanel(id, getModel());
    } else if(question.isArrayOfSharedCategories()) {
      content = createSharedCategoriesPanel(id, getModel());
    } else {
      content = createJoinedCategoriesPanel(id, getModel());
    }
    add(content);
  }

  protected abstract Panel createCategoriesPanel(String id, IModel<Question> questionModel);

  protected abstract Panel createQuetionListPanel(String id, IModel<Question> questionModel);

  protected abstract Panel createSharedCategoriesPanel(String id, IModel<Question> questionModel);

  protected abstract Panel createJoinedCategoriesPanel(String id, IModel<Question> questionModel);

}
