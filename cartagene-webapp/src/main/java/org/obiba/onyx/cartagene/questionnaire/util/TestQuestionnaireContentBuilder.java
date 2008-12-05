/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.cartagene.questionnaire.util;

import java.util.List;

import org.apache.wicket.validation.validator.NumberValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ComparisionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ConditionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.DropDownQuestionPanelFactory;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Returns the content for the Test Questionnaire
 */
public class TestQuestionnaireContentBuilder {

  private static final String NO = "NO";

  private static final String YES = "YES";

  private static final String OTHER = "OTHER";

  private static final String FULL_TIME = "FULL_TIME";

  private static final String PART_TIME = "PART_TIME";

  private static final String NO_ANSWER = "NO_ANSWER";

  private static final String DONT_KNOW = "DONT_KNOW";

  public static QuestionnaireBuilder buildTestQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("S_MULTIPLE_OPEN").withPage("P_MULTIPLE_OPEN").withQuestion("MULTIPLE_OPEN").withSharedCategory("DURATION").withOpenAnswerDefinition("DURATION_OPEN", DataType.INTEGER);
    builder.inOpenAnswerDefinition("DURATION_OPEN").withOpenAnswerDefinition("DURATION_OPEN_HOURS", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 16));
    builder.inOpenAnswerDefinition("DURATION_OPEN").withOpenAnswerDefinition("DURATION_OPEN_MINUTES", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 960));
    builder.inQuestion("MULTIPLE_OPEN").withSharedCategories(true, DONT_KNOW, NO_ANSWER);

    builder.inPage("P_MULTIPLE_OPEN").withQuestion("INNER_CONDITION_QUESTION").withSharedCategories(YES, NO);
    builder.inQuestion("INNER_CONDITION_QUESTION").setAnswerCondition("INNER_CONDITION", "MULTIPLE_OPEN", DONT_KNOW);

    builder.inPage("P_MULTIPLE_OPEN").withQuestion("INNER_CASCADING_CONDITION_QUESTION").withSharedCategories(YES, NO);
    builder.inQuestion("INNER_CASCADING_CONDITION_QUESTION").setAnswerCondition("INNER_CASCADING_CONDITION", "INNER_CONDITION_QUESTION", NO);

    builder.inPage("P_MULTIPLE_OPEN").withQuestion("INNER_DATA_CONDITION_QUESTION").withSharedCategories(YES, NO);
    builder.inQuestion("INNER_DATA_CONDITION_QUESTION").setDataCondition("INNER_DATA_CONDITION", "MULTIPLE_OPEN", "DURATION", "DURATION_OPEN_HOURS", ComparisionOperator.gt, DataBuilder.buildInteger(5));

    builder.withSection("S_MULTIPLE").withPage("P_MULTIPLE").withQuestion("MULTIPLE_1", true).setAnswerCount(1, 2).withCategories("1", "2", "3");

    builder.withSection("S_ARRAY").withPage("P_ARRAY").withQuestion("SHARED_CATEGORIES_QUESTION_ARRAY").withCategories("1", "2").withSharedCategories(DONT_KNOW, NO_ANSWER).withCategory("OTHER_OPINION").withOpenAnswerDefinition("SPECIFY_OPINION", DataType.TEXT);
    builder.inQuestion("SHARED_CATEGORIES_QUESTION_ARRAY").withQuestion("SHARED_CATEGORIES_QUESTION_1");
    builder.inQuestion("SHARED_CATEGORIES_QUESTION_ARRAY").withQuestion("SHARED_CATEGORIES_QUESTION_2");

    builder.inPage("P_ARRAY").withQuestion("SHARED_MULTIPLE_CATEGORIES_QUESTION_ARRAY", true).setAnswerCount(1, null).withCategories("BLUE", "RED", "YELLOW").withCategory("OTHER_COLOR").withOpenAnswerDefinition("SPECIFY_COLOR", DataType.TEXT);
    builder.inQuestion("SHARED_MULTIPLE_CATEGORIES_QUESTION_ARRAY").withQuestion("SHARED_MULTIPLE_CATEGORIES_QUESTION_1", true).setAnswerCount(2, null);
    builder.inQuestion("SHARED_MULTIPLE_CATEGORIES_QUESTION_ARRAY").withQuestion("SHARED_MULTIPLE_CATEGORIES_QUESTION_2", true);

    builder.withSection("SB").withSection("GENDER").withPage("P1").withQuestion("Q1").withCategories("1", "2");
    builder.inQuestion("Q1").withSharedCategory(OTHER).setExportName("3");
    builder.inQuestion("Q1").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q1").withSharedCategory(DONT_KNOW).setExportName("9");

    builder.inSection("SB").withSection("BIRTHDATE").withPage("P2").withQuestion("Q2", DropDownQuestionPanelFactory.class).withCategory("1").withOpenAnswerDefinition("year", DataType.INTEGER).addValidator(new PatternValidator("\\d{4}"), DataType.TEXT);
    builder.inQuestion("Q2").withCategory("2");
    builder.inQuestion("Q2").withSharedCategory(NO_ANSWER).setExportName("8888");
    builder.inQuestion("Q2").withSharedCategory(DONT_KNOW).setExportName("9999");
    builder.inSection("BIRTHDATE").withPage("P3").withQuestion("Q3").withCategory("1").withOpenAnswerDefinition("month", DataType.INTEGER).addValidator(new PatternValidator("\\d\\d?"), DataType.TEXT);
    builder.inQuestion("Q3").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q3").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("BIRTHDATE").withPage("P4").withQuestion("Q4").withCategory("1").withOpenAnswerDefinition("age", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(40, 70));
    builder.inQuestion("Q4").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q4").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inQuestion("Q4").setNotCondition("NO1").withMultipleCondition("MC1", ConditionOperator.AND).withAnswerCondition("AC1", "Q2", "1");
    builder.inCondition("MC1").withAnswerCondition("AC2", "Q3", "1");

    builder.inSection("SB").withSection("MARITALSTATUS").withPage("P5").withQuestion("Q5").withCategories("1", "2", "3", "4", "5");
    builder.inQuestion("Q5").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q5").withSharedCategory(DONT_KNOW).setExportName("99");
    // builder.inQuestion("Q5").setAnswerCondition("AC4", "Q4", "1", "age",
    // DataBuilder.buildInteger(Long.valueOf("45")), ComparisionOperator.gt);

    builder.inSection("SB").withSection("HOUSEHOLDSTATUS").withPage("P6").withQuestion("Q6").withCategory("1").withOpenAnswerDefinition("adults", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(1, 100));
    builder.inQuestion("Q6").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q6").withSharedCategory(DONT_KNOW).setExportName("99");
    // builder.inQuestion("Q6").setAnswerCondition("AC5", "Q5");
    builder.inSection("HOUSEHOLDSTATUS").withPage("P7").withQuestion("Q7").withCategory("1").withOpenAnswerDefinition("children", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 100));
    builder.inQuestion("Q7").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q7").withSharedCategory(DONT_KNOW).setExportName("99");
    // builder.inQuestion("Q7").setNoAnswerCondition("NO2").withAnswerCondition("AC6", "Q5");

    builder.inSection("SB").withSection("SIBLING").withPage("P8").withQuestion("Q8").withCategory("1").withOpenAnswerDefinition("siblings", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 20));
    builder.inQuestion("Q8").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q8").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("SIBLING").withPage("P9").withQuestion("Q9").withCategory("1").withOpenAnswerDefinition("olderSiblings", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 20));
    builder.inQuestion("Q9").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q9").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("SIBLING").withPage("P10").withQuestion("Q10").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q10").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q10").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q10").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("SIBLING").withPage("P11").withQuestion("Q11").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q11").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q11").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q11").withSharedCategory(DONT_KNOW).setExportName("9");

    builder.inSection("SB").withSection("EDUCATIONLEVEL").withPage("P12").withQuestion("Q12");
    builder.inSection("EDUCATIONLEVEL").withPage("P13").withQuestion("Q13").withCategory("1").withOpenAnswerDefinition("years", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 20));
    builder.inQuestion("Q13").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q13").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("EDUCATIONLEVEL").withPage("P14").withQuestion("Q14").withCategories("1", "2", "3", "4", "5", "6", "7", "8");
    builder.inQuestion("Q14").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q14").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("EDUCATIONLEVEL").withPage("P15").withQuestion("Q15").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q15").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q15").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q15").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("SB").withSection("WORKINGSTATUS").withPage("P16").withQuestion("Q16");
    builder.inSection("WORKINGSTATUS").withPage("P17_1").withQuestion("Q17").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q17").withSharedCategory(FULL_TIME).setExportName("1");
    builder.inQuestion("Q17").withSharedCategory(PART_TIME).setExportName("2");
    builder.inQuestion("Q17").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q17").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P17_2").withQuestion("Q18").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q18").withSharedCategory(FULL_TIME).setExportName("1");
    builder.inQuestion("Q18").withSharedCategory(PART_TIME).setExportName("2");
    builder.inQuestion("Q18").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q18").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P17_3").withQuestion("Q19").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q19").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q19").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q19").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P17_4").withQuestion("Q20").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q20").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q20").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q20").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P17_5").withQuestion("Q21").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q21").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q21").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q21").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P17_6").withQuestion("Q22").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q22").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q22").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q22").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P17_7").withQuestion("Q23").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q23").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q23").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q23").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P17_8").withQuestion("Q24").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q24").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q24").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q24").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P18").withQuestion("Q25").withCategory("1").withOpenAnswerDefinition("job", DataType.TEXT);
    builder.inQuestion("Q25").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q25").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("WORKINGSTATUS").withPage("P19").withQuestion("Q26").withCategories("1", "2", "3", "4", "5", "6", "7", "8");
    builder.inQuestion("Q26").withSharedCategory(OTHER).setExportName("9");
    builder.inQuestion("Q26").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q26").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("WORKINGSTATUS").withPage("P20").withQuestion("Q27").withCategory("1").withOpenAnswerDefinition("specify", DataType.TEXT);
    builder.inQuestion("Q27").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q27").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("WORKINGSTATUS").withPage("P21").withQuestion("Q28").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q28").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q28").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q28").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P22").withQuestion("Q29").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q29").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q29").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q29").withSharedCategory(DONT_KNOW).setExportName("9");

    builder.inSection("WORKINGSTATUS").withPage("P23").withQuestion("Q30").withCategories("1", "2", "3");
    builder.inPage("P23").withQuestion("Q31").withCategories("1", "2");
    builder.inQuestion("Q31").setAnswerCondition("AC3", "Q29", "NO");

    // Add Timestamps to pages
    List<Page> pages = builder.getQuestionnaire().getPages();
    for(Page page : pages) {
      builder.inPage(page.getName()).addTimestamp();
    }

    return builder;
  }

}
