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
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ConditionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Returns the content for the CI Preliminary Questionnaire
 */
public class CIPreliminaryQuestionnaireContentBuilder {

  private static final String N = "N";

  private static final String Y = "Y";

  private static final String NC = "NC";

  private static final String YES_SPECIFY = "YES_SPECIFY";

  private static final String OTHER = "OTHER";

  private static final String DNK = "DNK";

  private static final String PNA = "PNA";

  private static final String OPEN_N40 = "OPEN_N40";

  private static final String TIME_24 = "TIME_24";

  private static final String MORE_24_HOURS = "MORE_24_HOURS";

  private static final String YEAR_MONTH = "YEAR_MONTH";

  private static final String AGE = "AGE";

  public static QuestionnaireBuilder buildCIPreliminaryQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("CIPreliminaryQuestionnaire", "1.0");

    // section S1_PARTICIPANT
    builder.withSection("S1_COMMON").withSection("S1_PARTICIPANT").withPage("1").withQuestion("S1_BP1");
    builder.inPage("1").withQuestion("ABLE_TO_STAND"/* , "0" */).withSharedCategories(N, Y);

    builder.inSection("S1_PARTICIPANT").withPage("2").withQuestion("S1_BP2").setDataCondition("CURRENTLY_PREGNANT_DCONDITION", ComparisonOperator.eq, Gender.FEMALE);
    builder.inPage("2").withQuestion("CURRENTLY_PREGNANT"/* , "1" */).withSharedCategories(N, Y, PNA, DNK);
    builder.inQuestion("CURRENTLY_PREGNANT").setDataCondition("CURRENTLY_PREGNANT_DCONDITION", ComparisonOperator.eq, Gender.FEMALE);
    builder.inPage("2").withQuestion("CURRENT_PREGNANCY_WEEKS"/* , "2" */).withSharedCategory(OPEN_N40).withOpenAnswerDefinition(OPEN_N40, DataType.INTEGER).setSize(2).addValidator(new NumberValidator.RangeValidator(0, 40)).setSize(2);
    builder.inQuestion("CURRENT_PREGNANCY_WEEKS").withSharedCategories(PNA, DNK);
    builder.inQuestion("CURRENT_PREGNANCY_WEEKS").setAnswerCondition("CURRENT_PREGNANCY_WEEKS_ACONDITION", "CURRENTLY_PREGNANT", Y);

    builder.inSection("S1_PARTICIPANT").withPage("3").withQuestion("LAST_MEAL_WHEN"/* , "3" */).withSharedCategory(TIME_24).withOpenAnswerDefinition(TIME_24, DataType.DATE).withOpenAnswerDefinition("HOUR_24", DataType.INTEGER).setDefaultData(DataBuilder.buildInteger(0l), DataBuilder.buildInteger(1l), DataBuilder.buildInteger(2l), DataBuilder.buildInteger(3l), DataBuilder.buildInteger(4l), DataBuilder.buildInteger(5l), DataBuilder.buildInteger(6l), DataBuilder.buildInteger(7l), DataBuilder.buildInteger(8l), DataBuilder.buildInteger(9l), DataBuilder.buildInteger(10l), DataBuilder.buildInteger(11l), DataBuilder.buildInteger(12l), DataBuilder.buildInteger(13l), DataBuilder.buildInteger(14l), DataBuilder.buildInteger(15l), DataBuilder.buildInteger(16l), DataBuilder.buildInteger(17l), DataBuilder.buildInteger(18l), DataBuilder.buildInteger(19l), DataBuilder.buildInteger(20l), DataBuilder.buildInteger(21l), DataBuilder.buildInteger(22l), DataBuilder.buildInteger(23l));
    builder.inOpenAnswerDefinition(TIME_24).withOpenAnswerDefinition("MINUTE_15", DataType.INTEGER).setDefaultData(DataBuilder.buildInteger(0l), DataBuilder.buildInteger(15l), DataBuilder.buildInteger(30l), DataBuilder.buildInteger(45l));
    builder.inOpenAnswerDefinition(TIME_24).withOpenAnswerDefinition("TODAY_YESTERDAY", DataType.TEXT).setDefaultData(DataBuilder.buildText("TODAY"), DataBuilder.buildText("YESTERDAY"));
    builder.inPage("3").withQuestion("LAST_CAFFEINE_WHEN"/* , "4" */).withSharedCategories(TIME_24, MORE_24_HOURS);

    builder.inSection("S1_PARTICIPANT").withPage("4").withQuestion("LAST_ALCOHOL_WHEN"/* , "5" */).withSharedCategories(TIME_24, MORE_24_HOURS);
    builder.inPage("4").withQuestion("LAST_TOBACCO_WHEN"/* , "6" */).withSharedCategories(TIME_24, MORE_24_HOURS);

    // section S2_BP_HR
    builder.withSection("S2_BP_HR").withSection("S2_EC_OBS").withPage("6").withQuestion("BP_OBSERVED_CI"/* , "10" */).withCategories(NC, "RASHES_BOTH_ARMS", "CAST_BOTH_ARMS", "PARALYSIS_AMPUTATION_WITHERED");
    builder.inQuestion("BP_OBSERVED_CI").withSharedCategory(OTHER).withOpenAnswerDefinition(OTHER, DataType.TEXT).setSize(60);

