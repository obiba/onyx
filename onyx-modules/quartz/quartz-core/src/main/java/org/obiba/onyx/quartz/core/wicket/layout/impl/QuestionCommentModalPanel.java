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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.wicket.behavior.FocusBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

public abstract class QuestionCommentModalPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private FeedbackWindow feedbackWindow;

  private String comment;

  public QuestionCommentModalPanel(String id, ModalWindow commentWindow, IModel questionModel) {
    super(id, questionModel);
    setComment(activeQuestionnaireAdministrationService.getComment((Question) getModelObject()));

    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    TextArea newComment = new TextArea("newComment", new PropertyModel(QuestionCommentModalPanel.this, "comment"));
    newComment.setOutputMarkupId(true);
    newComment.add(new AttributeModifier("class", true, new Model("comment-text")));
    newComment.add(new FocusBehavior());
    newComment.add(new StringValidator.MaximumLengthValidator(2000));
    add(newComment);
  }

  public String getComment() {
    return comment;
  }

  protected abstract void onAddComment(AjaxRequestTarget target);

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void displayFeedback(AjaxRequestTarget target) {
    feedbackWindow.setContent(new FeedbackPanel("content"));
    feedbackWindow.show(target);
  }

  public FeedbackWindow getFeedbackWindow() {
    return feedbackWindow;
  }

}
