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

import java.util.NoSuchElementException;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireMetric;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 *
 */
public class QuestionnaireBeanResolver extends AbstractQuartzBeanResolver {

  public boolean resolves(Class<?> type) {
    return QuestionnaireParticipant.class.equals(type) || QuestionAnswer.class.equals(type) || CategoryAnswer.class.equals(type) || OpenAnswer.class.equals(type) || QuestionnairePageMetricAlgorithm.class.equals(type) || QuestionnaireComment.class.equals(type);
  }

  public Object resolve(Class<?> type, ValueSet valueSet, final Variable variable) {
    QuestionnaireParticipant qp = lookupBean(valueSet, variable);
    if(qp == null) {
      // This happens when the participant did not answer a particular questionnaire
      return null;
    }
    try {
      if(type.equals(QuestionnaireParticipant.class)) {
        return qp;
      }
      if(type.equals(QuestionAnswer.class)) {
        return lookupQuestionAnswer(valueSet, variable);
      }
      if(type.equals(CategoryAnswer.class)) {
        QuestionAnswer qa = lookupQuestionAnswer(valueSet, variable);
        if(qa != null) {
          if(variable.isRepeatable()) {
            return qa.getCategoryAnswers();
          }
          // Return the only category present in the list. If there are more than one, this will throw and
          // IllegalArgumentException. If there is no such element, then this will return null
          return Iterables.getOnlyElement(qa.getCategoryAnswers(), null);
        }
      }
      if(type.equals(OpenAnswer.class)) {
        QuestionAnswer qa = lookupQuestionAnswer(valueSet, variable);
        if(qa != null) {
          return findOpenAnswer(findCategoryAnswer(qa, variable.getAttributeStringValue("categoryName")), variable.getAttributeStringValue("openAnswerName"));
        }
      }
      if(type.equals(QuestionnairePageMetricAlgorithm.class)) {
        return Iterables.transform(qp.getQuestionnaireMetrics(), new Function<QuestionnaireMetric, QuestionnairePageMetricAlgorithm>() {
          @Override
          public QuestionnairePageMetricAlgorithm apply(QuestionnaireMetric from) {
            return new QuestionnairePageMetricAlgorithm(getQuestionnaire(variable), from);
          };
        });
      }
      if(type.equals(QuestionnaireComment.class)) {
        Iterable<QuestionAnswer> answerComments = Iterables.filter(qp.getParticipantAnswers(), new Predicate<QuestionAnswer>() {
          @Override
          public boolean apply(QuestionAnswer input) {
            return input.getComment() != null;
          }
        });
        return Iterables.transform(answerComments, new Function<QuestionAnswer, QuestionnaireComment>() {
          @Override
          public QuestionnaireComment apply(QuestionAnswer from) {
            return new QuestionnaireComment(getQuestionnaire(variable), from);
          }
        });
      }

    } catch(NoSuchElementException e) {
      // Ignore: it only means that the participant did not answer a particular question or category or open answer
      return null;
    }
    return null;

  }
}
