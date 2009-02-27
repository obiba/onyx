/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.test;

import org.apache.wicket.validation.validator.NumberValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.junit.Test;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanelFactory;
import org.obiba.onyx.quartz.test.provider.AnswerProvider;
import org.obiba.onyx.quartz.test.provider.impl.ConfigurableAnswerProvider;
import org.obiba.onyx.util.data.DataType;

/**
 * Tests for Health Questionnaire.
 */
public class HealthQuestionnaireTest extends AbstractQuestionnaireTest {

  protected String getQuestionnaireName() {
    return "HealthQuestionnaire";
  }

  protected String getQuestionnaireVersion() {
    return "1.0";
  }

  @Test
  @Dataset
  public void testQ1() {
    startQuestionnaire();
    assertCurrentPage(getPage("P1"));

    AnswerProvider answerProvider = ConfigurableAnswerProvider.fromXmlResource(getAnswerProviderResourcePath() + "/answerProvider.xml");

    Question q31 = getQuestion("Q31");
    answerQuestionsUpTo(answerProvider, q31);
    assertCurrentPage(getPage("P23"));

    returnToEarlierQuestion(getQuestion("Q1"));
    assertCurrentPage(getPage("P1"));

    returnToLaterQuestion(q31);
    assertCurrentPage(getPage("P23"));
  }

  @Test
  @Dataset
  public void testCondition() {
    startQuestionnaire();
    assertCurrentPage(getPage("P1"));

    AnswerProvider answerProvider = ConfigurableAnswerProvider.fromXmlResource(getAnswerProviderResourcePath() + "/answerProviderForConditionTest.xml");

    // if Q4 > 45 => question Q5 available
    // if Q5 answered => Q6 available but not Q7
    answerQuestionsUpTo(answerProvider, getQuestion("Q5"));
    assertNextPage(getPage("P6"));
    assertNextPage(getPage("P8"));

    // if Q4 < 45 => question Q7 available only
    returnToEarlierQuestion(getQuestion("Q1"));
    answerQuestionsUpTo(answerProvider, getQuestion("Q3"));
    answerQuestion(getQuestion("Q4"), answerProvider.getAnswer(new Question("Q4Changed")));
    assertCurrentPage(getPage("P4"));
    assertNextPage(getPage("P7"));

  }

  private static final String QUESTIONNAIRE = "HealthQuestionnaire";

  private static final String N = "N";

  private static final String Y = "Y";

  private static final String OTHER = "OTHER";

  private static final String PNA = "PNA";

  private static final String DNK = "DNK";

  public static QuestionnaireBuilder buildQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire(QUESTIONNAIRE, "1.0");

    builder.withSection("GENDER").withPage("P1").withQuestion("Q1").withCategories("1", "2").withSharedCategories(OTHER, PNA, DNK);
    builder.inPage("P1").addTimestamp();

    builder.withSection("BIRTHDATE").withPage("P2").withQuestion("Q2", DropDownQuestionPanelFactory.class).withCategory("1").withOpenAnswerDefinition("year", DataType.INTEGER).addValidator(new PatternValidator("\\d{4}"));
    builder.inQuestion("Q2").withSharedCategories(PNA, DNK);
    builder.inPage("P2").addTimestamp();

    builder.inSection("BIRTHDATE").withPage("P3").withQuestion("Q3").withCategory("1").withOpenAnswerDefinition("month", DataType.INTEGER).addValidator(new PatternValidator("\\d\\d?"));
    builder.inQuestion("Q3").withSharedCategories(PNA, DNK);
    builder.inPage("P3").addTimestamp();

    builder.inSection("BIRTHDATE").withPage("P4").withQuestion("Q4").withCategory("1").withOpenAnswerDefinition("age", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(40, 70));
    builder.inQuestion("Q4").withSharedCategories(PNA, DNK);
    builder.inQuestion("Q4").setCondition("!($1 && $2)", builder.newDataSource("Q2", "1"), builder.newDataSource("Q3", "1"));
    builder.inPage("P4").addTimestamp();

    builder.withSection("MARITALSTATUS").withPage("P5").withQuestion("Q5").withCategories("1", "2", "3", "4", "5").withSharedCategories(PNA, DNK);
    builder.inQuestion("Q5").setCondition("$1 && $2 > 45", builder.newDataSource("Q4", "1"), builder.newDataSource("Q4", "1", "age"));

    builder.withSection("HOUSEHOLDSTATUS").withPage("P6").withQuestion("Q6").withCategory("1").withOpenAnswerDefinition("adults", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(1, 100));
    builder.inQuestion("Q6").withSharedCategories(PNA, DNK);
    builder.inQuestion("Q6").setCondition("$1", builder.newDataSource("Q5"));

    builder.inSection("HOUSEHOLDSTATUS").withPage("P7").withQuestion("Q7").withCategory("1").withOpenAnswerDefinition("children", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 100));
    builder.inQuestion("Q7").withSharedCategories(PNA, DNK);
    builder.inQuestion("Q7").setCondition("!$1", builder.newDataSource("Q5"));

    builder.inSection("HOUSEHOLDSTATUS").withPage("P8").withQuestion("Q8").withCategory("1").withOpenAnswerDefinition("siblings", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 20));
    builder.inQuestion("Q8").withSharedCategories(PNA, DNK);

    builder.inSection("HOUSEHOLDSTATUS").withPage("P9").withQuestion("Q9").withCategory("1").withOpenAnswerDefinition("olderSiblings", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 20));
    builder.inQuestion("Q9").withSharedCategories(PNA, DNK);

    builder.inSection("HOUSEHOLDSTATUS").withPage("P10").withQuestion("Q10");
    builder.inQuestion("Q10").withSharedCategories(Y, N, PNA, DNK);

    builder.inSection("HOUSEHOLDSTATUS").withPage("P11").withQuestion("Q11");
    builder.inQuestion("Q11").withSharedCategories(Y, N, PNA, DNK);

    builder.withSection("EDUCATIONLEVEL").withPage("P12").withQuestion("Q12");

    builder.inSection("EDUCATIONLEVEL").withPage("P13").withQuestion("Q13").withCategory("1").withOpenAnswerDefinition("years", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 20));
    builder.inQuestion("Q13").withSharedCategories(PNA, DNK);

    builder.inSection("EDUCATIONLEVEL").withPage("P14").withQuestion("Q14").withCategories("1", "2", "3", "4", "5");
    builder.inQuestion("Q14").withSharedCategories(PNA, DNK);

    builder.withSection("FINISH").withPage("P22").withQuestion("Q29").withSharedCategories(Y, N, PNA, DNK);

    builder.inSection("FINISH").withPage("P23").withQuestion("Q30").withCategories("1", "2", "3");
    builder.inPage("P23").withQuestion("Q31").withCategories("1", "2");
    builder.inQuestion("Q31").setCondition("$1", builder.newDataSource("Q29", Y));

    return builder;
  }
}
