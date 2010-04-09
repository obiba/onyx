/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import static org.easymock.EasyMock.isNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.CategoryBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.QuestionBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Test that only unique questionnaire names are resolved.
 */
public class QuestionnaireUniqueVariableNameResolverTest {

  private QuestionnaireUniqueVariableNameResolver questionnaireUniqueVariableNameResolver;

  private QuestionnaireBuilder builder;

  @Before
  public void setUp() {
    builder = QuestionnaireBuilder.createQuestionnaire("UniqueVariableTestQuestionnaire", "6.0");
    questionnaireUniqueVariableNameResolver = new QuestionnaireUniqueVariableNameResolver(builder.getQuestionnaire());
  }

  @After
  public void tearDown() {
    questionnaireUniqueVariableNameResolver = null;
    builder = null;
  }

  @Test
  public void testUniqueQuestions() throws Exception {
    QuestionBuilder q1 = QuestionBuilder.inQuestion(builder, new Question("BINGE_DRINKING_MALE_HEAVY_FREQ")).setVariableName("binge_male_heavy");
    QuestionBuilder q2 = QuestionBuilder.inQuestion(builder, new Question("BINGE_DRINKING_MALE")).setVariableName("binge_male");
    String name1 = questionnaireUniqueVariableNameResolver.variableName(q1.getElement());
    String name2 = questionnaireUniqueVariableNameResolver.variableName(q2.getElement());
    assertThat(name1, is("binge_male_heavy"));
    assertThat(name2, is("binge_male"));
  }

  @Ignore
  @Test(expected = QuestionnaireVariableNameNotUniqueException.class)
  public void testNonUniqueQuestions() throws Exception {
    QuestionBuilder q1 = QuestionBuilder.inQuestion(builder, new Question("BINGE_DRINKING_MALE_HEAVY_FREQ")).setVariableName("binge_male");
    QuestionBuilder q2 = QuestionBuilder.inQuestion(builder, new Question("BINGE_DRINKING_MALE")).setVariableName("binge_male");

    questionnaireUniqueVariableNameResolver.variableName(q1.getElement());
    questionnaireUniqueVariableNameResolver.variableName(q2.getElement());
  }

  @Ignore
  @Test(expected = QuestionnaireVariableNameNotUniqueException.class)
  public void testNonUniqueCategories() throws Exception {
    QuestionBuilder q1 = QuestionBuilder.inQuestion(builder, new Question("CURRENTLY_PREGNANT"));
    Category category1 = new Category("PNA");
    CategoryBuilder c1 = CategoryBuilder.createQuestionCategory(q1, category1);
    QuestionCategory qc = new QuestionCategory();
    qc.setQuestion(q1.getElement());
    qc.setCategory(c1.getElement());
    qc.setVariableName("NonsenseName");

    QuestionBuilder q2 = QuestionBuilder.inQuestion(builder, new Question("BINGE_DRINKING_MALE")).setVariableName("NonsenseName");

    questionnaireUniqueVariableNameResolver.variableName(q1.getElement(), qc);
    questionnaireUniqueVariableNameResolver.variableName(q2.getElement());
  }

  @Test
  public void testOpenAnswerBuilderSpecifyInContextOfAGivenQuestion() throws Exception {
    builder.withSection("a").withPage("p").withQuestion("DATE_OF_BIRTH").withCategory("YEAR_OF_BIRTH").withOpenAnswerDefinition("YEAR", DataType.INTEGER);
    builder.inOpenAnswerDefinition("YEAR").setVariableName("DATE_OF_BIRTH", "dob_year");
    Questionnaire questionnaire = builder.getQuestionnaire();

    assertThat(QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition("YEAR").getVariableName("DATE_OF_BIRTH"), is("dob_year"));
    assertThat(QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition("YEAR").getVariableName("FAKE_NAME"), is(isNull()));
  }

  @Test
  public void testOpenAnswerBuilderImplicitlyRelatedToCurrentQuestion() throws Exception {
    // the open answer definition is implicitly related to the current question which is NUMBER_SIBLINGS
    builder.withSection("a").withPage("p").withQuestion("NUMBER_SIBLINGS");
    builder.inQuestion("NUMBER_SIBLINGS").withCategory("BROTHER").withOpenAnswerDefinition("BROTHER_OPEN", DataType.INTEGER).setVariableName("brother_number");
    Questionnaire questionnaire = builder.getQuestionnaire();

    assertThat(QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition("BROTHER_OPEN").getVariableName("NUMBER_SIBLINGS"), is("brother_number"));
    assertThat(QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition("BROTHER_OPEN").getVariableName("FAKE_NAME"), is(isNull()));
  }

  @Test
  public void testOpenAnswerBuilderJoinedCategoryArray() throws Exception {
    builder.withSection("a").withPage("P1").withQuestion("ARRAY_OPEN", true).withCategory("WEEK").withOpenAnswerDefinition("WEEK_QUANTITY", DataType.INTEGER);
    builder.inQuestion("ARRAY_OPEN").withQuestion("RED_WINE", true);
    builder.inQuestion("ARRAY_OPEN").withQuestion("WHITE_WINE", true);
    // specify the open answer variable names for each of the sub questions
    builder.inOpenAnswerDefinition("WEEK_QUANTITY").setVariableName("RED_WINE", "red_wine_week_quantity");
    builder.inOpenAnswerDefinition("WEEK_QUANTITY").setVariableName("WHITE_WINE", "white_wine_week_quantity");
    Questionnaire questionnaire = builder.getQuestionnaire();

    assertThat(QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition("WEEK_QUANTITY").getVariableName("RED_WINE"), is("red_wine_week_quantity"));
    assertThat(QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition("WEEK_QUANTITY").getVariableName("WHITE_WINE"), is("white_wine_week_quantity"));
    assertThat(QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition("WEEK_QUANTITY").getVariableName("FAKE_NAME"), is(isNull()));
  }
}
