/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire.utils;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.obiba.onyx.magma.OnyxAdminVariableValueSourceFactory.ONYX_ADMIN_PREFIX;
import static org.obiba.onyx.magma.OnyxAdminVariableValueSourceFactory.PARTICIPANT;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.IntegerType;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.CurrentDateSource;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.data.ParticipantPropertyDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.data.QuestionnaireDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.QuestionBuilder;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnairePanel;
import org.obiba.onyx.quartz.editor.variable.VariableUtils;
import org.obiba.onyx.util.data.Data;

/**
 *
 */
public class QuestionnaireConverter {

  // private static final String GENDER = "gender";

  // private static final String ADMIN_PARTICIPANT_GENDER_NAME = "Admin.Participant.gender";

  // private static final String ADMIN_PARTICIPANT_GENDER_PATH = "Participants:Admin.Participant.gender";

  private static final String DATE_NAME = "Date.Now";

  private static final String DATE_YEAR_NAME = "Date.Now.Year";

  private static final String DATE_MONTH_NAME = "Date.Now.Month";

  private final Questionnaire questionnaire;

  private final QuestionnaireBuilder questionnaireBuilder;

  private final QuestionnaireFinder questionnaireFinder;

  public static QuestionnaireConverter getInstance(Questionnaire questionnaire) {
    return new QuestionnaireConverter(questionnaire);
  }

  private QuestionnaireConverter(Questionnaire questionnaire) {
    this.questionnaire = questionnaire;
    questionnaireBuilder = QuestionnaireBuilder.getInstance(questionnaire);
    questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
    if(questionnaire.getQuestionnaireCache() == null) {
      questionnaireFinder.buildQuestionnaireCache();
    }
  }

  public void convert() throws QuestionnaireConverterException {

    QuestionnairePanel.guessUIType(questionnaire);

    for(Question question : questionnaire.getQuestionnaireCache().getQuestionCache().values()) {

      QuestionBuilder questionBuilder = QuestionBuilder.inQuestion(questionnaireBuilder, question);

      // convert question condition
      IDataSource condition = question.getCondition();
      if(condition == null) continue;
      if(condition instanceof QuestionnaireDataSource) {
        question.setCondition(new VariableDataSource(getVariablePath(condition, question)));
      } else if(condition instanceof ComputingDataSource) {
        questionBuilder.setQuestionnaireVariableCondition(question.getName() + ".condition", getConvertedExpression((ComputingDataSource) condition, question));
      } else if(condition instanceof ComparingDataSource) {
        String variableName = findOrCreateComparingVariable(question, (ComparingDataSource) condition);
        questionBuilder.setQuestionnaireVariableCondition(variableName);
      }

      // convert open-answer validator
      for(OpenAnswerDefinition openAnswer : questionnaire.getQuestionnaireCache().getOpenAnswerDefinitionCache().values()) {
        int i = 1;
        List<ComparingDataSource> validationDataSources = new ArrayList<ComparingDataSource>(openAnswer.getValidationDataSources());
        openAnswer.getValidationDataSources().clear();
        for(ComparingDataSource comparingDataSource : validationDataSources) {
          String variablePath = null;
          IDataSource dataSourceRight = comparingDataSource.getDataSourceRight();
          if(dataSourceRight instanceof FixedDataSource || dataSourceRight instanceof ComputingDataSource) {
            String variableName = openAnswer.getName() + ".validation";
            if(validationDataSources.size() > 1) {
              variableName += "." + i++;
            }

            if(dataSourceRight instanceof FixedDataSource) {
              Data data = ((FixedDataSource) dataSourceRight).getData(null);
              questionnaireBuilder.withVariable(variableName, VariableUtils.convertToValueType(data.getType()), data.getValueAsString());
            } else if(dataSourceRight instanceof ComputingDataSource) {
              ComputingDataSource computingDataSource = (ComputingDataSource) dataSourceRight;
              questionnaireBuilder.withVariable(variableName, VariableUtils.convertToValueType(computingDataSource.getType()), getConvertedExpression(computingDataSource, question));
            }
            variablePath = questionnaire.getName() + ":" + variableName;

          } else if(dataSourceRight instanceof QuestionnaireDataSource || dataSourceRight instanceof CurrentDateSource || dataSourceRight instanceof VariableDataSource || dataSourceRight instanceof ParticipantPropertyDataSource) {
            variablePath = getVariablePath(dataSourceRight, question);
          } else {
            throw new QuestionnaireConverterException("Unsupported dataSource[" + dataSourceRight + "] for open answer " + openAnswer.getName() + " in questionnaire " + questionnaire);
          }
          questionnaireBuilder.inOpenAnswerDefinition(openAnswer.getName()).addValidator(comparingDataSource.getComparisonOperator(), variablePath);
        }
      }
    }
  }

  private String findOrCreateComparingVariable(Question question, ComparingDataSource comparingDataSource) {
    String variableName = null;
    String variablePath = null;
    String property = ((ParticipantPropertyDataSource) comparingDataSource.getDataSourceLeft()).getProperty();
    if("gender".equals(property)) {
      variableName = ONYX_ADMIN_PREFIX + "." + PARTICIPANT + ".gender";
      variablePath = "Participants:" + variableName;
    } else if("birthYear".equals(property)) {
      variableName = ONYX_ADMIN_PREFIX + "." + PARTICIPANT + ".birthYear";
      variablePath = "Participants:" + variableName;
    } else if("age".equals(property)) {
      variableName = ONYX_ADMIN_PREFIX + "." + PARTICIPANT + ".age";
      variablePath = "Participants:" + variableName;
    } else {
      throw new QuestionnaireConverterException("Unsupported property[ " + property + " for dataSource " + comparingDataSource.getDataSourceLeft() + " for question " + question);
    }

    Data data = ((FixedDataSource) comparingDataSource.getDataSourceRight()).getData(null);
    String value = data.getValueAsString();
    if(StringUtils.isBlank(variableName)) variableName = question.getName();
    variableName += "." + value;

    Variable variable = questionnaireFinder.findVariable(variableName);
    if(variable == null) {
      String scriptValue = null;
      switch(data.getType()) {
      case DECIMAL:
      case INTEGER:
        scriptValue = value;
        break;
      default:
        scriptValue = "'" + value + "'";
      }
      questionnaireBuilder.withVariable(variableName, BooleanType.get(), "$('" + variablePath + "')." + comparingDataSource.getComparisonOperator() + "(" + scriptValue + ")");
    }
    return variableName;
  }