    builder.inSection("S2_BP_HR").withSection("S2_EC_IC_ASKED").withPage("7").withQuestion("S2_BP1").setAnswerCondition("S2_BP1_ACONDITION", "BP_OBSERVED_CI", NC);
    builder.inPage("7").withQuestion("LEFT_ARM_SURGERY_SHUNT"/* , "13" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("LEFT_ARM_SURGERY_SHUNT").setAnswerCondition("LEFT_ARM_SURGERY_SHUNT_ACONDITION", "BP_OBSERVED_CI", NC);
    builder.inPage("7").withQuestion("RIGHT_ARM_SURGERY_SHUNT"/* , "14" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("RIGHT_ARM_SURGERY_SHUNT").setAnswerCondition("RIGHT_ARM_SURGERY_SHUNT_ACONDITION", "BP_OBSERVED_CI", NC);

    builder.inSection("S2_BP_HR").withSection("S2_EC_IC_OBS").withPage("8").withQuestion("S2_BP3").setAnswerCondition("S2_BP3_ACONDITION", "BP_OBSERVED_CI", NC);
    builder.inPage("8").withQuestion("BLOOD_PRESSURE_MEASURE_SAFE"/* , "14b" */).withSharedCategories(N, Y);
    builder.inQuestion("BLOOD_PRESSURE_MEASURE_SAFE").setAnswerCondition("BLOOD_PRESSURE_MEASURE_SAFE_ACONDITION", "BP_OBSERVED_CI", NC);
    builder.inPage("8").withQuestion("BP_ARM_CHOSEN"/* , "17" */).withCategories("RIGHT_ARM", "LEFT_ARM");
    builder.inQuestion("BP_ARM_CHOSEN").setAnswerCondition("BP_ARM_CHOSEN_ACONDITION", "BLOOD_PRESSURE_MEASURE_SAFE", Y);

    // section S3_BONE_DENSITY
    builder.withSection("S3_BONE_DENSITY").withSection("S3_EC_IC_ASKED").withPage("11").withQuestion("S3_BP1");
    builder.inPage("11").withQuestion("ISOPROPYL_ALCOHOL_ALLERGY"/* , "21" */).withSharedCategories(N, Y, DNK);

