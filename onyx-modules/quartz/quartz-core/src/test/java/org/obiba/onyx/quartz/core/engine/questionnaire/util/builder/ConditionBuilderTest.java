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

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.magma.type.BooleanType;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.util.data.DataType;

/**
 *
 */
public class ConditionBuilderTest {

  // @BeforeClass
  // public static void setUp() {
  // new MagmaEngine().extend(new MagmaJsExtension()).extend(new MagmaXStreamExtension());
  // }

  @Test
  public void testQuestionVariableCondition() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("TestQuestionnaire", "1.0");
    builder.withSection("S1").withPage("P1").withQuestion("Q1").setVariableName("q1").withCategories("Y", "N", "PNA", "DNK");
    builder.inPage("P1").withQuestion("Q2").withCategories("A", "B", "C", "D");
    builder.inQuestion("Q2").setCondition("Q1");
    Assert.assertEquals(1, builder.getQuestionnaire().getVariables().size());
    Assert.assertEquals(true, builder.getQuestionnaire().hasVariable("q1_answered"));
    Assert.assertNotNull(QuestionnaireFinder.getInstance(builder.getQuestionnaire()).findVariable("q1_answered"));
    Assert.assertEquals("$('q1').isNull().not()", builder.getQuestionnaire().getVariable("q1_answered").getAttributeStringValue("script"));
  }

  @Test
  public void testQuestionCategoryVariableCondition() {
    String questionnaireName = "TestQuestionnaire";
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire(questionnaireName, "1.0");
    builder.withSection("S1").withPage("P1").withQuestion("Q1").setVariableName("q1").withCategories("Y", "N", "PNA", "DNK");
    builder.inPage("P1").withQuestion("Q2").withCategories("A", "B", "C", "D");
    builder.inQuestion("Q2").setCondition("Q1", "Y");
    VariableDataSource q2Condition = getAndAssertVariableDatasourceCondition(builder, "Q2");
    Assert.assertEquals(questionnaireName, q2Condition.getTableName());
    Assert.assertEquals("q1.Y", q2Condition.getVariableName());
  }

  @Test
  public void testOpenAnswerVariableCondition() {
    String questionnaireName = "TestQuestionnaire";
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire(questionnaireName, "1.0");
    builder.withSection("S1").withPage("P1").withQuestion("Q1").setVariableName("q1").withCategories("Y", "N", "PNA", "DNK").withCategory("OTHER").withOpenAnswerDefinition("OPEN", DataType.TEXT).setVariableName("open");
    builder.inPage("P1").withQuestion("Q2").withCategories("A", "B", "C", "D");
    String openAnswered = "open_answered";
    builder.withVariable(openAnswered, BooleanType.get(), "$('open').isNull().not()");
    Assert.assertEquals(1, builder.getQuestionnaire().getVariables().size());
    Assert.assertEquals(true, builder.getQuestionnaire().hasVariable(openAnswered));
    builder.inQuestion("Q2").setQuestionnaireVariableCondition(openAnswered);
    VariableDataSource q2Condition = getAndAssertVariableDatasourceCondition(builder, "Q2");
    Assert.assertEquals(questionnaireName, q2Condition.getTableName());
    Assert.assertEquals(openAnswered, q2Condition.getVariableName());
  }

  private VariableDataSource getAndAssertVariableDatasourceCondition(QuestionnaireBuilder builder, String questionName) {
    Question q = QuestionnaireFinder.getInstance(builder.getQuestionnaire()).findQuestion(questionName);
    Assert.assertNotNull(q);
    Assert.assertNotNull(q.getCondition());
    Assert.assertEquals(VariableDataSource.class, q.getCondition().getClass());
    return (VariableDataSource) q.getCondition();
  }

}
