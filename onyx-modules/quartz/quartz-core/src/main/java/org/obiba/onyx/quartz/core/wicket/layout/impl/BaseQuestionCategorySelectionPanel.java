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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionListener;
import org.obiba.onyx.quartz.core.wicket.layout.impl.behavior.QuestionnaireStyleBehavior;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base utility methods for sharing some question category selection callbacks.
 */
public abstract class BaseQuestionCategorySelectionPanel extends Panel {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(BaseQuestionCategorySelectionPanel.class);

  /**
   * The question model (not necessarily the question of the category in the case of shared categories question).
   */
  private IModel questionModel;

  /**
   * @param id
   * @param model
   */
  public BaseQuestionCategorySelectionPanel(String id, IModel questionModel, IModel questionCategoryModel) {
    super(id, questionCategoryModel);
    this.questionModel = questionModel;

    // add a css class that represents this page instance
    add(new QuestionnaireStyleBehavior());
  }

  public IModel getQuestionModel() {
    return questionModel;
  }

  public Question getQuestion() {
    return (Question) questionModel.getObject();
  }

  public IModel getQuestionCategoryModel() {
    return getDefaultModel();
  }

  public QuestionCategory getQuestionCategory() {
    return (QuestionCategory) getDefaultModel().getObject();
  }

  /**
   * Find the feedback panel associated to the wizard form and update it regarding the ajax target.
   * @param target
   */
  protected void updateFeedbackPanel(AjaxRequestTarget target) {
    WizardForm wizard = (WizardForm) findParent(WizardForm.class);
    if(wizard != null && wizard.getFeedbackWindow() != null) {
      if(wizard.getFeedbackMessage() != null) wizard.getFeedbackWindow().show(target);
    }
    target.appendJavascript("Resizer.resizeWizard();");
  }

  /**
   * Find the first parent that will to be warned about a changing question answer.
   * @param target
   * @param questionModel
   * @param questionCategoryModel
   * @see IQuestionAnswerChangedListener
   */
  protected void fireQuestionCategorySelected(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
    IQuestionCategorySelectionListener parentListener = (IQuestionCategorySelectionListener) findParent(IQuestionCategorySelectionListener.class);
    if(parentListener != null) {
      parentListener.onQuestionCategorySelection(target, questionModel, questionCategoryModel, true);
    }
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    questionModel.detach();
  }

}
