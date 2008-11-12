/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import java.util.List;

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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.wicket.toggle.ToggleLink;

public abstract class BaseQuestionPanel extends QuestionPanel {

  private static final long serialVersionUID = -391676180175754335L;

  @SuppressWarnings("serial")
  public BaseQuestionPanel(String id, IModel questionModel) {
    super(id, questionModel);

    setOutputMarkupId(true);

    Question question = (Question) getModelObject();
    if(question.getNumber() != null) {
      add(new Label("number", question.getNumber()));
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

    add(new Label("instructions", new QuestionnaireStringResourceModel(question, "instructions")));
    add(new Label("caption", new QuestionnaireStringResourceModel(question, "caption")));

    // change the css rendering in case of a boiler plate
    if(question.isBoilerPlate()) {
      add(new AttributeModifier("class", new Model("boilerplate")));
      add(new EmptyPanel("content").setVisible(false));
    } else {
      setContent("content");
    }
  }

  @SuppressWarnings("serial")
  private void addCommentModalWindow(Question question) {

    // Create modal comments window
    final ModalWindow commentWindow;
    add(commentWindow = new ModalWindow("addCommentModal"));

    String commentWindowTitle = (new StringResourceModel("CommentsWindow", this, null)).getString() + " - " + new QuestionnaireStringResourceModel(question, "label").getString();
    if(commentWindowTitle.length() > 60) {
      commentWindow.setTitle(commentWindowTitle.substring(0, 60) + "...");
    } else {
      commentWindow.setTitle(commentWindowTitle);
    }

    commentWindow.setInitialHeight(220);
    commentWindow.setInitialWidth(550);
    commentWindow.setResizable(false);

    commentWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
      public void onClose(AjaxRequestTarget target) {
      }
    });

    // Question with no categories should not have comments.
    List<Category> categories = question.getCategories();
    if(categories != null && categories.size() > 0) {

      // Add comment action link.
      add(new AjaxLink("addComment") {
        public void onClick(AjaxRequestTarget target) {
          commentWindow.setContent(new QuestionCommentModalPanel("content", commentWindow, BaseQuestionPanel.this.getModel()));
          commentWindow.show(target);
        }
      });

    } else {
      add(new WebMarkupContainer("addComment").setVisible(false));
    }

  }

  protected abstract void setContent(String string);

}
