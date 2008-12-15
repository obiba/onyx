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

import java.util.Calendar;

import org.apache.wicket.validation.validator.NumberValidator;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ComparisionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ConditionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.DataSourceBuilder;
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

    // section S1_PARTICIPANT
    builder.withSection("S1_COMMON").withSection("S1_PARTICIPANT").withPage("1").withQuestion("CURRENTLY_PREGNANT", "1").withSharedCategory(N, "0");
    builder.inQuestion("CURRENTLY_PREGNANT").withSharedCategory(Y, "1");
    builder.inQuestion("CURRENTLY_PREGNANT").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENTLY_PREGNANT").withSharedCategory(DNK, "9");
    builder.inQuestion("CURRENTLY_PREGNANT").setDataCondition("CURRENTLY_PREGNANT_DCONDITION", ComparisionOperator.eq, Gender.FEMALE);

    builder.inPage("1").withQuestion("BIOIMPEDANCE_CI_PREGNANT");
    builder.inQuestion("BIOIMPEDANCE_CI_PREGNANT").setMultipleCondition("BIOIMPEDANCE_CI_PREGNANT_MCONDITION", ConditionOperator.OR).withAnswerCondition("BIOIMPEDANCE_CI_PREGNANT_ACONDITION_0", "CURRENTLY_PREGNANT", Y);
    builder.inCondition("BIOIMPEDANCE_CI_PREGNANT_MCONDITION").withAnswerCondition("BIOIMPEDANCE_CI_PREGNANT_ACONDITION_1", "CURRENTLY_PREGNANT", DNK);
    builder.inPage("1").withQuestion("CURRENT_PREGNANCY_WEEKS", "2").withSharedCategory(OPEN_N).withOpenAnswerDefinition(OPEN_N, DataType.INTEGER).setSize(2).addValidator(new NumberValidator.RangeValidator(0, 40));
    builder.inQuestion("CURRENT_PREGNANCY_WEEKS").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENT_PREGNANCY_WEEKS").withSharedCategory(DNK, "9");
    builder.inQuestion("CURRENT_PREGNANCY_WEEKS").setAnswerCondition("CURRENT_PREGNANCY_WEEKS_ACONDITION", "CURRENTLY_PREGNANT", Y);
    builder.inPage("1").withQuestion("HW_CI_BP").setMultipleCondition("HW_CI_BP_MCONDITION", ConditionOperator.OR).withDataCondition("HW_CI_BP_DCONDITION", "CURRENT_PREGNANCY_WEEKS", OPEN_N, OPEN_N, ComparisionOperator.ge, DataBuilder.buildInteger(12l));
    builder.inCondition("HW_CI_BP_MCONDITION").withAnswerCondition("HW_CI_BP_ACONDITION_0", "CURRENT_PREGNANCY_WEEKS", DNK);
    builder.inCondition("HW_CI_BP_MCONDITION").withAnswerCondition("HW_CI_BP_ACONDITION_1", "CURRENT_PREGNANCY_WEEKS", PNA);

    builder.inSection("S1_PARTICIPANT").withPage("4").withQuestion("LAST_FULL_MEAL_WHEN", "3").withSharedCategory(TIME_TODAY).withOpenAnswerDefinition(TIME_TODAY, DataType.INTEGER).withOpenAnswerDefinition("HOUR_TODAY", DataType.INTEGER).setSize(2).addValidator(new NumberValidator.RangeValidator(0, 23));
    builder.inOpenAnswerDefinition(TIME_TODAY).withOpenAnswerDefinition("MINUTE_TODAY", DataType.INTEGER).setSize(2).addValidator(new NumberValidator.RangeValidator(0, 59));
    builder.inQuestion("LAST_FULL_MEAL_WHEN").withSharedCategory(TIME_YESTERDAY).withOpenAnswerDefinition(TIME_YESTERDAY, DataType.INTEGER).withOpenAnswerDefinition("HOUR_YESTERDAY", DataType.INTEGER).setSize(2).addValidator(new NumberValidator.RangeValidator(0, 23));
    builder.inOpenAnswerDefinition(TIME_YESTERDAY).withOpenAnswerDefinition("MINUTE_YESTERDAY", DataType.INTEGER).setSize(2).addValidator(new NumberValidator.RangeValidator(0, 59));
    builder.inQuestion("LAST_FULL_MEAL_WHEN").withSharedCategory(HOURS_AGO).withOpenAnswerDefinition(HOURS_AGO, DataType.INTEGER).setSize(2).addValidator(new NumberValidator.RangeValidator(0, 24));
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

    builder.inSection("S1_PARTICIPANT").withPage("5").withQuestion("UNABLE_TO_STAND", "7").withSharedCategory(N, "0");
    builder.inQuestion("UNABLE_TO_STAND").withSharedCategory(Y, "1");
    builder.inPage("5").withQuestion("UNABLE_TO_STAND_CI_BP", "8").setAnswerCondition("UNABLE_TO_STAND_CI_BP_ACONDITION", "UNABLE_TO_STAND", Y);

    // section S2_BP_HR
    builder.withSection("S2_BP_HR").withSection("S2_EC_OBS").withPage("6").withQuestion("BP_OBSERVED_CI", "10").setRowCount(6).withSharedCategory(RASHES_BOTH_ARMS);
    builder.inQuestion("BP_OBSERVED_CI").withSharedCategory(CAST_BOTH_ARMS);
    builder.inQuestion("BP_OBSERVED_CI").withSharedCategory(DOUBLE_ARM_PARALYSIS);
    builder.inQuestion("BP_OBSERVED_CI").withSharedCategory(AMPUTATION_WITHERED);
    builder.inQuestion("BP_OBSERVED_CI").withSharedCategory(OTHER).withOpenAnswerDefinition(OTHER, DataType.TEXT);
    builder.inQuestion("BP_OBSERVED_CI").withSharedCategory(NOT_APP);

    builder.inSection("S2_BP_HR").withSection("S2_EC_IC_ASKED").withPage("7").withQuestion("LEFT_ARM_SURGERY_SHUNT", "13").withSharedCategory(N, "0");
    builder.inQuestion("LEFT_ARM_SURGERY_SHUNT").withSharedCategory(Y, "1");
    builder.inQuestion("LEFT_ARM_SURGERY_SHUNT").withSharedCategory(DNK, "9");
    builder.inQuestion("LEFT_ARM_SURGERY_SHUNT").setAnswerCondition("LEFT_ARM_SURGERY_SHUNT_ACONDITION", "BP_OBSERVED_CI", NOT_APP);
    builder.inPage("7").withQuestion("BP_LEFT_ARM_POSSIBLE").setAnswerCondition("BP_LEFT_ARM_POSSIBLE_ACONDITION", "LEFT_ARM_SURGERY_SHUNT", N);
    builder.inPage("7").withQuestion("RIGHT_ARM_SURGERY_SHUNT", "14").withSharedCategory(N, "0");
    builder.inQuestion("RIGHT_ARM_SURGERY_SHUNT").withSharedCategory(Y, "1");
    builder.inQuestion("RIGHT_ARM_SURGERY_SHUNT").withSharedCategory(DNK, "9");
    builder.inQuestion("RIGHT_ARM_SURGERY_SHUNT").setMultipleCondition("RIGHT_ARM_SURGERY_SHUNT_MCONDITION", ConditionOperator.OR).withAnswerCondition("RIGHT_ARM_SURGERY_SHUNT_ACONDITION_0", "LEFT_ARM_SURGERY_SHUNT", Y);
    builder.inCondition("RIGHT_ARM_SURGERY_SHUNT_MCONDITION").withAnswerCondition("RIGHT_ARM_SURGERY_SHUNT_ACONDITION_1", "LEFT_ARM_SURGERY_SHUNT", DNK);
    builder.inPage("7").withQuestion("BP_RIGHT_ARM_POSSIBLE").setAnswerCondition("BP_RIGHT_ARM_POSSIBLE_ACONDITION", "RIGHT_ARM_SURGERY_SHUNT", N);
    builder.inPage("7").withQuestion("BP_SAMPLES_SKIPPED_BP").setMultipleCondition("BP_SAMPLES_SKIPPED_BP_MCONDITION", ConditionOperator.OR).withAnswerCondition("BP_SAMPLES_SKIPPED_BP_ACONDITION_0", "RIGHT_ARM_SURGERY_SHUNT", Y);
    builder.inCondition("BP_SAMPLES_SKIPPED_BP_MCONDITION").withAnswerCondition("BP_SAMPLES_SKIPPED_BP_ACONDITION_1", "RIGHT_ARM_SURGERY_SHUNT", DNK);

    builder.inSection("S2_EC_IC_ASKED").withPage("8").withQuestion("BLOOD_DRAWN_LAST_7_DAYS", "15").withSharedCategory(N, "0");
    builder.inQuestion("BLOOD_DRAWN_LAST_7_DAYS").withSharedCategory(Y, "1");
    builder.inQuestion("BLOOD_DRAWN_LAST_7_DAYS").setMultipleCondition("BLOOD_DRAWN_LAST_7_DAYS_MCONDITION", ConditionOperator.OR).withAnswerCondition("BLOOD_DRAWN_LAST_7_DAYS_ACONDITION_0", "LEFT_ARM_SURGERY_SHUNT", N);
    builder.inCondition("BLOOD_DRAWN_LAST_7_DAYS_MCONDITION").withAnswerCondition("BLOOD_DRAWN_LAST_7_DAYS_ACONDITION_1", "RIGHT_ARM_SURGERY_SHUNT", N);
    builder.inPage("8").withQuestion("BLOOD_DRAWN_WHEN", "16").withCategory("DATE").withOpenAnswerDefinition("DATE", DataType.DATE).addValidator(ComparisionOperator.ge, DataSourceBuilder.createDateSource(Calendar.DAY_OF_YEAR, -7).getDataSource()).addValidator(ComparisionOperator.le, DataSourceBuilder.createDateSource().getDataSource());
    builder.inQuestion("BLOOD_DRAWN_WHEN").withCategory("OPEN_N7").withOpenAnswerDefinition("OPEN_N7", DataType.INTEGER).addValidator(new NumberValidator.MaximumValidator(7));
    builder.inQuestion("BLOOD_DRAWN_WHEN").setAnswerCondition("BLOOD_DRAWN_WHEN_ACONDITION", "BLOOD_DRAWN_LAST_7_DAYS", Y);

    builder.inSection("S2_BP_HR").withSection("S2_IC_OBS").withPage("9").withQuestion("BP_ARM_CHOSEN", "17").withCategories("RIGHT_ARM", "LEFT_ARM");
    builder.inQuestion("BP_ARM_CHOSEN").setMultipleCondition("BP_ARM_CHOSEN_MCONDITION", ConditionOperator.OR).withAnswerCondition("BP_ARM_CHOSEN_ACONDITION_0", "LEFT_ARM_SURGERY_SHUNT", N);
    builder.inCondition("BP_ARM_CHOSEN_MCONDITION").withAnswerCondition("BP_ARM_CHOSEN_ACONDITION_1", "RIGHT_ARM_SURGERY_SHUNT", N);

    // section S3_BONE_DENSITY
    builder.withSection("S3_BONE_DENSITY").withSection("S3_EC_OBS").withPage("10").withQuestion("BD_OBSERVED_CI", "18").withCategory("OPEN_WOUNDS_BOTH_HEELS");
    builder.inQuestion("BD_OBSERVED_CI").withSharedCategories(OTHER, NOT_APP);

    builder.inSection("S3_BONE_DENSITY").withSection("S3_EC_IC_ASKED").withPage("11").withQuestion("ISOPROPYL_ALCOHOL_ALLERGY", "21").withSharedCategories(N, Y, DNK);
    builder.inQuestion("ISOPROPYL_ALCOHOL_ALLERGY").setAnswerCondition("ISOPROPYL_ALCOHOL_ALLERGY_ACONDITION", "BD_OBSERVED_CI", NOT_APP);

    builder.inSection("S3_EC_IC_ASKED").withPage("18").withQuestion("BD_SAMPLES_CI_BP").setMultipleCondition("BD_SAMPLES_CI_BP_MCONDITION", ConditionOperator.OR).withAnswerCondition("BD_SAMPLES_CI_BP_ACONDITION_0", "ISOPROPYL_ALCOHOL_ALLERGY", Y);
    builder.inCondition("BD_SAMPLES_CI_BP_MCONDITION").withAnswerCondition("BD_SAMPLES_CI_BP_ACONDITION_1", "ISOPROPYL_ALCOHOL_ALLERGY", DNK);

    builder.inSection("S3_EC_IC_ASKED").withPage("19").withQuestion("LEFT_LOWER_EXTREMITY_FRACTURE_5_YEARS", "23").withSharedCategories(N, Y, NOT_APP, DNK);
    builder.inQuestion("LEFT_LOWER_EXTREMITY_FRACTURE_5_YEARS").setAnswerCondition("LEFT_LOWER_EXTREMITY_FRACTURE_5_YEARS_ACONDITION", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inPage("19").withQuestion("RIGHT_LOWER_EXTREMITY_FRACTURE_5_YEARS", "24").withSharedCategories(N, Y, NOT_APP, DNK);
    builder.inQuestion("RIGHT_LOWER_EXTREMITY_FRACTURE_5_YEARS").setAnswerCondition("RIGHT_LOWER_EXTREMITY_FRACTURE_5_YEARS_ACONDITION", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inPage("19").withQuestion("LEFT_LOWER_EXTREMITY_IMPLANT_5_YEARS", "25").withSharedCategories(N, Y, NOT_APP, DNK);
    builder.inQuestion("LEFT_LOWER_EXTREMITY_IMPLANT_5_YEARS").setAnswerCondition("LEFT_LOWER_EXTREMITY_IMPLANT_5_YEARS_ACONDITION", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inPage("19").withQuestion("RIGHT_LOWER_EXTREMITY_IMPLANT_5_YEARS", "26").withSharedCategories(N, Y, NOT_APP, DNK);
    builder.inQuestion("RIGHT_LOWER_EXTREMITY_IMPLANT_5_YEARS").setAnswerCondition("RIGHT_LOWER_EXTREMITY_IMPLANT_5_YEARS_ACONDITION", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inPage("19").withQuestion("LEFT_LOWER_EXTREMITY_INJURY_1_MONTH", "27").withSharedCategories(N, Y, NOT_APP, DNK);
    builder.inQuestion("LEFT_LOWER_EXTREMITY_INJURY_1_MONTH").setAnswerCondition("LEFT_LOWER_EXTREMITY_INJURY_1_MONTH_ACONDITION", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inPage("19").withQuestion("RIGHT_LOWER_EXTREMITY_INJURY_1_MONTH", "28").withSharedCategories(N, Y, NOT_APP, DNK);
    builder.inQuestion("RIGHT_LOWER_EXTREMITY_INJURY_1_MONTH").setAnswerCondition("RIGHT_LOWER_EXTREMITY_INJURY_1_MONTH_ACONDITION", "ISOPROPYL_ALCOHOL_ALLERGY", N);

    builder.inSection("S3_EC_IC_ASKED").withPage("20").withQuestion("BD_HANDEDNESS", "29").withCategory("LEFT_HANDED");
    builder.inQuestion("BD_HANDEDNESS").withCategory("RIGHT_HANDED");
    builder.inQuestion("BD_HANDEDNESS").withCategory("AMBIDEXTROUS");
    builder.inQuestion("BD_HANDEDNESS").setAnswerCondition("BD_HANDEDNESS_ACONDITION", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inPage("20").withQuestion("BD_LEFT_POSSIBLE");

    builder.inQuestion("BD_LEFT_POSSIBLE").setMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_0", ConditionOperator.OR);
    /*
     * ( ((Q29.RIGHT) OR (Q29.AMBI)) AND ( ((Q23.NO) AND (Q25.NO) AND (Q27.NO)) OR ( ((Q23.YES) OR (Q25.YES) OR
     * (Q27.YES)) AND ((Q24.YES) OR (Q26.YES) OR (Q28.YES)) ) ) ) OR ( (Q29.LEFT) AND ((Q24.YES) OR (Q26.YES) OR
     * (Q28.YES)) AND ((Q23.NO) AND (Q25.NO) AND (Q27.NO)) )
     */
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_0").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_1", ConditionOperator.AND);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_1_1", ConditionOperator.OR);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_0", "BD_HANDEDNESS", "RIGHT_HANDED");
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_1", "BD_HANDEDNESS", "AMBIDEXTROUS");
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2", ConditionOperator.OR);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_1", ConditionOperator.AND);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_2", "LEFT_LOWER_EXTREMITY_FRACTURE_5_YEARS", N);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_3", "LEFT_LOWER_EXTREMITY_IMPLANT_5_YEARS", N);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_4", "LEFT_LOWER_EXTREMITY_INJURY_1_MONTH", N);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_2", ConditionOperator.AND);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_2").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_2_1", ConditionOperator.OR);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_2_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_5", "LEFT_LOWER_EXTREMITY_FRACTURE_5_YEARS", Y);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_2_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_6", "LEFT_LOWER_EXTREMITY_IMPLANT_5_YEARS", Y);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_2_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_7", "LEFT_LOWER_EXTREMITY_INJURY_1_MONTH", Y);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_2").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_2_2", ConditionOperator.OR);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_2_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_8", "RIGHT_LOWER_EXTREMITY_FRACTURE_5_YEARS", Y);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_2_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_9", "RIGHT_LOWER_EXTREMITY_IMPLANT_5_YEARS", Y);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_2_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_10", "RIGHT_LOWER_EXTREMITY_INJURY_1_MONTH", Y);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_0").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_2", ConditionOperator.AND);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_2").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_11", "BD_HANDEDNESS", "LEFT_HANDED");
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_2").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_2_1", ConditionOperator.OR);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_2_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_12", "RIGHT_LOWER_EXTREMITY_FRACTURE_5_YEARS", Y);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_2_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_13", "RIGHT_LOWER_EXTREMITY_IMPLANT_5_YEARS", Y);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_2_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_14", "RIGHT_LOWER_EXTREMITY_INJURY_1_MONTH", Y);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_2").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_2_2", ConditionOperator.AND);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_2_2").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_15", "LEFT_LOWER_EXTREMITY_FRACTURE_5_YEARS", N);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_2_2").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_16", "LEFT_LOWER_EXTREMITY_IMPLANT_5_YEARS", N);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_2_2").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_17", "LEFT_LOWER_EXTREMITY_INJURY_1_MONTH", N);

    builder.inPage("20").withQuestion("BD_RIGHT_POSSIBLE").setMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_0", ConditionOperator.OR);
    /*
     * ( (Q29.LEFT) AND ( ((Q24.NO) AND (Q26.NO) AND (Q28.NO)) OR ( ((Q23.YES) OR (Q25.YES) OR (Q27.YES)) AND ((Q24.YES)
     * OR (Q26.YES) OR (Q28.YES)) ) ) ) OR ( ((Q29.RIGHT) OR (Q29.AMBIDEXTRE)) AND ((Q23.YES) OR (Q25.YES) OR (Q27.YES))
     * AND ((Q24.NO) AND (Q26.NO) AND (Q28.NO)) )
     */
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_0").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_1", ConditionOperator.AND);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_0", "BD_HANDEDNESS", "LEFT_HANDED");
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1", ConditionOperator.OR);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_1", ConditionOperator.AND);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_1", "RIGHT_LOWER_EXTREMITY_FRACTURE_5_YEARS", N);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_2", "RIGHT_LOWER_EXTREMITY_IMPLANT_5_YEARS", N);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_3", "RIGHT_LOWER_EXTREMITY_INJURY_1_MONTH", N);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_2", ConditionOperator.AND);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_2").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_2_1", ConditionOperator.OR);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_2_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_4", "LEFT_LOWER_EXTREMITY_FRACTURE_5_YEARS", Y);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_2_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_5", "LEFT_LOWER_EXTREMITY_IMPLANT_5_YEARS", Y);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_2_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_6", "LEFT_LOWER_EXTREMITY_INJURY_1_MONTH", Y);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_2").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_2_2", ConditionOperator.OR);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_2_2").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_7", "RIGHT_LOWER_EXTREMITY_FRACTURE_5_YEARS", Y);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_2_2").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_8", "RIGHT_LOWER_EXTREMITY_IMPLANT_5_YEARS", Y);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_2_2").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_9", "RIGHT_LOWER_EXTREMITY_INJURY_1_MONTH", Y);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_0").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_2", ConditionOperator.AND);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_2_1", ConditionOperator.OR);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_10", "BD_HANDEDNESS", "RIGHT_HANDED");
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_11", "BD_HANDEDNESS", "AMBIDEXTROUS");
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_2_2", ConditionOperator.OR);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2_2").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_12", "LEFT_LOWER_EXTREMITY_FRACTURE_5_YEARS", Y);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2_2").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_13", "LEFT_LOWER_EXTREMITY_IMPLANT_5_YEARS", Y);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2_2").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_14", "LEFT_LOWER_EXTREMITY_INJURY_1_MONTH", Y);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_2_3", ConditionOperator.AND);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2_3").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_15", "RIGHT_LOWER_EXTREMITY_FRACTURE_5_YEARS", N);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2_3").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_16", "RIGHT_LOWER_EXTREMITY_IMPLANT_5_YEARS", N);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2_3").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_17", "RIGHT_LOWER_EXTREMITY_INJURY_1_MONTH", N);

    builder.inSection("S3_BONE_DENSITY").withSection("S3_IC_OBS").withPage("21").withQuestion("BD_FOOT_CHOSEN", "30").withCategories("LEFT_FOOT", "RIGHT_FOOT");
    builder.inQuestion("BD_FOOT_CHOSEN").setAnswerCondition("BD_FOOT_CHOSEN_ACONDITION", "ISOPROPYL_ALCOHOL_ALLERGY", N);

    // section S4_GRIP_STRENGTH
    builder.withSection("S4_GRIP_STRENGTH").withSection("S4_EC_OBS").withPage("22").withQuestion("GS_OBS_CI", "31").withCategories("DOUBLE_ARM_PARALYSIS", "AMPUTATION_WITHERED_BOTH_ARMS");
    builder.inQuestion("GS_OBS_CI").withSharedCategories(OTHER, NOT_APP);
    builder.inQuestion("GS_OBS_CI").setAnswerCondition("GS_OBS_CI_ACONDITION", "UNABLE_TO_STAND", N);

    builder.inSection("S4_GRIP_STRENGTH").withSection("S4_EC_ASKED").withPage("23").withQuestion("GS_SURGERY_BOTH_HANDS_13_WEEKS", "33").withSharedCategories(N, Y, DNK);
    builder.inQuestion("GS_SURGERY_BOTH_HANDS_13_WEEKS").setAnswerCondition("GS_SURGERY_BOTH_HANDS_13_WEEKS_ACONDIITON", "GS_OBS_CI", NOT_APP);
    builder.inPage("23").withQuestion("GS_SURGERY_CI_BP").setMultipleCondition("GS_SURGERY_CI_BP_MCONDITION", ConditionOperator.OR).withAnswerCondition("GS_SURGERY_CI_BP_ACONDITION_0", "GS_SURGERY_BOTH_HANDS_13_WEEKS", Y);
    builder.inCondition("GS_SURGERY_CI_BP_MCONDITION").withAnswerCondition("GS_SURGERY_CI_BP_ACONDITION_1", "GS_SURGERY_BOTH_HANDS_13_WEEKS", DNK);

    builder.inSection("S4_EC_ASKED").withPage("27").withQuestion("GS_PAIN_BOTH_HANDS", "34").withSharedCategories(N, Y, DNK);
    builder.inQuestion("GS_PAIN_BOTH_HANDS").setAnswerCondition("GS_PAIN_BOTH_HANDS_ACONDITION", "GS_SURGERY_BOTH_HANDS_13_WEEKS", N);

    builder.inSection("S4_EC_ASKED").withPage("28").withQuestion("GS_PAIN_CI_BP");
    builder.inQuestion("GS_PAIN_CI_BP").setMultipleCondition("GS_PAIN_CI_BP_MCONDITION", ConditionOperator.OR).withAnswerCondition("GS_PAIN_CI_BP_ACONDITION_0", "GS_PAIN_BOTH_HANDS", Y);
    builder.inCondition("GS_PAIN_CI_BP_MCONDITION").withAnswerCondition("GS_PAIN_CI_BP_ACONDITION_1", "GS_PAIN_BOTH_HANDS", DNK);

    // section S5_STANDING_HEIGHT
    builder.withSection("S5_STANDING_HEIGHT").withSection("S5_EC_OBS").withPage("29").withQuestion("SH_OBS_CI", "35").withSharedCategories(N, Y);
    builder.inQuestion("SH_OBS_CI").setAnswerCondition("SH_OBS_CI_ACONDITION", "UNABLE_TO_STAND", N);

    builder.inSection("S5_STANDING_HEIGHT").withSection("S5_IC_OBS").withPage("30").withQuestion("SH_BACK_NOT_STRAIGHT", "38").withSharedCategories(N, Y);
    builder.inQuestion("SH_BACK_NOT_STRAIGHT").setAnswerCondition("SH_BACK_NOT_STRAIGHT_ACONDITION", "SH_OBS_CI", N);

    // section S6_SITTING_HEIGHT
    builder.withSection("S6_SITTING_HEIGHT").withSection("S6_EC_OBS").withPage("31").withQuestion("SITH_OBS_CI", "39").withCategory("UNABLE_TO_SIT");
    builder.inQuestion("SITH_OBS_CI").withSharedCategories(OTHER, NOT_APP);
    builder.inSection("S6_SITTING_HEIGHT").withSection("S6_IC_OBS").withPage("32").withQuestion("SITH_BACK_NOT_STRAIGHT", "42").withSharedCategories(N, Y);
    builder.inQuestion("SITH_BACK_NOT_STRAIGHT").setAnswerCondition("SITH_BACK_NOT_STRAIGHT_ACONDITION", "SITH_OBS_CI", NOT_APP);

    // section S7_WAIST_HIPS
    builder.withSection("S7_WAIST_HIPS").withSection("S7_EC_OBS").withPage("33").withQuestion("WH_OBS_CI", "43").withSharedCategories(N, Y);
    builder.inQuestion("WH_OBS_CI").setMultipleCondition("WH_OBS_CI_MCONDITION", ConditionOperator.OR).withAnswerCondition("WH_OBS_CI_ACONDITION", "CURRENTLY_PREGNANT", N);
    builder.inCondition("WH_OBS_CI_MCONDITION").withDataCondition("WH_OBS_CI_DCONDITION", "CURRENT_PREGNANCY_WEEKS", OPEN_N, OPEN_N, ComparisionOperator.le, DataBuilder.buildInteger(12l));

    // section S8_SPIROMETRY
    builder.withSection("S8_SPIROMETRY").withSection("S8_EC_OBS").withPage("34").withQuestion("SP_OBS_CI", "45").withSharedCategories(N, Y);
    builder.inQuestion("SP_OBS_CI").setAnswerCondition("SP_OBS_CI_ACONDITION", "CURRENTLY_PREGNANT", N);

    builder.inSection("S8_SPIROMETRY").withSection("S8_EC_ASKED").withPage("35").withQuestion("SP_CI_CURRENT", "47").withCategory("YES_SPECIFY").withOpenAnswerDefinition("YES_SPECIFY", DataType.TEXT);
    builder.inQuestion("SP_CI_CURRENT").withSharedCategories(N, DNK);
    builder.inQuestion("SP_CI_CURRENT").setAnswerCondition("SP_CI_CURRENT_ACONDITION", "SP_OBS_CI", N);
    builder.inQuestion("SP_CI_CURRENT").withQuestion("SP_MED_TB", "1");
    builder.inQuestion("SP_CI_CURRENT").withQuestion("SP_STOMA", "2");
    builder.inQuestion("SP_CI_CURRENT").withQuestion("SP_SEVERE_RESP_COND", "3");

    builder.inSection("S8_EC_ASKED").withPage("37").withQuestion("SP_PULMONARY_INF_LAST_MONTH", "48").withSharedCategories(N, Y, DNK);
    builder.inQuestion("SP_PULMONARY_INF_LAST_MONTH").setMultipleCondition("SP_PULMONARY_INF_LAST_MONTH_MCONDITION", ConditionOperator.AND).withAnswerCondition("SP_PULMONARY_INF_LAST_MONTH_ACONDITION_0", "SP_MED_TB", N);
    builder.inCondition("SP_PULMONARY_INF_LAST_MONTH_MCONDITION").withAnswerCondition("SP_PULMONARY_INF_LAST_MONTH_ACONDITION_1", "SP_STOMA", N);
    builder.inCondition("SP_PULMONARY_INF_LAST_MONTH_MCONDITION").withAnswerCondition("SP_PULMONARY_INF_LAST_MONTH_ACONDITION_2", "SP_SEVERE_RESP_COND", N);
    builder.inPage("37").withQuestion("SP_CI_3_MONTHS", "49").withSharedCategories(N, Y, DNK);
    builder.inQuestion("SP_CI_3_MONTHS").setAnswerCondition("SP_CI_3_MONTHS_ACONDITION", "SP_PULMONARY_INF_LAST_MONTH", N);
    builder.inQuestion("SP_CI_3_MONTHS").withQuestion("SP_ABD_THORACIC_SURGERY", "1");
    builder.inQuestion("SP_CI_3_MONTHS").withQuestion("SP_EYE_SURGERY", "2");
    builder.inQuestion("SP_CI_3_MONTHS").withQuestion("SP_HEART_ATTACK", "3");

    builder.inSection("S8_EC_ASKED").withPage("38").withQuestion("SP_CI_EVER_HAD", "50").withSharedCategories(N, Y, DNK);
    builder.inQuestion("SP_CI_EVER_HAD").setMultipleCondition("SP_CI_EVER_HAD_MCONDITION", ConditionOperator.AND).withAnswerCondition("SP_CI_EVER_HAD_ACONDITION_0", "SP_ABD_THORACIC_SURGERY", N);
    builder.inCondition("SP_CI_EVER_HAD_MCONDITION").withAnswerCondition("SP_CI_EVER_HAD_ACONDITION_1", "SP_EYE_SURGERY", N);
    builder.inCondition("SP_CI_EVER_HAD_MCONDITION").withAnswerCondition("SP_CI_EVER_HAD_ACONDITION_2", "SP_HEART_ATTACK", N);
    builder.inQuestion("SP_CI_EVER_HAD").withQuestion("SP_COLLAPSED_LUNG", "1");
    builder.inQuestion("SP_CI_EVER_HAD").withQuestion("SP_DETACHED_RETINA", "1");

    builder.inSection("S8_SPIROMETRY").withSection("S8_IC_ASKED").withPage("39").withQuestion("SP_BRONCHODILATATOR_USE_24_HOURS", "51").withSharedCategories(N, Y, DNK);
    builder.inQuestion("SP_BRONCHODILATATOR_USE_24_HOURS").setMultipleCondition("SP_BRONCHODILATATOR_USE_24_HOURS_MCONDITION", ConditionOperator.AND).withAnswerCondition("SP_BRONCHODILATATOR_USE_24_HOURS_ACONDITION_0", "SP_COLLAPSED_LUNG", N);
    builder.inCondition("SP_BRONCHODILATATOR_USE_24_HOURS_MCONDITION").withAnswerCondition("SP_BRONCHODILATATOR_USE_24_HOURS_ACONDITION_1", "SP_DETACHED_RETINA", N);
    builder.inPage("39").withQuestion("SP_BRONCHODILATATOR_USE_WHEN", "52").withSharedCategories(TIME_TODAY, TIME_YESTERDAY, HOURS_AGO);

    // section S9_BIOIMPEDANCE_WEIGHT
    builder.withSection("S9_BIOIMPEDANCE_WEIGHT").withSection("S9_EC_OBS").withPage("40").withQuestion("BW_OBS_CI_BIOIMPEDANCE", "54").withSharedCategories(N, Y);
    builder.inQuestion("BW_OBS_CI_BIOIMPEDANCE").setMultipleCondition("BW_OBS_CI_BIOIMPEDANCE_MCONDITION", ConditionOperator.AND).withAnswerCondition("BW_OBS_CI_BIOIMPEDANCE_ACONDITION_0", "CURRENTLY_PREGNANT", N);
    builder.inCondition("BW_OBS_CI_BIOIMPEDANCE_MCONDITION").withAnswerCondition("BW_OBS_CI_BIOIMPEDANCE_ACONDITION_1", "UNABLE_TO_STAND", N);

    builder.inSection("S9_EC_OBS").withPage("41").withQuestion("BW_OBS_CI_WEIGHT", "57").withSharedCategories(N, Y);
    builder.inQuestion("BW_OBS_CI_WEIGHT").setMultipleCondition("BW_OBS_CI_WEIGHT_MCONDITION_0", ConditionOperator.OR).withAnswerCondition("BW_OBS_CI_WEIGHT_ACONDITION_0", "BW_OBS_CI_BIOIMPEDANCE", Y);
    builder.inCondition("BW_OBS_CI_WEIGHT_MCONDITION_0").withMultipleCondition("BW_OBS_CI_WEIGHT_MCONDITION_1", ConditionOperator.AND).withAnswerCondition("BW_OBS_CI_WEIGHT_ACONDITION_1", "CURRENTLY_PREGNANT", Y);
    builder.inCondition("BW_OBS_CI_WEIGHT_MCONDITION_1").withDataCondition("BW_OBS_CI_WEIGHT_DCONDITION", "CURRENT_PREGNANCY_WEEKS", OPEN_N, OPEN_N, ComparisionOperator.le, DataBuilder.buildInteger(12l));

    builder.inSection("S9_BIOIMPEDANCE_WEIGHT").withSection("S9_EC_ASKED").withPage("42").withQuestion("BW_PACEMAKER_CI", "60").withSharedCategories(N, Y, DNK);
    builder.inQuestion("BW_PACEMAKER_CI").setAnswerCondition("BW_PACEMAKER_CI_ACONDITION", "BW_OBS_CI_BIOIMPEDANCE");

    // section S10_ARTERIAL_STIFFNESS
    builder.withSection("S10_ARTERIAL_STIFFNESS").withSection("S10_EC_ASKED").withPage("43").withQuestion("AS_STENOSIS_CI", "60a").withSharedCategories(N, Y, DNK);
    builder.inPage("43").withQuestion("AS_STENOSIS_GRADIENT", "60b").withCategory("GRADIENT").withOpenAnswerDefinition("GRADIENT", DataType.INTEGER).setUnit("PERCENT");
    builder.inQuestion("AS_STENOSIS_GRADIENT").withSharedCategory(DNK);
    builder.inQuestion("AS_STENOSIS_GRADIENT").setAnswerCondition("AS_STENOSIS_GRADIENT", "AS_STENOSIS_CI", Y);

    // section S11_SAMPLES
    builder.withSection("S11_SAMPLES").withSection("S11_EC_OBS").withPage("44").withQuestion("SC_OBS_CI", "61").withCategories("RASHES_BOTH_ARMS", "CAST_BOTH_ARMS", "DOUBLE_ARM_PARALYSIS", "AMPUTATION_WITHERED", "OCCLUDED_VEINS_BOTH_ARMS", "BURNED_SCARRED_BOTH_ARMS");
    builder.inQuestion("SC_OBS_CI").withSharedCategories(OTHER, NOT_APP);
    builder.inQuestion("SC_OBS_CI").setMultipleCondition("SC_OBS_CI_MCONDITION_0", ConditionOperator.OR).withAnswerCondition("SC_OBS_CI_ACONDITION_0", "BP_OBSERVED_CI", OTHER);
    builder.inCondition("SC_OBS_CI_MCONDITION_0").withMultipleCondition("SC_OBS_CI_MCONDITION_1", ConditionOperator.AND).withAnswerCondition("SC_OBS_CI_ACONDITION_1", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inCondition("SC_OBS_CI_MCONDITION_1").withMultipleCondition("SC_OBS_CI_MCONDITION_2", ConditionOperator.OR).withAnswerCondition("SC_OBS_CI_ACONDITION_2", "LEFT_ARM_SURGERY_SHUNT", N);
    builder.inCondition("SC_OBS_CI_MCONDITION_2").withAnswerCondition("SC_OBS_CI_ACONDITION_3", "RIGHT_ARM_SURGERY_SHUNT", N);

    builder.inSection("S11_EC_OBS").withPage("45").withQuestion("SC_LEFT_ARM_SURGERY_SHUNT", "64").withSharedCategories(N, Y, DNK);
    builder.inQuestion("SC_LEFT_ARM_SURGERY_SHUNT").setMultipleCondition("SC_LEFT_ARM_SURGERY_SHUNT_MCONDITION", ConditionOperator.AND).withAnswerCondition("SC_LEFT_ARM_SURGERY_SHUNT_ACONDITION_0", "SC_OBS_CI", NOT_APP);
    builder.inCondition("SC_LEFT_ARM_SURGERY_SHUNT_MCONDITION").withNoAnswerCondition("SC_LEFT_ARM_SURGERY_SHUNT_NCONDITION").withAnswerCondition("SC_LEFT_ARM_SURGERY_SHUNT_ACONDITION_1", "LEFT_ARM_SURGERY_SHUNT");
    builder.inPage("45").withQuestion("SC_RIGHT_ARM_SURGERY_SHUNT", "65").withSharedCategories(N, Y, DNK);
    builder.inQuestion("SC_RIGHT_ARM_SURGERY_SHUNT").setMultipleCondition("SC_RIGHT_ARM_SURGERY_SHUNT_MCONDITION", ConditionOperator.OR).withAnswerCondition("SC_RIGHT_ARM_SURGERY_SHUNT_ACONDITION_0", "SC_LEFT_ARM_SURGERY_SHUNT", Y);
    builder.inCondition("SC_RIGHT_ARM_SURGERY_SHUNT_MCONDITION").withAnswerCondition("SC_RIGHT_ARM_SURGERY_SHUNT_ACONDITION_1", "SC_LEFT_ARM_SURGERY_SHUNT", DNK);
    builder.inPage("45").withQuestion("SC_HEMOPHILIA_CI", "66").withSharedCategories(N, Y, DNK);
    builder.inQuestion("SC_HEMOPHILIA_CI").setMultipleCondition("SC_HEMOPHILIA_CI_MCONDITION_0", ConditionOperator.AND).withAnswerCondition("SC_HEMOPHILIA_CI_ACONDITION_0", "SC_OBS_CI", NOT_APP);
    builder.inCondition("SC_HEMOPHILIA_CI_MCONDITION_0").withMultipleCondition("SC_HEMOPHILIA_CI_MCONDITION_1", ConditionOperator.OR).withAnswerCondition("SC_HEMOPHILIA_CI_ACONDITION_1", "SC_LEFT_ARM_SURGERY_SHUNT", N);
    builder.inCondition("SC_HEMOPHILIA_CI_MCONDITION_1").withAnswerCondition("SC_HEMOPHILIA_CI_ACONDITION_2", "SC_RIGHT_ARM_SURGERY_SHUNT", N);
    builder.inCondition("SC_HEMOPHILIA_CI_MCONDITION_1").withAnswerCondition("SC_HEMOPHILIA_CI_ACONDITION_3", "LEFT_ARM_SURGERY_SHUNT", N);
    builder.inCondition("SC_HEMOPHILIA_CI_MCONDITION_1").withAnswerCondition("SC_HEMOPHILIA_CI_ACONDITION_4", "RIGHT_ARM_SURGERY_SHUNT", N);
    builder.inPage("45").withQuestion("SC_ISOPROPYL_ALCOHOL_ALLERGY", "67").withSharedCategories(N, Y, DNK);
    builder.inQuestion("SC_ISOPROPYL_ALCOHOL_ALLERGY").setMultipleCondition("SC_ISOPROPYL_ALCOHOL_ALLERGY_MCONDITION", ConditionOperator.AND).withAnswerCondition("SC_ISOPROPYL_ALCOHOL_ALLERGY_ACONDITION_0", "SC_HEMOPHILIA_CI", N);
    builder.inCondition("SC_ISOPROPYL_ALCOHOL_ALLERGY_MCONDITION").withNoAnswerCondition("SC_ISOPROPYL_ALCOHOL_ALLERGY_NCONDITION").withAnswerCondition("SC_ISOPROPYL_ALCOHOL_ALLERGY_ACONDITION_1", "ISOPROPYL_ALCOHOL_ALLERGY");
    builder.inPage("45").withQuestion("BLOOD_TRANSFUSION_24_HOURS", "68").withSharedCategories(N, Y, DNK);
    builder.inQuestion("BLOOD_TRANSFUSION_24_HOURS").setMultipleCondition("BLOOD_TRANSFUSION_24_HOURS_MCONDITION_0", ConditionOperator.OR).withAnswerCondition("BLOOD_TRANSFUSION_24_HOURS_ACONDITION_0", "SC_ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inCondition("BLOOD_TRANSFUSION_24_HOURS_MCONDITION_0").withMultipleCondition("BLOOD_TRANSFUSION_24_HOURS_MCONDITION_1", ConditionOperator.AND).withAnswerCondition("BLOOD_TRANSFUSION_24_HOURS_ACONDITION_1", "SC_HEMOPHILIA_CI", N);
    builder.inCondition("BLOOD_TRANSFUSION_24_HOURS_MCONDITION_1").withAnswerCondition("BLOOD_TRANSFUSION_24_HOURS_ACONDITION_2", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inPage("45").withQuestion("DONATED_BLOOD_24_HOURS", "69").withSharedCategories(N, Y, DNK);
    builder.inQuestion("DONATED_BLOOD_24_HOURS").setAnswerCondition("DONATED_BLOOD_24_HOURS_ACONDITION", "BLOOD_TRANSFUSION_24_HOURS", N);
    builder.inPage("45").withQuestion("BLOOD_DRAWN_FROM_LEFT_POSSIBLE").setMultipleCondition("BLOOD_DRAWN_FROM_LEFT_POSSIBLE_MCONDITION_0", ConditionOperator.AND).withAnswerCondition("BLOOD_DRAWN_FROM_LEFT_POSSIBLE_ACONDITION_0", "DONATED_BLOOD_24_HOURS", N);
    builder.inCondition("BLOOD_DRAWN_FROM_LEFT_POSSIBLE_MCONDITION_0").withMultipleCondition("BLOOD_DRAWN_FROM_LEFT_POSSIBLE_MCONDITION_1", ConditionOperator.OR).withAnswerCondition("BLOOD_DRAWN_FROM_LEFT_POSSIBLE_ACONDITION_1", "SC_LEFT_ARM_SURGERY_SHUNT", N);
    builder.inCondition("BLOOD_DRAWN_FROM_LEFT_POSSIBLE_MCONDITION_1").withAnswerCondition("BLOOD_DRAWN_FROM_LEFT_POSSIBLE_ACONDITION_2", "LEFT_ARM_SURGERY_SHUNT", N);
    builder.inPage("45").withQuestion("BLOOD_DRAWN_FROM_RIGHT_POSSIBLE").setMultipleCondition("BLOOD_DRAWN_FROM_RIGHT_POSSIBLE_MCONDITION_0", ConditionOperator.AND).withAnswerCondition("BLOOD_DRAWN_FROM_RIGHT_POSSIBLE_ACONDITION_0", "DONATED_BLOOD_24_HOURS", N);
    builder.inCondition("BLOOD_DRAWN_FROM_RIGHT_POSSIBLE_MCONDITION_0").withMultipleCondition("BLOOD_DRAWN_FROM_RIGHT_POSSIBLE_MCONDITION_1", ConditionOperator.OR).withAnswerCondition("BLOOD_DRAWN_FROM_RIGHT_POSSIBLE_ACONDITION_1", "SC_RIGHT_ARM_SURGERY_SHUNT", N);
    builder.inCondition("BLOOD_DRAWN_FROM_RIGHT_POSSIBLE_MCONDITION_1").withAnswerCondition("BLOOD_DRAWN_FROM_RIGHT_POSSIBLE_ACONDITION_2", "RIGHT_ARM_SURGERY_SHUNT", N);
    builder.inPage("45").withQuestion("BLOOD_DRAW_CONTRAINDICATED").setMultipleCondition("BLOOD_DRAW_CONTRAINDICATED_MCONDITION_0", ConditionOperator.AND).withNoAnswerCondition("BLOOD_DRAW_CONTRAINDICATED_NCONDITION_0").withMultipleCondition("BLOOD_DRAW_CONTRAINDICATED_MCONDITION_1", ConditionOperator.AND).withAnswerCondition("BLOOD_DRAW_CONTRAINDICATED_ACONDITION_0", "DONATED_BLOOD_24_HOURS", N);
    builder.inCondition("BLOOD_DRAW_CONTRAINDICATED_MCONDITION_1").withMultipleCondition("BLOOD_DRAW_CONTRAINDICATED_MCONDITION_2", ConditionOperator.OR).withAnswerCondition("BLOOD_DRAW_CONTRAINDICATED_ACONDITION_1", "SC_LEFT_ARM_SURGERY_SHUNT", N);
    builder.inCondition("BLOOD_DRAW_CONTRAINDICATED_MCONDITION_2").withAnswerCondition("BLOOD_DRAW_CONTRAINDICATED_ACONDITION_2", "LEFT_ARM_SURGERY_SHUNT", N);
    builder.inCondition("BLOOD_DRAW_CONTRAINDICATED_MCONDITION_0").withNoAnswerCondition("BLOOD_DRAW_CONTRAINDICATED_NCONDITION_1").withMultipleCondition("BLOOD_DRAW_CONTRAINDICATED_MCONDITION_3", ConditionOperator.AND).withAnswerCondition("BLOOD_DRAW_CONTRAINDICATED_ACONDITION_3", "DONATED_BLOOD_24_HOURS", N);
    builder.inCondition("BLOOD_DRAW_CONTRAINDICATED_MCONDITION_3").withMultipleCondition("BLOOD_DRAW_CONTRAINDICATED_MCONDITION_4", ConditionOperator.OR).withAnswerCondition("BLOOD_DRAW_CONTRAINDICATED_ACONDITION_4", "SC_RIGHT_ARM_SURGERY_SHUNT", N);
    builder.inCondition("BLOOD_DRAW_CONTRAINDICATED_MCONDITION_4").withAnswerCondition("BLOOD_DRAW_CONTRAINDICATED_ACONDITION_5", "RIGHT_ARM_SURGERY_SHUNT", N);

    return builder;
  }
}
