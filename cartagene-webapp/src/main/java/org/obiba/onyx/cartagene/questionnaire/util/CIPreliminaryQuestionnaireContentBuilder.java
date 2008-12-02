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

import org.apache.wicket.validation.validator.NumberValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ComparisionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ConditionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoriesToMatrixPermutator;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Returns the content for the CI Preliminary Questionnaire
 */
public class CIPreliminaryQuestionnaireContentBuilder {

  private static final String N = "N";

  private static final String Y = "Y";

  private static final String OTHER = "OTHER";

  private static final String DNK = "DNK";

  private static final String PNA = "PNA";

  private static final String OPEN_N = "OPEN_N";

  private static final String TIME_TODAY = "TIME_TODAY";

  private static final String TIME_YESTERDAY = "TIME_YESTERDAY";

  private static final String HOURS_AGO = "HOURS_AGO";

  private static final String RASHES_BOTH_ARMS = "RASHES_BOTH_ARMS";

  private static final String CAST_BOTH_ARMS = "CAST_BOTH_ARMS";

  private static final String DOUBLE_ARM_PARALYSIS = "DOUBLE_ARM_PARALYSIS";

  private static final String AMPUTATION_WITHERED = "AMPUTATION_WITHERED";

  private static final String NOT_APP = "NOT_APP";

  private static final String MORE_24_HOURS = "MORE_24_HOURS";

  public static QuestionnaireBuilder buildCIPreliminaryQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("CIPreliminaryQuestionnaire", "1.0");

    // Common Section
    builder.withSection("S1_COMMON").withSection("S1_PARTICIPANT").withPage("1").withQuestion("CURRENTLY_PREGNANT", "1").withSharedCategory(N, "0");
    builder.inQuestion("CURRENTLY_PREGNANT").withSharedCategory(Y, "1");
    builder.inQuestion("CURRENTLY_PREGNANT").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENTLY_PREGNANT").withSharedCategory(DNK, "9");
    // TODO: implémenter condition à partir de participant information

