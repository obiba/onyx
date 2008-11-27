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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.util.data.Data;

/**
 * 
 */
public abstract class AbstractOpenAnswerDefinitionPanel extends Panel {

  private static final long serialVersionUID = 1L;

  /**
   * The question model (not necessarily the question of the category in the case of shared categories question).
   */
  private IModel questionModel;

  private IModel openAnswerDefinitionModel;

  private Data data;

  /**
   * 
   * @param id
   * @param questionCategoryModel
   */
  public AbstractOpenAnswerDefinitionPanel(String id, IModel questionCategoryModel) {
    this(id, new QuestionnaireModel(((QuestionCategory) questionCategoryModel.getObject()).getQuestion()), questionCategoryModel);
  }

  /**
   * 
   * @param id
   * @param questionModel
   * @param questionCategoryModel
   */
  public AbstractOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel) {
    this(id, questionModel, questionCategoryModel, new QuestionnaireModel(((QuestionCategory) questionCategoryModel.getObject()).getCategory().getOpenAnswerDefinition()));
  }

  /**
   * 
   * @param id
   * @param questionModel
   * @param questionCategoryModel
   * @param openAnswerDefinitionModel
   */
  public AbstractOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel) {
    super(id, questionCategoryModel);
    this.questionModel = questionModel;
    this.openAnswerDefinitionModel = openAnswerDefinitionModel;
  }

  protected IModel getQuestionModel() {
    return questionModel;
  }

  protected Question getQuestion() {
    return (Question) getQuestionModel().getObject();
  }

  protected IModel getQuestionCategoryModel() {
    return getModel();
  }

  protected QuestionCategory getQuestionCategory() {
    return (QuestionCategory) getModel().getObject();
  }

  protected IModel getOpenAnswerDefinitionModel() {
    return openAnswerDefinitionModel;
  }

  protected OpenAnswerDefinition getOpenAnswerDefinition() {
    return (OpenAnswerDefinition) getOpenAnswerDefinitionModel().getObject();
  }

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }

  /**
   * Called when open field is selected.
   * @param target
   * @param questionModel
   * @param questionCategoryModel
   * @param openAnswerDefinitionModel TODO
   */
  public void onSelect(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel) {
  }

  /**
   * Called when open field is submitted.
   * @param target
   * @param questionModel
   * @param questionCategoryModel
   */
  public void onSubmit(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
  }

  /**
   * Called when open field is submitted and error occures.
   * @param target
   * @param questionModel
   * @param questionCategoryModel
   */
  public void onError(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
  }

  public abstract void setRequired(boolean required);

  public abstract void setFieldModelObject(Data data);

}
