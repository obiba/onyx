package org.obiba.onyx.quartz.core.wicket.layout.impl;

import java.util.Collection;

import org.apache.wicket.extensions.model.AbstractCheckBoxModel;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;

/**
 * Class for storing category selections in case of a multiple choice question.
 */
public class QuestionCategoryCheckBoxModel extends AbstractCheckBoxModel {

  private static final long serialVersionUID = 1L;

  private IModel questionCategoryModel;

  private IModel selectionsModel;

  public QuestionCategoryCheckBoxModel(IModel selectionsModel, QuestionCategory questionCategory) {
    this(selectionsModel, new QuestionnaireModel(questionCategory), questionCategory.isSelected());
  }

  public QuestionCategoryCheckBoxModel(IModel selectionsModel, IModel questionCategoryModel) {
    this(selectionsModel, questionCategoryModel, ((QuestionCategory) questionCategoryModel.getObject()).isSelected());
  }

  public QuestionCategoryCheckBoxModel(IModel selectionsModel, IModel questionCategoryModel, boolean selected) {
    this.selectionsModel = selectionsModel;
    this.questionCategoryModel = questionCategoryModel;
    if(selected) select();
  }

  public QuestionCategory getQuestionCategory() {
    return (QuestionCategory) questionCategoryModel.getObject();
  }

  public Collection<IModel> getSelections() {
    return (Collection<IModel>) selectionsModel.getObject();
  }

  @Override
  public void select() {
    getSelections().add(questionCategoryModel);
  }

  @Override
  public void unselect() {
    getSelections().remove(questionCategoryModel);
  }

  @Override
  public boolean isSelected() {
    return getSelections().contains(questionCategoryModel);
  }

}