/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.condition;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.FixedSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class ConditionTest {
  static final Logger log = LoggerFactory.getLogger(ConditionTest.class);

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  private Questionnaire questionnaire;

  @Before
  public void setUp() {
    activeQuestionnaireAdministrationServiceMock = createMock(ActiveQuestionnaireAdministrationService.class);
    questionnaire = createQuestionnaire();
  }

  @Test
  public void testQuestionCategoryAnswerCondition() {

    Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q1");
    Category category = question.findCategory("1");

    CategoryAnswer answer = new CategoryAnswer();
    answer.setCategoryName(category.getName());
    answer.setActive(true);
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, category)).andReturn(answer);
    replay(activeQuestionnaireAdministrationServiceMock);

    AnswerCondition condition = new AnswerCondition("condition", question, category);
    Assert.assertTrue(condition.isToBeAnswered(activeQuestionnaireAdministrationServiceMock));

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testQuestionCategoryAnswerConditionFailOnActive() {

    Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q1");
    Category category = question.findCategory("1");

    CategoryAnswer answer = new CategoryAnswer();
    answer.setActive(false);
    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, category)).andReturn(answer);
    replay(activeQuestionnaireAdministrationServiceMock);

    AnswerCondition condition = new AnswerCondition("condition", question, category);
    Assert.assertFalse(condition.isToBeAnswered(activeQuestionnaireAdministrationServiceMock));

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testQuestionCategoryAnswerConditionFailOnCategory() {

    Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q1");
    Category category = question.findCategory("1");

    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, category)).andReturn(null);
    replay(activeQuestionnaireAdministrationServiceMock);

    AnswerCondition condition = new AnswerCondition("condition", question, category);
    Assert.assertFalse(condition.isToBeAnswered(activeQuestionnaireAdministrationServiceMock));

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testQuestionCategoryWithoutAnswerCondition() {

    Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q1");
    Category category = question.findCategory("1");

    expect(activeQuestionnaireAdministrationServiceMock.findAnswer(question, category)).andReturn(null);
    replay(activeQuestionnaireAdministrationServiceMock);

    AnswerCondition condition = new AnswerCondition("condition", question, category);
    Assert.assertFalse(condition.isToBeAnswered(activeQuestionnaireAdministrationServiceMock));

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testQuestionAnswerCondition() {

    Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q1");

    expect(activeQuestionnaireAdministrationServiceMock.findActiveAnswers(question)).andReturn(Arrays.asList(new CategoryAnswer[] { new CategoryAnswer() }));
    replay(activeQuestionnaireAdministrationServiceMock);

    AnswerCondition condition = new AnswerCondition("condition", question, null);
    Assert.assertTrue(condition.isToBeAnswered(activeQuestionnaireAdministrationServiceMock));

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testQuestionWithoutAnswerCondition() {

    Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q1");

    expect(activeQuestionnaireAdministrationServiceMock.findActiveAnswers(question)).andReturn(new ArrayList<CategoryAnswer>());
    replay(activeQuestionnaireAdministrationServiceMock);

    AnswerCondition condition = new AnswerCondition("condition", question, null);
    Assert.assertFalse(condition.isToBeAnswered(activeQuestionnaireAdministrationServiceMock));

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testQuestionCategoryExternalAnswerCondition() {
    Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q1");

    expect(activeQuestionnaireAdministrationServiceMock.findActiveAnswers(questionnaire.getName(), question.getName())).andReturn(Arrays.asList(new CategoryAnswer[] { new CategoryAnswer() }));
    replay(activeQuestionnaireAdministrationServiceMock);

    ExternalAnswerCondition condition = new ExternalAnswerCondition("condition", questionnaire.getName(), question.getName(), null);
    Assert.assertTrue(condition.isToBeAnswered(activeQuestionnaireAdministrationServiceMock));

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testQuestionCategoryExternalAnswerConditionFailOnActive() {
    Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion("Q1");

    CategoryAnswer answer = new CategoryAnswer();
    answer.setActive(false);
    expect(activeQuestionnaireAdministrationServiceMock.findActiveAnswers(questionnaire.getName(), question.getName())).andReturn(Arrays.asList(new CategoryAnswer[] { answer }));
    replay(activeQuestionnaireAdministrationServiceMock);

    ExternalAnswerCondition condition = new ExternalAnswerCondition("condition", questionnaire.getName(), question.getName(), null);
    Assert.assertTrue(condition.isToBeAnswered(activeQuestionnaireAdministrationServiceMock));

    verify(activeQuestionnaireAdministrationServiceMock);
  }

  @Test
  public void testDataCondition() {
    DataCondition condition = new DataCondition("condition", new FixedSource(DataBuilder.buildInteger(1)), ComparisonOperator.lt, new FixedSource(DataBuilder.buildInteger(2)));
    Assert.assertTrue(condition.isToBeAnswered(activeQuestionnaireAdministrationServiceMock));
  }

  @Test
  public void testNullDataCondition() {
    DataCondition condition = new DataCondition("condition", new FixedSource(null), ComparisonOperator.lt, new FixedSource(null));
    Assert.assertFalse(condition.isToBeAnswered(activeQuestionnaireAdministrationServiceMock));

    condition = new DataCondition("condition", new FixedSource(null), ComparisonOperator.eq, new FixedSource(null));
    Assert.assertTrue(condition.isToBeAnswered(activeQuestionnaireAdministrationServiceMock));
  }

  public Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("SB").withSection("GENDER").withPage("P1").withQuestion("Q1").withCategories("1", "2", "3");
    builder.withSection("S1").withPage("P2").withQuestion("Q2").withCategory("1").withOpenAnswerDefinition("OPEN_INT", DataType.INTEGER);
    builder.inQuestion("Q2").withCategory("2").withOpenAnswerDefinition("OPEN_TEXT", DataType.TEXT);
    builder.inQuestion("Q2").withCategory("3").withOpenAnswerDefinition("OPEN_DATE", DataType.DATE);
    builder.inQuestion("Q2").withCategory("4").withOpenAnswerDefinition("OPEN_TEXT_DEFAULT_VALUES", DataType.TEXT).setDefaultData("a", "b", "c");

    Questionnaire q = builder.getQuestionnaire();
    q.addLocale(Locale.FRENCH);
    q.addLocale(Locale.ENGLISH);

    return q;
  }
}