    builder.inSection("S1_PARTICIPANT").withPage("2").withQuestion("CURRENT_PREGNANCY_WEEKS", "2").withSharedCategory(OPEN_N).withOpenAnswerDefinition(OPEN_N, DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 40));
    builder.inQuestion("CURRENT_PREGNANCY_WEEKS").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENT_PREGNANCY_WEEKS").withSharedCategory(DNK, "9");
    builder.inQuestion("CURRENT_PREGNANCY_WEEKS").setAnswerCondition("CURRENT_PREGNANCY_WEEKS_ACONDITION", "CURRENTLY_PREGNANT", Y);

    builder.inSection("S1_PARTICIPANT").withPage("3").withQuestion("HW_CI_BP", "2a").setMultipleCondition("HW_CI_BP_MCONDITION", ConditionOperator.OR).withDataCondition("HW_CI_BP_DCONDITION", "CURRENT_PREGNANCY_WEEKS", OPEN_N, OPEN_N, ComparisionOperator.ge, DataBuilder.buildInteger(12l));
    builder.inCondition("HW_CI_BP_MCONDITION").withAnswerCondition("HW_CI_BP_ACONDITION_0", "CURRENT_PREGNANCY_WEEKS", DNK);
    builder.inCondition("HW_CI_BP_MCONDITION").withAnswerCondition("HW_CI_BP_ACONDITION_1", "CURRENT_PREGNANCY_WEEKS", PNA);

    builder.inSection("S1_PARTICIPANT").withPage("4").withQuestion("LAST_FULL_MEAL_WHEN", "3").withSharedCategory(TIME_TODAY).withOpenAnswerDefinition(TIME_TODAY, DataType.INTEGER).withOpenAnswerDefinition("HOUR_TODAY", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 23));
    builder.inOpenAnswerDefinition(TIME_TODAY).withOpenAnswerDefinition("MINUTE_TODAY", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 59));
    builder.inQuestion("LAST_FULL_MEAL_WHEN").withSharedCategory(TIME_YESTERDAY).withOpenAnswerDefinition(TIME_YESTERDAY, DataType.INTEGER).withOpenAnswerDefinition("HOUR_YESTERDAY", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 23));
    builder.inOpenAnswerDefinition(TIME_YESTERDAY).withOpenAnswerDefinition("MINUTE_YESTERDAY", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 59));
    builder.inQuestion("LAST_FULL_MEAL_WHEN").withSharedCategory(HOURS_AGO).withOpenAnswerDefinition(HOURS_AGO, DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 24));

    builder.inPage("4").withQuestion("LAST_CAFFEINE_WHEN", "4").withSharedCategory(TIME_TODAY);
    builder.inQuestion("LAST_CAFFEINE_WHEN").withSharedCategory(TIME_YESTERDAY);
    builder.inQuestion("LAST_CAFFEINE_WHEN").withSharedCategory(HOURS_AGO);
    builder.inQuestion("LAST_CAFFEINE_WHEN").withSharedCategory(MORE_24_HOURS);

    builder.inPage("4").withQuestion("LAST_ALCOHOL_WHEN", "5").withSharedCategory(TIME_TODAY);
    builder.inQuestion("LAST_ALCOHOL_WHEN").withSharedCategory(TIME_YESTERDAY);
    builder.inQuestion("LAST_ALCOHOL_WHEN").withSharedCategory(HOURS_AGO);
    builder.inQuestion("LAST_ALCOHOL_WHEN").withSharedCategory(MORE_24_HOURS);

    builder.inPage("4").withQuestion("LAST_TOBACCO_WHEN", "6").withSharedCategory(TIME_TODAY);
    builder.inQuestion("LAST_TOBACCO_WHEN").withSharedCategory(TIME_YESTERDAY);
    builder.inQuestion("LAST_TOBACCO_WHEN").withSharedCategory(HOURS_AGO);
    builder.inQuestion("LAST_TOBACCO_WHEN").withSharedCategory(MORE_24_HOURS);

    builder.inSection("S1_PARTICIPANT").withPage("4a").withQuestion("UNABLE_TO_STAND", "7").withSharedCategory(N, "0");
    builder.inQuestion("UNABLE_TO_STAND").withSharedCategory(Y, "1");

    builder.inSection("S1_PARTICIPANT").withPage("4b").withQuestion("UNABLE_TO_STAND_CI_BP", "8").setAnswerCondition("UNABLE_TO_STAND_CI_BP_ACONDITION", "UNABLE_TO_STAND", Y);

    // Blood Pressure and Heart Rate

    builder.withSection("S2_BP_HR").withSection("S2_EC_OBS").withPage("5").withQuestion("BP_OBSERVED_CI", "10").addUIArgument(QuestionCategoriesToMatrixPermutator.ROW_COUNT_KEY, "6").withSharedCategory(RASHES_BOTH_ARMS);
    builder.inQuestion("BP_OBSERVED_CI").withSharedCategory(CAST_BOTH_ARMS);
    builder.inQuestion("BP_OBSERVED_CI").withSharedCategory(DOUBLE_ARM_PARALYSIS);
    builder.inQuestion("BP_OBSERVED_CI").withSharedCategory(AMPUTATION_WITHERED);
    builder.inQuestion("BP_OBSERVED_CI").withSharedCategory(OTHER).withOpenAnswerDefinition(OTHER, DataType.TEXT);
    builder.inQuestion("BP_OBSERVED_CI").withSharedCategory(NOT_APP);

    builder.inSection("S2_BP_HR").withSection("S2_EC_IC_ASKED").withPage("7").withQuestion("LEFT_ARM_SURGERY_SHUNT", "13").withSharedCategory(N, "0");
    builder.inQuestion("LEFT_ARM_SURGERY_SHUNT").withSharedCategory(Y, "1");
    builder.inQuestion("LEFT_ARM_SURGERY_SHUNT").withSharedCategory(DNK, "9");
    builder.inQuestion("LEFT_ARM_SURGERY_SHUNT").setAnswerCondition("LEFT_ARM_SURGERY_SHUNT_ACONDITION", "BP_OBSERVED_CI", NOT_APP);

    builder.inSection("S2_EC_IC_ASKED").withPage("8").withQuestion("BP_LEFT_ARM_POSSIBLE", "13a").setAnswerCondition("BP_LEFT_ARM_POSSIBLE_ACONDITION", "LEFT_ARM_SURGERY_SHUNT", N);

    builder.inSection("S2_EC_IC_ASKED").withPage("9").withQuestion("RIGHT_ARM_SURGERY_SHUNT", "14").withSharedCategory(N, "0");
    builder.inQuestion("RIGHT_ARM_SURGERY_SHUNT").withSharedCategory(Y, "1");
    builder.inQuestion("RIGHT_ARM_SURGERY_SHUNT").withSharedCategory(DNK, "9");
    builder.inQuestion("RIGHT_ARM_SURGERY_SHUNT").setMultipleCondition("RIGHT_ARM_SURGERY_SHUNT_MCONDITION", ConditionOperator.OR).withAnswerCondition("RIGHT_ARM_SURGERY_SHUNT_ACONDITION_0", "LEFT_ARM_SURGERY_SHUNT", Y);
    builder.inCondition("RIGHT_ARM_SURGERY_SHUNT_MCONDITION").withAnswerCondition("RIGHT_ARM_SURGERY_SHUNT_ACONDITION_1", "LEFT_ARM_SURGERY_SHUNT", DNK);

    builder.inSection("S2_EC_IC_ASKED").withPage("10").withQuestion("BP_RIGHT_ARM_POSSIBLE", "14a").setAnswerCondition("BP_RIGHT_ARM_POSSIBLE_ACONDITION", "RIGHT_ARM_SURGERY_SHUNT", N);

    builder.inSection("S2_EC_IC_ASKED").withPage("11").withQuestion("BP_SAMPLES_SKIPPED_BP", "14b").setMultipleCondition("BP_SAMPLES_SKIPPED_BP_MCONDITION", ConditionOperator.OR).withAnswerCondition("BP_SAMPLES_SKIPPED_BP_ACONDITION_0", "RIGHT_ARM_SURGERY_SHUNT", Y);
    builder.inCondition("BP_SAMPLES_SKIPPED_BP_MCONDITION").withAnswerCondition("BP_SAMPLES_SKIPPED_BP_ACONDITION_1", "RIGHT_ARM_SURGERY_SHUNT", DNK);

    builder.inSection("S2_EC_IC_ASKED").withPage("12").withQuestion("BLOOD_DRAWN_LAST_7_DAYS", "15").withSharedCategory(N, "0");
    builder.inQuestion("BLOOD_DRAWN_LAST_7_DAYS").withSharedCategory(Y, "1");
    builder.inQuestion("BLOOD_DRAWN_LAST_7_DAYS").setMultipleCondition("BLOOD_DRAWN_LAST_7_DAYS_MCONDITION", ConditionOperator.OR).withAnswerCondition("BLOOD_DRAWN_LAST_7_DAYS_ACONDITION_0", "LEFT_ARM_SURGERY_SHUNT", N);
    builder.inCondition("BLOOD_DRAWN_LAST_7_DAYS_MCONDITION").withAnswerCondition("BLOOD_DRAWN_LAST_7_DAYS_ACONDITION_1", "RIGHT_ARM_SURGERY_SHUNT", N);

    builder.inSection("S2_EC_IC_ASKED").withPage("13").withQuestion("BLOOD_DRAWN_WHEN", "16").withCategory("DATE").withOpenAnswerDefinition("DATE", DataType.DATE);
    // TODO: implémenter validation sur date courante + 7 jours
    builder.inQuestion("BLOOD_DRAWN_WHEN").withCategory("OPEN_N7").withOpenAnswerDefinition("OPEN_N7", DataType.INTEGER).addValidator(new NumberValidator.MaximumValidator(7));
    builder.inQuestion("BLOOD_DRAWN_WHEN").setAnswerCondition("BLOOD_DRAWN_WHEN_ACONDITION", "BLOOD_DRAWN_LAST_7_DAYS", Y);

    builder.inSection("S2_BP_HR").withSection("S2_IC_OBS").withPage("14").withQuestion("BP_ARM_CHOSEN", "17").withCategories("RIGHT_ARM", "LEFT_ARM");
    builder.inQuestion("BP_ARM_CHOSEN").setMultipleCondition("BP_ARM_CHOSEN_MCONDITION", ConditionOperator.OR).withAnswerCondition("BP_ARM_CHOSEN_ACONDITION_0", "LEFT_ARM_SURGERY_SHUNT", N);
    builder.inCondition("BP_ARM_CHOSEN_MCONDITION").withAnswerCondition("BP_ARM_CHOSEN_ACONDITION_1", "RIGHT_ARM_SURGERY_SHUNT", N);

    return builder;
  }
}
