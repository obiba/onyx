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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.extensions.validation.validator.RfcCompliantEmailAddressValidator;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.MaximumValidator;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.Category;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableHelper;
import org.obiba.onyx.quartz.core.data.QuestionnaireDataSource;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireMetric;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.SimplifiedUIPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.quartz.engine.variable.IQuestionToVariableMappingStrategy;
import org.obiba.onyx.util.data.Data;
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

  public static final String QUESTIONNAIRE_METRIC = "QuestionnaireMetric";

  public static final String QUESTIONNAIRE_PAGE = "page";

  public static final String QUESTIONNAIRE_DURATION = "duration";

  public static final String QUESTIONNAIRE_SECTION = "section";

  public static final String QUESTIONNAIRE_QUESTION_COUNT = "questionCount";

  public static final String QUESTIONNAIRE_MISSING_COUNT = "missingCount"; // escape count

  private QuestionnaireBundle questionnaireBundle;

  // choose the one with the most properties declared
  private IPropertyKeyProvider propertyKeyProvider = new SimplifiedUIPropertyKeyProviderImpl();

  private IVariablePathNamingStrategy variablePathNamingStrategy;

  public void setVariablePathNamingStrategy(IVariablePathNamingStrategy variablePathNamingStrategy) {
    this.variablePathNamingStrategy = variablePathNamingStrategy;
  }

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

    // add questionnaire metric variable
    Variable questionnaireMetric = questionnaireVariable.addVariable(new Variable(QUESTIONNAIRE_METRIC).setDataType(DataType.TEXT).setRepeatable(true));
    questionnaireMetric.addVariable(new Variable(QUESTIONNAIRE_PAGE).setDataType(DataType.TEXT));
    questionnaireMetric.addVariable(new Variable(QUESTIONNAIRE_DURATION).setDataType(DataType.INTEGER).setUnit("s"));
    questionnaireMetric.addVariable(new Variable(QUESTIONNAIRE_SECTION).setDataType(DataType.TEXT));
    questionnaireMetric.addVariable(new Variable(QUESTIONNAIRE_QUESTION_COUNT).setDataType(DataType.INTEGER));
    questionnaireMetric.addVariable(new Variable(QUESTIONNAIRE_MISSING_COUNT).setDataType(DataType.INTEGER));

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

    Page page = question.getPage();
    if(page != null) {
      VariableHelper.addGroupAttribute(variable, page.getName());

      Section currentSection = page.getSection();
      String sections = "";
      if(currentSection != null) {
        sections = currentSection.getName();
      }
      while(currentSection != null) {
        currentSection = currentSection.getParentSection();
        if(currentSection != null) {
          sections = currentSection.getName() + "/" + sections;
        }
      }
      if(sections.length() > 0) {
        VariableHelper.addAttribute(variable, "sections", sections);
      }
    }

    return variable;
  }

  private void addLocalizedAttributes(Variable variable, IQuestionnaireElement localizable) {
    if(questionnaireBundle != null) {
      boolean open = localizable instanceof OpenAnswerDefinition;
      for(Locale locale : questionnaireBundle.getAvailableLanguages()) {
        for(String property : propertyKeyProvider.getProperties(localizable)) {
          if(open) {
            // the property may be the default value, not to be added to open answer definition annotations
            OpenAnswerDefinition openAnswerDefinition = (OpenAnswerDefinition) localizable;
            boolean defaultValueProperty = false;
            for(Data defaultValue : openAnswerDefinition.getDefaultValues()) {
              if(defaultValue.getValueAsString().equals(property)) {
                defaultValueProperty = true;
                break;
              }
            }
            if(defaultValueProperty) {
              continue;
            }
          }
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
   * Localization of the open answer category.
   * @param category
   * @param openAnswerDefinition
   * @param defaultValue
   */
  private void addLocalizedAttributes(Category category, OpenAnswerDefinition openAnswerDefinition, String defaultValue) {
    if(questionnaireBundle != null) {
      for(Locale locale : questionnaireBundle.getAvailableLanguages()) {
        try {
          String stringResource = QuestionnaireStringResourceModelHelper.getMessage(questionnaireBundle, openAnswerDefinition, defaultValue, null, locale);
          if(stringResource.trim().length() > 0) {
            String noHTMLString = stringResource.replaceAll("\\<.*?\\>", "");
            VariableHelper.addAttribute(category, locale, VariableHelper.LABEL, noHTMLString);
          }
        } catch(NoSuchMessageException ex) {
          // ignored
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
      if(open.getOpenAnswerDefinitions().size() == 0) {
        categoryVariable.addVariable(getOpenAnswerVariable(questionCategory, open));
      } else {
        // we do not have values for englobing open answer
        // only for children
        for(OpenAnswerDefinition openChild : open.getOpenAnswerDefinitions()) {
          categoryVariable.addVariable(getOpenAnswerVariable(questionCategory, openChild));
        }
      }
    }

    addLocalizedAttributes(categoryVariable, questionCategory);

    return categoryVariable;
  }

  /**
   * Open answer variable.
   * @param questionCategory
   * @param openAnswerDefinition
   * @return
   */
  private Variable getOpenAnswerVariable(QuestionCategory questionCategory, OpenAnswerDefinition openAnswerDefinition) {
    Variable variable = null;

    variable = new Variable(openAnswerDefinition.getName()).setDataType(openAnswerDefinition.getDataType()).setUnit(openAnswerDefinition.getUnit());

    if(openAnswerDefinition.getDataType().equals(DataType.TEXT)) {
      // only textual open answers may be categorical, otherwise it would not be consistent with our categorical
      // variable handling
      if(openAnswerDefinition.getDefaultValues().size() > 0) {
        int pos = 1;
        for(Data defaultValue : openAnswerDefinition.getDefaultValues()) {
          Category categoryVariable = new Category(defaultValue.getValueAsString(), Integer.toString(pos++));
          variable.addCategory(categoryVariable);
          addLocalizedAttributes(categoryVariable, openAnswerDefinition, defaultValue.getValueAsString());
        }
      }
    }

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

    // an open answer is implicitly always conditioned by the selection of its parent category.
    if(questionnaireBundle != null) {
      VariableHelper.addConditionAttribute(variable, new QuestionnaireDataSource(questionnaireBundle.getQuestionnaire().getName(), questionCategory.getQuestion().getName(), questionCategory.getCategory().getName()).toString());
    }

    return variable;
  }

  public VariableData getVariableData(QuestionnaireParticipantService questionnaireParticipantService, Participant participant, Variable variable, VariableData variableData, Questionnaire questionnaire) {

    // variable is a question
    if(variable.getCategories().size() > 0 && !Category.class.isInstance(variable.getParent())) {
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
        if(variable.getParent().getParent() instanceof Category) {
          // open answer default answer was selected
          // get the open answer
          OpenAnswer answer = questionnaireParticipantService.getOpenAnswer(participant, questionnaire.getName(), variable.getParent().getParent().getParent().getName(), variable.getParent().getParent().getName(), variable.getParent().getName());
          if(answer != null && variable.getName().equals(answer.getData().getValueAsString())) {
            variableData.addData(DataBuilder.buildBoolean(true));
          }
        } else {
          // real category was selected
          CategoryAnswer categoryAnswer = questionnaireParticipantService.getCategoryAnswer(participant, questionnaire.getName(), variable.getParent().getName(), variable.getName());
          if(categoryAnswer != null) {
            variableData.addData(DataBuilder.buildBoolean(true));
          }
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
      } else if(variable.getParent().getName().equals(QUESTIONNAIRE_METRIC) || variable.getName().equals(QUESTIONNAIRE_METRIC)) {
        QuestionnaireParticipant questionnaireParticipant = questionnaireParticipantService.getQuestionnaireParticipant(participant, questionnaire.getName());
        QuestionnaireFinder questionnaireFinder = new QuestionnaireFinder(questionnaireBundle.getQuestionnaire());

        if(questionnaireParticipant != null) {
          Data data = null;

          Map<String, Integer> questionCountMap = new HashMap<String, Integer>();
          Map<String, Integer> missingCountMap = new HashMap<String, Integer>();

          for(QuestionnaireMetric questionnaireMetric : questionnaireParticipant.getQuestionnaireMetrics()) {
            Page page = questionnaireFinder.findPage(questionnaireMetric.getPage());

            if(variable.getName().equals(QUESTIONNAIRE_METRIC)) {
              variableData.addData(DataBuilder.buildText(questionnaireMetric.getId().toString()));
            } else if(variable.getName().equals(QUESTIONNAIRE_PAGE)) {
              data = DataBuilder.buildText(questionnaireMetric.getPage());
            } else if(variable.getName().equals(QUESTIONNAIRE_DURATION)) {
              data = DataBuilder.buildInteger(questionnaireMetric.getDuration());
            } else if(variable.getName().equals(QUESTIONNAIRE_SECTION)) {
              data = DataBuilder.buildText(page.getSection().getName());
            } else if(variable.getName().equals(QUESTIONNAIRE_QUESTION_COUNT)) {
              int questionCount = getQuestionCount(questionCountMap, questionnaireParticipant, questionnaireFinder, page);
              data = DataBuilder.buildInteger(questionCount);
            } else if(variable.getName().equals(QUESTIONNAIRE_MISSING_COUNT)) {
              int missingCount = getMissingCount(missingCountMap, questionnaireParticipant, questionnaireFinder, page);
              data = DataBuilder.buildInteger(missingCount);
            }

            if(data != null) {
              VariableData childVarData = new VariableData(variablePathNamingStrategy.getPath(variable, QUESTIONNAIRE_METRIC, questionnaireMetric.getId().toString()));
              variableData.addVariableData(childVarData);
              childVarData.addData(data);
            }
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

    if(validator instanceof MaximumValidator) {
      return "Number.Maximum[" + ((MaximumValidator) validator).getMaximum() + "]";
    } else if(validator instanceof MinimumValidator) {
      return "Number.Minimum[" + ((MinimumValidator) validator).getMinimum() + "]";
    } else if(validator instanceof RangeValidator) {
      return "Number.Range[" + ((RangeValidator) validator).getMinimum() + ", " + ((RangeValidator) validator).getMaximum() + "]";
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

  private int getQuestionCount(Map<String, Integer> questionCountCache, QuestionnaireParticipant questionnaireParticipant, QuestionnaireFinder questionnaireFinder, Page page) {
    Integer questionCount = questionCountCache.get(page.getName());

    if(questionCount == null) {
      int total = 0;

      for(QuestionAnswer questionAnswer : questionnaireParticipant.getParticipantAnswers()) {
        if(questionAnswer.isActive()) {
          Question question = questionnaireFinder.findQuestion(questionAnswer.getQuestionName());
          if(question.getPage().getName().equals(page.getName())) {
            total++;
          }
        }
      }

      questionCount = total;
      questionCountCache.put(page.getName(), questionCount);
    }

    return questionCount;
  }

  private int getMissingCount(Map<String, Integer> missingCountCache, QuestionnaireParticipant questionnaireParticipant, QuestionnaireFinder questionnaireFinder, Page page) {
    Integer missingCount = missingCountCache.get(page.getName());

    if(missingCount == null) {
      int total = 0;

      for(QuestionAnswer questionAnswer : questionnaireParticipant.getParticipantAnswers()) {
        if(questionAnswer.isActive()) {
          Question question = questionnaireFinder.findQuestion(questionAnswer.getQuestionName());
          if(question.getPage().getName().equals(page.getName())) {
            for(CategoryAnswer categoryAnswer : questionAnswer.getCategoryAnswers()) {
              QuestionCategory questionCategory = questionnaireFinder.findQuestionCategory(question.getName(), categoryAnswer.getCategoryName());
              if(questionCategory != null && questionCategory.getCategory() != null && questionCategory.getCategory().isEscape()) {
                total++;
                break;
              }
            }
          }
        }
      }

      missingCount = total;
      missingCountCache.put(page.getName(), missingCount);
    }

    return missingCount;
  }
}