  private String getConvertedExpression(ComputingDataSource computingDataSource, Question question) {
    int i = 1;
    String expression = computingDataSource.getExpression();
    for(IDataSource dataSource : computingDataSource.getDataSources()) {
      expression = expression.replaceAll("\\$" + i++, "\\$('" + getVariablePath(dataSource, question) + "').value()");
    }
    return expression;
  }

  private String getVariablePath(IDataSource dataSource, Question question) {
    if(dataSource instanceof VariableDataSource) {
      return ((VariableDataSource) dataSource).getTableName() + ":" + ((VariableDataSource) dataSource).getVariableName();
    }
    if(dataSource instanceof QuestionnaireDataSource) {
      QuestionnaireDataSource questionnaireDataSource = (QuestionnaireDataSource) dataSource;
      StringBuilder path = new StringBuilder(isBlank(questionnaireDataSource.getQuestionnaire()) ? questionnaire.getName() : questionnaireDataSource.getQuestionnaire());
      path.append(":" + questionnaireDataSource.getQuestion());
      if(isNotBlank(questionnaireDataSource.getCategory())) path.append("." + questionnaireDataSource.getCategory());
      if(isNotBlank(questionnaireDataSource.getOpenAnswerDefinition())) path.append("." + questionnaireDataSource.getOpenAnswerDefinition());
      return path.toString();
    }
    if(dataSource instanceof ComparingDataSource) {
      String variableName = findOrCreateComparingVariable(question, (ComparingDataSource) dataSource);
      return questionnaire.getName() + ":" + variableName;
    }
    if(dataSource instanceof ParticipantPropertyDataSource) {
      String property = ((ParticipantPropertyDataSource) dataSource).getProperty();
      if("gender".equals(property)) {
        return "Participants:" + ONYX_ADMIN_PREFIX + "." + PARTICIPANT + ".gender";
      } else if("birthYear".equals(property)) {
        return "Participants:" + ONYX_ADMIN_PREFIX + "." + PARTICIPANT + ".birthYear";
      } else if("age".equals(property)) {
        return "Participants:" + ONYX_ADMIN_PREFIX + "." + PARTICIPANT + ".age";
      } else {
        throw new QuestionnaireConverterException("Unsupported property[ " + property + " for dataSource " + dataSource);
      }
    }
    if(dataSource instanceof CurrentDateSource) {
      CurrentDateSource currentDateSource = (CurrentDateSource) dataSource;
      String variableName = null;
      if(currentDateSource.getField() == null) {
        variableName = DATE_NAME;
        if(questionnaireFinder.findVariable(variableName) == null) {
          questionnaireBuilder.withVariable(variableName, IntegerType.get(), "now().value()");
        }
      } else {
        switch(currentDateSource.getField()) {
        case YEAR:
          variableName = DATE_YEAR_NAME;
          if(questionnaireFinder.findVariable(variableName) == null) {
            questionnaireBuilder.withVariable(variableName, IntegerType.get(), "now().year().value()");
          }
          break;
        case MONTH:
          variableName = DATE_MONTH_NAME;
          if(questionnaireFinder.findVariable(variableName) == null) {
            questionnaireBuilder.withVariable(variableName, IntegerType.get(), "now().month().value()");
          }
          break;
        case DAY_OF_YEAR:
          variableName = DATE_MONTH_NAME;
          if(questionnaireFinder.findVariable(variableName) == null) {
            questionnaireBuilder.withVariable(variableName, IntegerType.get(), "now().dayOfYear().value()");
          }
          break;
        case DAY_OF_MONTH:
          variableName = DATE_MONTH_NAME;
          if(questionnaireFinder.findVariable(variableName) == null) {
            questionnaireBuilder.withVariable(variableName, IntegerType.get(), "now().dayOfMonth().value()");
          }
          break;
        case DAY_OF_WEEK:
          variableName = DATE_MONTH_NAME;
          if(questionnaireFinder.findVariable(variableName) == null) {
            questionnaireBuilder.withVariable(variableName, IntegerType.get(), "now().dayOfWeek().value()");
          }
          break;
        case WEEK_OF_MONTH:
          variableName = DATE_MONTH_NAME;
          if(questionnaireFinder.findVariable(variableName) == null) {
            questionnaireBuilder.withVariable(variableName, IntegerType.get(), "now().weekOfMonth().value()");
          }
          break;
        case WEEK_OF_YEAR:
          variableName = DATE_MONTH_NAME;
          if(questionnaireFinder.findVariable(variableName) == null) {
            questionnaireBuilder.withVariable(variableName, IntegerType.get(), "now().weekOfYear().value()");
          }
          break;
        default:
          throw new QuestionnaireConverterException("Unsupported dateField for " + currentDateSource);
        }
      }
      return questionnaire.getName() + ":" + variableName;
    }
    throw new QuestionnaireConverterException("DataSource " + dataSource + " was not converted to VariableDataSource for question " + question);
  }

}
