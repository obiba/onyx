/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.condition;

import java.util.List;

import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;

/**
 * Condition on whether or not a ({@link Question}, {@link Category}) has been answered.
 */
public class AnswerCondition extends Condition {

  private static final long serialVersionUID = -7608048954030186313L;

  private Question question;

  private Category category;

  private AnswerCondition parentAnswerCondition;

  /**
   * @param name
   */
  public AnswerCondition(String name, Question question, Category category) {
    super(name);
    this.question = question;
    this.category = category;
  }

  public Question getQuestion() {
    return question;
  }

  public void setQuestion(Question question) {
    this.question = question;
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
    if(category == null) {
      List<CategoryAnswer> answerList = activeQuestionnaireAdministrationService.findActiveAnswers(question);
      if(answerList.size() > 0) {
        return true;
      } else {
        return false;
      }
    }

    CategoryAnswer categoryAnswer = activeQuestionnaireAdministrationService.findAnswer(question, category);

    if(categoryAnswer == null) return false;

    return (categoryAnswer.isActive());
  }

}
