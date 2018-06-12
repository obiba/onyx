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

import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.onyx.magma.AbstractOnyxBeanResolver;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 *
 */
public abstract class AbstractQuartzBeanResolver extends AbstractOnyxBeanResolver {

  private QuestionnaireBundleManager questionnaireBundleManager;

  private QuestionnaireParticipantService questionnaireParticipantService;

  @Autowired(required = true)
  public void setQuestionnaireParticipantService(QuestionnaireParticipantService questionnaireParticipantService) {
    this.questionnaireParticipantService = questionnaireParticipantService;
  }

  @Autowired(required = true)
  public void setQuestionnaireBundleManager(QuestionnaireBundleManager questionnaireBundleManager) {
    this.questionnaireBundleManager = questionnaireBundleManager;
  }

  protected Questionnaire getQuestionnaire(Variable variable) throws NoSuchAttributeException {
    Value questionnaireName = variable.getAttribute("questionnaire").getValue();
    Questionnaire questionnaire = questionnaireBundleManager.getBundle(questionnaireName.toString()).getQuestionnaire();
    if(questionnaire == null) {
      throw new IllegalArgumentException("Cannot find questionnaire '" + questionnaireName.toString() + "' for variable '" + variable.getName() + "'.");
    }
    return questionnaire;
  }

  protected QuestionAnswer findQuestionAnswer(final QuestionnaireParticipant qp, final String questionName) {
    return Iterables.find(qp.getParticipantAnswers(), new Predicate<QuestionAnswer>() {
      public boolean apply(QuestionAnswer input) {
        return input.isActive() && input.getQuestionName().equals(questionName);
      }
    });
  }

  protected CategoryAnswer findCategoryAnswer(final QuestionAnswer qa, final String categoryName) {
    return Iterables.find(qa.getCategoryAnswers(), new Predicate<CategoryAnswer>() {
      public boolean apply(CategoryAnswer input) {
        return input.isActive() && input.getCategoryName().equals(categoryName);
      }
    });
  }

  protected OpenAnswer findOpenAnswer(final CategoryAnswer ca, final String openAnswerDefinitionName) {
    return Iterables.find(ca.getOpenAnswers(), new Predicate<OpenAnswer>() {
      public boolean apply(OpenAnswer input) {
        return input.getOpenAnswerDefinitionName().equals(openAnswerDefinitionName);
      }
    });
  }

  protected QuestionAnswer lookupQuestionAnswer(final ValueSet valueSet, final Variable variable) {
    QuestionnaireParticipant qp = lookupBean(valueSet, variable);
    if(qp == null) {
      return null;
    }
    return findQuestionAnswer(qp, variable.getAttributeStringValue("questionName"));
  }

  protected QuestionnaireParticipant lookupBean(ValueSet valueSet, Variable variable) {
    String questionnaireName = variable.getAttributeStringValue("questionnaire");
    return questionnaireParticipantService.getQuestionnaireParticipant(super.getParticipant(valueSet), questionnaireName);
  }
}
