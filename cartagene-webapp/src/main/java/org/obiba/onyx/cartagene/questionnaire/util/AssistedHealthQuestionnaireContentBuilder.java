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
import org.obiba.onyx.quartz.core.wicket.layout.impl.DropDownQuestionPanelFactory;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Returns the content for the Self Administered Health Questionnaire
 */
public class AssistedHealthQuestionnaireContentBuilder {

  private static final String N = "N";

  private static final String Y = "Y";

  private static final String OTHER = "OTHER";

  private static final String PNA = "PNA";

  private static final String DNK = "DNK";

  private static final String TYPE = "TYPE";

  private static final String PACKAGE = "PACKAGE";

  private static final String LIST = "LIST";

  private static final String PARTICIPANT = "PARTICIPANT";

  private static final String OPEN_N = "OPEN_N";

  private static final String OPEN_AN = "OPEN_AN";

  private static final String OTHER_AGE = "OTHER_AGE";

  private static final String BIRTH_RANK = "BIRTH_RANK";

  public static QuestionnaireBuilder buildHealthQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaireAssisted", "1.0");

    builder.withSection("C_LIFE_HABITS").withSection("JOB_RELATED_PA").withSection("PART1").withPage("1").withQuestion("CURRENTLY_WORK").withSharedCategory(N, "0");
    builder.inQuestion("CURRENTLY_WORK").withSharedCategories(Y, "1");
    builder.inQuestion("CURRENTLY_WORK").withSharedCategories(PNA, "8");
    builder.inQuestion("CURRENTLY_WORK").withSharedCategories(DNK, "9");
    builder.inSection("PART1").withPage("2").withQuestion("WORK_VIGOROUS_ACTIVITY_DAYS_WEEK").withCategory("DAYS_WEEK").withOpenAnswerDefinition("DAYS_WEEK", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 7));
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_DAYS_WEEK").withSharedCategory(PNA, "8");
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_DAYS_WEEK").withSharedCategory(DNK, "9");
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_DAYS_WEEK").setAnswerCondition("WORK_VIGOROUS_ACTIVITY_DAYS_WEEK_ACONDITION", "CURRENTLY_WORK", Y);
    builder.inPage("2").withQuestion("WORK_VIGOROUS_ACTIVITY_TIME_DAY").withCategory("ACTIVITY_TIME_DAY").withOpenAnswerDefinition("ACTIVITY_TIME_DAY", DataType.INTEGER).withOpenAnswerDefinition("ACTIVITY_HOURS_DAY", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 16));
    builder.inOpenAnswerDefinition("ACTIVITY_TIME_DAY").withOpenAnswerDefinition("ACTIVITY_MIN_DAY", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 960));
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_TIME_DAY").withSharedCategory(PNA, "888");
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_TIME_DAY").withSharedCategory(DNK, "999");
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_TIME_DAY").setDataCondition("WORK_VIGOROUS_ACTIVITY_TIME_DAY_DCONDITION", "WORK_VIGOROUS_ACTIVITY_DAYS_WEEK", "DAYS_WEEK", "DAYS_WEEK", ComparisionOperator.gt, DataBuilder.buildInteger(0l));
    builder.inSection("PART1").withPage("3").withQuestion("WORK_VIGOROUS_ACTIVITY_TIME_WEEK").withCategory("ACTIVITY_TIME_WEEK").withOpenAnswerDefinition("ACTIVITY_TIME_WEEK", DataType.INTEGER).withOpenAnswerDefinition("ACTIVITY_HOURS_WEEK", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 112));
    builder.inOpenAnswerDefinition("ACTIVITY_TIME_WEEK").withOpenAnswerDefinition("ACTIVITY_MIN_WEEK", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 6720));
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_TIME_WEEK").withSharedCategory(PNA, "8888");
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_TIME_WEEK").withSharedCategory(DNK, "9999");
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_TIME_WEEK").setAnswerCondition("WORK_VIGOROUS_ACTIVITY_TIME_WEEK_ACONDITION", "WORK_VIGOROUS_ACTIVITY_TIME_DAY", DNK);

    builder.withSection("F_IND_HISTORY_DISEASES").withSection("INDIVIDUAL_HISTORY_INJURIES").withPage("4").withQuestion("BONE_FRACTURE_OCCURRENCE").withSharedCategory(N, "0");
    builder.inQuestion("BONE_FRACTURE_OCCURRENCE").withSharedCategory(Y, "1");
    builder.inQuestion("BONE_FRACTURE_OCCURRENCE").withSharedCategory(PNA, "8");
    builder.inQuestion("BONE_FRACTURE_OCCURRENCE").withSharedCategory(DNK, "9");
    builder.inSection("INDIVIDUAL_HISTORY_INJURIES").withPage("5").withQuestion("BONE_FRACTURE_WHICH").withSharedCategory(N, "0");
    builder.inQuestion("BONE_FRACTURE_WHICH").withSharedCategory(Y, "1");
    builder.inQuestion("BONE_FRACTURE_WHICH").withSharedCategory(PNA, "8");
    builder.inQuestion("BONE_FRACTURE_WHICH").withSharedCategory(DNK, "9");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("VERTEBRA_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("STERNUM_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("SACRUM_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("WRIST_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("FOREARM_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("HUMERUS_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("SCAPULA_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("CLAVICLE_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("RIB_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("FOOT_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("ANKLE_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("TIBIA_FIBULA_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("FEMUR_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("HIP_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("PELVIS_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("OTHER_BONE_FRACTURE_OCCURRENCE");
    builder.inQuestion("BONE_FRACTURE_WHICH").setAnswerCondition("BONE_FRACTURE_WHICH_ACONDITION", "BONE_FRACTURE_OCCURRENCE", Y);
    builder.inSection("INDIVIDUAL_HISTORY_INJURIES").withPage("6").withQuestion("OTHER_BONE_FRACTURE_TYPE").withSharedCategory(OPEN_AN).withOpenAnswerDefinition(OPEN_AN, DataType.TEXT);
    builder.inQuestion("OTHER_BONE_FRACTURE_TYPE").withSharedCategory(PNA, "88");
    builder.inQuestion("OTHER_BONE_FRACTURE_TYPE").withSharedCategory(DNK, "99");
    builder.inQuestion("OTHER_BONE_FRACTURE_TYPE").setAnswerCondition("OTHER_BONE_FRACTURE_TYPE_ACONDITION", "OTHER_BONE_FRACTURE_OCCURRENCE", Y);
    builder.inSection("INDIVIDUAL_HISTORY_INJURIES").withPage("7").withQuestion("BONE_FRACTURE_SIDE").withCategory("LEFT").setExportName("1");
    builder.inQuestion("BONE_FRACTURE_SIDE").withCategory("RIGHT").setExportName("2");
    builder.inQuestion("BONE_FRACTURE_SIDE").withCategory("BOTH").setExportName("3");
    builder.inQuestion("BONE_FRACTURE_SIDE").withSharedCategory(PNA, "8");
    builder.inQuestion("BONE_FRACTURE_SIDE").withSharedCategory(DNK, "9");
    builder.inQuestion("BONE_FRACTURE_SIDE").setMultipleCondition("BONE_FRACTURE_SIDE_MCONDITION", ConditionOperator.OR).withAnswerCondition("BONE_FRACTURE_SIDE_ACONDITION_0", "WRIST_FRACTURE_OCCURRENCE", Y);
    builder.inCondition("BONE_FRACTURE_SIDE_MCONDITION").withAnswerCondition("BONE_FRACTURE_SIDE_ACONDITION_1", "FOREARM_FRACTURE_OCCURRENCE", Y);
    builder.inCondition("BONE_FRACTURE_SIDE_MCONDITION").withAnswerCondition("BONE_FRACTURE_SIDE_ACONDITION_2", "HUMERUS_FRACTURE_OCCURRENCE", Y);
    builder.inCondition("BONE_FRACTURE_SIDE_MCONDITION").withAnswerCondition("BONE_FRACTURE_SIDE_ACONDITION_3", "SCAPULA_FRACTURE_OCCURRENCE", Y);
    builder.inCondition("BONE_FRACTURE_SIDE_MCONDITION").withAnswerCondition("BONE_FRACTURE_SIDE_ACONDITION_4", "CLAVICLE_FRACTURE_OCCURRENCE", Y);
    builder.inCondition("BONE_FRACTURE_SIDE_MCONDITION").withAnswerCondition("BONE_FRACTURE_SIDE_ACONDITION_5", "RIB_FRACTURE_OCCURRENCE", Y);
    builder.inCondition("BONE_FRACTURE_SIDE_MCONDITION").withAnswerCondition("BONE_FRACTURE_SIDE_ACONDITION_6", "FOOT_FRACTURE_OCCURRENCE", Y);
    builder.inCondition("BONE_FRACTURE_SIDE_MCONDITION").withAnswerCondition("BONE_FRACTURE_SIDE_ACONDITION_7", "ANKLE_FRACTURE_OCCURRENCE", Y);
    builder.inCondition("BONE_FRACTURE_SIDE_MCONDITION").withAnswerCondition("BONE_FRACTURE_SIDE_ACONDITION_8", "TIBIA_FIBULA_FRACTURE_OCCURRENCE", Y);
    builder.inCondition("BONE_FRACTURE_SIDE_MCONDITION").withAnswerCondition("BONE_FRACTURE_SIDE_ACONDITION_9", "FEMUR_FRACTURE_OCCURRENCE", Y);
    builder.inCondition("BONE_FRACTURE_SIDE_MCONDITION").withAnswerCondition("BONE_FRACTURE_SIDE_ACONDITION_10", "HIP_FRACTURE_OCCURRENCE", Y);
    builder.inCondition("BONE_FRACTURE_SIDE_MCONDITION").withAnswerCondition("BONE_FRACTURE_SIDE_ACONDITION_11", "PELVIS_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("WRIST_FRACTURE_SIDE");
    builder.inQuestion("WRIST_FRACTURE_SIDE").setAnswerCondition("WRIST_FRACTURE_SIDE_ACONDITION", "WRIST_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("FOREARM_FRACTURE_SIDE");
    builder.inQuestion("FOREARM_FRACTURE_SIDE").setAnswerCondition("FOREARM_FRACTURE_SIDE_ACONDITION", "FOREARM_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("HUMERUS_FRACTURE_SIDE");
    builder.inQuestion("HUMERUS_FRACTURE_SIDE").setAnswerCondition("HUMERUS_FRACTURE_SIDE_ACONDITION", "HUMERUS_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("SCAPULA_FRACTURE_SIDE");
    builder.inQuestion("SCAPULA_FRACTURE_SIDE").setAnswerCondition("SCAPULA_FRACTURE_SIDE_ACONDITION", "SCAPULA_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("CLAVICLE_FRACTURE_SIDE");
    builder.inQuestion("CLAVICLE_FRACTURE_SIDE").setAnswerCondition("CLAVICLE_FRACTURE_SIDE_ACONDITION", "CLAVICLE_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("RIB_FRACTURE_SIDE");
    builder.inQuestion("RIB_FRACTURE_SIDE").setAnswerCondition("RIB_FRACTURE_SIDE_ACONDITION", "RIB_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("FOOT_FRACTURE_SIDE");
    builder.inQuestion("FOOT_FRACTURE_SIDE").setAnswerCondition("FOOT_FRACTURE_SIDE_ACONDITION", "FOOT_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("ANKLE_FRACTURE_SIDE");
    builder.inQuestion("ANKLE_FRACTURE_SIDE").setAnswerCondition("ANKLE_FRACTURE_SIDE_ACONDITION", "ANKLE_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("TIBIA_FIBULA_FRACTURE_SIDE");
    builder.inQuestion("TIBIA_FIBULA_FRACTURE_SIDE").setAnswerCondition("TIBIA_FIBULA_FRACTURE_SIDE_ACONDITION", "TIBIA_FIBULA_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("FEMUR_FRACTURE_SIDE");
    builder.inQuestion("FEMUR_FRACTURE_SIDE").setAnswerCondition("FEMUR_FRACTURE_SIDE_ACONDITION", "FEMUR_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("HIP_FRACTURE_SIDE");
    builder.inQuestion("HIP_FRACTURE_SIDE").setAnswerCondition("HIP_FRACTURE_SIDE_ACONDITION", "HIP_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("PELVIS_FRACTURE_SIDE");
    builder.inQuestion("PELVIS_FRACTURE_SIDE").setAnswerCondition("PELVIS_FRACTURE_SIDE_ACONDITION", "PELVIS_FRACTURE_OCCURRENCE", Y);

    builder.withSection("H_MEDICATION_INTAKE").withSection("PRESCRIBED_MEDICATION").withPage("8").withQuestion("PM0");
    builder.inSection("PRESCRIBED_MEDICATION").withPage("9").withQuestion("TAKING_RX_MED_OCCURENCE").withSharedCategory(N, "0");
    builder.inQuestion("TAKING_RX_MED_OCCURENCE").withSharedCategory(Y, "1");
    builder.inQuestion("TAKING_RX_MED_OCCURENCE").withSharedCategory(PNA, "8");
    builder.inQuestion("TAKING_RX_MED_OCCURENCE").withSharedCategory(DNK, "9");
    builder.inSection("PRESCRIBED_MEDICATION").withPage("9a").withQuestion("RX_MEDICATION").withCategory("MED_NUMBER").withOpenAnswerDefinition("MED_NUMBER", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 3));
    builder.inQuestion("RX_MEDICATION").withSharedCategory(PNA, "88");
    builder.inQuestion("RX_MEDICATION").withSharedCategory(DNK, "99");
    builder.inQuestion("RX_MEDICATION").setAnswerCondition("RX_MEDICATION_ACONDITION", "TAKING_RX_MED_OCCURENCE", Y);
    builder.inSection("PRESCRIBED_MEDICATION").withPage("9b").withQuestion("MEDICATION1_TYPE").withSharedCategory(TYPE).withOpenAnswerDefinition(TYPE, DataType.TEXT);
    builder.inQuestion("MEDICATION1_TYPE").withSharedCategory(PNA, "88");
    builder.inQuestion("MEDICATION1_TYPE").withSharedCategory(DNK, "99");
    builder.inQuestion("MEDICATION1_TYPE").setDataCondition("MEDICATION1_TYPE_DCONDITION", "RX_MEDICATION", "MED_NUMBER", "MED_NUMBER", ComparisionOperator.gt, DataBuilder.buildInteger(0l));
    builder.inPage("9b").withQuestion("MED1_TYPE_INFO_SOURCE").withSharedCategory(PACKAGE, "0");
    builder.inQuestion("MED1_TYPE_INFO_SOURCE").withSharedCategory(LIST, "1");
    builder.inQuestion("MED1_TYPE_INFO_SOURCE").withSharedCategory(PARTICIPANT, "2");
    builder.inQuestion("MED1_TYPE_INFO_SOURCE").withSharedCategory(DNK, "9");
    builder.inQuestion("MED1_TYPE_INFO_SOURCE").setDataCondition("MED1_TYPE_INFO_SOURCE_DCONDITION", "RX_MEDICATION", "MED_NUMBER", "MED_NUMBER", ComparisionOperator.gt, DataBuilder.buildInteger(0l));
    builder.inSection("PRESCRIBED_MEDICATION").withPage("9c").withQuestion("MEDICATION2_TYPE").withSharedCategory(TYPE);
    builder.inQuestion("MEDICATION2_TYPE").withSharedCategory(PNA, "88");
    builder.inQuestion("MEDICATION2_TYPE").withSharedCategory(DNK, "99");
    builder.inQuestion("MEDICATION2_TYPE").setDataCondition("MEDICATION2_TYPE_DCONDITION", "RX_MEDICATION", "MED_NUMBER", "MED_NUMBER", ComparisionOperator.gt, DataBuilder.buildInteger(1l));
    builder.inPage("9c").withQuestion("MED2_TYPE_INFO_SOURCE").withSharedCategory(PACKAGE, "0");
    builder.inQuestion("MED2_TYPE_INFO_SOURCE").withSharedCategory(LIST, "1");
    builder.inQuestion("MED2_TYPE_INFO_SOURCE").withSharedCategory(PARTICIPANT, "2");
    builder.inQuestion("MED2_TYPE_INFO_SOURCE").withSharedCategory(DNK, "9");
    builder.inQuestion("MED2_TYPE_INFO_SOURCE").setDataCondition("MED2_TYPE_INFO_SOURCE_DCONDITION", "RX_MEDICATION", "MED_NUMBER", "MED_NUMBER", ComparisionOperator.gt, DataBuilder.buildInteger(1l));
    builder.inSection("PRESCRIBED_MEDICATION").withPage("9d").withQuestion("MEDICATION3_TYPE").withSharedCategory(TYPE);
    builder.inQuestion("MEDICATION3_TYPE").withSharedCategory(PNA, "88");
    builder.inQuestion("MEDICATION3_TYPE").withSharedCategory(DNK, "99");
    builder.inQuestion("MEDICATION3_TYPE").setDataCondition("MEDICATION3_TYPE_DCONDITION", "RX_MEDICATION", "MED_NUMBER", "MED_NUMBER", ComparisionOperator.gt, DataBuilder.buildInteger(2l));
    builder.inPage("9d").withQuestion("MED3_TYPE_INFO_SOURCE").withSharedCategory(PACKAGE, "0");
    builder.inQuestion("MED3_TYPE_INFO_SOURCE").withSharedCategory(LIST, "1");
    builder.inQuestion("MED3_TYPE_INFO_SOURCE").withSharedCategory(PARTICIPANT, "2");
    builder.inQuestion("MED3_TYPE_INFO_SOURCE").withSharedCategory(DNK, "9");
    builder.inQuestion("MED3_TYPE_INFO_SOURCE").setDataCondition("MED3_TYPE_INFO_SOURCE_DCONDITION", "RX_MEDICATION", "MED_NUMBER", "MED_NUMBER", ComparisionOperator.gt, DataBuilder.buildInteger(2l));
    builder.inPage("9d").addTimestamp("TS_VMH");

    builder.withSection("J_FAMILIAL_HISTORY_DISEASES").withPage("10").withQuestion("FHD01");
    builder.inSection("J_FAMILIAL_HISTORY_DISEASES").withPage("11").withQuestion("KNOWS_HEALTH_FIRST_DEG_REL").withSharedCategory(N, "0");
    builder.inQuestion("KNOWS_HEALTH_FIRST_DEG_REL").withSharedCategory(Y, "1");
    builder.inQuestion("KNOWS_HEALTH_FIRST_DEG_REL").withSharedCategory(PNA, "8");
    builder.inQuestion("KNOWS_HEALTH_FIRST_DEG_REL").withSharedCategory(DNK, "9");
    builder.inSection("J_FAMILIAL_HISTORY_DISEASES").withSection("NEOPLASMS").withPage("12").withQuestion("NEO_FIRST_DEG_REL_OCCURRENCE").withSharedCategory(N, "0");
    builder.inQuestion("NEO_FIRST_DEG_REL_OCCURRENCE").withSharedCategory(Y, "1");
    builder.inQuestion("NEO_FIRST_DEG_REL_OCCURRENCE").withSharedCategory(PNA, "8");
    builder.inQuestion("NEO_FIRST_DEG_REL_OCCURRENCE").withSharedCategory(DNK, "9");
    builder.inQuestion("NEO_FIRST_DEG_REL_OCCURRENCE").setAnswerCondition("NEO_FIRST_DEG_REL_OCCURRENCE_ACONDITION", "KNOWS_HEALTH_FIRST_DEG_REL", Y);
    builder.inSection("NEOPLASMS").withPage("13").withQuestion("NEO_FIRST_DEG_REL_WHICH").withSharedCategory(N, "0");
    builder.inQuestion("NEO_FIRST_DEG_REL_WHICH").withSharedCategory(Y, "1");
    builder.inQuestion("NEO_FIRST_DEG_REL_WHICH").withSharedCategory(PNA, "8");
    builder.inQuestion("NEO_FIRST_DEG_REL_WHICH").withSharedCategory(DNK, "9");
    builder.inQuestion("NEO_FIRST_DEG_REL_WHICH").setAnswerCondition("NEO_FIRST_DEG_REL_WHICH_ACONDITION", "NEO_FIRST_DEG_REL_OCCURRENCE", Y);
    builder.inQuestion("NEO_FIRST_DEG_REL_WHICH").withQuestion("NEOPLASM_MOTHER");
    builder.inQuestion("NEO_FIRST_DEG_REL_WHICH").withQuestion("NEOPLASM_FATHER");
    builder.inPage("13").withQuestion("NEO_FIRST_DEG_REL_NUMBER").withSharedCategory(OPEN_N).withOpenAnswerDefinition(OPEN_N, DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 2));
    builder.inQuestion("NEO_FIRST_DEG_REL_NUMBER").withSharedCategory(PNA, "88");
    builder.inQuestion("NEO_FIRST_DEG_REL_NUMBER").withSharedCategory(DNK, "99");
    builder.inQuestion("NEO_FIRST_DEG_REL_NUMBER").setDataCondition("NEO_FIRST_DEG_REL_NUMBER_DCONDITION_0", "HealthQuestionnaireSelfAdministered", "NUMBER_SIBLINGS_ALL", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(0l));
    builder.inQuestion("NEO_FIRST_DEG_REL_NUMBER").withQuestion("NEOPLASM_NUMBER_SIBLINGS");
    builder.inQuestion("NEO_FIRST_DEG_REL_NUMBER").withQuestion("NEOPLASM_NUMBER_CHILDREN");

    builder.inSection("NEOPLASMS").withPage("14").withQuestion("FHD02");
    builder.inQuestion("FHD02").setAnswerCondition("FHD02_ACONDITION", "NEO_FIRST_DEG_REL_OCCURRENCE", Y);
    builder.inPage("14").withQuestion("NEOPLASM_TYPE_FATHER", DropDownQuestionPanelFactory.class);
    buildNeoplasmList(builder, "NEOPLASM_TYPE_FATHER");
    builder.inQuestion("NEOPLASM_TYPE_FATHER").withSharedCategory(OTHER, "22");
    builder.inQuestion("NEOPLASM_TYPE_FATHER").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_TYPE_FATHER").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_TYPE_FATHER").setAnswerCondition("NEOPLASM_TYPE_FATHER_ACONDITION", "NEOPLASM_FATHER", Y);
    builder.inPage("14").withQuestion("NEOPLASM_OTHER_TYPE_FATHER").withSharedCategory(OPEN_AN);
    builder.inQuestion("NEOPLASM_OTHER_TYPE_FATHER").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_FATHER").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_FATHER").setAnswerCondition("NEOPLASM_OTHER_TYPE_FATHER_ACONDITION", "NEOPLASM_TYPE_FATHER", OTHER);
    builder.inPage("14").withQuestion("NEOPLASM_AGE_FATHER").withSharedCategory(OTHER_AGE).withOpenAnswerDefinition(OTHER_AGE, DataType.INTEGER).addValidator(new NumberValidator.MinimumValidator(0));
    builder.inQuestion("NEOPLASM_AGE_FATHER").withSharedCategory(PNA, "888");
    builder.inQuestion("NEOPLASM_AGE_FATHER").withSharedCategory(DNK, "999");
    builder.inQuestion("NEOPLASM_AGE_FATHER").setAnswerCondition("NEOPLASM_AGE_FATHER_ACONDITION", "NEOPLASM_FATHER", Y);
    builder.inPage("14").withQuestion("NEOPLASM_TYPE_MOTHER", DropDownQuestionPanelFactory.class);
    buildNeoplasmList(builder, "NEOPLASM_TYPE_MOTHER");
    builder.inQuestion("NEOPLASM_TYPE_MOTHER").withSharedCategory(OTHER, "22");
    builder.inQuestion("NEOPLASM_TYPE_MOTHER").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_TYPE_MOTHER").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_TYPE_MOTHER").setAnswerCondition("NEOPLASM_TYPE_MOTHER_ACONDITION", "NEOPLASM_MOTHER", Y);
    builder.inPage("14").withQuestion("NEOPLASM_OTHER_TYPE_MOTHER").withSharedCategory(OPEN_AN);
    builder.inQuestion("NEOPLASM_OTHER_TYPE_MOTHER").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_MOTHER").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_MOTHER").setAnswerCondition("NEOPLASM_OTHER_TYPE_MOTHER_ACONDITION", "NEOPLASM_TYPE_MOTHER", OTHER);
    builder.inPage("14").withQuestion("NEOPLASM_AGE_MOTHER").withSharedCategory(OTHER_AGE);
    builder.inQuestion("NEOPLASM_AGE_MOTHER").withSharedCategory(PNA, "888");
    builder.inQuestion("NEOPLASM_AGE_MOTHER").withSharedCategory(DNK, "999");
    builder.inQuestion("NEOPLASM_AGE_MOTHER").setAnswerCondition("NEOPLASM_AGE_MOTHER_ACONDITION", "NEOPLASM_MOTHER", Y);

    builder.inSection("NEOPLASMS").withPage("15").withQuestion("FHD03");
    builder.inQuestion("FHD03").setDataCondition("FHD03_ACONDITION", "NEOPLASM_NUMBER_SIBLINGS", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(0l));
    builder.inPage("15").withQuestion("NEOPLASM_TYPE_SIB1", DropDownQuestionPanelFactory.class);
    buildNeoplasmList(builder, "NEOPLASM_TYPE_SIB1");
    builder.inQuestion("NEOPLASM_TYPE_SIB1").withSharedCategory(OTHER, "22");
    builder.inQuestion("NEOPLASM_TYPE_SIB1").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_TYPE_SIB1").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_TYPE_SIB1").setDataCondition("NEOPLASM_TYPE_SIB1_DCONDITION", "NEOPLASM_NUMBER_SIBLINGS", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(0l));
    builder.inPage("15").withQuestion("NEOPLASM_OTHER_TYPE_SIB1").withSharedCategory(OPEN_AN);
    builder.inQuestion("NEOPLASM_OTHER_TYPE_SIB1").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_SIB1").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_SIB1").setAnswerCondition("NEOPLASM_OTHER_TYPE_SIB1_ACONDITION", "NEOPLASM_TYPE_SIB1", OTHER);
    builder.inPage("15").withQuestion("NEOPLASM_AGE_SIB1").withSharedCategory(OTHER_AGE);
    builder.inQuestion("NEOPLASM_AGE_SIB1").withSharedCategory(PNA, "888");
    builder.inQuestion("NEOPLASM_AGE_SIB1").withSharedCategory(DNK, "999");
    builder.inQuestion("NEOPLASM_AGE_SIB1").setDataCondition("NEOPLASM_AGE_SIB1_DCONDITION", "NEOPLASM_NUMBER_SIBLINGS", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(0l));
    builder.inPage("15").withQuestion("BIRTH_RANK_SIB1").withSharedCategory(BIRTH_RANK).withOpenAnswerDefinition(BIRTH_RANK, DataType.INTEGER).addValidator(new NumberValidator.MinimumValidator(0));
    builder.inQuestion("BIRTH_RANK_SIB1").withSharedCategory(PNA, "88");
    builder.inQuestion("BIRTH_RANK_SIB1").withSharedCategory(DNK, "99");
    builder.inQuestion("BIRTH_RANK_SIB1").setDataCondition("BIRTH_RANK_SIB1_DCONDITION", "NEOPLASM_NUMBER_SIBLINGS", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(0l));
    builder.inPage("15").withQuestion("NEOPLASM_TYPE_SIB2", DropDownQuestionPanelFactory.class);
    buildNeoplasmList(builder, "NEOPLASM_TYPE_SIB2");
    builder.inQuestion("NEOPLASM_TYPE_SIB2").withSharedCategory(OTHER, "22");
    builder.inQuestion("NEOPLASM_TYPE_SIB2").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_TYPE_SIB2").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_TYPE_SIB2").setDataCondition("NEOPLASM_TYPE_SIB2_DCONDITION", "NEOPLASM_NUMBER_SIBLINGS", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(1l));
    builder.inPage("15").withQuestion("NEOPLASM_OTHER_TYPE_SIB2").withSharedCategory(OPEN_AN);
    builder.inQuestion("NEOPLASM_OTHER_TYPE_SIB2").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_SIB2").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_SIB2").setAnswerCondition("NEOPLASM_OTHER_TYPE_SIB2_ACONDITION", "NEOPLASM_TYPE_SIB2", OTHER);
    builder.inPage("15").withQuestion("NEOPLASM_AGE_SIB2").withSharedCategory(OTHER_AGE);
    builder.inQuestion("NEOPLASM_AGE_SIB2").withSharedCategory(PNA, "888");
    builder.inQuestion("NEOPLASM_AGE_SIB2").withSharedCategory(DNK, "999");
    builder.inQuestion("NEOPLASM_AGE_SIB2").setDataCondition("NEOPLASM_AGE_SIB2_DCONDITION", "NEOPLASM_NUMBER_SIBLINGS", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(1l));
    builder.inPage("15").withQuestion("BIRTH_RANK_SIB2").withSharedCategory(BIRTH_RANK);
    builder.inQuestion("BIRTH_RANK_SIB2").withSharedCategory(PNA, "88");
    builder.inQuestion("BIRTH_RANK_SIB2").withSharedCategory(DNK, "99");
    builder.inQuestion("BIRTH_RANK_SIB2").setDataCondition("BIRTH_RANK_SIB2_DCONDITION", "NEOPLASM_NUMBER_SIBLINGS", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(1l));

    builder.inSection("NEOPLASMS").withPage("16").withQuestion("FHD04");
    builder.inQuestion("FHD04").setDataCondition("FHD04_DCONDITION", "NEOPLASM_NUMBER_CHILDREN", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(0l));
    builder.inPage("16").withQuestion("NEOPLASM_TYPE_CHILD1", DropDownQuestionPanelFactory.class);
    buildNeoplasmList(builder, "NEOPLASM_TYPE_CHILD1");
    builder.inQuestion("NEOPLASM_TYPE_CHILD1").withSharedCategory(OTHER, "22");
    builder.inQuestion("NEOPLASM_TYPE_CHILD1").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_TYPE_CHILD1").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_TYPE_CHILD1").setDataCondition("NEOPLASM_TYPE_CHILD1_DCONDITION", "NEOPLASM_NUMBER_CHILDREN", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(0l));
    builder.inPage("16").withQuestion("NEOPLASM_OTHER_TYPE_CHILD1").withSharedCategory(OPEN_AN);
    builder.inQuestion("NEOPLASM_OTHER_TYPE_CHILD1").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_CHILD1").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_CHILD1").setAnswerCondition("NEOPLASM_OTHER_TYPE_CHILD1_ACONDITION", "NEOPLASM_TYPE_CHILD1", OTHER);
    builder.inPage("16").withQuestion("NEOPLASM_AGE_CHILD1").withSharedCategory(OTHER_AGE);
    builder.inQuestion("NEOPLASM_AGE_CHILD1").withSharedCategory(PNA, "888");
    builder.inQuestion("NEOPLASM_AGE_CHILD1").withSharedCategory(DNK, "999");
    builder.inQuestion("NEOPLASM_AGE_CHILD1").setDataCondition("NEOPLASM_AGE_CHILD1_DCONDITION", "NEOPLASM_NUMBER_CHILDREN", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(0l));
    builder.inPage("16").withQuestion("BIRTH_RANK_CHILD1").withSharedCategory(BIRTH_RANK);
    builder.inQuestion("BIRTH_RANK_CHILD1").withSharedCategory(PNA, "88");
    builder.inQuestion("BIRTH_RANK_CHILD1").withSharedCategory(DNK, "99");
    builder.inQuestion("BIRTH_RANK_CHILD1").setDataCondition("BIRTH_RANK_CHILD1_DCONDITION", "NEOPLASM_NUMBER_CHILDREN", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(0l));
    builder.inPage("16").withQuestion("NEOPLASM_TYPE_CHILD2", DropDownQuestionPanelFactory.class);
    buildNeoplasmList(builder, "NEOPLASM_TYPE_CHILD2");
    builder.inQuestion("NEOPLASM_TYPE_CHILD2").withSharedCategory(OTHER, "22");
    builder.inQuestion("NEOPLASM_TYPE_CHILD2").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_TYPE_CHILD2").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_TYPE_CHILD2").setDataCondition("NEOPLASM_TYPE_CHILD2_DCONDITION", "NEOPLASM_NUMBER_CHILDREN", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(1l));
    builder.inPage("16").withQuestion("NEOPLASM_OTHER_TYPE_CHILD2").withSharedCategory(OPEN_AN);
    builder.inQuestion("NEOPLASM_OTHER_TYPE_CHILD2").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_CHILD2").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_CHILD2").setAnswerCondition("NEOPLASM_OTHER_TYPE_CHILD2_ACONDITION", "NEOPLASM_TYPE_CHILD2", OTHER);
    builder.inPage("16").withQuestion("NEOPLASM_AGE_CHILD2").withSharedCategory(OTHER_AGE);
    builder.inQuestion("NEOPLASM_AGE_CHILD2").withSharedCategory(PNA, "888");
    builder.inQuestion("NEOPLASM_AGE_CHILD2").withSharedCategory(DNK, "999");
    builder.inQuestion("NEOPLASM_AGE_CHILD2").setDataCondition("NEOPLASM_AGE_CHILD2_DCONDITION", "NEOPLASM_NUMBER_CHILDREN", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(1l));
    builder.inPage("16").withQuestion("BIRTH_RANK_CHILD2").withSharedCategory(BIRTH_RANK);
    builder.inQuestion("BIRTH_RANK_CHILD2").withSharedCategory(PNA, "88");
    builder.inQuestion("BIRTH_RANK_CHILD2").withSharedCategory(DNK, "99");
    builder.inQuestion("BIRTH_RANK_CHILD2").setDataCondition("BIRTH_RANK_CHILD2_DCONDITION", "NEOPLASM_NUMBER_CHILDREN", OPEN_N, OPEN_N, ComparisionOperator.gt, DataBuilder.buildInteger(1l));

    return builder;
  }

  private static void buildNeoplasmList(QuestionnaireBuilder builder, String questionName) {
    builder.inQuestion(questionName).withSharedCategory("BREAST", "1");
    builder.inQuestion(questionName).withSharedCategory("COLON", "2");
    builder.inQuestion(questionName).withSharedCategory("LUNG", "3");
    builder.inQuestion(questionName).withSharedCategory("LIVER", "4");
    builder.inQuestion(questionName).withSharedCategory("PROSTATE", "5");
    builder.inQuestion(questionName).withSharedCategory("OVARY", "6");
    builder.inQuestion(questionName).withSharedCategory("PANCREAS", "7");
    builder.inQuestion(questionName).withSharedCategory("STOMACH", "8");
    builder.inQuestion(questionName).withSharedCategory("OESOPHAGUS", "9");
    builder.inQuestion(questionName).withSharedCategory("LARYNX", "10");
    builder.inQuestion(questionName).withSharedCategory("TRACHEA", "11");
    builder.inQuestion(questionName).withSharedCategory("RECTUM", "12");
    builder.inQuestion(questionName).withSharedCategory("SKIN", "13");
    builder.inQuestion(questionName).withSharedCategory("CERVIX", "14");
    builder.inQuestion(questionName).withSharedCategory("UTERUS", "15");
    builder.inQuestion(questionName).withSharedCategory("KIDNEY", "16");
    builder.inQuestion(questionName).withSharedCategory("BLADDER", "17");
    builder.inQuestion(questionName).withSharedCategory("BRAIN", "18");
    builder.inQuestion(questionName).withSharedCategory("THYROID", "19");
    builder.inQuestion(questionName).withSharedCategory("NN_HODGKIN", "20");
    builder.inQuestion(questionName).withSharedCategory("LEUKAEMIA", "21");
  }
}
