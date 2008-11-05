/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.condition;

import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;

public class AnswerCondition extends Condition {

  private static final long serialVersionUID = -7608048954030186313L;

  private QuestionCategory questionCategory;

  private Integer occurence;

  private AnswerCondition parentAnswerCondition;

  private DataComparator dataComparator;

  public QuestionCategory getQuestionCategory() {
    return questionCategory;
  }

  public void setQuestionCategory(QuestionCategory questionCategory) {
    this.questionCategory = questionCategory;
  }

  public Question getQuestion() {
    return questionCategory.getQuestion();
  }

  public Integer getOccurence() {
    return occurence;
  }

  public void setOccurence(Integer occurence) {
    this.occurence = occurence;
  }

  public DataComparator getDataComparator() {
    return dataComparator;
  }

  public void setDataComparator(DataComparator dataComparator) {
    this.dataComparator = dataComparator;
  }

  public Category getCategory() {
    return questionCategory.getCategory();
  }

  public AnswerCondition getParentAnswerCondition() {
    return parentAnswerCondition;
  }

  public void setParentAnswerCondition(AnswerCondition parentAnswerCondition) {
    this.parentAnswerCondition = parentAnswerCondition;
  }

  public boolean isToBeAnswered() {
    CategoryAnswer categoryAnswer = activeQuestionnaireAdministrationService.findAnswer(questionCategory);

    if(categoryAnswer == null) return false;

    if(dataComparator != null) {
      int compareResult = categoryAnswer.getData().compareTo(dataComparator.getData());
      return dataComparator.isComparisonValid(compareResult);
    }

    if(parentAnswerCondition != null) return parentAnswerCondition.isToBeAnswered();

    return true;
  }

}
