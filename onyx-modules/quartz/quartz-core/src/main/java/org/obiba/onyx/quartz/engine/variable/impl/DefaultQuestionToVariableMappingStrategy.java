/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.engine.variable.impl;

import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;
import org.obiba.onyx.quartz.engine.variable.IQuestionToVariableMappingStrategy;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class DefaultQuestionToVariableMappingStrategy implements IQuestionToVariableMappingStrategy {

  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionToVariableMappingStrategy.class);

  private static final String QUESTIONNAIRE_LOCALE = "locale";

  private static final String QUESTIONNAIRE_VERSION = "version";

  public Variable getVariable(Questionnaire questionnaire) {
    Variable questionnaireVariable = new Variable(questionnaire.getName());
    // add participant dependent information
    questionnaireVariable.addVariable(new Variable(QUESTIONNAIRE_VERSION).setDataType(DataType.TEXT));
    questionnaireVariable.addVariable(new Variable(QUESTIONNAIRE_LOCALE).setDataType(DataType.TEXT));

    return questionnaireVariable;
  }

  public Variable getVariable(Question question) {
    Variable entity = null;

    // simple question
    if(question.getQuestions().size() == 0) {
      Variable variable = new Variable(question.getName());
      entity = variable;
      for(Category category : question.getCategories()) {
        variable.addCategory(category.getName());
        variable.addVariable(getVariable(category));
      }
      if(variable.getCategories().size() > 0) {
        variable.setDataType(DataType.TEXT);
      }
    } else if(question.getQuestionCategories().size() == 0) {
      // sub questions
      entity = new Variable(question.getName());
      for(Question subQuestion : question.getQuestions()) {
        entity.addVariable(getVariable(subQuestion));
      }
    } else {
      boolean shared = true;
      for(Question child : question.getQuestions()) {
        if(child.getCategories().size() > 0) {
          shared = false;
          break;
        }
      }
      if(shared) {
        // shared categories question
        entity = new Variable(question.getName());
        for(Question subQuestion : question.getQuestions()) {
          Variable variable = new Variable(subQuestion.getName());
          for(Category category : question.getCategories()) {
            variable.addCategory(category.getName());
            variable.addVariable(getVariable(category));
          }
          if(variable.getCategories().size() > 0) {
            variable.setDataType(DataType.TEXT);
          }
          entity.addVariable(variable);
        }
      } else {
        // joined categories question
        throw new UnsupportedOperationException("Joined categories question array not supported yet.");
      }
    }

    log.debug("getEntity({})={}", question, entity);

    return entity;
  }

  private Variable getVariable(Category category) {
    Variable categoryVariable = null;

    OpenAnswerDefinition open = category.getOpenAnswerDefinition();
    if(open != null) {
      categoryVariable = new Variable(category.getName());

      categoryVariable.addVariable(getVariable(open));
      for(OpenAnswerDefinition openChild : open.getOpenAnswerDefinitions()) {
        categoryVariable.addVariable(getVariable(openChild));
      }
    }

    return categoryVariable;
  }

  private Variable getVariable(OpenAnswerDefinition openAnswerDefinition) {
    Variable variable = null;

    variable = new Variable(openAnswerDefinition.getName()).setDataType(openAnswerDefinition.getDataType()).setUnit(openAnswerDefinition.getUnit());

    return variable;
  }

  public VariableData getVariableData(QuestionnaireParticipantService questionnaireParticipantService, Participant participant, Variable variable, VariableData variableData, Questionnaire questionnaire) {

    QuestionnaireFinder finder = QuestionnaireFinder.getInstance(questionnaire);

    // variable is a question
    if(variable.getCategories().size() > 0) {
      Question question = finder.findQuestion(variable.getName());
      if(question != null) {
        List<CategoryAnswer> answers = questionnaireParticipantService.getCategoryAnswers(participant, questionnaire.getName(), question.getName());
        for(CategoryAnswer answer : answers) {
          variableData.addData(DataBuilder.buildText(answer.getCategoryName()));
        }
      }
    }

    // variable is an open answer
    else if(variable.getDataType() != null) {
      Question question = finder.findQuestion(variable.getParent().getParent().getName());
      log.debug("question={}", question);
      if(question != null) {
        QuestionCategory questionCategory = finder.findQuestionCategory(question.getName(), variable.getParent().getName());
        log.debug("questionCategory={}", questionCategory);
        if(questionCategory != null) {
          OpenAnswer answer = questionnaireParticipantService.getOpenAnswer(participant, questionnaire.getName(), question.getName(), questionCategory.getCategory().getName(), variable.getName());
          if(answer != null) {
            variableData.addData(answer.getData());
          }
        }
      }
    }

    return variableData;
  }

  public Variable getQuestionnaireVariable(Variable variable) {
    Variable questionnaireVariable = variable;

    while(questionnaireVariable.getParent() != null && questionnaireVariable.getParent().getParent() != null) {
      questionnaireVariable = questionnaireVariable.getParent();
    }

    return questionnaireVariable;
  }

}
