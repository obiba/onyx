/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder;

import org.obiba.onyx.quartz.core.engine.questionnaire.answer.CurrentYearSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.DataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.ExternalOpenAnswerSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.FixedSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.OpenAnswerSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.ParticipantPropertySource;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.TimestampSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.util.data.Data;

/**
 * DataSource builder.
 */
public class DataSourceBuilder extends AbstractQuestionnaireElementBuilder<DataSource> {

  private DataSourceBuilder(Questionnaire questionnaire, String questionName, String categoryName, String openAnswerDefinitionName) {
    super(questionnaire);

    QuestionnaireFinder finder = QuestionnaireFinder.getInstance(questionnaire);
    Question question = finder.findQuestion(questionName);
    if(question == null) throw invalidElementNameException(Question.class, questionName);
    Category category = finder.findCategory(categoryName);
    if(category == null) throw invalidElementNameException(Category.class, categoryName);
    OpenAnswerDefinition open = finder.findOpenAnswerDefinition(openAnswerDefinitionName);
    if(open == null) throw invalidElementNameException(OpenAnswerDefinition.class, openAnswerDefinitionName);

    this.element = new OpenAnswerSource(question, category, open);
  }

  private DataSourceBuilder(Questionnaire questionnaire, DataSource source) {
    super(questionnaire);
    this.element = source;
  }

  public static DataSourceBuilder createOpenAnswerSource(Questionnaire questionnaire, String questionName, String categoryName, String openAnswerDefinitionName) {
    return new DataSourceBuilder(questionnaire, questionName, categoryName, openAnswerDefinitionName);
  }

  public static DataSourceBuilder createTimestampSource(Questionnaire questionnaire) {
    return new DataSourceBuilder(questionnaire, new TimestampSource());
  }

  public static DataSourceBuilder createCurrentYearSource(Questionnaire questionnaire) {
    return new DataSourceBuilder(questionnaire, new CurrentYearSource());
  }

  public static DataSourceBuilder createParticipantPropertySource(Questionnaire questionnaire, String property) {
    return new DataSourceBuilder(questionnaire, new ParticipantPropertySource(property));
  }

  public static DataSourceBuilder createExternalOpenAnswerSource(Questionnaire questionnaire, String questionnaireName, String questionName, String categoryName, String openAnswerDefinitionName) {
    return new DataSourceBuilder(questionnaire, new ExternalOpenAnswerSource(questionnaireName, questionName, categoryName, openAnswerDefinitionName));
  }

  public static DataSourceBuilder createFixedSource(Questionnaire questionnaire, Data data) {
    return new DataSourceBuilder(questionnaire, new FixedSource(data));
  }

  public DataSource getDataSource() {
    return element;
  }

}
