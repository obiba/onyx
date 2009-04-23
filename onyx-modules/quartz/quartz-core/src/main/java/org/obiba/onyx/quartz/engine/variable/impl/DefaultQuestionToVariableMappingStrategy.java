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
import org.obiba.onyx.engine.variable.Category;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
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

  public static final String QUESTIONNAIRE_RUN = "QuestionnaireRun";

  public static final String QUESTIONNAIRE_LOCALE = "locale";

  public static final String QUESTIONNAIRE_VERSION = "version";

  public static final String QUESTIONNAIRE_TIMESTART = "timeStart";

  public static final String QUESTIONNAIRE_TIMEEND = "timeEnd";

  public static final String QUESTIONNAIRE_USER = "user";

  public static final String QUESTION_COMMENT = "comment";

  public static final String QUESTION_ACTIVE = "active";

  public Variable getVariable(Questionnaire questionnaire) {
    Variable questionnaireVariable = new Variable(questionnaire.getName());
    // add participant dependent information
    Variable questionnaireRunVariable = questionnaireVariable.addVariable(new Variable(QUESTIONNAIRE_RUN));
    questionnaireRunVariable.addVariable(new Variable(QUESTIONNAIRE_VERSION).setDataType(DataType.TEXT));
    questionnaireRunVariable.addVariable(new Variable(QUESTIONNAIRE_LOCALE).setDataType(DataType.TEXT));
    questionnaireRunVariable.addVariable(new Variable(QUESTIONNAIRE_USER).setDataType(DataType.TEXT));
    questionnaireRunVariable.addVariable(new Variable(QUESTIONNAIRE_TIMESTART).setDataType(DataType.DATE));
    questionnaireRunVariable.addVariable(new Variable(QUESTIONNAIRE_TIMEEND).setDataType(DataType.DATE));

    return questionnaireVariable;
  }

  public Variable getVariable(Question question) {
    Variable entity = null;

    // simple question or boiler plate
    if(question.getQuestions().size() == 0) {
      entity = getQuestionVariable(question, question.getQuestionCategories());
    } else if(question.getQuestionCategories().size() == 0) {
      // sub questions
      entity = getQuestionVariable(question, null);
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
        entity = getQuestionVariable(question, null);
        for(Question subQuestion : question.getQuestions()) {
          Variable variable = getQuestionVariable(subQuestion, question.getQuestionCategories());

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

  /**
   * Question variable, with always a sub variable for the attached comment.
   * @param question
   * @return
   */
  private Variable getQuestionVariable(Question question, List<QuestionCategory> questionCategories) {
    Variable variable = new Variable(question.getName());
    if(question.isMultiple()) {
      variable.setMultiple(true);
    }

    if(!question.isBoilerPlate() && !question.hasDataSource()) {
      variable.addVariable(new Variable(QUESTION_COMMENT).setDataType(DataType.TEXT));
    }
    variable.addVariable(new Variable(QUESTION_ACTIVE).setDataType(DataType.BOOLEAN));

    // log.info("question.name={} questionCategories={}", question.getName(), questionCategories);
    if(questionCategories != null) {
      for(QuestionCategory questionCategory : questionCategories) {
        variable.addVariable(getCategoryVariable(questionCategory));
      }
      if(variable.getCategories().size() > 0) {
        variable.setDataType(DataType.TEXT);
      }
    }

    return variable;
  }

  /**
   * Category variable which is a container of open answer variables.
   * @param category
   * @return null if no open answer variables
   */
  private Variable getCategoryVariable(QuestionCategory questionCategory) {
    Variable categoryVariable = null;

    categoryVariable = new Category(questionCategory.getCategory().getName(), questionCategory.getExportName()).setEscape(questionCategory.getCategory().isEscape());

    // one variable to show if category is selected or not
    categoryVariable.setDataType(DataType.BOOLEAN);

    // one variable per open answer
    OpenAnswerDefinition open = questionCategory.getCategory().getOpenAnswerDefinition();
    if(open != null) {
      categoryVariable.addVariable(getOpenAnswerVariable(open));
      for(OpenAnswerDefinition openChild : open.getOpenAnswerDefinitions()) {
        categoryVariable.addVariable(getOpenAnswerVariable(openChild));
      }
    }

    return categoryVariable;
  }

  /**
   * Open answer variable.
   * @param openAnswerDefinition
   * @return
   */
  private Variable getOpenAnswerVariable(OpenAnswerDefinition openAnswerDefinition) {
    Variable variable = null;

    variable = new Variable(openAnswerDefinition.getName()).setDataType(openAnswerDefinition.getDataType()).setUnit(openAnswerDefinition.getUnit());

    return variable;
  }

  public VariableData getVariableData(QuestionnaireParticipantService questionnaireParticipantService, Participant participant, Variable variable, VariableData variableData, Questionnaire questionnaire) {

    // variable is a question
    if(variable.getCategories().size() > 0) {
      List<CategoryAnswer> answers = questionnaireParticipantService.getCategoryAnswers(participant, questionnaire.getName(), variable.getName());
      if(answers != null) {
        for(CategoryAnswer answer : answers) {
          variableData.addData(DataBuilder.buildText(answer.getCategoryName()));
        }
      }
    }

    // variable is about an open answer or the question comment or the questionnaire or a category
    else if(variable.getDataType() != null) {
      if(Category.class.isInstance(variable)) {
        // category was selected
        CategoryAnswer categoryAnswer = questionnaireParticipantService.getCategoryAnswer(participant, questionnaire.getName(), variable.getParent().getName(), variable.getName());
        if(categoryAnswer != null) {
          variableData.addData(DataBuilder.buildBoolean(true));
        }
      } else if(variable.getParent().getName().equals(QUESTIONNAIRE_RUN)) {
        QuestionnaireParticipant questionnaireParticipant = questionnaireParticipantService.getQuestionnaireParticipant(participant, questionnaire.getName());
        if(questionnaireParticipant != null) {
          if(variable.getName().equals(QUESTIONNAIRE_LOCALE) && questionnaireParticipant.getLocale() != null) {
            variableData.addData(DataBuilder.buildText(questionnaireParticipant.getLocale().toString()));
          } else if(variable.getName().equals(QUESTIONNAIRE_VERSION) && questionnaireParticipant.getQuestionnaireVersion() != null) {
            variableData.addData(DataBuilder.buildText(questionnaireParticipant.getQuestionnaireVersion()));
          } else if(variable.getName().equals(QUESTIONNAIRE_USER) && questionnaireParticipant.getUser() != null) {
            variableData.addData(DataBuilder.buildText(questionnaireParticipant.getUser().getLogin()));
          } else if(variable.getName().equals(QUESTIONNAIRE_TIMESTART) && questionnaireParticipant.getTimeStart() != null) {
            variableData.addData(DataBuilder.buildDate(questionnaireParticipant.getTimeStart()));
          } else if(variable.getName().equals(QUESTIONNAIRE_TIMEEND) && questionnaireParticipant.getTimeEnd() != null) {
            variableData.addData(DataBuilder.buildDate(questionnaireParticipant.getTimeEnd()));
          }
        }
      } else if(variable.getName().equals(QUESTION_COMMENT)) {
        // question comment variable
        String comment = questionnaireParticipantService.getQuestionComment(participant, questionnaire.getName(), variable.getParent().getName());
        if(comment != null) {
          variableData.addData(DataBuilder.buildText(comment));
        }
      } else if(variable.getName().equals(QUESTION_ACTIVE)) {
        Boolean active = questionnaireParticipantService.isQuestionActive(participant, questionnaire.getName(), variable.getParent().getName());
        if(active != null) {
          variableData.addData(DataBuilder.buildBoolean(active));
        }
      } else {
        // get the open answer
        OpenAnswer answer = questionnaireParticipantService.getOpenAnswer(participant, questionnaire.getName(), variable.getParent().getParent().getName(), variable.getParent().getName(), variable.getName());
        if(answer != null) {
          variableData.addData(answer.getData());
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
