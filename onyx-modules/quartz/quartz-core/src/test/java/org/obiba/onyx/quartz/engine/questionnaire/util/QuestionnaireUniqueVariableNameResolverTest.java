/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.engine.questionnaire.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireUniqueVariableNameResolver;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireVariableNameNotUniqueException;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.CategoryBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.QuestionBuilder;

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

  @Test(expected = QuestionnaireVariableNameNotUniqueException.class)
  public void testNonUniqueQuestions() throws Exception {
    QuestionBuilder q1 = QuestionBuilder.inQuestion(builder, new Question("BINGE_DRINKING_MALE_HEAVY_FREQ")).setVariableName("binge_male");
    QuestionBuilder q2 = QuestionBuilder.inQuestion(builder, new Question("BINGE_DRINKING_MALE")).setVariableName("binge_male");

    questionnaireUniqueVariableNameResolver.variableName(q1.getElement());
    questionnaireUniqueVariableNameResolver.variableName(q2.getElement());
  }

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

}
