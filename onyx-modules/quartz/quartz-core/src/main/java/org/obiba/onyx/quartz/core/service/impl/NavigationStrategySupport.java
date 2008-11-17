/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.service.impl;

import java.util.List;

import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;

/**
 * This class provides a set of convenience methods for <code>INavigationStrategy</code> implementors.
 * 
 * @author cag-dspathis
 * 
 */
public class NavigationStrategySupport {

  /**
   * Indicates whether the specified page contains at least one question that is to be answered (whether or not an
   * answer currently exists).
   * 
   * @param service service
   * @param page page
   * @return <code>true</code> if the page contains at least one question that is to be answered.
   */
  public static boolean hasQuestionToBeAnswered(ActiveQuestionnaireAdministrationService service, Page page) {
    List<Question> questions = page.getQuestions();

    for(Question question : questions) {
      if(question.hasDataSource()) continue;
      if(question.isToBeAnswered(service)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Indicates whether the specified page contains at least one question that is to be answered but currently
   * unanswered.
   * 
   * @param service service
   * @param page page
   * @return <code>true</code> if the page contains at least one question to be answered but as yet unanswered
   */
  public static boolean hasUnansweredQuestion(ActiveQuestionnaireAdministrationService service, Page page) {
    List<Question> questions = page.getQuestions();

    for(Question question : questions) {
      if(question.isToBeAnswered(service)) {
        List<CategoryAnswer> answers = service.findAnswers(question);

        if(!answers.isEmpty()) {
          for(CategoryAnswer answer : answers) {
            if(answer.getOpenAnswers() == null) {
              return true;
            } else {
              for(OpenAnswer openAnswer : answer.getOpenAnswers()) {
                if(openAnswer.getData() == null || openAnswer.getData().getValue() == null) return true;
              }
            }
          }
        } else {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Indicates whether a page contains at least one question that is to be answered and that currently has an answer
   * with the specified "active" status.
   * 
   * @param service service
   * @param page page
   * @param active active status
   * @return <code>true</code> if the page contains at least one question to be answered, that has an answer with the
   * specified "active" status
   */
  public static boolean hasAnsweredQuestion(ActiveQuestionnaireAdministrationService service, Page page, boolean active) {
    List<Question> questions = page.getQuestions();

    for(Question question : questions) {
      if(question.isToBeAnswered(service)) {
        List<CategoryAnswer> answers = service.findAnswers(question);

        for(CategoryAnswer answer : answers) {
          if(answer.getActive().booleanValue() == active) {
            return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * Indicates whether a page contains at least one question that does not have an answer source.
   * 
   * @param service service
   * @param page page
   * @return <code>true</code> if the page contains a question without an answer source
   */
  public static boolean hasNonAnswerSourceQuestion(ActiveQuestionnaireAdministrationService service, Page page) {
    List<Question> questions = page.getQuestions();

    for(Question question : questions) {
      if(!question.hasDataSource()) {
        return true;
      }
    }

    return false;
  }
}