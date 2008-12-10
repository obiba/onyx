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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.wicket.behavior.InvalidFormFieldBehavior;
import org.obiba.onyx.wicket.toggle.ToggleLink;

/**
 * Base class for implementing question UIs: it defines the question header including label, help, instructions, comment
 * etc and content place holder.
 */
public abstract class BaseQuestionPanel extends QuestionPanel {

  private static final long serialVersionUID = -391676180175754335L;

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
    add(new Label("label", new QuestionnaireStringResourceModel(question, "label")));

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

    // specifications
    QuestionnaireStringResourceModel specificationsModel = new QuestionnaireStringResourceModel(question, "specifications");
    if(specificationsModel.getString() != null && !specificationsModel.getString().trim().equals("")) {
      Label specifications = new Label("specifications", specificationsModel);
      specifications.setEscapeModelStrings(false);
      add(specifications);
    } else {
      add(new EmptyPanel("specifications").setVisible(false));
    }

    add(new Label("instructions", new QuestionnaireStringResourceModel(question, "instructions")).setEscapeModelStrings(false));
    add(new Label("caption", new QuestionnaireStringResourceModel(question, "caption")).setEscapeModelStrings(false));

    // change the css rendering in case of a boiler plate
    if(question.isBoilerPlate()) {
      add(new AttributeModifier("class", new Model("boilerplate")));
      add(new EmptyPanel("content").setVisible(false));
    } else {
      setContent("content");
      add(new InvalidFormFieldBehavior());
    }

  }

  @SuppressWarnings("serial")
  private void addCommentModalWindow(Question question) {

    // Create modal comments window
    final ModalWindow commentWindow;
    add(commentWindow = new ModalWindow("addCommentModal"));

    String commentWindowTitle = (new StringResourceModel("CommentsWindow", this, null)).getString() + " - " + new QuestionnaireStringResourceModel(question, "label").getString();

    // Question label is truncated if too long for Modal Window title bar.
    if(commentWindowTitle.length() > 60) {
      commentWindow.setTitle(commentWindowTitle.substring(0, 60) + "...");
    } else {
      commentWindow.setTitle(commentWindowTitle);
    }

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
      String comment = activeQuestionnaireAdministrationService.getComment(question);
      if(comment != null) {
        switchToEditStyle(imageLink);
      }

      // Add comment action link.
      imageLink.add(new AjaxLink("addComment") {
        public void onClick(AjaxRequestTarget target) {
          commentWindow.setContent(new QuestionCommentModalPanel("content", commentWindow, BaseQuestionPanel.this.getModel(), target) {

            protected void onAddComment(AjaxRequestTarget target) {
              switchToEditStyle(imageLink);
              target.addComponent(imageLink);
            }

          });
          commentWindow.show(target);
        }
      });

    } else {
      imageLink.add(new WebMarkupContainer("addComment").setVisible(false));
    }

  }

  private void switchToEditStyle(WebMarkupContainer imageLink) {
    imageLink.add(new AttributeModifier("class", new Model("comment-edit")));
  }

  /**
   * Method to implement for the definition of question content component.
   * @param string
   */
  protected abstract void setContent(String string);

}
