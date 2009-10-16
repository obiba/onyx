/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.example.questionnaire.util;

import java.util.Locale;

import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.RangeValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanelFactory;
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

  private static final String OPEN_N_0 = "OPEN_N_0";

  private static final String OPEN_AN = "OPEN_AN";

  private static final String OTHER_AGE = "OTHER_AGE";

  private static final String BIRTH_RANK = "BIRTH_RANK";

  private static final String[] LIST_OF_COUNTRY = Locale.getISOCountries();

  public static QuestionnaireBuilder buildQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaireAssisted", "1.0");

    builder.withSection("B_DEMOGRAPHY").withSection("BIRTH_LOCATION").withPage("1").withQuestion("COUNTRY_BIRTH_ELSEWHERE", "1", DropDownQuestionPanelFactory.class).withSharedCategories(LIST_OF_COUNTRY);
    builder.inQuestion("COUNTRY_BIRTH_ELSEWHERE").withSharedCategory(PNA, "88").setEscape(true);
    builder.inQuestion("COUNTRY_BIRTH_ELSEWHERE").withSharedCategory(DNK, "99").setEscape(true);
    builder.inQuestion("COUNTRY_BIRTH_ELSEWHERE").setCondition("HealthQuestionnaireSelfAdministered", "COUNTRY_BIRTH", "ELSEWHERE");
    // Pour les parents? Pays de résidence?

    builder.withSection("C_LIFE_HABITS").withSection("JOB_RELATED_PA").withSection("PART1").withPage("2").withQuestion("CURRENTLY_WORK", "2").withSharedCategory(N, "0");
    builder.inQuestion("CURRENTLY_WORK").withSharedCategory(Y, "1");
    builder.inQuestion("CURRENTLY_WORK").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENTLY_WORK").withSharedCategory(DNK, "9");
    builder.inSection("PART1").withPage("2a").withQuestion("WORK_VIGOROUS_ACTIVITY_DAYS_WEEK", "3").withCategory("DAYS_WEEK").withOpenAnswerDefinition("DAYS_WEEK", DataType.INTEGER).addValidator(new RangeValidator(0, 7));
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_DAYS_WEEK").withSharedCategory(PNA, "8");
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_DAYS_WEEK").withSharedCategory(DNK, "9");
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_DAYS_WEEK").setCondition("CURRENTLY_WORK", Y);
    builder.inSection("PART1").withPage("2b").withQuestion("WORK_VIGOROUS_ACTIVITY_TIME_DAY", "4").withCategory("ACTIVITY_TIME_DAY").withOpenAnswerDefinition("ACTIVITY_TIME_DAY", DataType.INTEGER).withOpenAnswerDefinition("ACTIVITY_HOURS_DAY", DataType.INTEGER).setRequired(false).addValidator(new RangeValidator(0, 16));
    builder.inOpenAnswerDefinition("ACTIVITY_TIME_DAY").withOpenAnswerDefinition("ACTIVITY_MIN_DAY", DataType.INTEGER).setRequired(false).addValidator(new RangeValidator(0, 960));
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_TIME_DAY").withSharedCategory(PNA, "888");
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_TIME_DAY").withSharedCategory(DNK, "999");
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_TIME_DAY").setCondition("$1 > 0", builder.newDataSource("WORK_VIGOROUS_ACTIVITY_DAYS_WEEK", "DAYS_WEEK", "DAYS_WEEK"));
    builder.inSection("PART1").withPage("3").withQuestion("WORK_VIGOROUS_ACTIVITY_TIME_WEEK", "5").withCategory("ACTIVITY_TIME_WEEK").withOpenAnswerDefinition("ACTIVITY_TIME_WEEK", DataType.INTEGER).withOpenAnswerDefinition("ACTIVITY_HOURS_WEEK", DataType.INTEGER).setRequired(false).addValidator(new RangeValidator(0, 112));
    builder.inOpenAnswerDefinition("ACTIVITY_TIME_WEEK").withOpenAnswerDefinition("ACTIVITY_MIN_WEEK", DataType.INTEGER).setRequired(false).addValidator(new RangeValidator(0, 6720));
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_TIME_WEEK").withSharedCategory(PNA, "8888");
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_TIME_WEEK").withSharedCategory(DNK, "9999");
    builder.inQuestion("WORK_VIGOROUS_ACTIVITY_TIME_WEEK").setCondition("WORK_VIGOROUS_ACTIVITY_TIME_DAY", DNK);

    builder.withSection("F_IND_HISTORY_DISEASES").withSection("INDIVIDUAL_HISTORY_INJURIES").withPage("4").withQuestion("BONE_FRACTURE_OCCURRENCE", "6").withSharedCategory(N, "0");
    builder.inQuestion("BONE_FRACTURE_OCCURRENCE").withSharedCategory(Y, "1");
    builder.inQuestion("BONE_FRACTURE_OCCURRENCE").withSharedCategory(PNA, "8");
    builder.inQuestion("BONE_FRACTURE_OCCURRENCE").withSharedCategory(DNK, "9");
    builder.inSection("INDIVIDUAL_HISTORY_INJURIES").withPage("5").withQuestion("BONE_FRACTURE_WHICH", "7").withSharedCategory(N, "0");
    builder.inQuestion("BONE_FRACTURE_WHICH").withSharedCategory(Y, "1");
    builder.inQuestion("BONE_FRACTURE_WHICH").withSharedCategory(PNA, "8");
    builder.inQuestion("BONE_FRACTURE_WHICH").withSharedCategory(DNK, "9");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("VERTEBRA_FRACTURE_OCCURRENCE", "1.1");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("STERNUM_FRACTURE_OCCURRENCE", "1.2");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("SACRUM_FRACTURE_OCCURRENCE", "1.3");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("WRIST_FRACTURE_OCCURRENCE", "1.4");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("FOREARM_FRACTURE_OCCURRENCE", "1.5");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("HUMERUS_FRACTURE_OCCURRENCE", "1.6");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("SCAPULA_FRACTURE_OCCURRENCE", "1.7");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("CLAVICLE_FRACTURE_OCCURRENCE", "1.8");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("RIB_FRACTURE_OCCURRENCE", "1.9");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("FOOT_FRACTURE_OCCURRENCE", "1.10");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("ANKLE_FRACTURE_OCCURRENCE", "1.11");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("TIBIA_FIBULA_FRACTURE_OCCURRENCE", "1.12");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("FEMUR_FRACTURE_OCCURRENCE", "1.13");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("HIP_FRACTURE_OCCURRENCE", "1.14");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("PELVIS_FRACTURE_OCCURRENCE", "1.15");
    builder.inQuestion("BONE_FRACTURE_WHICH").withQuestion("OTHER_BONE_FRACTURE_OCCURRENCE", "1.16");
    builder.inQuestion("BONE_FRACTURE_WHICH").setCondition("BONE_FRACTURE_OCCURRENCE", Y);
    builder.inSection("INDIVIDUAL_HISTORY_INJURIES").withPage("6").withQuestion("OTHER_BONE_FRACTURE_TYPE", "8").withSharedCategory(OPEN_AN).withOpenAnswerDefinition(OPEN_AN, DataType.TEXT);
    builder.inQuestion("OTHER_BONE_FRACTURE_TYPE").withSharedCategory(PNA, "88");
    builder.inQuestion("OTHER_BONE_FRACTURE_TYPE").withSharedCategory(DNK, "99");
    builder.inQuestion("OTHER_BONE_FRACTURE_TYPE").setCondition("OTHER_BONE_FRACTURE_OCCURRENCE", Y);
    builder.inSection("INDIVIDUAL_HISTORY_INJURIES").withPage("7").withQuestion("BONE_FRACTURE_SIDE", "9").withCategory("LEFT").setExportName("1");
    builder.inQuestion("BONE_FRACTURE_SIDE").withCategory("RIGHT").setExportName("2");
    builder.inQuestion("BONE_FRACTURE_SIDE").withCategory("BOTH").setExportName("3");
    builder.inQuestion("BONE_FRACTURE_SIDE").withSharedCategory(PNA, "8");
    builder.inQuestion("BONE_FRACTURE_SIDE").withSharedCategory(DNK, "9");
    builder.inQuestion("BONE_FRACTURE_SIDE").setCondition("$1 || $2 || $3 || $4 || $5 || $6 || $7 || $8 || $9 || $10 || $11 || $12", builder.newDataSource("WRIST_FRACTURE_OCCURRENCE", Y), builder.newDataSource("FOREARM_FRACTURE_OCCURRENCE", Y), builder.newDataSource("HUMERUS_FRACTURE_OCCURRENCE", Y), builder.newDataSource("SCAPULA_FRACTURE_OCCURRENCE", Y), builder.newDataSource("CLAVICLE_FRACTURE_OCCURRENCE", Y), builder.newDataSource("RIB_FRACTURE_OCCURRENCE", Y), builder.newDataSource("FOOT_FRACTURE_OCCURRENCE", Y), builder.newDataSource("ANKLE_FRACTURE_OCCURRENCE", Y), builder.newDataSource("TIBIA_FIBULA_FRACTURE_OCCURRENCE", Y), builder.newDataSource("FEMUR_FRACTURE_OCCURRENCE", Y), builder.newDataSource("HIP_FRACTURE_OCCURRENCE", Y), builder.newDataSource("PELVIS_FRACTURE_OCCURRENCE", Y));
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("WRIST_FRACTURE_SIDE", "1.4");
    builder.inQuestion("WRIST_FRACTURE_SIDE").setCondition("WRIST_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("FOREARM_FRACTURE_SIDE", "1.5");
    builder.inQuestion("FOREARM_FRACTURE_SIDE").setCondition("FOREARM_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("HUMERUS_FRACTURE_SIDE", "1.6");
    builder.inQuestion("HUMERUS_FRACTURE_SIDE").setCondition("HUMERUS_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("SCAPULA_FRACTURE_SIDE", "1.7");
    builder.inQuestion("SCAPULA_FRACTURE_SIDE").setCondition("SCAPULA_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("CLAVICLE_FRACTURE_SIDE", "1.8");
    builder.inQuestion("CLAVICLE_FRACTURE_SIDE").setCondition("CLAVICLE_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("RIB_FRACTURE_SIDE", "1.9");
    builder.inQuestion("RIB_FRACTURE_SIDE").setCondition("RIB_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("FOOT_FRACTURE_SIDE", "1.10");
    builder.inQuestion("FOOT_FRACTURE_SIDE").setCondition("FOOT_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("ANKLE_FRACTURE_SIDE", "1.11");
    builder.inQuestion("ANKLE_FRACTURE_SIDE").setCondition("ANKLE_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("TIBIA_FIBULA_FRACTURE_SIDE", "1.12");
    builder.inQuestion("TIBIA_FIBULA_FRACTURE_SIDE").setCondition("TIBIA_FIBULA_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("FEMUR_FRACTURE_SIDE", "1.13");
    builder.inQuestion("FEMUR_FRACTURE_SIDE").setCondition("FEMUR_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("HIP_FRACTURE_SIDE", "1.14");
    builder.inQuestion("HIP_FRACTURE_SIDE").setCondition("HIP_FRACTURE_OCCURRENCE", Y);
    builder.inQuestion("BONE_FRACTURE_SIDE").withQuestion("PELVIS_FRACTURE_SIDE", "1.15");
    builder.inQuestion("PELVIS_FRACTURE_SIDE").setCondition("PELVIS_FRACTURE_OCCURRENCE", Y);

    builder.withSection("H_MEDICATION_INTAKE").withSection("PRESCRIBED_MEDICATION").withPage("8").withQuestion("PM0");
    builder.inSection("PRESCRIBED_MEDICATION").withPage("9").withQuestion("TAKING_RX_MED_OCCURENCE", "10").withSharedCategory(N, "0");
    builder.inQuestion("TAKING_RX_MED_OCCURENCE").withSharedCategory(Y, "1");
    builder.inQuestion("TAKING_RX_MED_OCCURENCE").withSharedCategory(PNA, "8");
    builder.inQuestion("TAKING_RX_MED_OCCURENCE").withSharedCategory(DNK, "9");
    builder.inSection("PRESCRIBED_MEDICATION").withPage("9a").withQuestion("RX_MEDICATION", "11").withCategory("MED_NUMBER").withOpenAnswerDefinition("MED_NUMBER", DataType.INTEGER).addValidator(new RangeValidator(0, 3));
    builder.inQuestion("RX_MEDICATION").withSharedCategory(PNA, "88");
    builder.inQuestion("RX_MEDICATION").withSharedCategory(DNK, "99");
    builder.inQuestion("RX_MEDICATION").setCondition("TAKING_RX_MED_OCCURENCE", Y);
    builder.inSection("PRESCRIBED_MEDICATION").withPage("9b").withQuestion("MEDICATION1_TYPE", "12").withSharedCategory(TYPE).withOpenAnswerDefinition(TYPE, DataType.TEXT);
    builder.inQuestion("MEDICATION1_TYPE").withSharedCategory(PNA, "88");
    builder.inQuestion("MEDICATION1_TYPE").withSharedCategory(DNK, "99");
    builder.inQuestion("MEDICATION1_TYPE").setCondition("$1 > 1", builder.newDataSource("RX_MEDICATION", "MED_NUMBER", "MED_NUMBER"));
    builder.inPage("9b").withQuestion("MED1_TYPE_INFO_SOURCE", "13").withSharedCategory(PACKAGE, "0");
    builder.inQuestion("MED1_TYPE_INFO_SOURCE").withSharedCategory(LIST, "1");
    builder.inQuestion("MED1_TYPE_INFO_SOURCE").withSharedCategory(PARTICIPANT, "2");
    builder.inQuestion("MED1_TYPE_INFO_SOURCE").withSharedCategory(DNK, "9");
    builder.inQuestion("MED1_TYPE_INFO_SOURCE").setCondition("$1 > 0", builder.newDataSource("RX_MEDICATION", "MED_NUMBER", "MED_NUMBER"));
    builder.inSection("PRESCRIBED_MEDICATION").withPage("9c").withQuestion("MEDICATION2_TYPE", "14").withSharedCategory(TYPE);
    builder.inQuestion("MEDICATION2_TYPE").withSharedCategory(PNA, "88");
    builder.inQuestion("MEDICATION2_TYPE").withSharedCategory(DNK, "99");
    builder.inQuestion("MEDICATION2_TYPE").setCondition("$1 > 1", builder.newDataSource("RX_MEDICATION", "MED_NUMBER", "MED_NUMBER"));
    builder.inPage("9c").withQuestion("MED2_TYPE_INFO_SOURCE", "15").withSharedCategory(PACKAGE, "0");
    builder.inQuestion("MED2_TYPE_INFO_SOURCE").withSharedCategory(LIST, "1");
    builder.inQuestion("MED2_TYPE_INFO_SOURCE").withSharedCategory(PARTICIPANT, "2");
    builder.inQuestion("MED2_TYPE_INFO_SOURCE").withSharedCategory(DNK, "9");
    builder.inQuestion("MED2_TYPE_INFO_SOURCE").setCondition("$1 > 1", builder.newDataSource("RX_MEDICATION", "MED_NUMBER", "MED_NUMBER"));
    builder.inSection("PRESCRIBED_MEDICATION").withPage("9d").withQuestion("MEDICATION3_TYPE", "16").withSharedCategory(TYPE);
    builder.inQuestion("MEDICATION3_TYPE").withSharedCategory(PNA, "88");
    builder.inQuestion("MEDICATION3_TYPE").withSharedCategory(DNK, "99");
    builder.inQuestion("MEDICATION3_TYPE").setCondition("$1 > 2", builder.newDataSource("RX_MEDICATION", "MED_NUMBER", "MED_NUMBER"));
    builder.inPage("9d").withQuestion("MED3_TYPE_INFO_SOURCE", "17").withSharedCategory(PACKAGE, "0");
    builder.inQuestion("MED3_TYPE_INFO_SOURCE").withSharedCategory(LIST, "1");
    builder.inQuestion("MED3_TYPE_INFO_SOURCE").withSharedCategory(PARTICIPANT, "2");
    builder.inQuestion("MED3_TYPE_INFO_SOURCE").withSharedCategory(DNK, "9");
    builder.inQuestion("MED3_TYPE_INFO_SOURCE").setCondition("$1 > 2", builder.newDataSource("RX_MEDICATION", "MED_NUMBER", "MED_NUMBER"));

    builder.withSection("I_WOMEN_AND_MEN_HEALTH").withSection("MENSTRUATION").withPage("10").withQuestion("ME0");
    builder.inSection("I_WOMEN_AND_MEN_HEALTH").withSection("REPRODUCTION_BREASTFEEDING").withPage("11").withQuestion("NUMBER_PREGNANCIES_ALL", "18").withSharedCategory(OPEN_N_0).withOpenAnswerDefinition(OPEN_N_0, DataType.INTEGER).addValidator(new MinimumValidator(0l));
    builder.inQuestion("NUMBER_PREGNANCIES_ALL").withSharedCategory(PNA, "88");
    builder.inQuestion("NUMBER_PREGNANCIES_ALL").withSharedCategory(DNK, "99");
    builder.inQuestion("NUMBER_PREGNANCIES_ALL").setCondition("HealthQuestionnaireSelfAdministered", "SEX", "FEMALE");
    builder.inSection("REPRODUCTION_BREASTFEEDING").withPage("12").withQuestion("NUMBER_LIVE_BIRTHS", "19").withSharedCategory(OPEN_N_0);
    builder.inQuestion("NUMBER_LIVE_BIRTHS").withSharedCategory(PNA, "88");
    builder.inQuestion("NUMBER_LIVE_BIRTHS").withSharedCategory(DNK, "99");
    builder.inQuestion("NUMBER_LIVE_BIRTHS").setCondition("$1 > 0", builder.newDataSource("NUMBER_PREGNANCIES_ALL", OPEN_N_0, OPEN_N_0));
    builder.inSection("I_WOMEN_AND_MEN_HEALTH").withSection("MEN_REPRODUCTION").withPage("13").withQuestion("NUMBER_CHILDREN_FATHERED", "20").withSharedCategory(OPEN_N_0);
    builder.inQuestion("NUMBER_CHILDREN_FATHERED").withSharedCategory(PNA, "88");
    builder.inQuestion("NUMBER_CHILDREN_FATHERED").withSharedCategory(DNK, "99");
    builder.inQuestion("NUMBER_CHILDREN_FATHERED").setCondition("HealthQuestionnaireSelfAdministered", "SEX", "MALE");

    builder.withSection("J_FAMILIAL_HISTORY_DISEASES").withPage("14").withQuestion("FHD01");
    builder.inSection("J_FAMILIAL_HISTORY_DISEASES").withPage("15").withQuestion("KNOWS_HEALTH_FIRST_DEG_REL", "21").withSharedCategory(N, "0");
    builder.inQuestion("KNOWS_HEALTH_FIRST_DEG_REL").withSharedCategory(Y, "1");
    builder.inQuestion("KNOWS_HEALTH_FIRST_DEG_REL").withSharedCategory(PNA, "8");
    builder.inQuestion("KNOWS_HEALTH_FIRST_DEG_REL").withSharedCategory(DNK, "9");
    builder.inSection("J_FAMILIAL_HISTORY_DISEASES").withSection("NEOPLASMS").withPage("16").withQuestion("NEO_FIRST_DEG_REL_OCCURRENCE", "22").withSharedCategory(N, "0");
    builder.inQuestion("NEO_FIRST_DEG_REL_OCCURRENCE").withSharedCategory(Y, "1");
    builder.inQuestion("NEO_FIRST_DEG_REL_OCCURRENCE").withSharedCategory(PNA, "8");
    builder.inQuestion("NEO_FIRST_DEG_REL_OCCURRENCE").withSharedCategory(DNK, "9");
    builder.inQuestion("NEO_FIRST_DEG_REL_OCCURRENCE").setCondition("KNOWS_HEALTH_FIRST_DEG_REL", Y);
    builder.inSection("NEOPLASMS").withPage("17").withQuestion("NEO_FIRST_DEG_REL_WHICH", "23").withSharedCategory(N, "0");
    builder.inQuestion("NEO_FIRST_DEG_REL_WHICH").withSharedCategory(Y, "1");
    builder.inQuestion("NEO_FIRST_DEG_REL_WHICH").withSharedCategory(PNA, "8");
    builder.inQuestion("NEO_FIRST_DEG_REL_WHICH").withSharedCategory(DNK, "9");
    builder.inQuestion("NEO_FIRST_DEG_REL_WHICH").setCondition("NEO_FIRST_DEG_REL_OCCURRENCE", Y);
    builder.inQuestion("NEO_FIRST_DEG_REL_WHICH").withQuestion("NEOPLASM_MOTHER", "1.1");
    builder.inQuestion("NEO_FIRST_DEG_REL_WHICH").withQuestion("NEOPLASM_FATHER", "1.2");
    builder.inPage("17").withQuestion("NEO_FIRST_DEG_REL_NUMBER").withSharedCategory(OPEN_N).withOpenAnswerDefinition(OPEN_N, DataType.INTEGER).addValidator(new RangeValidator(0, 2));
    builder.inQuestion("NEO_FIRST_DEG_REL_NUMBER").withSharedCategory(PNA, "88");
    builder.inQuestion("NEO_FIRST_DEG_REL_NUMBER").withSharedCategory(DNK, "99");
    builder.inQuestion("NEO_FIRST_DEG_REL_NUMBER").setCondition("$1 > 0 || $2 > 0 || $3 > 0", builder.newExternalDataSource("HealthQuestionnaireSelfAdministered", "NUMBER_SIBLINGS_ALL", OPEN_N, OPEN_N), builder.newDataSource("NUMBER_LIVE_BIRTHS", OPEN_N_0, OPEN_N_0), builder.newDataSource("NUMBER_CHILDREN_FATHERED", OPEN_N_0, OPEN_N_0));
    builder.inQuestion("NEO_FIRST_DEG_REL_NUMBER").withQuestion("NEOPLASM_NUMBER_SIBLINGS", "1.3");
    builder.inQuestion("NEOPLASM_NUMBER_SIBLINGS").setCondition("$1 > 0", builder.newExternalDataSource("HealthQuestionnaireSelfAdministered", "NUMBER_SIBLINGS_ALL", OPEN_N, OPEN_N));
    builder.inQuestion("NEO_FIRST_DEG_REL_NUMBER").withQuestion("NEOPLASM_NUMBER_CHILDREN", "1.4");
    builder.inQuestion("NEOPLASM_NUMBER_CHILDREN").setCondition("$1 > 0 || $2 > 0 ", builder.newDataSource("NUMBER_LIVE_BIRTHS", OPEN_N_0, OPEN_N_0), builder.newDataSource("NUMBER_CHILDREN_FATHERED", OPEN_N_0, OPEN_N_0));

    builder.inSection("NEOPLASMS").withPage("18").withQuestion("FHD02");
    builder.inQuestion("FHD02").setCondition("NEO_FIRST_DEG_REL_OCCURRENCE", Y);
    builder.inPage("18").withQuestion("NEOPLASM_TYPE_FATHER", "24", DropDownQuestionPanelFactory.class);
    buildNeoplasmList(builder, "NEOPLASM_TYPE_FATHER", "male");
    builder.inQuestion("NEOPLASM_TYPE_FATHER").withSharedCategory(OTHER, "22");
    builder.inQuestion("NEOPLASM_TYPE_FATHER").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_TYPE_FATHER").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_TYPE_FATHER").setCondition("NEOPLASM_FATHER", Y);
    builder.inPage("18").withQuestion("NEOPLASM_OTHER_TYPE_FATHER", "25").withSharedCategory(OPEN_AN);
    builder.inQuestion("NEOPLASM_OTHER_TYPE_FATHER").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_FATHER").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_FATHER").setCondition("NEOPLASM_TYPE_FATHER", OTHER);
    builder.inPage("18").withQuestion("NEOPLASM_AGE_FATHER", "26").withSharedCategory(OTHER_AGE).withOpenAnswerDefinition(OTHER_AGE, DataType.INTEGER).addValidator(new MinimumValidator(0));
    builder.inQuestion("NEOPLASM_AGE_FATHER").withSharedCategory(PNA, "888");
    builder.inQuestion("NEOPLASM_AGE_FATHER").withSharedCategory(DNK, "999");
    builder.inQuestion("NEOPLASM_AGE_FATHER").setCondition("NEOPLASM_FATHER", Y);
    builder.inPage("18").withQuestion("NEOPLASM_TYPE_MOTHER", "27", DropDownQuestionPanelFactory.class);
    buildNeoplasmList(builder, "NEOPLASM_TYPE_MOTHER", "female");
    builder.inQuestion("NEOPLASM_TYPE_MOTHER").withSharedCategory(OTHER, "22");
    builder.inQuestion("NEOPLASM_TYPE_MOTHER").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_TYPE_MOTHER").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_TYPE_MOTHER").setCondition("NEOPLASM_MOTHER", Y);
    builder.inPage("18").withQuestion("NEOPLASM_OTHER_TYPE_MOTHER", "28").withSharedCategory(OPEN_AN);
    builder.inQuestion("NEOPLASM_OTHER_TYPE_MOTHER").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_MOTHER").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_MOTHER").setCondition("NEOPLASM_TYPE_MOTHER", OTHER);
    builder.inPage("18").withQuestion("NEOPLASM_AGE_MOTHER", "29").withSharedCategory(OTHER_AGE);
    builder.inQuestion("NEOPLASM_AGE_MOTHER").withSharedCategory(PNA, "888");
    builder.inQuestion("NEOPLASM_AGE_MOTHER").withSharedCategory(DNK, "999");
    builder.inQuestion("NEOPLASM_AGE_MOTHER").setCondition("NEOPLASM_MOTHER", Y);

    builder.inSection("NEOPLASMS").withPage("19").withQuestion("FHD03");
    builder.inQuestion("FHD03").setCondition("$1 > 0", builder.newDataSource("NEOPLASM_NUMBER_SIBLINGS", OPEN_N, OPEN_N));
    builder.inPage("19").withQuestion("NEOPLASM_TYPE_SIB1", "30", DropDownQuestionPanelFactory.class);
    buildNeoplasmList(builder, "NEOPLASM_TYPE_SIB1", "mixte");
    builder.inQuestion("NEOPLASM_TYPE_SIB1").withSharedCategory(OTHER, "22");
    builder.inQuestion("NEOPLASM_TYPE_SIB1").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_TYPE_SIB1").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_TYPE_SIB1").setCondition("$1 > 0", builder.newDataSource("NEOPLASM_NUMBER_SIBLINGS", OPEN_N, OPEN_N));
    builder.inPage("19").withQuestion("NEOPLASM_OTHER_TYPE_SIB1", "31").withSharedCategory(OPEN_AN);
    builder.inQuestion("NEOPLASM_OTHER_TYPE_SIB1").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_SIB1").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_SIB1").setCondition("NEOPLASM_TYPE_SIB1", OTHER);
    builder.inPage("19").withQuestion("NEOPLASM_AGE_SIB1", "32").withSharedCategory(OTHER_AGE);
    builder.inQuestion("NEOPLASM_AGE_SIB1").withSharedCategory(PNA, "888");
    builder.inQuestion("NEOPLASM_AGE_SIB1").withSharedCategory(DNK, "999");
    builder.inQuestion("NEOPLASM_AGE_SIB1").setCondition("$1 > 0", builder.newDataSource("NEOPLASM_NUMBER_SIBLINGS", OPEN_N, OPEN_N));
    builder.inPage("19").withQuestion("BIRTH_RANK_SIB1", "33").withSharedCategory(BIRTH_RANK).withOpenAnswerDefinition(BIRTH_RANK, DataType.INTEGER).addValidator(new MinimumValidator(0));
    builder.inQuestion("BIRTH_RANK_SIB1").withSharedCategory(PNA, "88");
    builder.inQuestion("BIRTH_RANK_SIB1").withSharedCategory(DNK, "99");
    builder.inQuestion("BIRTH_RANK_SIB1").setCondition("$1 > 0", builder.newDataSource("NEOPLASM_NUMBER_SIBLINGS", OPEN_N, OPEN_N));
    builder.inPage("19").withQuestion("NEOPLASM_TYPE_SIB2", "34", DropDownQuestionPanelFactory.class);
    buildNeoplasmList(builder, "NEOPLASM_TYPE_SIB2", "mixte");
    builder.inQuestion("NEOPLASM_TYPE_SIB2").withSharedCategory(OTHER, "22");
    builder.inQuestion("NEOPLASM_TYPE_SIB2").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_TYPE_SIB2").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_TYPE_SIB2").setCondition("$1 > 1", builder.newDataSource("NEOPLASM_NUMBER_SIBLINGS", OPEN_N, OPEN_N));
    builder.inPage("19").withQuestion("NEOPLASM_OTHER_TYPE_SIB2", "35").withSharedCategory(OPEN_AN);
    builder.inQuestion("NEOPLASM_OTHER_TYPE_SIB2").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_SIB2").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_SIB2").setCondition("NEOPLASM_TYPE_SIB2", OTHER);
    builder.inPage("19").withQuestion("NEOPLASM_AGE_SIB2", "36").withSharedCategory(OTHER_AGE);
    builder.inQuestion("NEOPLASM_AGE_SIB2").withSharedCategory(PNA, "888");
    builder.inQuestion("NEOPLASM_AGE_SIB2").withSharedCategory(DNK, "999");
    builder.inQuestion("NEOPLASM_AGE_SIB2").setCondition("$1 > 1", builder.newDataSource("NEOPLASM_NUMBER_SIBLINGS", OPEN_N, OPEN_N));
    builder.inPage("19").withQuestion("BIRTH_RANK_SIB2", "37").withSharedCategory(BIRTH_RANK);
    builder.inQuestion("BIRTH_RANK_SIB2").withSharedCategory(PNA, "88");
    builder.inQuestion("BIRTH_RANK_SIB2").withSharedCategory(DNK, "99");
    builder.inQuestion("BIRTH_RANK_SIB2").setCondition("$1 > 1", builder.newDataSource("NEOPLASM_NUMBER_SIBLINGS", OPEN_N, OPEN_N));

    builder.inSection("NEOPLASMS").withPage("20").withQuestion("FHD04");
    builder.inQuestion("FHD04").setCondition("$1 > 0", builder.newDataSource("NEOPLASM_NUMBER_CHILDREN", OPEN_N, OPEN_N));
    builder.inPage("20").withQuestion("NEOPLASM_TYPE_CHILD1", "38", DropDownQuestionPanelFactory.class);
    buildNeoplasmList(builder, "NEOPLASM_TYPE_CHILD1", "mixte");
    builder.inQuestion("NEOPLASM_TYPE_CHILD1").withSharedCategory(OTHER, "22");
    builder.inQuestion("NEOPLASM_TYPE_CHILD1").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_TYPE_CHILD1").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_TYPE_CHILD1").setCondition("$1 > 0", builder.newDataSource("NEOPLASM_NUMBER_CHILDREN", OPEN_N, OPEN_N));
    builder.inPage("20").withQuestion("NEOPLASM_OTHER_TYPE_CHILD1", "39").withSharedCategory(OPEN_AN);
    builder.inQuestion("NEOPLASM_OTHER_TYPE_CHILD1").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_CHILD1").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_CHILD1").setCondition("NEOPLASM_TYPE_CHILD1", OTHER);
    builder.inPage("20").withQuestion("NEOPLASM_AGE_CHILD1", "40").withSharedCategory(OTHER_AGE);
    builder.inQuestion("NEOPLASM_AGE_CHILD1").withSharedCategory(PNA, "888");
    builder.inQuestion("NEOPLASM_AGE_CHILD1").withSharedCategory(DNK, "999");
    builder.inQuestion("NEOPLASM_AGE_CHILD1").setCondition("$1 > 0", builder.newDataSource("NEOPLASM_NUMBER_CHILDREN", OPEN_N, OPEN_N));
    builder.inPage("20").withQuestion("BIRTH_RANK_CHILD1", "41").withSharedCategory(BIRTH_RANK);
    builder.inQuestion("BIRTH_RANK_CHILD1").withSharedCategory(PNA, "88");
    builder.inQuestion("BIRTH_RANK_CHILD1").withSharedCategory(DNK, "99");
    builder.inQuestion("BIRTH_RANK_CHILD1").setCondition("$1 > 0", builder.newDataSource("NEOPLASM_NUMBER_CHILDREN", OPEN_N, OPEN_N));
    builder.inPage("20").withQuestion("NEOPLASM_TYPE_CHILD2", "42", DropDownQuestionPanelFactory.class);
    buildNeoplasmList(builder, "NEOPLASM_TYPE_CHILD2", "mixte");
    builder.inQuestion("NEOPLASM_TYPE_CHILD2").withSharedCategory(OTHER, "22");
    builder.inQuestion("NEOPLASM_TYPE_CHILD2").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_TYPE_CHILD2").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_TYPE_CHILD2").setCondition("$1 > 1", builder.newDataSource("NEOPLASM_NUMBER_CHILDREN", OPEN_N, OPEN_N));
    builder.inPage("20").withQuestion("NEOPLASM_OTHER_TYPE_CHILD2", "43").withSharedCategory(OPEN_AN);
    builder.inQuestion("NEOPLASM_OTHER_TYPE_CHILD2").withSharedCategory(PNA, "88");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_CHILD2").withSharedCategory(DNK, "99");
    builder.inQuestion("NEOPLASM_OTHER_TYPE_CHILD2").setCondition("NEOPLASM_TYPE_CHILD2", OTHER);
    builder.inPage("20").withQuestion("NEOPLASM_AGE_CHILD2", "44").withSharedCategory(OTHER_AGE);
    builder.inQuestion("NEOPLASM_AGE_CHILD2").withSharedCategory(PNA, "888");
    builder.inQuestion("NEOPLASM_AGE_CHILD2").withSharedCategory(DNK, "999");
    builder.inQuestion("NEOPLASM_AGE_CHILD2").setCondition("$1 > 1", builder.newDataSource("NEOPLASM_NUMBER_CHILDREN", OPEN_N, OPEN_N));
    builder.inPage("20").withQuestion("BIRTH_RANK_CHILD2", "45").withSharedCategory(BIRTH_RANK);
    builder.inQuestion("BIRTH_RANK_CHILD2").withSharedCategory(PNA, "88");
    builder.inQuestion("BIRTH_RANK_CHILD2").withSharedCategory(DNK, "99");
    builder.inQuestion("BIRTH_RANK_CHILD2").setCondition("$1 > 1", builder.newDataSource("NEOPLASM_NUMBER_CHILDREN", OPEN_N, OPEN_N));

    return builder;
  }

  private static void buildNeoplasmList(QuestionnaireBuilder builder, String questionName, String sex) {

    if(sex.equals("male")) {
      builder.inQuestion(questionName).withSharedCategory("PROSTATE", "5");
    } else {
      builder.inQuestion(questionName).withSharedCategory("BREAST", "1");
    }

    builder.inQuestion(questionName).withSharedCategory("COLON", "2");
    builder.inQuestion(questionName).withSharedCategory("LUNG", "3");
    builder.inQuestion(questionName).withSharedCategory("LIVER", "4");

    if(sex.equals("mixte")) builder.inQuestion(questionName).withSharedCategory("PROSTATE", "5");
    if(!sex.equals("male")) builder.inQuestion(questionName).withSharedCategory("OVARY", "6");

    builder.inQuestion(questionName).withSharedCategory("PANCREAS", "7");
    builder.inQuestion(questionName).withSharedCategory("STOMACH", "8");
    builder.inQuestion(questionName).withSharedCategory("OESOPHAGUS", "9");
    builder.inQuestion(questionName).withSharedCategory("LARYNX", "10");
    builder.inQuestion(questionName).withSharedCategory("TRACHEA", "11");
    builder.inQuestion(questionName).withSharedCategory("RECTUM", "12");
    builder.inQuestion(questionName).withSharedCategory("SKIN", "13");

    if(!sex.equals("male")) {
      builder.inQuestion(questionName).withSharedCategory("CERVIX", "14");
      builder.inQuestion(questionName).withSharedCategory("UTERUS", "15");
    }

    builder.inQuestion(questionName).withSharedCategory("KIDNEY", "16");
    builder.inQuestion(questionName).withSharedCategory("BLADDER", "17");
    builder.inQuestion(questionName).withSharedCategory("BRAIN", "18");
    builder.inQuestion(questionName).withSharedCategory("THYROID", "19");
    builder.inQuestion(questionName).withSharedCategory("NON_HODGKIN", "20");
    builder.inQuestion(questionName).withSharedCategory("LEUKAEMIA", "21");

    if(sex.equals("male")) builder.inQuestion(questionName).withSharedCategory("BREAST", "1");

  }
}
