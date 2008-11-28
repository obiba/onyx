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
public class ExternalAnswerCondition extends Condition {

  private static final long serialVersionUID = -7608048954030186313L;

  private String questionName;

  private String categoryName;

  private String questionnaireName;

  /**
   * @param name
   */
  public ExternalAnswerCondition(String name, String questionnaireName, String questionName, String categoryName) {
    super(name);
    this.questionnaireName = questionnaireName;
    this.questionName = questionName;
    this.categoryName = categoryName;
  }

  public boolean isToBeAnswered(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService) {

    if(categoryName == null) {
      List<CategoryAnswer> answerList = activeQuestionnaireAdministrationService.findActiveAnswers(questionnaireName, questionName);
      if(answerList.size() > 0) {
        return true;
      } else {
        return false;
      }
    }

    CategoryAnswer categoryAnswer = activeQuestionnaireAdministrationService.findAnswer(questionnaireName, questionName, categoryName);
    if(categoryAnswer == null) return false;

    return categoryAnswer.isActive();
  }

}
