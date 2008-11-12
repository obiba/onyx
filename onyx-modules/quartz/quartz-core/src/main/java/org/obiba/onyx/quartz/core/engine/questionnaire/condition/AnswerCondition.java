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
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;

public class AnswerCondition extends Condition {

  private static final long serialVersionUID = -7608048954030186313L;

  private Question question;

  private Category category;

  private Integer occurence;

  private AnswerCondition parentAnswerCondition;

  private DataComparator dataComparator;

  public Question getQuestion() {
    return question;
  }

  public void setQuestion(Question question) {
    this.question = question;
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
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public AnswerCondition getParentAnswerCondition() {
    return parentAnswerCondition;
  }

  public void setParentAnswerCondition(AnswerCondition parentAnswerCondition) {
    this.parentAnswerCondition = parentAnswerCondition;
  }

  public boolean isToBeAnswered(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService) {
    CategoryAnswer categoryAnswer = activeQuestionnaireAdministrationService.findAnswer(question, category);

    if(categoryAnswer == null) return false;

    if(dataComparator != null) {
      OpenAnswer openAnswer = activeQuestionnaireAdministrationService.findOpenAnswer(question, category, dataComparator.getOpenAnswerDefinition());
      int compareResult = openAnswer.getData().compareTo(dataComparator.getData());
      return dataComparator.isComparisonValid(compareResult);
    }

    return true;
  }

}
