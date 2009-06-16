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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.extensions.validation.validator.RfcCompliantEmailAddressValidator;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.NumberValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.Category;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableHelper;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.SimplifiedUIPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.quartz.engine.variable.IQuestionToVariableMappingStrategy;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataValidator;
import org.obiba.onyx.wicket.data.IDataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.NoSuchMessageException;

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

  private QuestionnaireBundle questionnaireBundle;

  // choose the one with the most properties declared
  private IPropertyKeyProvider propertyKeyProvider = new SimplifiedUIPropertyKeyProviderImpl();

  public Variable getVariable(Questionnaire questionnaire) {
    Variable questionnaireVariable = new Variable(questionnaire.getName());

    addLocalizedAttributes(questionnaireVariable, questionnaire);

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

    addLocalizedAttributes(variable, question);

    if(question.getCondition() != null) {
      VariableHelper.addConditionAttribute(variable, question.getCondition().toString());
    }

    // get the min/max settings : for questions having a parent question, if no settings is found parent settings is
    // used.
    Integer minCount = question.getMinCount();
    if(minCount == null && question.getParentQuestion() != null) {
      minCount = question.getParentQuestion().getMinCount();
    }
    Integer maxCount = question.getMaxCount();
    if(maxCount == null && question.getParentQuestion() != null) {
      maxCount = question.getParentQuestion().getMaxCount();
    }

    VariableHelper.addRequiredAttribute(variable, question.isRequired());

    if(minCount != null && minCount > 1) {
      VariableHelper.addMinCountAttribute(variable, minCount);
    }

    if(maxCount != null) {
      VariableHelper.addMaxCountAttribute(variable, maxCount);
    }

    return variable;
  }

  private void addLocalizedAttributes(Variable variable, IQuestionnaireElement localizable) {
    if(questionnaireBundle != null) {
      for(Locale locale : questionnaireBundle.getAvailableLanguages()) {
        for(String property : propertyKeyProvider.getProperties(localizable)) {
          try {
            String stringResource = QuestionnaireStringResourceModelHelper.getMessage(questionnaireBundle, localizable, property, null, locale);
            if(stringResource.trim().length() > 0) {
              String noHTMLString = stringResource.replaceAll("\\<.*?\\>", "");
              VariableHelper.addAttribute(variable, locale, property, noHTMLString);
            }
          } catch(NoSuchMessageException ex) {
            // ignored
          }
        }
      }
    }
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

    addLocalizedAttributes(categoryVariable, questionCategory);

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

    addLocalizedAttributes(variable, openAnswerDefinition);

    if(openAnswerDefinition.getDataSource() != null) {
      VariableHelper.addSourceAttribute(variable, openAnswerDefinition.getDataSource().toString());
    }

    List<String> validations = new ArrayList<String>();
    if(openAnswerDefinition.getDataValidators().size() > 0) {

      for(IDataValidator v : openAnswerDefinition.getDataValidators()) {
        if(v instanceof DataValidator) {
          String validator = validatorToString(((DataValidator) v).getValidator());
          if(validator != null) {
            validations.add(validator);
          }
        }
      }
    }

    if(openAnswerDefinition.getValidationDataSources().size() > 0) {
      validations.add(openAnswerDefinition.getValidationDataSources().toString());
    }
    if(validations.size() > 0) {
      VariableHelper.addValidationAttribute(variable, validations.toString());
    }

    VariableHelper.addRequiredAttribute(variable, openAnswerDefinition.isRequired());

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

  public void setQuestionnaireBundle(QuestionnaireBundle bundle) {
    this.questionnaireBundle = bundle;
  }

  /**
   * Turns a IValidator into a String.
   * @param validator
   * @return null if validator not identified.
   */
  private String validatorToString(IValidator validator) {
    if(validator == null) {
      return null;
    }

    if(validator instanceof NumberValidator) {
      if(validator instanceof NumberValidator.DoubleMaximumValidator) {
        return "Number.Maximum[" + ((NumberValidator.DoubleMaximumValidator) validator).getMaximum() + "]";
      } else if(validator instanceof NumberValidator.DoubleMinimumValidator) {
        return "Number.Minimum[" + ((NumberValidator.DoubleMinimumValidator) validator).getMinimum() + "]";
      } else if(validator instanceof NumberValidator.DoubleRangeValidator) {
        return "Number.Range[" + ((NumberValidator.DoubleRangeValidator) validator).getMinimum() + ", " + ((NumberValidator.DoubleRangeValidator) validator).getMaximum() + "]";
      } else if(validator instanceof NumberValidator.MaximumValidator) {
        return "Number.Maximum[" + ((NumberValidator.MaximumValidator) validator).getMaximum() + "]";
      } else if(validator instanceof NumberValidator.MinimumValidator) {
        return "Number.Minimum[" + ((NumberValidator.MinimumValidator) validator).getMinimum() + "]";
      } else if(validator instanceof NumberValidator.RangeValidator) {
        return "Number.Range[" + ((NumberValidator.RangeValidator) validator).getMinimum() + ", " + ((NumberValidator.RangeValidator) validator).getMaximum() + "]";
      }
    } else if(validator instanceof StringValidator) {
      if(validator instanceof StringValidator.ExactLengthValidator) {
        return "String.ExactLength[" + ((StringValidator.ExactLengthValidator) validator).getLength() + "]";
      } else if(validator instanceof StringValidator.LengthBetweenValidator) {
        return "String.LengthBetween[" + ((StringValidator.LengthBetweenValidator) validator).getMinimum() + ", " + ((StringValidator.LengthBetweenValidator) validator).getMaximum() + "]";
      } else if(validator instanceof StringValidator.MaximumLengthValidator) {
        return "String.MaximumLength[" + ((StringValidator.MaximumLengthValidator) validator).getMaximum() + "]";
      } else if(validator instanceof StringValidator.MinimumLengthValidator) {
        return "String.MinimumLength[" + ((StringValidator.MinimumLengthValidator) validator).getMinimum() + "]";
      } else if(validator instanceof PatternValidator) {
        if(validator instanceof EmailAddressValidator) {
          return "String.EmailAddress";
        } else if(validator instanceof RfcCompliantEmailAddressValidator) {
          return "String.RfcCompliantEmailAddress";
        } else {
          return "String.Pattern[" + ((PatternValidator) validator).getPattern() + "]";
        }
      }
    }

    return validator.getClass().getSimpleName().replaceAll("Validator", "");
  }

}
