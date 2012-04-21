/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.magma;

import java.util.Set;

import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireMetric;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class QuestionnairePageMetricAlgorithm {

  private static final Logger log = LoggerFactory.getLogger(QuestionnairePageMetricAlgorithm.class);

  private final QuestionnaireMetric pageMetrics;

  private final QuestionnaireFinder questionnaireFinder;

  private final String sectionName;

  private final Set<String> pageQuestions;

  public QuestionnairePageMetricAlgorithm(Questionnaire questionnaire, QuestionnaireMetric pageMetrics) {
    this.questionnaireFinder = new QuestionnaireFinder(questionnaire);
    this.pageMetrics = pageMetrics;

    Page page = questionnaireFinder.findPage(getPage());
    if(page == null) {
      // ONYX-1342
      log.warn("Page {} no longer exists in questionnaire {}.", getPage(), questionnaire.getName());
      sectionName = "";
      pageQuestions = ImmutableSet.of();
    } else {
      sectionName = page.getSection().getName();
      // Build a Set of this Page's question's name
      pageQuestions = ImmutableSet.copyOf(Iterables.transform(questionnaireFinder.findPage(pageMetrics.getPage()).getQuestions(), new Function<Question, String>() {
        public String apply(Question q) {
          return q.getName();
        }
      }));
    }

  }

  public String getPage() {
    return pageMetrics.getPage();
  }

  public String getSection() {
    return sectionName;
  }

  public int getDuration() {
    return pageMetrics.getDuration();
  }

  /**
   * Returns the number of questions with active answers within a page.
   * @return
   */
  public int getQuestionCount() {
    int questionCount = 0;
    for(QuestionAnswer questionAnswer : pageMetrics.getQuestionnaireParticipant().getParticipantAnswers()) {
      if(questionAnswer.isActive()) {
        if(pageQuestions.contains(questionAnswer.getQuestionName())) {
          questionCount++;
        }
      }
    }

    return questionCount;
  }

  /**
   * Returns the number of questions for which an escape category was chosen as an answer within a page.
   * @return
   */
  public int getMissingCount() {
    int missingCount = 0;
    for(QuestionAnswer questionAnswer : pageMetrics.getQuestionnaireParticipant().getParticipantAnswers()) {
      if(questionAnswer.isActive()) {
        String questionName = questionAnswer.getQuestionName();
        if(pageQuestions.contains(questionName)) {
          for(CategoryAnswer categoryAnswer : questionAnswer.getCategoryAnswers()) {
            QuestionCategory questionCategory = questionnaireFinder.findQuestionCategory(questionName, categoryAnswer.getCategoryName());
            if(questionCategory != null && questionCategory.getCategory() != null && questionCategory.getCategory().isEscape()) {
              missingCount++;
              break;
            }
          }
        }
      }
    }

    return missingCount;
  }
}
