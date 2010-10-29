/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.Variable;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.data.ParticipantPropertyDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.data.QuestionnaireDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.QuestionBuilder;
import org.obiba.onyx.util.data.Data;

/**
 *
 */
public class QuestionnaireDataSourceConverter {

  private static final String GENDER = "gender";

  private static final String ADMIN_PARTICIPANT_GENDER_PATH = "Admin.Participant.gender";

  // private static Logger logger = LoggerFactory.getLogger(QuestionnaireDataSourceConverter.class);

  public static void convertToVariableDataSources(Questionnaire questionnaire) {

    QuestionnaireBuilder builder = QuestionnaireBuilder.getInstance(questionnaire);

    if(questionnaire.getQuestionnaireCache() == null) {
      QuestionnaireFinder.getInstance(questionnaire).buildQuestionnaireCache();
    }
    for(Question question : questionnaire.getQuestionnaireCache().getQuestionCache().values()) {
      IDataSource condition = question.getCondition();
      if(condition == null) continue;
      if(condition instanceof ComputingDataSource) {
        ComputingDataSource computingDataSource = (ComputingDataSource) condition;
        List<IDataSource> dataSources = computingDataSource.getDataSources();
        for(IDataSource dataSource : new ArrayList<IDataSource>(dataSources)) {
          IDataSource newDataSource = convert(dataSource, question, questionnaire);
          if(newDataSource != dataSource) {
            int index = dataSources.indexOf(dataSource);
            dataSources.remove(dataSource);
            dataSources.add(index, newDataSource);
          }
        }
      } else if(condition instanceof ComparingDataSource) {
        ComparingDataSource comparingDataSource = (ComparingDataSource) condition;
        String variableName = null;
        String property = ((ParticipantPropertyDataSource) comparingDataSource.getDataSourceLeft()).getProperty();
        if(GENDER.equals(property)) {
          variableName = ADMIN_PARTICIPANT_GENDER_PATH;
        } else {
          throw new IllegalArgumentException("Unsupported property[ " + property + " for dataSource " + comparingDataSource.getDataSourceLeft() + " for question " + question);
        }
        Data data = ((FixedDataSource) comparingDataSource.getDataSourceRight()).getData(null);
        String value = data.getValueAsString();
        variableName += "." + value;

        QuestionBuilder questionBuilder = QuestionBuilder.inQuestion(builder, question);
        Variable variable = QuestionnaireFinder.getInstance(questionnaire).findVariable(variableName);
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
          questionBuilder.setQuestionnaireVariableCondition(variableName, "${'" + variableName + "'}.any(" + scriptValue + ")");
        } else {
          question.setCondition(new VariableDataSource(questionnaire.getName() + ":" + variable.getName()));
        }
      }
    }
  }

  private static IDataSource convert(IDataSource dataSource, Question question, Questionnaire questionnaire) {
    if(dataSource instanceof VariableDataSource) {
      return dataSource; // no conversion
    }
    if(dataSource instanceof QuestionnaireDataSource) {
      return convertQuestionnaireDataSource((QuestionnaireDataSource) dataSource, questionnaire);
    }
    throw new IllegalArgumentException("DataSource " + dataSource + " was not converted to VariableDataSource for question " + question);
  }

  private static VariableDataSource convertQuestionnaireDataSource(QuestionnaireDataSource dataSource, Questionnaire questionnaire) {
    StringBuilder path = new StringBuilder(isBlank(dataSource.getQuestionnaire()) ? questionnaire.getName() : dataSource.getQuestionnaire());
    path.append(":" + dataSource.getQuestion());
    if(isNotBlank(dataSource.getCategory())) path.append("." + dataSource.getCategory());
    if(isNotBlank(dataSource.getOpenAnswerDefinition())) path.append("." + dataSource.getOpenAnswerDefinition());
    return new VariableDataSource(path.toString());
  }

}
