/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;

public class QuestionCommentModalPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private ModalWindow commentWindow;

  private FeedbackPanel feedback;

  private String comment;

  public QuestionCommentModalPanel(String id, ModalWindow commentWindow, IModel questionModel, AjaxRequestTarget target) {
    super(id, questionModel);
    this.commentWindow = commentWindow;

    setComment(activeQuestionnaireAdministrationService.getComment((Question) getModelObject()));

    add(feedback = new FeedbackPanel("feedback"));
    feedback.setOutputMarkupId(true);

    add(new CommentForm("commentForm", target));

  }

  private class CommentForm extends Form {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("serial")
    public CommentForm(String id, AjaxRequestTarget target) {
      super(id);

      setModel(new Model(QuestionCommentModalPanel.this));

      final TextArea newComment = new TextArea("newComment", new PropertyModel(getModel(), "comment"));
      newComment.add(new RequiredFormFieldBehavior());
      newComment.setOutputMarkupId(true);
      target.focusComponent(newComment);
      add(newComment);

      // Save a new comment.
      add(new AjaxButton("saveComment", this) {

        protected void onSubmit(AjaxRequestTarget target, Form form) {
          activeQuestionnaireAdministrationService.addComment((Question) QuestionCommentModalPanel.this.getModelObject(), comment);
          commentWindow.close(target);
        }

        protected void onError(AjaxRequestTarget target, Form form) {
          target.addComponent(feedback);
        }

      });

      // Cancel comment.
      add(new AjaxLink("cancelComment") {

        @Override
        public void onClick(AjaxRequestTarget target) {
          commentWindow.close(target);
        }

      });

    }
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

}
