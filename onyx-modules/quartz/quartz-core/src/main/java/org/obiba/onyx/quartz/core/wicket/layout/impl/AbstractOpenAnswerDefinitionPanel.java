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
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionListener;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.MultipleDefaultOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class the building open answer panels.
 * @see DefaultOpenAnswerDefinitionPanel
 * @see MultipleDefaultOpenAnswerDefinitionPanel
 */
public abstract class AbstractOpenAnswerDefinitionPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractOpenAnswerDefinitionPanel.class);

  /**
   * The question model (not necessarily the question of the category in the case of shared categories question).
   */
  private IModel questionModel;

  /**
   * The open answer definition we are dealing with.
   */
  private IModel openAnswerDefinitionModel;

  /**
   * The data being provisionned.
   */
  private Data data;

  /**
   * Constructor, using the question to be associated to the question category. Question is not necessarily the parent
   * question of the category (case of array questions).
   * @param id
   * @param questionModel
   * @param questionCategoryModel
   */
  public AbstractOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel) {
    this(id, questionModel, questionCategoryModel, new QuestionnaireModel(((QuestionCategory) questionCategoryModel.getObject()).getOpenAnswerDefinition()));
  }

  /**
   * Constructor, using the question to be associated to the question category and the open answer definition. Question
   * is not necessarily the parent question of the category (case of array questions).
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

  public IModel getQuestionModel() {
    return questionModel;
  }

  public Question getQuestion() {
    return (Question) getQuestionModel().getObject();
  }

  public IModel getQuestionCategoryModel() {
    return getModel();
  }

  public QuestionCategory getQuestionCategory() {
    return (QuestionCategory) getModel().getObject();
  }

  public IModel getOpenAnswerDefinitionModel() {
    return openAnswerDefinitionModel;
  }

  public OpenAnswerDefinition getOpenAnswerDefinition() {
    return (OpenAnswerDefinition) getOpenAnswerDefinitionModel().getObject();
  }

  /**
   * Set the current data.
   * @return
   */
  public Data getData() {
    return data;
  }

  /**
   * Get the current data.
   * @param data
   */
  public void setData(Data data) {
    this.data = data;
  }

  /**
   * Call this to reset the open field content.
   */
  public abstract void resetField();

  protected void fireQuestionCategorySelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel, boolean isSelected) {
    log.debug("fireQuestionCategorySelection({},{},{})", new Object[] { questionModel.getObject(), questionCategoryModel.getObject(), Boolean.valueOf(isSelected) });
    IQuestionCategorySelectionListener listener = (IQuestionCategorySelectionListener) findParent(IQuestionCategorySelectionListener.class);
    if(listener != null) {
      listener.onQuestionCategorySelection(target, questionModel, questionCategoryModel, isSelected);
    }
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    questionModel.detach();
    openAnswerDefinitionModel.detach();
  }

}
