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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.wicket.toggle.ToggleLink;

/**
 * Base class for implementing question UIs: it defines the question header including label, help, instructions, comment
 * etc and content place holder.
 */
public abstract class BaseQuestionPanel extends QuestionPanel {

  private static final long serialVersionUID = -391676180175754335L;

  private static final String BOILERPLATE_CLASS = "obiba-quartz-boilerplate";

  @SuppressWarnings("serial")
  public BaseQuestionPanel(String id, IModel questionModel) {
    super(id, questionModel);

    setOutputMarkupId(true);

    Question question = (Question) getModelObject();
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
      ToggleLink toggleLink = new ToggleLink("helpToggle", new Model("&nbsp;&nbsp;&nbsp;&nbsp;"), new Model("&nbsp;&nbsp;&nbsp;&nbsp;"), helpContent);
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
      add(new AttributeModifier("class", new Model(BOILERPLATE_CLASS)));
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
    final ModalWindow commentWindow;
    add(commentWindow = new ModalWindow("addCommentModal"));
    commentWindow.setCssClassName("onyx");

    final IModel commentWindowTitleModel = new LoadableDetachableModel() {

      @Override
      protected Object load() {
        String title = (new StringResourceModel("CommentsWindow", BaseQuestionPanel.this, null)).getString() + " - " + new QuestionnaireStringResourceModel(BaseQuestionPanel.this.getModel(), "label").getString();

        // Question label is truncated if too long for Modal Window title bar.
        if(title.length() > 60) {
          title = title.substring(0, 60) + "...";
        }

        return title;
      }

    };
    commentWindow.setInitialHeight(220);
    commentWindow.setInitialWidth(550);
    commentWindow.setResizable(false);

    commentWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        // same as cancel
        return true;
      }
    });

    commentWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
      public void onClose(AjaxRequestTarget target) {
      }
    });

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
          String comment = activeQuestionnaireAdministrationService.getComment((Question) BaseQuestionPanel.this.getModel().getObject());

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
          commentWindow.setContent(new QuestionCommentModalPanel("content", commentWindow, BaseQuestionPanel.this.getModel(), target) {

            protected void onAddComment(AjaxRequestTarget target) {
              target.addComponent(imageLink);
            }

          });
          commentWindow.setTitle(commentWindowTitleModel);
          commentWindow.show(target);
        }
      });

    } else {
      imageLink.add(new WebMarkupContainer("addComment"));
      imageLink.setVisible(false);
    }

  }

  /**
   * Method to implement for the definition of question content component.
   * @param string
   */
  protected abstract void setContent(String string);

}
