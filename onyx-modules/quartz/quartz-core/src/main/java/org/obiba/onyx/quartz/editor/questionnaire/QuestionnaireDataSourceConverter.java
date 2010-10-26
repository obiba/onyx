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

import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.data.ParticipantPropertyDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.data.QuestionnaireDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;

/**
 *
 */
public class QuestionnaireDataSourceConverter {

  private static final String GENDER = "gender";

  private static final String ADMIN_PARTICIPANT_GENDER_PATH = "Admin.Participant.gender";

  // private static Logger logger = LoggerFactory.getLogger(QuestionnaireDataSourceConverter.class);

  public static void convertToVariableDataSources(Questionnaire questionnaire) {
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
          IDataSource newDataSource = convert(dataSource, questionnaire);
          if(newDataSource != dataSource) {
            int index = dataSources.indexOf(dataSource);
            dataSources.remove(dataSource);
            dataSources.add(index, newDataSource);
          }
        }
      } else if(condition instanceof ComparingDataSource) {
        ComparingDataSource comparingDataSource = (ComparingDataSource) condition;
        comparingDataSource.setDataSourceLeft(convert(comparingDataSource.getDataSourceLeft(), questionnaire));
        comparingDataSource.setDataSourceRight(convert(comparingDataSource.getDataSourceRight(), questionnaire));
      }
    }
  }

  private static IDataSource convert(IDataSource dataSource, Questionnaire questionnaire) {
    if(dataSource instanceof VariableDataSource || dataSource instanceof FixedDataSource) {
      return dataSource; // no conversion
    }
    if(dataSource instanceof QuestionnaireDataSource) {
      return convertQuestionnaireDataSource((QuestionnaireDataSource) dataSource, questionnaire);
    }
    if(dataSource instanceof ParticipantPropertyDataSource) {
      return convertParticipantPropertyDataSource((ParticipantPropertyDataSource) dataSource);
    }
    throw new IllegalArgumentException("DataSource " + dataSource + " was not converted to VariableDataSource");
  }

  private static VariableDataSource convertQuestionnaireDataSource(QuestionnaireDataSource dataSource, Questionnaire questionnaire) {
    StringBuilder path = new StringBuilder(isBlank(dataSource.getQuestionnaire()) ? questionnaire.getName() : dataSource.getQuestionnaire());
    path.append(":" + dataSource.getQuestion());
    if(isNotBlank(dataSource.getCategory())) path.append("." + dataSource.getCategory());
    if(isNotBlank(dataSource.getOpenAnswerDefinition())) path.append("." + dataSource.getOpenAnswerDefinition());
    return new VariableDataSource(path.toString());
  }

  private static VariableDataSource convertParticipantPropertyDataSource(ParticipantPropertyDataSource dataSource) {
    if(GENDER.equals(dataSource.getProperty())) {
      return new VariableDataSource(ADMIN_PARTICIPANT_GENDER_PATH);
    }
    throw new IllegalArgumentException("DataSource " + dataSource + " was not converted to VariableDataSource");
  }
}
