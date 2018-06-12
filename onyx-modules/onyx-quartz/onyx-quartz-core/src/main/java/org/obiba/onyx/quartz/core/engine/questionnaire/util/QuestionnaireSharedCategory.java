/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.util.Collection;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;

public class QuestionnaireSharedCategory {
  /**
   * Return true is category associated to the given questionCategory is shared, false otherwise. we use this method if
   * question associated to questionCategory is not yet linked to the questionnaire.
   * (QuestionnaireFinder.findSharedCategories do not contains yet the category)
   * 
   * @param question
   * @param questionCategory
   * @param category
   * @return
   */
  public static boolean isSharedIfLink(final QuestionCategory questionCategory, Questionnaire questionnaire) {
    QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
    questionnaire.setQuestionnaireCache(null);
    Multimap<Category, Question> categoriesFilterName = questionnaireFinder.findCategories(questionCategory.getCategory().getName());
    Collection<Category> categories = Collections2.filter(categoriesFilterName.keySet(), new Predicate<Category>() {

      @Override
      public boolean apply(Category input) {
        return input == questionCategory.getCategory();
      }
    });
    if(categoriesFilterName.isEmpty() || categories.isEmpty()) {
      return false;
    }
    Collection<Question> questions = categoriesFilterName.get(categories.iterator().next());
    Collection<Question> otherQuestions = Collections2.filter(questions, new Predicate<Question>() {

      @Override
      public boolean apply(Question input) {
        return input != questionCategory.getQuestion();
      }
    });
    return !otherQuestions.isEmpty();
  }
}