    builder.inSection("S3_EC_IC_ASKED").withPage("19").withQuestion("LEFT_LOWER_EXTREMITY_EVENT"/* , "23" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("LEFT_LOWER_EXTREMITY_EVENT").setAnswerCondition("LEFT_LOWER_EXTREMITY_EVENT_ACONDITION", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inPage("19").withQuestion("LEFT_LOWER_EXTREMITY_EVENT_WHEN"/* , "23a" */).withSharedCategory(YEAR_MONTH).withOpenAnswerDefinition(YEAR_MONTH, DataType.INTEGER).withOpenAnswerDefinition("YEAR", DataType.INTEGER).addCurrentYearValidator(ComparisonOperator.le).setSize(4);
    builder.inOpenAnswerDefinition(YEAR_MONTH).withOpenAnswerDefinition("MONTH", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 12)).setSize(2);
    builder.inQuestion("LEFT_LOWER_EXTREMITY_EVENT_WHEN").withSharedCategory(AGE).withOpenAnswerDefinition(AGE, DataType.INTEGER).setSize(2);
    builder.inQuestion("LEFT_LOWER_EXTREMITY_EVENT_WHEN").withSharedCategory(DNK);
    builder.inQuestion("LEFT_LOWER_EXTREMITY_EVENT_WHEN").setAnswerCondition("LEFT_LOWER_EXTREMITY_EVENT_WHEN_ACONDITION", "LEFT_LOWER_EXTREMITY_EVENT", Y);

    builder.inPage("19").withQuestion("RIGHT_LOWER_EXTREMITY_EVENT"/* , "24" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("RIGHT_LOWER_EXTREMITY_EVENT").setAnswerCondition("RIGHT_LOWER_EXTREMITY_EVENT_ACONDITION", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inPage("19").withQuestion("RIGHT_LOWER_EXTREMITY_EVENT_WHEN"/* , "24a" */).withSharedCategories(YEAR_MONTH, AGE, DNK);
    builder.inQuestion("RIGHT_LOWER_EXTREMITY_EVENT_WHEN").setAnswerCondition("RIGHT_LOWER_EXTREMITY_EVENT_WHEN_ACONDITION", "RIGHT_LOWER_EXTREMITY_EVENT", Y);

    builder.inSection("S3_EC_IC_ASKED").withPage("20").withQuestion("BD_HANDEDNESS"/* , "29" */).withCategories("LEFT_HANDED", "RIGHT_HANDED", "AMBIDEXTROUS");
    builder.inQuestion("BD_HANDEDNESS").setAnswerCondition("BD_HANDEDNESS_ACONDITION", "ISOPROPYL_ALCOHOL_ALLERGY", N);

    builder.inPage("20").withQuestion("BD_LEFT_POSSIBLE");
    /*
     * ( ((Q29.RIGHT) OR (Q29.AMBI)) AND ( (Q23.NO) OR ((Q23.YES) AND (Q24.YES)))) OR ( (Q29.LEFT) AND (Q24.YES) AND
     * (Q23.NO))
     */
    builder.inQuestion("BD_LEFT_POSSIBLE").setMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_0", ConditionOperator.OR);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_0").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_1", ConditionOperator.AND);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_1_1", ConditionOperator.OR);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_0", "BD_HANDEDNESS", "RIGHT_HANDED");
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_1", "BD_HANDEDNESS", "AMBIDEXTROUS");
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2", ConditionOperator.OR);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_2", "LEFT_LOWER_EXTREMITY_EVENT", N);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_1", ConditionOperator.AND);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_3", "LEFT_LOWER_EXTREMITY_EVENT", Y);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_1_2_1").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_4", "RIGHT_LOWER_EXTREMITY_EVENT", Y);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_0").withMultipleCondition("BD_LEFT_POSSIBLE_MCONDITION_2", ConditionOperator.AND);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_2").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_5", "BD_HANDEDNESS", "LEFT_HANDED");
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_2").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_6", "RIGHT_LOWER_EXTREMITY_EVENT", Y);
    builder.inCondition("BD_LEFT_POSSIBLE_MCONDITION_2").withAnswerCondition("BD_LEFT_POSSIBLE_ACONDITION_7", "LEFT_LOWER_EXTREMITY_EVENT", N);

    builder.inPage("20").withQuestion("BD_RIGHT_POSSIBLE");
    /*
     * ((Q29.LEFT) AND ((Q24.NO) OR ((Q23.YES) AND (Q24.YES)))) OR (((Q29.RIGHT) OR (Q29.AMBIDEXTRE)) AND (Q23.YES) AND
     * (Q24.NO))
     */
    builder.inQuestion("BD_RIGHT_POSSIBLE").setMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_0", ConditionOperator.OR);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_0").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_1", ConditionOperator.AND);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_0", "BD_HANDEDNESS", "LEFT_HANDED");
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1", ConditionOperator.OR);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_1", "RIGHT_LOWER_EXTREMITY_EVENT", N);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_1", ConditionOperator.AND);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_2", "LEFT_LOWER_EXTREMITY_EVENT", Y);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_1_1_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_3", "RIGHT_LOWER_EXTREMITY_EVENT", Y);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_0").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_2", ConditionOperator.AND);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2").withMultipleCondition("BD_RIGHT_POSSIBLE_MCONDITION_2_1", ConditionOperator.OR);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_4", "BD_HANDEDNESS", "RIGHT_HANDED");
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2_1").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_5", "BD_HANDEDNESS", "AMBIDEXTROUS");
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_6", "LEFT_LOWER_EXTREMITY_EVENT", Y);
    builder.inCondition("BD_RIGHT_POSSIBLE_MCONDITION_2").withAnswerCondition("BD_RIGHT_POSSIBLE_ACONDITION_7", "RIGHT_LOWER_EXTREMITY_EVENT", N);

    builder.inSection("S3_BONE_DENSITY").withSection("S3_IC_OBS").withPage("21").withQuestion("S3_BP3").setAnswerCondition("S3_BP3_ACONDITION", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inPage("21").withQuestion("BD_FOOT_CHOSEN"/* , "30" */).withCategories("RIGHT_FOOT", "LEFT_FOOT");
    builder.inQuestion("BD_FOOT_CHOSEN").setAnswerCondition("BD_FOOT_CHOSEN_ACONDITION", "ISOPROPYL_ALCOHOL_ALLERGY", N);

    // section S4_GRIP_STRENGTH
    builder.withSection("S4_GRIP_STRENGTH").withSection("S4_EC_OBS").withPage("22").withQuestion("GS_OBS_CI"/* , "31" */).withSharedCategory(NC);
    builder.inQuestion("GS_OBS_CI").withCategories("PARALYSIS_AMPUTATION_WITHERED");
    builder.inQuestion("GS_OBS_CI").withSharedCategory(OTHER);
    builder.inQuestion("GS_OBS_CI").setAnswerCondition("GS_OBS_CI_ACONDITION", "ABLE_TO_STAND", Y);

    builder.inSection("S4_GRIP_STRENGTH").withSection("S4_EC_ASKED").withPage("23").withQuestion("S4_BP1").setAnswerCondition("S4_BP1_ACONDIITON", "GS_OBS_CI", NC);
    builder.inPage("23").withQuestion("GS_SURGERY_BOTH_HANDS_13_WEEKS"/* , "33" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("GS_SURGERY_BOTH_HANDS_13_WEEKS").setAnswerCondition("GS_SURGERY_BOTH_HANDS_13_WEEKS_ACONDITION", "GS_OBS_CI", NC);
    builder.inPage("23").withQuestion("GS_PAIN_BOTH_HANDS"/* , "34" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("GS_PAIN_BOTH_HANDS").setAnswerCondition("GS_PAIN_BOTH_HANDS_ACONDITION", "GS_SURGERY_BOTH_HANDS_13_WEEKS", N);

    // section S5_STANDING_HEIGHT
    builder.withSection("S5_STANDING_HEIGHT").withSection("S5_EC_OBS").withPage("29").withQuestion("SH_OBS_CI"/* , "35" */).withSharedCategory(NC);
    builder.inQuestion("SH_OBS_CI").withCategory("SEVERE_SPINE_CONDITION");
    builder.inQuestion("SH_OBS_CI").withSharedCategory(OTHER);
    builder.inQuestion("SH_OBS_CI").setAnswerCondition("SH_OBS_CI_ACONDITION", "ABLE_TO_STAND", Y);

    // section S7_WAIST_HIPS
    builder.withSection("S7_WAIST_HIPS").withSection("S7_EC_OBS").withPage("33").withQuestion("WH_OBS_CI"/* , "43" */).withSharedCategory(NC);
    builder.inQuestion("WH_OBS_CI").withSharedCategory(YES_SPECIFY).withOpenAnswerDefinition(YES_SPECIFY, DataType.TEXT).setSize(60);
    builder.inQuestion("WH_OBS_CI").setMultipleCondition("WH_OBS_CI_MCONDITION_0", ConditionOperator.AND).withAnswerCondition("WH_OBS_CI_ACONDITION_0", "ABLE_TO_STAND", Y);
    builder.inCondition("WH_OBS_CI_MCONDITION_0").withMultipleCondition("WH_OBS_CI_MCONDITION_1", ConditionOperator.OR).withNoAnswerCondition("WH_OBS_CI_NCONDITION").withAnswerCondition("WH_OBS_CI_ACONDITION_2", "CURRENTLY_PREGNANT");
    builder.inCondition("WH_OBS_CI_MCONDITION_1").withAnswerCondition("WH_OBS_CI_ACONDITION_1", "CURRENTLY_PREGNANT", N);
    builder.inCondition("WH_OBS_CI_MCONDITION_1").withDataCondition("WH_OBS_CI_DCONDITION", "CURRENT_PREGNANCY_WEEKS", OPEN_N40, OPEN_N40, ComparisonOperator.le, DataBuilder.buildInteger(12));

    // section S8_SPIROMETRY
    builder.withSection("S8_SPIROMETRY").withSection("S8_EC_OBS").withPage("34").withQuestion("SP_OBS_CI"/* , "45" */).withSharedCategories(NC, YES_SPECIFY);
    builder.inQuestion("SP_OBS_CI").setMultipleCondition("SP_OBS_CI_MCONDITION", ConditionOperator.OR).withNoAnswerCondition("SP_OBS_CI_NCONDITION").withAnswerCondition("SP_OBS_CI_ACONDITION_0", "CURRENTLY_PREGNANT");
    builder.inCondition("SP_OBS_CI_MCONDITION").withAnswerCondition("SP_OBS_CI_ACONDITION_1", "CURRENTLY_PREGNANT", N);

    builder.inSection("S8_SPIROMETRY").withSection("S8_EC_ASKED").withPage("35").withQuestion("S8_BP1").setAnswerCondition("S8_BP1_ACONDITION", "SP_OBS_CI", NC);
    builder.inPage("35").withQuestion("SP_CI_CURRENT"/* , "47" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("SP_CI_CURRENT").setAnswerCondition("SP_CI_CURRENT_ACONDITION", "SP_OBS_CI", NC);
    builder.inQuestion("SP_CI_CURRENT").withQuestion("SP_MED_TB"/* , "1" */);
    builder.inQuestion("SP_CI_CURRENT").withQuestion("SP_STOMA"/* , "2" */);
    builder.inQuestion("SP_CI_CURRENT").withQuestion("SP_SEVERE_RESP_COND"/* , "3" */);
    builder.inQuestion("SP_CI_CURRENT").withQuestion("SP_UNSTABLE_ANGINA"/* , "4" */);

    builder.inSection("S8_EC_ASKED").withPage("37").withQuestion("SP_CI_3_MONTHS"/* , "49" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("SP_CI_3_MONTHS").setMultipleCondition("SP_CI_3_MONTHS_MCONDITION", ConditionOperator.AND).withAnswerCondition("SP_CI_3_MONTHS_ACONDITION_0", "SP_MED_TB", N);
    builder.inCondition("SP_CI_3_MONTHS_MCONDITION").withAnswerCondition("SP_CI_3_MONTHS_ACONDITION_1", "SP_STOMA", N);
    builder.inCondition("SP_CI_3_MONTHS_MCONDITION").withAnswerCondition("SP_CI_3_MONTHS_ACONDITION_2", "SP_SEVERE_RESP_COND", N);
    builder.inCondition("SP_CI_3_MONTHS_MCONDITION").withAnswerCondition("SP_CI_3_MONTHS_ACONDITION_3", "SP_UNSTABLE_ANGINA", N);
    builder.inQuestion("SP_CI_3_MONTHS").withQuestion("SP_ABD_THORACIC_SURGERY"/* , "1" */);
    builder.inQuestion("SP_CI_3_MONTHS").withQuestion("SP_EYE_SURGERY"/* , "2" */);
    builder.inQuestion("SP_CI_3_MONTHS").withQuestion("SP_HEART_ATTACK"/* , "3" */);

    builder.inSection("S8_EC_ASKED").withPage("38").withQuestion("SP_CI_EVER_HAD"/* , "50" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("SP_CI_EVER_HAD").setMultipleCondition("SP_CI_EVER_HAD_MCONDITION", ConditionOperator.AND).withAnswerCondition("SP_CI_EVER_HAD_ACONDITION_0", "SP_ABD_THORACIC_SURGERY", N);
    builder.inCondition("SP_CI_EVER_HAD_MCONDITION").withAnswerCondition("SP_CI_EVER_HAD_ACONDITION_1", "SP_EYE_SURGERY", N);
    builder.inCondition("SP_CI_EVER_HAD_MCONDITION").withAnswerCondition("SP_CI_EVER_HAD_ACONDITION_2", "SP_HEART_ATTACK", N);
    builder.inQuestion("SP_CI_EVER_HAD").withQuestion("SP_COLLAPSED_LUNG"/* , "1" */);
    builder.inQuestion("SP_CI_EVER_HAD").withQuestion("SP_DETACHED_RETINA"/* , "2" */);

    builder.inSection("S8_EC_ASKED").withPage("39").withQuestion("SP_BRONCHODILATATOR_USE_24_HOURS"/* , "51" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("SP_BRONCHODILATATOR_USE_24_HOURS").setMultipleCondition("SP_BRONCHODILATATOR_USE_24_HOURS_MCONDITION", ConditionOperator.AND).withAnswerCondition("SP_BRONCHODILATATOR_USE_24_HOURS_ACONDITION_0", "SP_COLLAPSED_LUNG", N);
    builder.inCondition("SP_BRONCHODILATATOR_USE_24_HOURS_MCONDITION").withAnswerCondition("SP_BRONCHODILATATOR_USE_24_HOURS_ACONDITION_1", "SP_DETACHED_RETINA", N);
    builder.inPage("39").withQuestion("SP_BRONCHODILATATOR_USE_WHEN"/* , "52" */).withSharedCategory(TIME_24);
    builder.inQuestion("SP_BRONCHODILATATOR_USE_WHEN").setAnswerCondition("SP_BRONCHODILATATOR_USE_WHEN_ACONDITION", "SP_BRONCHODILATATOR_USE_24_HOURS", Y);

    // section S9_BIOIMPEDANCE_WEIGHT
    builder.withSection("S9_BIOIMPEDANCE_WEIGHT").withSection("S9_EC_OBS").withPage("40").withQuestion("BW_OBS_CI_BIOIMPEDANCE"/* ,"54" */).withSharedCategory(NC);
    builder.inQuestion("BW_OBS_CI_BIOIMPEDANCE").withCategories("CAST", "LIMB_AMPUTATION");
    builder.inQuestion("BW_OBS_CI_BIOIMPEDANCE").withSharedCategory(OTHER);
    builder.inQuestion("BW_OBS_CI_BIOIMPEDANCE").setMultipleCondition("BW_OBS_CI_BIOIMPEDANCE_MCONDITION_0", ConditionOperator.AND).withMultipleCondition("BW_OBS_CI_BIOIMPEDANCE_MCONDITION_1", ConditionOperator.OR).withNoAnswerCondition("BW_OBS_CI_BIOIMPEDANCE_NCONDITION").withAnswerCondition("BW_OBS_CI_BIOIMPEDANCE_ACONDITION_0", "CURRENTLY_PREGNANT");
    builder.inCondition("BW_OBS_CI_BIOIMPEDANCE_MCONDITION_1").withAnswerCondition("BW_OBS_CI_BIOIMPEDANCE_ACONDITION_1", "CURRENTLY_PREGNANT", N);
    builder.inCondition("BW_OBS_CI_BIOIMPEDANCE_MCONDITION_0").withAnswerCondition("BW_OBS_CI_BIOIMPEDANCE_ACONDITION_2", "ABLE_TO_STAND", Y);

    builder.inSection("S9_EC_OBS").withPage("41").withQuestion("BW_OBS_CI_WEIGHT"/* , "57" */).withSharedCategories(NC, YES_SPECIFY);
    builder.inQuestion("BW_OBS_CI_WEIGHT").setMultipleCondition("BW_OBS_CI_WEIGHT_MCONDITION_0", ConditionOperator.OR).withAnswerCondition("BW_OBS_CI_WEIGHT_ACONDITION_0", "BW_OBS_CI_BIOIMPEDANCE", "CAST");
    builder.inCondition("BW_OBS_CI_WEIGHT_MCONDITION_0").withAnswerCondition("BW_OBS_CI_WEIGHT_ACONDITION_1", "BW_OBS_CI_BIOIMPEDANCE", "LIMB_AMPUTATION");
    builder.inCondition("BW_OBS_CI_WEIGHT_MCONDITION_0").withAnswerCondition("BW_OBS_CI_WEIGHT_ACONDITION_2", "BW_OBS_CI_BIOIMPEDANCE", OTHER);
    builder.inCondition("BW_OBS_CI_WEIGHT_MCONDITION_0").withMultipleCondition("BW_OBS_CI_WEIGHT_MCONDITION_1", ConditionOperator.AND).withAnswerCondition("BW_OBS_CI_WEIGHT_ACONDITION_3", "CURRENTLY_PREGNANT", Y);
    builder.inCondition("BW_OBS_CI_WEIGHT_MCONDITION_1").withDataCondition("BW_OBS_CI_WEIGHT_DCONDITION", "CURRENT_PREGNANCY_WEEKS", OPEN_N40, OPEN_N40, ComparisonOperator.le, DataBuilder.buildInteger(12));

    builder.inSection("S9_BIOIMPEDANCE_WEIGHT").withSection("S9_EC_ASKED").withPage("42").withQuestion("S9_BP1").setAnswerCondition("S9_BP1_ACONDITION", "BW_OBS_CI_BIOIMPEDANCE", NC);
    builder.inPage("42").withQuestion("BW_PACEMAKER_CI"/* , "60" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("BW_PACEMAKER_CI").setAnswerCondition("BW_PACEMAKER_CI_ACONDITION", "BW_OBS_CI_BIOIMPEDANCE", NC);

    // section S10_ARTERIAL_STIFFNESS
    builder.withSection("S10_ARTERIAL_STIFFNESS").withSection("S10_EC_ASKED").withPage("43").withQuestion("S10_BP1");
    builder.inPage("43").withQuestion("AS_STENOSIS_CI"/* , "60a" */).withSharedCategories(N, Y, DNK);
    builder.inPage("43").withQuestion("AS_STENOSIS_GRADIENT"/* , "60b" */).withCategory("GRADIENT").withOpenAnswerDefinition("GRADIENT", DataType.INTEGER).setUnit("PERCENT").setSize(2);
    builder.inQuestion("AS_STENOSIS_GRADIENT").withSharedCategory(DNK);
    builder.inQuestion("AS_STENOSIS_GRADIENT").setAnswerCondition("AS_STENOSIS_GRADIENT_ACONDITION", "AS_STENOSIS_CI", Y);

    // section S11_SAMPLES
    builder.withSection("S11_SAMPLES").withSection("S11_EC_OBS").withPage("44").withQuestion("SC_OBS_CI"/* , "61" */).setRowCount(6);
    builder.inQuestion("SC_OBS_CI").withSharedCategory(NC);
    builder.inQuestion("SC_OBS_CI").withCategories("RASHES_BOTH_ARMS", "CAST_BOTH_ARMS", "PARALYSIS_AMPUTATION_WITHERED", "OCCLUDED_VEINS_BOTH_ARMS");
    builder.inQuestion("SC_OBS_CI").withSharedCategory(OTHER);
    builder.inQuestion("SC_OBS_CI").setMultipleCondition("SC_OBS_CI_MCONDITION", ConditionOperator.OR).withAnswerCondition("SC_OBS_CI_ACONDITION_0", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inCondition("SC_OBS_CI_MCONDITION").withNoAnswerCondition("SC_OBS_CI_NCONDITION").withAnswerCondition("SC_OBS_CI_ACONDITION_1", "ISOPROPYL_ALCOHOL_ALLERGY");

    builder.inSection("S11_EC_OBS").withPage("45").withQuestion("S11_BP1").setAnswerCondition("S11_BP1_ACONDITION", "SC_OBS_CI", NC);
    builder.inPage("45").withQuestion("SC_HEMOPHILIA_CI"/* , "66" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("SC_HEMOPHILIA_CI").setAnswerCondition("SC_HEMOPHILIA_CI_ACONDITION", "SC_OBS_CI", NC);
    builder.inPage("45").withQuestion("SC_ISOPROPYL_ALCOHOL_ALLERGY"/* , "67" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("SC_ISOPROPYL_ALCOHOL_ALLERGY").setMultipleCondition("SC_ISOPROPYL_ALCOHOL_ALLERGY_MCONDITION", ConditionOperator.AND).withAnswerCondition("SC_ISOPROPYL_ALCOHOL_ALLERGY_ACONDITION_0", "SC_HEMOPHILIA_CI", N);
    builder.inCondition("SC_ISOPROPYL_ALCOHOL_ALLERGY_MCONDITION").withNoAnswerCondition("SC_ISOPROPYL_ALCOHOL_ALLERGY_NCONDITION").withAnswerCondition("SC_ISOPROPYL_ALCOHOL_ALLERGY_ACONDITION_1", "ISOPROPYL_ALCOHOL_ALLERGY");
    builder.inPage("45").withQuestion("BLOOD_TRANSFUSION_24_HOURS"/* , "68" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("BLOOD_TRANSFUSION_24_HOURS").setMultipleCondition("BLOOD_TRANSFUSION_24_HOURS_MCONDITION_0", ConditionOperator.OR).withAnswerCondition("BLOOD_TRANSFUSION_24_HOURS_ACONDITION_0", "SC_ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inCondition("BLOOD_TRANSFUSION_24_HOURS_MCONDITION_0").withMultipleCondition("BLOOD_TRANSFUSION_24_HOURS_MCONDITION_1", ConditionOperator.AND).withAnswerCondition("BLOOD_TRANSFUSION_24_HOURS_ACONDITION_1", "SC_HEMOPHILIA_CI", N);
    builder.inCondition("BLOOD_TRANSFUSION_24_HOURS_MCONDITION_1").withAnswerCondition("BLOOD_TRANSFUSION_24_HOURS_ACONDITION_2", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inPage("45").withQuestion("SC_LEFT_ARM_SURGERY_SHUNT"/* , "64" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("SC_LEFT_ARM_SURGERY_SHUNT").setMultipleCondition("SC_LEFT_ARM_SURGERY_SHUNT_MCONDITION", ConditionOperator.AND).withAnswerCondition("SC_LEFT_ARM_SURGERY_SHUNT_ACONDITION_0", "BLOOD_TRANSFUSION_24_HOURS", N);
    builder.inCondition("SC_LEFT_ARM_SURGERY_SHUNT_MCONDITION").withNoAnswerCondition("SC_LEFT_ARM_SURGERY_SHUNT_NCONDITION").withAnswerCondition("SC_LEFT_ARM_SURGERY_SHUNT_ACONDITION_1", "LEFT_ARM_SURGERY_SHUNT");
    builder.inPage("45").withQuestion("SC_RIGHT_ARM_SURGERY_SHUNT"/* , "65" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("SC_RIGHT_ARM_SURGERY_SHUNT").setMultipleCondition("SC_RIGHT_ARM_SURGERY_SHUNT_MCONDITION", ConditionOperator.AND).withAnswerCondition("SC_RIGHT_ARM_SURGERY_SHUNT_ACONDITION_0", "BLOOD_TRANSFUSION_24_HOURS", N);
    builder.inCondition("SC_RIGHT_ARM_SURGERY_SHUNT_MCONDITION").withNoAnswerCondition("SC_RIGHT_ARM_SURGERY_SHUNT_NCONDITION").withAnswerCondition("SC_RIGHT_ARM_SURGERY_SHUNT_ACONDITION_1", "RIGHT_ARM_SURGERY_SHUNT");

    builder.inSection("S11_EC_OBS").withPage("46").withQuestion("S11_BP2").setAnswerCondition("S11_BP2_ACONDITION", "BLOOD_TRANSFUSION_24_HOURS", N);
    builder.inPage("46").withQuestion("BLOOD_COLLECTION_SAFE"/* , "69b" */).withSharedCategories(N, Y);
    builder.inQuestion("BLOOD_COLLECTION_SAFE").setAnswerCondition("BLOOD_COLLECTION_SAFE_ACONDITION", "BLOOD_TRANSFUSION_24_HOURS", N);

    builder.inSection("S11_SAMPLES").withSection("S11_IC_ASKED").withPage("47").withQuestion("S11_BP3").setAnswerCondition("S11_BP3_ACONDITION", "BLOOD_COLLECTION_SAFE", Y);
    builder.inPage("47").withQuestion("BLOOD_TRANSFUSION_LAST_2_MONTHS"/* , "69c" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("BLOOD_TRANSFUSION_LAST_2_MONTHS").setAnswerCondition("BLOOD_TRANSFUSION_LAST_2_MONTHS_ACONDITION", "BLOOD_COLLECTION_SAFE", Y);
    builder.inPage("47").withQuestion("CHEMO_LAST_12_WEEKS"/* , "69d" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("CHEMO_LAST_12_WEEKS").setAnswerCondition("CHEMO_LAST_12_WEEKS_ACONDITION", "BLOOD_COLLECTION_SAFE", Y);
    builder.inPage("47").withQuestion("RADIOTHERAPY_LAST_12_WEEKS"/* , "69e" */).withSharedCategories(N, Y, DNK);
    builder.inQuestion("RADIOTHERAPY_LAST_12_WEEKS").setAnswerCondition("RADIOTHERAPY_LAST_12_WEEKS_ACONDITION", "BLOOD_COLLECTION_SAFE", Y);

    // section S12_CONCLUSION

    builder.withSection("S12_CONCLUSION").withPage("51").withQuestion("STAGE_STATUS"/* , "110" */).setMultipleCondition("STAGE_STATUS_MCONDITION", ConditionOperator.OR);
    builder.inCondition("STAGE_STATUS_MCONDITION").withNoAnswerCondition("STAGE_STATUS_NCONDITION_0").withAnswerCondition("STAGE_STATUS_ACONDITION_0", "BLOOD_PRESSURE_MEASURE_SAFE", Y);
    builder.inCondition("STAGE_STATUS_MCONDITION").withNoAnswerCondition("STAGE_STATUS_NCONDITION_1").withAnswerCondition("STAGE_STATUS_ACONDITION_1", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inCondition("STAGE_STATUS_MCONDITION").withNoAnswerCondition("STAGE_STATUS_NCONDITION_2").withAnswerCondition("STAGE_STATUS_ACONDITION_2", "GS_PAIN_BOTH_HANDS", N);
    builder.inCondition("STAGE_STATUS_MCONDITION").withNoAnswerCondition("STAGE_STATUS_NCONDITION_3").withAnswerCondition("STAGE_STATUS_ACONDITION_3", "SH_OBS_CI", NC);
    builder.inCondition("STAGE_STATUS_MCONDITION").withNoAnswerCondition("STAGE_STATUS_NCONDITION_4").withAnswerCondition("STAGE_STATUS_ACONDITION_4", "WH_OBS_CI", NC);
    builder.inCondition("STAGE_STATUS_MCONDITION").withNoAnswerCondition("STAGE_STATUS_NCONDITION_5").withAnswerCondition("STAGE_STATUS_ACONDITION_5", "SP_BRONCHODILATATOR_USE_24_HOURS", N);
    builder.inCondition("STAGE_STATUS_MCONDITION").withNoAnswerCondition("STAGE_STATUS_NCONDITION_6").withAnswerCondition("STAGE_STATUS_ACONDITION_6", "BW_OBS_CI_BIOIMPEDANCE", NC);
    builder.inCondition("STAGE_STATUS_MCONDITION").withAnswerCondition("STAGE_STATUS_ACONDITION_7", "BW_OBS_CI_WEIGHT", YES_SPECIFY);
    builder.inCondition("STAGE_STATUS_MCONDITION").withNoAnswerCondition("STAGE_STATUS_NCONDITION_7").withAnswerCondition("STAGE_STATUS_ACONDITION_8", "AS_STENOSIS_CI", N);
    builder.inCondition("STAGE_STATUS_MCONDITION").withNoAnswerCondition("STAGE_STATUS_NCONDITION_8").withAnswerCondition("STAGE_STATUS_ACONDITION_9", "BLOOD_COLLECTION_SAFE", Y);

    builder.inQuestion("STAGE_STATUS").withQuestion("BP_CI"/* , "110a" */).setNotCondition("BP_CI_NCONDITION").withAnswerCondition("BP_CI_ACONDITION", "BLOOD_PRESSURE_MEASURE_SAFE", Y);
    builder.inQuestion("STAGE_STATUS").withQuestion("BD_CI"/* , "110b" */).setNotCondition("BD_CI_NCONDITION").withAnswerCondition("BD_CI_ACONDITION", "ISOPROPYL_ALCOHOL_ALLERGY", Y);
    builder.inQuestion("STAGE_STATUS").withQuestion("GS_CI"/* , "110c" */).setNotCondition("GS_CI_NCONDITION").withAnswerCondition("GS_CI_ACONDITION", "GS_PAIN_BOTH_HANDS", N);
    builder.inQuestion("STAGE_STATUS").withQuestion("SH_CI"/* , "110d" */).setNotCondition("SH_CI_NCONDITION").withAnswerCondition("SH_CI_ACONDITION", "SH_OBS_CI", NC);
    builder.inQuestion("STAGE_STATUS").withQuestion("WH_CI"/* , "110f" */).setNotCondition("WH_CI_NCONDITION").withAnswerCondition("WH_CI_ACONDITION", "WH_OBS_CI", NC);
    builder.inQuestion("STAGE_STATUS").withQuestion("SP_CI"/* , "110g" */).setNotCondition("SP_CI_NCONDITION").withAnswerCondition("SP_CI_ACONDITION", "SP_BRONCHODILATATOR_USE_24_HOURS", N);
    builder.inQuestion("STAGE_STATUS").withQuestion("BI_CI"/* , "110h" */).setNotCondition("BI_CI_NCONDITION").withAnswerCondition("BI_CI_ACONDITION", "BW_OBS_CI_BIOIMPEDANCE", NC);
    builder.inQuestion("STAGE_STATUS").withQuestion("WT_CI"/* , "110i" */).setAnswerCondition("WT_CI_ACONDITION", "BW_OBS_CI_WEIGHT", YES_SPECIFY);
    builder.inQuestion("STAGE_STATUS").withQuestion("AS_CI"/* , "110j" */).setNotCondition("AS_CI_NCONDITION").withAnswerCondition("AS_CI_ACONDITION", "AS_STENOSIS_CI", N);
    builder.inQuestion("STAGE_STATUS").withQuestion("BSC_CI"/* , "110k" */).setNotCondition("BSC_CI_NCONDITION").withAnswerCondition("BSC_CI_ACONDITION", "BLOOD_COLLECTION_SAFE", Y);

    builder.inPage("51").withQuestion("NO_CI"/* , "111" */).setMultipleCondition("NO_CI_MCONDITION", ConditionOperator.AND);
    builder.inCondition("NO_CI_MCONDITION").withAnswerCondition("NO_CI_ACONDITION_0", "BLOOD_PRESSURE_MEASURE_SAFE", Y);
    builder.inCondition("NO_CI_MCONDITION").withAnswerCondition("NO_CI_ACONDITION_1", "ISOPROPYL_ALCOHOL_ALLERGY", N);
    builder.inCondition("NO_CI_MCONDITION").withAnswerCondition("NO_CI_ACONDITION_2", "GS_PAIN_BOTH_HANDS", N);
    builder.inCondition("NO_CI_MCONDITION").withAnswerCondition("NO_CI_ACONDITION_3", "SH_OBS_CI", NC);
    builder.inCondition("NO_CI_MCONDITION").withAnswerCondition("NO_CI_ACONDITION_4", "WH_OBS_CI", NC);
    builder.inCondition("NO_CI_MCONDITION").withAnswerCondition("NO_CI_ACONDITION_5", "SP_BRONCHODILATATOR_USE_24_HOURS", N);
    builder.inCondition("NO_CI_MCONDITION").withAnswerCondition("NO_CI_ACONDITION_6", "BW_OBS_CI_BIOIMPEDANCE", NC);
    builder.inCondition("NO_CI_MCONDITION").withAnswerCondition("NO_CI_ACONDITION_7", "AS_STENOSIS_CI", N);
    builder.inCondition("NO_CI_MCONDITION").withAnswerCondition("NO_CI_ACONDITION_8", "BLOOD_COLLECTION_SAFE", Y);

    builder.inPage("51").withQuestion("WT_NA"/* , "112" */).setAnswerCondition("WT_NA_ACONDITION", "BW_OBS_CI_BIOIMPEDANCE", NC);
    builder.inPage("51").withQuestion("BP_ARM_CHOSEN_LEFT"/* , "104" */).setAnswerCondition("BP_ARM_CHOSEN_LEFT_ACONDITION", "BP_ARM_CHOSEN", "LEFT_ARM");
    builder.inPage("51").withQuestion("BP_ARM_CHOSEN_RIGHT"/* , "105" */).setAnswerCondition("BP_ARM_CHOSEN_RIGHT_ACONDITION", "BP_ARM_CHOSEN", "RIGHT_ARM");
    builder.inPage("51").withQuestion("BD_FOOT_CHOSEN_LEFT"/* , "108" */).setAnswerCondition("BD_FOOT_CHOSEN_LEFT_ACONDITION", "BD_FOOT_CHOSEN", "LEFT_FOOT");
    builder.inPage("51").withQuestion("BD_FOOT_CHOSEN_RIGHT"/* , "109" */).setAnswerCondition("BD_FOOT_CHOSEN_RIGHT_ACONDITION", "BD_FOOT_CHOSEN", "RIGHT_FOOT");

    return builder;
  }
}
