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
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.ArithmeticOperationSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.CurrentYearSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.FixedSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ConditionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.DropDownQuestionPanelFactory;
import org.obiba.onyx.util.data.ArithmeticOperator;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Returns the content for the Self Administered Health Questionnaire
 */
public class QuartzDemoQuestionnaireContentBuilder {

  private static final String N = "N";

  private static final String Y = "Y";

  private static final String PNA = "PNA";

  private static final String DNK = "DNK";

  private static final String AGE = "AGE";

  private static final String YEAR = "YEAR";

  private static final String OPEN_0_PARTICIPANT = "OPEN_0_PARTICIPANT";

  private static final String ELSEWHERE = "ELSEWHERE";

  private static final String OPEN_N = "OPEN_N";

  private static final String OPEN_AN = "OPEN_AN";

  // Frequency static variables

  private static final String NEVER = "NEVER";

  private static final String SOMETIMES = "SOMETIMES";

  private static final String OFTEN = "OFTEN";

  private static final String ALWAYS = "ALWAYS";

  private static final String TYPE = "TYPE";

  private static final String PACKAGE = "PACKAGE";

  private static final String LIST = "LIST";

  private static final String PARTICIPANT = "PARTICIPANT";

  public static QuestionnaireBuilder buildHealthQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("QuartzDemoQuestionnaire", "1.0");

    builder.withSection("SECTION_GEN").withPage("1").withQuestion("S1_BP1");

    builder.withSection("QUESTION_TYPES").withSection("RADIO_BUTTONS").withPage("2").withQuestion("MARITAL_STATUS").setRowCount(7);
    builder.inQuestion("MARITAL_STATUS").withCategory("MARRIED").setExportName("1");
    builder.inQuestion("MARITAL_STATUS").withCategory("DIVORCED").setExportName("2");
    builder.inQuestion("MARITAL_STATUS").withCategory("SEPARATED").setExportName("3");
    builder.inQuestion("MARITAL_STATUS").withCategory("WIDOWED").setExportName("4");
    builder.inQuestion("MARITAL_STATUS").withCategory("SINGLE").setExportName("5");
    builder.inQuestion("MARITAL_STATUS").withSharedCategory(PNA, "88").setEscape(true);
    builder.inQuestion("MARITAL_STATUS").withSharedCategory(DNK, "99").setEscape(true);

    builder.inSection("QUESTION_TYPES").withSection("OPT_INST_SPEC").withPage("3").withQuestion("INCOME_LAST_YEAR").withCategory("LESS_10000").setExportName("1");
    builder.inQuestion("INCOME_LAST_YEAR").withCategory("_10000_24999").setExportName("2");
    builder.inQuestion("INCOME_LAST_YEAR").withCategory("_25000_49999").setExportName("3");
    builder.inQuestion("INCOME_LAST_YEAR").withCategory("_50000_74999").setExportName("4");
    builder.inQuestion("INCOME_LAST_YEAR").withCategory("_75000_99999").setExportName("5");
    builder.inQuestion("INCOME_LAST_YEAR").withCategory("_100000_149999").setExportName("6");
    builder.inQuestion("INCOME_LAST_YEAR").withCategory("_150000_200000").setExportName("7");
    builder.inQuestion("INCOME_LAST_YEAR").withCategory("MORE_200000").setExportName("8");
    builder.inQuestion("INCOME_LAST_YEAR").withSharedCategory(PNA, "88");
    builder.inQuestion("INCOME_LAST_YEAR").withSharedCategory(DNK, "99");

    builder.inSection("QUESTION_TYPES").withSection("MANY_Q_PER_SCREEN").withPage("4").withQuestion("STRESS_WORK_PAST_YEAR").setRowCount(6);
    builder.inQuestion("STRESS_WORK_PAST_YEAR").withSharedCategory(NEVER, "1");
    builder.inQuestion("STRESS_WORK_PAST_YEAR").withSharedCategory(SOMETIMES, "2");
    builder.inQuestion("STRESS_WORK_PAST_YEAR").withSharedCategory(OFTEN, "3");
    builder.inQuestion("STRESS_WORK_PAST_YEAR").withSharedCategory(ALWAYS, "4");
    builder.inQuestion("STRESS_WORK_PAST_YEAR").withSharedCategory(PNA, "88");
    builder.inQuestion("STRESS_WORK_PAST_YEAR").withSharedCategory(DNK, "99");
    builder.inPage("4").withQuestion("STRESS_HOME_PAST_YEAR").setRowCount(6);
    builder.inQuestion("STRESS_HOME_PAST_YEAR").withSharedCategory(NEVER, "1");
    builder.inQuestion("STRESS_HOME_PAST_YEAR").withSharedCategory(SOMETIMES, "2");
    builder.inQuestion("STRESS_HOME_PAST_YEAR").withSharedCategory(OFTEN, "3");
    builder.inQuestion("STRESS_HOME_PAST_YEAR").withSharedCategory(ALWAYS, "4");
    builder.inQuestion("STRESS_HOME_PAST_YEAR").withSharedCategory(PNA, "88");
    builder.inQuestion("STRESS_HOME_PAST_YEAR").withSharedCategory(DNK, "99");

    builder.inSection("QUESTION_TYPES").withSection("MULTI_SELECT").withPage("5").withQuestion("FIRST_LANGUAGE_LEARNED", true).setAnswerCount(1, 4).setRowCount(8);
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("ENGLISH").setExportName("1");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("FRENCH").setExportName("2");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("ARABIC").setExportName("3");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("CHINESE").setExportName("4");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("ABORIGINAL").setExportName("5");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("GERMAN").setExportName("6");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("GREEK").setExportName("7");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("HUNGARIAN").setExportName("8");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("ITALIAN").setExportName("9");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("KOREAN").setExportName("10");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("PERSIAN").setExportName("11");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("POLISH").setExportName("12");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("PORTUGUESE").setExportName("13");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("PUNJABI").setExportName("14");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("SPANISH").setExportName("15");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("TAGALOG").setExportName("16");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("UKRAINIAN").setExportName("17");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("VIETNAMESE").setExportName("18");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("DUTCH").setExportName("19");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("HINDI").setExportName("20");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("RUSSIAN").setExportName("21");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("TAMIL").setExportName("22");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("OTHER").setExportName("23");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withSharedCategory(PNA, "88");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withSharedCategory(DNK, "99");

    builder.inSection("QUESTION_TYPES").withSection("LIST_BOX").withPage("6").withQuestion("COUNTRY_BIRTH", DropDownQuestionPanelFactory.class).withCategory("CA").setExportName("124");
    builder.inQuestion("COUNTRY_BIRTH").withCategory("IT").setExportName("380");
    builder.inQuestion("COUNTRY_BIRTH").withCategory("FR").setExportName("250");
    builder.inQuestion("COUNTRY_BIRTH").withCategory("HT").setExportName("332");
    builder.inQuestion("COUNTRY_BIRTH").withCategory("LB").setExportName("422");
    builder.inQuestion("COUNTRY_BIRTH").withCategory("US").setExportName("840");
    builder.inQuestion("COUNTRY_BIRTH").withCategory("CN").setExportName("156");
    builder.inQuestion("COUNTRY_BIRTH").withCategory("VN").setExportName("704");
    builder.inQuestion("COUNTRY_BIRTH").withCategory("PT").setExportName("620");
    builder.inQuestion("COUNTRY_BIRTH").withCategory("GR").setExportName("300");
    builder.inQuestion("COUNTRY_BIRTH").withCategory("MA").setExportName("504");
    builder.inQuestion("COUNTRY_BIRTH").withCategory("GB").setExportName("826");
    builder.inQuestion("COUNTRY_BIRTH").withSharedCategory(ELSEWHERE, "7777").setEscape(true);
    builder.inQuestion("COUNTRY_BIRTH").withSharedCategory(PNA, "8888");
    builder.inQuestion("COUNTRY_BIRTH").withSharedCategory(DNK, "9999");

    builder.inSection("QUESTION_TYPES").withSection("OPEN_ANSWER").withPage("7").withQuestion("AGE_PRESENT_ADRESS").withSharedCategory(OPEN_N).withOpenAnswerDefinition(OPEN_N, DataType.INTEGER).setSize(2);
    builder.inQuestion("AGE_PRESENT_ADRESS").withSharedCategory(PNA, "88");
    builder.inQuestion("AGE_PRESENT_ADRESS").withSharedCategory(DNK, "99");

    builder.inSection("QUESTION_TYPES").withSection("EXCL_COMPOUND").withPage("8").withQuestion("TOOTH_BRUSHING_FREQ").withCategory("FREQ_DAY").withOpenAnswerDefinition("FREQ_DAY", DataType.INTEGER).addValidator(new NumberValidator.MinimumValidator(0)).setSize(1);
    builder.inQuestion("TOOTH_BRUSHING_FREQ").withCategory("FREQ_WEEK").withOpenAnswerDefinition("FREQ_WEEK", DataType.INTEGER).addValidator(new NumberValidator.MinimumValidator(0)).setSize(2);
    builder.inQuestion("TOOTH_BRUSHING_FREQ").withSharedCategory(PNA, "88");
    builder.inQuestion("TOOTH_BRUSHING_FREQ").withSharedCategory(DNK, "99");
    builder.inPage("8").withQuestion("WEIGHT_BIRTH").withCategory("WEIGHT_BIRTH").withOpenAnswerDefinition("WEIGHT_BIRTH", DataType.INTEGER).withOpenAnswerDefinition("WEIGHT_BIRTH_LBS", DataType.INTEGER).addValidator(new NumberValidator.MinimumValidator(0)).setSize(2);
    builder.inOpenAnswerDefinition("WEIGHT_BIRTH").withOpenAnswerDefinition("WEIGHT_BIRTH_OUNCES", DataType.INTEGER).addValidator(new NumberValidator.MinimumValidator(0)).setSize(2);
    builder.inQuestion("WEIGHT_BIRTH").withSharedCategory(PNA, "88");
    builder.inQuestion("WEIGHT_BIRTH").withSharedCategory(DNK, "99");

    builder.inSection("QUESTION_TYPES").withSection("MULTI_PARTS").withPage("9").withQuestion("DATE_OF_BIRTH").withQuestion("DOB_YEAR").withCategory("DOB_YEAR").withOpenAnswerDefinition("DOB_YEAR", DataType.INTEGER).addValidator(ComparisonOperator.le, new ArithmeticOperationSource(new CurrentYearSource(), ArithmeticOperator.minus, new FixedSource(DataBuilder.buildInteger(40)))).addValidator(ComparisonOperator.ge, new ArithmeticOperationSource(new CurrentYearSource(), ArithmeticOperator.minus, new FixedSource(DataBuilder.buildInteger(70)))).setSize(4);
    builder.inQuestion("DOB_YEAR").withSharedCategory(PNA, "88");
    builder.inQuestion("DOB_YEAR").withSharedCategory(DNK, "99");
    builder.inQuestion("DATE_OF_BIRTH").withQuestion("DOB_MONTH", DropDownQuestionPanelFactory.class).withCategory("01").setExportName("1");
    builder.inQuestion("DOB_MONTH").withCategory("02").setExportName("2");
    builder.inQuestion("DOB_MONTH").withCategory("03").setExportName("3");
    builder.inQuestion("DOB_MONTH").withCategory("04").setExportName("4");
    builder.inQuestion("DOB_MONTH").withCategory("05").setExportName("5");
    builder.inQuestion("DOB_MONTH").withCategory("06").setExportName("6");
    builder.inQuestion("DOB_MONTH").withCategory("07").setExportName("7");
    builder.inQuestion("DOB_MONTH").withCategory("08").setExportName("8");
    builder.inQuestion("DOB_MONTH").withCategory("09").setExportName("9");
    builder.inQuestion("DOB_MONTH").withCategory("10").setExportName("10");
    builder.inQuestion("DOB_MONTH").withCategory("11").setExportName("11");
    builder.inQuestion("DOB_MONTH").withCategory("12").setExportName("12");
    builder.inQuestion("DOB_MONTH").withSharedCategory(PNA, "88");
    builder.inQuestion("DOB_MONTH").withSharedCategory(DNK, "99");

    builder.inSection("QUESTION_TYPES").withSection("SHARED_CAT").withPage("10").withQuestion("ALLERGIES_KIND").withSharedCategory(N, "0");
    builder.inQuestion("ALLERGIES_KIND").withSharedCategory(Y, "1");
    builder.inQuestion("ALLERGIES_KIND").withSharedCategory(PNA, "8");
    builder.inQuestion("ALLERGIES_KIND").withSharedCategory(DNK, "9");
    builder.inQuestion("ALLERGIES_KIND").withQuestion("ALLERGY_ANIMALS", "1");
    builder.inQuestion("ALLERGIES_KIND").withQuestion("ALLERGY_FOOD", "2");
    builder.inQuestion("ALLERGIES_KIND").withQuestion("ALLERGY_MEDICATION", "3");
    builder.inQuestion("ALLERGIES_KIND").withQuestion("ALLERGY_POLLEN", "4");
    builder.inQuestion("ALLERGIES_KIND").withQuestion("ALLERGY_METAL", "5");
    builder.inQuestion("ALLERGIES_KIND").withQuestion("ALLERGY_INSECT_BITES", "6");
    builder.inQuestion("ALLERGIES_KIND").withQuestion("ALLERGY_LATEX", "7");

    builder.withSection("ANS_VALIDATION").withPage("11").withQuestion("VAL_BP");

    builder.inSection("ANS_VALIDATION").withSection("VALUES_RANGE").withPage("12").withQuestion("TRAVEL_WALK_DAYS_WEEK").withCategory("DAYS_WEEK").withOpenAnswerDefinition("DAYS_WEEK", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 7)).setSize(2);
    builder.inQuestion("TRAVEL_WALK_DAYS_WEEK").withSharedCategory(PNA, "8");
    builder.inQuestion("TRAVEL_WALK_DAYS_WEEK").withSharedCategory(DNK, "9");

    builder.inSection("ANS_VALIDATION").withSection("VALUES_FORMAT").withPage("13").withQuestion("POSTAL_CODE").withCategory("POSTAL_CODE3").withOpenAnswerDefinition("POSTAL_CODE3", DataType.TEXT).addValidator(new PatternValidator("[a-zA-Z]\\d[a-zA-Z]")).setSize(3);
    builder.inQuestion("POSTAL_CODE").withSharedCategory(PNA, "888");
    builder.inQuestion("POSTAL_CODE").withSharedCategory(DNK, "999");

    builder.inSection("ANS_VALIDATION").withSection("X_VAL").withPage("14").withQuestion("PARTICIPANT_AGE").withCategory("PARTICIPANT_AGE").withOpenAnswerDefinition("PARTICIPANT_AGE", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(40, 70)).setSize(2);
    builder.inQuestion("PARTICIPANT_AGE").withSharedCategory(PNA, "88");
    builder.inQuestion("PARTICIPANT_AGE").withSharedCategory(DNK, "99");
    builder.inPage("14").withQuestion("STARTED_SMOKING_AGE").withSharedCategory(OPEN_0_PARTICIPANT).withOpenAnswerDefinition(OPEN_0_PARTICIPANT, DataType.INTEGER).addValidator(new NumberValidator.MinimumValidator(0)).addValidator(ComparisonOperator.le, "PARTICIPANT_AGE", "PARTICIPANT_AGE", "PARTICIPANT_AGE").setSize(2);
    builder.inQuestion("STARTED_SMOKING_AGE").withSharedCategory(PNA, "88");
    builder.inQuestion("STARTED_SMOKING_AGE").withSharedCategory(DNK, "99");

    builder.withSection("SKIP_PATTERNS").withPage("15").withQuestion("SKIP_BP");
    builder.inSection("SKIP_PATTERNS").withSection("HIDDEN_QS").withPage("16").withQuestion("PREGNANT").withSharedCategory(N, "0");
    builder.inQuestion("PREGNANT").withSharedCategory(Y, "1");
    builder.inQuestion("PREGNANT").withSharedCategory(PNA, "8");
    builder.inQuestion("PREGNANT").withSharedCategory(DNK, "9");
    builder.inPage("16").withQuestion("WEEK_PREGNANCY").withCategory("OPEN_40").withOpenAnswerDefinition("OPEN_40", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 40)).setSize(2);
    builder.inQuestion("WEEK_PREGNANCY").setAnswerCondition("WEEK_PREGNANCY_ACONDITION", "PREGNANT", Y);

    builder.inSection("SKIP_PATTERNS").withSection("DIFF_SCREENS").withPage("17").withQuestion("EVER_DRUNK_ALCOHOL").withSharedCategory(N, "0");
    builder.inQuestion("EVER_DRUNK_ALCOHOL").withSharedCategory(Y, "1");
    builder.inQuestion("EVER_DRUNK_ALCOHOL").withSharedCategory(PNA, "8");
    builder.inQuestion("EVER_DRUNK_ALCOHOL").withSharedCategory(DNK, "9");

    builder.inSection("DIFF_SCREENS").withPage("18").withQuestion("ALCOHOL_FREQUENCY").setAnswerCondition("ALCOHOL_FREQUENCY_ACONDITION", "EVER_DRUNK_ALCOHOL", Y);
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory("DAILY", "7");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory("_4TO5_WEEK", "6");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory("_2TO3_WEEK", "5");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory("WEEKLY", "4");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory("_2TO3_MONTH", "3");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory("MONTHLY", "2");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory("LESS_MONTHLY", "1");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(NEVER, "0");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(PNA, "88");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(DNK, "99");

    builder.inSection("SKIP_PATTERNS").withSection("WITH_TABLES").withPage("19").withQuestion("EVER_USED").withSharedCategory(N, "0");
    builder.inQuestion("EVER_USED").withSharedCategory(Y, "1");
    builder.inQuestion("EVER_USED").withSharedCategory(PNA, "8");
    builder.inQuestion("EVER_USED").withSharedCategory(DNK, "9");
    builder.inQuestion("EVER_USED").withQuestion("CIGARS_EVER", "1");
    builder.inQuestion("EVER_USED").withQuestion("SMALL_CIGARS_EVER", "2");
    builder.inQuestion("EVER_USED").withQuestion("PIPES_EVER", "3");
    builder.inQuestion("EVER_USED").withQuestion("CHEWING_TOBACCO_SNUFF_EVER", "4");
    builder.inQuestion("EVER_USED").withQuestion("BETEL_NUT_EVER", "5");
    builder.inQuestion("EVER_USED").withQuestion("PAAN_EVER", "6");
    builder.inQuestion("EVER_USED").withQuestion("SHEESHA_EVER", "7");
    builder.inQuestion("EVER_USED").withQuestion("OTHER_NICOTIN_PRODUCT_EVER", "8");

    builder.inSection("WITH_TABLES").withPage("20").withQuestion("CURRENTLY_USE").withSharedCategory(N, "0");
    builder.inQuestion("CURRENTLY_USE").withSharedCategory(Y, "1");
    builder.inQuestion("CURRENTLY_USE").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENTLY_USE").withSharedCategory(DNK, "9");

    builder.inQuestion("CURRENTLY_USE").setMultipleCondition("CURRENTLY_USE_MCONDITION", ConditionOperator.OR).withAnswerCondition("CURRENTLY_USE_ACONDITION_0", "CIGARS_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_1", "SMALL_CIGARS_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_2", "PIPES_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_3", "CHEWING_TOBACCO_SNUFF_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_4", "BETEL_NUT_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_5", "PAAN_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_6", "SHEESHA_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_7", "OTHER_NICOTIN_PRODUCT_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("CIGARS_CURRENT", "1");
    builder.inQuestion("CIGARS_CURRENT").setAnswerCondition("CIGARS_CURRENT_ACONDITION", "CIGARS_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("SMALL_CIGARS_CURRENT", "2");
    builder.inQuestion("SMALL_CIGARS_CURRENT").setAnswerCondition("SMALL_CIGARS_CURRENT_ACONDITION", "SMALL_CIGARS_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("PIPES_CURRENT", "3");
    builder.inQuestion("PIPES_CURRENT").setAnswerCondition("PIPES_CURRENT_ACONDITION", "PIPES_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("CHEWING_TOBACCO_SNUFF_CURRENT", "4");
    builder.inQuestion("CHEWING_TOBACCO_SNUFF_CURRENT").setAnswerCondition("CHEWING_TOBACCO_SNUFF_CURRENT_ACONDITION", "CHEWING_TOBACCO_SNUFF_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("BETEL_NUT_CURRENT", "5");
    builder.inQuestion("BETEL_NUT_CURRENT").setAnswerCondition("BETEL_NUT_CURRENT_ACONDITION", "BETEL_NUT_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("PAAN_CURRENT", "6");
    builder.inQuestion("PAAN_CURRENT").setAnswerCondition("PAAN_CURRENT_ACONDITION", "PAAN_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("SHEESHA_CURRENT", "7");
    builder.inQuestion("SHEESHA_CURRENT").setAnswerCondition("SHEESHA_CURRENT_ACONDITION", "SHEESHA_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("OTHER_NICOTIN_PRODUCT_CURRENT", "8");
    builder.inQuestion("OTHER_NICOTIN_PRODUCT_CURRENT").setAnswerCondition("OTHER_NICOTIN_PRODUCT_CURRENT_ACONDITION", "OTHER_NICOTIN_PRODUCT_EVER", Y);

    builder.inSection("SKIP_PATTERNS").withSection("SK_MULTI_SEL").withPage("21").withQuestion("CARDIOVASCULAR_CONDITIONS", true).setRowCount(6);
    builder.inQuestion("CARDIOVASCULAR_CONDITIONS").withCategories("HIGH_BP", "HIGH_BLOOD_CHOL", "HEART_ATTACK", "ANGINA", "NONE");
    builder.inQuestion("CARDIOVASCULAR_CONDITIONS").withSharedCategory(PNA, "88");
    builder.inQuestion("CARDIOVASCULAR_CONDITIONS").withSharedCategory(DNK, "99");

    builder.inSection("SK_MULTI_SEL").withPage("22").withQuestion("AGE_HIGH_BP").setAnswerCondition("AGE_HIGH_BP_ACONDITION", "CARDIOVASCULAR_CONDITIONS", "HIGH_BP");
    builder.inQuestion("AGE_HIGH_BP").withSharedCategory(OPEN_0_PARTICIPANT);
    builder.inQuestion("AGE_HIGH_BP").withSharedCategory(PNA, "88");
    builder.inQuestion("AGE_HIGH_BP").withSharedCategory(DNK, "99");
    builder.inPage("22").withQuestion("MED_HIGH_BP").setAnswerCondition("MED_HIGH_BP_ACONDITION", "CARDIOVASCULAR_CONDITIONS", "HIGH_BP");
    builder.inQuestion("MED_HIGH_BP").withSharedCategory(N, "0");
    builder.inQuestion("MED_HIGH_BP").withSharedCategory(Y, "1");
    builder.inQuestion("MED_HIGH_BP").withSharedCategory(PNA, "8");
    builder.inQuestion("MED_HIGH_BP").withSharedCategory(DNK, "9");

    builder.inSection("SK_MULTI_SEL").withPage("23").withQuestion("AGE_HIGH_BLOOD_CHOL").setAnswerCondition("AGE_HIGH_BLOOD_CHOL_ACONDITION", "CARDIOVASCULAR_CONDITIONS", "HIGH_BLOOD_CHOL");
    builder.inQuestion("AGE_HIGH_BLOOD_CHOL").withSharedCategory(OPEN_0_PARTICIPANT);
    builder.inQuestion("AGE_HIGH_BLOOD_CHOL").withSharedCategory(PNA, "88");
    builder.inQuestion("AGE_HIGH_BLOOD_CHOL").withSharedCategory(DNK, "99");
    builder.inPage("23").withQuestion("MED_HIGH_BLOOD_CHOL").setAnswerCondition("MED_HIGH_BLOOD_CHOL_ACONDITION", "CARDIOVASCULAR_CONDITIONS", "HIGH_BLOOD_CHOL");
    builder.inQuestion("MED_HIGH_BLOOD_CHOL").withSharedCategory(N, "0");
    builder.inQuestion("MED_HIGH_BLOOD_CHOL").withSharedCategory(Y, "1");
    builder.inQuestion("MED_HIGH_BLOOD_CHOL").withSharedCategory(PNA, "8");
    builder.inQuestion("MED_HIGH_BLOOD_CHOL").withSharedCategory(DNK, "9");

    builder.inSection("SK_MULTI_SEL").withPage("24").withQuestion("AGE_HEART_ATTACK").setAnswerCondition("AGE_HEART_ATTACK_ACONDITION", "CARDIOVASCULAR_CONDITIONS", "HEART_ATTACK");
    builder.inQuestion("AGE_HEART_ATTACK").withSharedCategory(OPEN_0_PARTICIPANT);
    builder.inQuestion("AGE_HEART_ATTACK").withSharedCategory(PNA, "88");
    builder.inQuestion("AGE_HEART_ATTACK").withSharedCategory(DNK, "99");
    builder.inPage("24").withQuestion("MED_HEART_ATTACK").setAnswerCondition("MED_HEART_ATTACK_ACONDITION", "CARDIOVASCULAR_CONDITIONS", "HEART_ATTACK");
    builder.inQuestion("MED_HEART_ATTACK").withSharedCategory(N, "0");
    builder.inQuestion("MED_HEART_ATTACK").withSharedCategory(Y, "1");
    builder.inQuestion("MED_HEART_ATTACK").withSharedCategory(PNA, "8");
    builder.inQuestion("MED_HEART_ATTACK").withSharedCategory(DNK, "9");

    builder.inSection("SK_MULTI_SEL").withPage("25").withQuestion("AGE_ANGINA").setAnswerCondition("AGE_ANGINA_ACONDITION", "CARDIOVASCULAR_CONDITIONS", "ANGINA");
    builder.inQuestion("AGE_ANGINA").withSharedCategory(OPEN_0_PARTICIPANT);
    builder.inQuestion("AGE_ANGINA").withSharedCategory(PNA, "88");
    builder.inQuestion("AGE_ANGINA").withSharedCategory(DNK, "99");
    builder.inPage("25").withQuestion("MED_ANGINA").setAnswerCondition("MED_ANGINA_ACONDITION", "CARDIOVASCULAR_CONDITIONS", "ANGINA");
    builder.inQuestion("MED_ANGINA").withSharedCategory(N, "0");
    builder.inQuestion("MED_ANGINA").withSharedCategory(Y, "1");
    builder.inQuestion("MED_ANGINA").withSharedCategory(PNA, "8");
    builder.inQuestion("MED_ANGINA").withSharedCategory(DNK, "9");

    builder.inSection("SKIP_PATTERNS").withSection("A_PRIORI_VALUE").withPage("27").withQuestion("RX_MEDICATION").withCategory("MED_NUMBER").withOpenAnswerDefinition("MED_NUMBER", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(0, 4)).setSize(2);
    builder.inQuestion("RX_MEDICATION").withSharedCategory(PNA, "88");
    builder.inQuestion("RX_MEDICATION").withSharedCategory(DNK, "99");

    builder.inSection("A_PRIORI_VALUE").withPage("28").withQuestion("MEDICATION1_TYPE").withSharedCategory(TYPE).withOpenAnswerDefinition(TYPE, DataType.TEXT).setSize(50);
    builder.inQuestion("MEDICATION1_TYPE").withSharedCategory(PNA, "88");
    builder.inQuestion("MEDICATION1_TYPE").withSharedCategory(DNK, "99");
    builder.inQuestion("MEDICATION1_TYPE").setDataCondition("MEDICATION1_TYPE_DCONDITION", "RX_MEDICATION", "MED_NUMBER", "MED_NUMBER", ComparisonOperator.gt, DataBuilder.buildInteger(0l));
    builder.inPage("28").withQuestion("MED1_TYPE_INFO_SOURCE").withSharedCategory(PACKAGE, "0");
    builder.inQuestion("MED1_TYPE_INFO_SOURCE").withSharedCategory(LIST, "1");
    builder.inQuestion("MED1_TYPE_INFO_SOURCE").withSharedCategory(PARTICIPANT, "2");
    builder.inQuestion("MED1_TYPE_INFO_SOURCE").withSharedCategory(DNK, "9");
    builder.inQuestion("MED1_TYPE_INFO_SOURCE").setDataCondition("MED1_TYPE_INFO_SOURCE_DCONDITION", "RX_MEDICATION", "MED_NUMBER", "MED_NUMBER", ComparisonOperator.gt, DataBuilder.buildInteger(0l));

    builder.inSection("A_PRIORI_VALUE").withPage("29").withQuestion("MEDICATION2_TYPE").withSharedCategory(TYPE);
    builder.inQuestion("MEDICATION2_TYPE").withSharedCategory(PNA, "88");
    builder.inQuestion("MEDICATION2_TYPE").withSharedCategory(DNK, "99");
    builder.inQuestion("MEDICATION2_TYPE").setDataCondition("MEDICATION2_TYPE_DCONDITION", "RX_MEDICATION", "MED_NUMBER", "MED_NUMBER", ComparisonOperator.gt, DataBuilder.buildInteger(1l));
    builder.inPage("29").withQuestion("MED2_TYPE_INFO_SOURCE").withSharedCategory(PACKAGE, "0");
    builder.inQuestion("MED2_TYPE_INFO_SOURCE").withSharedCategory(LIST, "1");
    builder.inQuestion("MED2_TYPE_INFO_SOURCE").withSharedCategory(PARTICIPANT, "2");
    builder.inQuestion("MED2_TYPE_INFO_SOURCE").withSharedCategory(DNK, "9");
    builder.inQuestion("MED2_TYPE_INFO_SOURCE").setDataCondition("MED2_TYPE_INFO_SOURCE_DCONDITION", "RX_MEDICATION", "MED_NUMBER", "MED_NUMBER", ComparisonOperator.gt, DataBuilder.buildInteger(1l));

    builder.inSection("A_PRIORI_VALUE").withPage("30").withQuestion("MEDICATION3_TYPE").withSharedCategory(TYPE);
    builder.inQuestion("MEDICATION3_TYPE").withSharedCategory(PNA, "88");
    builder.inQuestion("MEDICATION3_TYPE").withSharedCategory(DNK, "99");
    builder.inQuestion("MEDICATION3_TYPE").setDataCondition("MEDICATION3_TYPE_DCONDITION", "RX_MEDICATION", "MED_NUMBER", "MED_NUMBER", ComparisonOperator.gt, DataBuilder.buildInteger(2l));
    builder.inPage("30").withQuestion("MED3_TYPE_INFO_SOURCE").withSharedCategory(PACKAGE, "0");
    builder.inQuestion("MED3_TYPE_INFO_SOURCE").withSharedCategory(LIST, "1");
    builder.inQuestion("MED3_TYPE_INFO_SOURCE").withSharedCategory(PARTICIPANT, "2");
    builder.inQuestion("MED3_TYPE_INFO_SOURCE").withSharedCategory(DNK, "9");
    builder.inQuestion("MED3_TYPE_INFO_SOURCE").setDataCondition("MED3_TYPE_INFO_SOURCE_DCONDITION", "RX_MEDICATION", "MED_NUMBER", "MED_NUMBER", ComparisonOperator.gt, DataBuilder.buildInteger(2l));

    builder.inSection("A_PRIORI_VALUE").withPage("31").withQuestion("MEDICATION4_TYPE").withSharedCategory(TYPE);
    builder.inQuestion("MEDICATION4_TYPE").withSharedCategory(PNA, "88");
    builder.inQuestion("MEDICATION4_TYPE").withSharedCategory(DNK, "99");
    builder.inQuestion("MEDICATION4_TYPE").setDataCondition("MEDICATION4_TYPE_DCONDITION", "RX_MEDICATION", "MED_NUMBER", "MED_NUMBER", ComparisonOperator.gt, DataBuilder.buildInteger(3l));
    builder.inPage("31").withQuestion("MED4_TYPE_INFO_SOURCE").withSharedCategory(PACKAGE, "0");
    builder.inQuestion("MED4_TYPE_INFO_SOURCE").withSharedCategory(LIST, "1");
    builder.inQuestion("MED4_TYPE_INFO_SOURCE").withSharedCategory(PARTICIPANT, "2");
    builder.inQuestion("MED4_TYPE_INFO_SOURCE").withSharedCategory(DNK, "9");
    builder.inQuestion("MED4_TYPE_INFO_SOURCE").setDataCondition("MED4_TYPE_INFO_SOURCE_DCONDITION", "RX_MEDICATION", "MED_NUMBER", "MED_NUMBER", ComparisonOperator.gt, DataBuilder.buildInteger(3l));

    builder.inSection("SKIP_PATTERNS").withSection("A_POSTERIORI_VALUE").withPage("32").withQuestion("MAJOR_OPERATION1OCC").withSharedCategory(N, "0");
    builder.inQuestion("MAJOR_OPERATION1OCC").withSharedCategory(Y, "1");
    builder.inQuestion("MAJOR_OPERATION1OCC").withSharedCategory(PNA, "8");
    builder.inQuestion("MAJOR_OPERATION1OCC").withSharedCategory(DNK, "9");

    builder.inSection("A_POSTERIORI_VALUE").withPage("33").withQuestion("MAJOR_OPERATION1TYPE").setAnswerCondition("MAJOR_OPERATION1TYPE_ACONDITION", "MAJOR_OPERATION1OCC", Y);
    builder.inQuestion("MAJOR_OPERATION1TYPE").withSharedCategory(OPEN_AN).withOpenAnswerDefinition(OPEN_AN, DataType.TEXT);
    builder.inQuestion("MAJOR_OPERATION1TYPE").withSharedCategory(PNA, "8");
    builder.inQuestion("MAJOR_OPERATION1TYPE").withSharedCategory(DNK, "9");
    builder.inPage("33").withQuestion("MAJOR_OPERATION1AGE").setAnswerCondition("MAJOR_OPERATION1AGE_ACONDITION", "MAJOR_OPERATION1OCC", Y);
    builder.inQuestion("MAJOR_OPERATION1AGE").withSharedCategory(AGE).withOpenAnswerDefinition(AGE, DataType.INTEGER).addValidator(new NumberValidator.MinimumValidator(0)).addValidator(ComparisonOperator.le, "PARTICIPANT_AGE", "PARTICIPANT_AGE", "PARTICIPANT_AGE").setSize(2);
    builder.inQuestion("MAJOR_OPERATION1AGE").withSharedCategory(YEAR).withOpenAnswerDefinition(YEAR, DataType.INTEGER).addValidator(ComparisonOperator.ge, "DOB_YEAR", "DOB_YEAR", "DOB_YEAR").addCurrentYearValidator(ComparisonOperator.le).setSize(4);
    builder.inQuestion("MAJOR_OPERATION1AGE").withSharedCategory(PNA, "8888");
    builder.inQuestion("MAJOR_OPERATION1AGE").withSharedCategory(DNK, "9999");
    builder.inPage("33").withQuestion("MAJOR_OPERATION2OCC").setAnswerCondition("MAJOR_OPERATION2OCC_ACONDITION", "MAJOR_OPERATION1OCC", Y);
    builder.inQuestion("MAJOR_OPERATION2OCC").withSharedCategory(N, "0");
    builder.inQuestion("MAJOR_OPERATION2OCC").withSharedCategory(Y, "1");
    builder.inQuestion("MAJOR_OPERATION2OCC").withSharedCategory(PNA, "8");
    builder.inQuestion("MAJOR_OPERATION2OCC").withSharedCategory(DNK, "9");

    builder.inSection("A_POSTERIORI_VALUE").withPage("34").withQuestion("MAJOR_OPERATION2TYPE").setAnswerCondition("MAJOR_OPERATION2TYPE_ACONDITION", "MAJOR_OPERATION2OCC", Y);
    builder.inQuestion("MAJOR_OPERATION2TYPE").withSharedCategory(OPEN_AN);
    builder.inQuestion("MAJOR_OPERATION2TYPE").withSharedCategory(PNA, "8");
    builder.inQuestion("MAJOR_OPERATION2TYPE").withSharedCategory(DNK, "9");
    builder.inPage("34").withQuestion("MAJOR_OPERATION2AGE").setAnswerCondition("MAJOR_OPERATION2AGE_ACONDITION", "MAJOR_OPERATION2OCC", Y);
    builder.inQuestion("MAJOR_OPERATION2AGE").withSharedCategories(AGE, YEAR);
    builder.inQuestion("MAJOR_OPERATION2AGE").withSharedCategory(PNA, "8888");
    builder.inQuestion("MAJOR_OPERATION2AGE").withSharedCategory(DNK, "9999");
    builder.inPage("34").withQuestion("MAJOR_OPERATION3OCC").setAnswerCondition("MAJOR_OPERATION3OCC_ACONDITION", "MAJOR_OPERATION2OCC", Y);
    builder.inQuestion("MAJOR_OPERATION3OCC").withSharedCategory(N, "0");
    builder.inQuestion("MAJOR_OPERATION3OCC").withSharedCategory(Y, "1");
    builder.inQuestion("MAJOR_OPERATION3OCC").withSharedCategory(PNA, "8");
    builder.inQuestion("MAJOR_OPERATION3OCC").withSharedCategory(DNK, "9");

    builder.inSection("A_POSTERIORI_VALUE").withPage("35").withQuestion("MAJOR_OPERATION3TYPE").setAnswerCondition("MAJOR_OPERATION3TYPE_ACONDITION", "MAJOR_OPERATION3OCC", Y);
    builder.inQuestion("MAJOR_OPERATION3TYPE").withSharedCategory(OPEN_AN);
    builder.inQuestion("MAJOR_OPERATION3TYPE").withSharedCategory(PNA, "8");
    builder.inQuestion("MAJOR_OPERATION3TYPE").withSharedCategory(DNK, "9");
    builder.inPage("35").withQuestion("MAJOR_OPERATION3AGE").setAnswerCondition("MAJOR_OPERATION3AGE_ACONDITION", "MAJOR_OPERATION3OCC", Y);
    builder.inQuestion("MAJOR_OPERATION3AGE").withSharedCategories(AGE, YEAR);
    builder.inQuestion("MAJOR_OPERATION3AGE").withSharedCategory(PNA, "8888");
    builder.inQuestion("MAJOR_OPERATION3AGE").withSharedCategory(DNK, "9999");
    builder.inPage("35").withQuestion("MAJOR_OPERATION4OCC").setAnswerCondition("MAJOR_OPERATION4OCC_ACONDITION", "MAJOR_OPERATION3OCC", Y);
    builder.inQuestion("MAJOR_OPERATION4OCC").withSharedCategory(N, "0");
    builder.inQuestion("MAJOR_OPERATION4OCC").withSharedCategory(Y, "1");
    builder.inQuestion("MAJOR_OPERATION4OCC").withSharedCategory(PNA, "8");
    builder.inQuestion("MAJOR_OPERATION4OCC").withSharedCategory(DNK, "9");

    builder.inSection("A_POSTERIORI_VALUE").withPage("36").withQuestion("MAJOR_OPERATION4TYPE").setAnswerCondition("MAJOR_OPERATION4TYPE_ACONDITION", "MAJOR_OPERATION4OCC", Y);
    builder.inQuestion("MAJOR_OPERATION4TYPE").withSharedCategory(OPEN_AN);
    builder.inQuestion("MAJOR_OPERATION4TYPE").withSharedCategory(PNA, "8");
    builder.inQuestion("MAJOR_OPERATION4TYPE").withSharedCategory(DNK, "9");
    builder.inPage("36").withQuestion("MAJOR_OPERATION4AGE").setAnswerCondition("MAJOR_OPERATION4AGE_ACONDITION", "MAJOR_OPERATION4OCC", Y);
    builder.inQuestion("MAJOR_OPERATION4AGE").withSharedCategories(AGE, YEAR);
    builder.inQuestion("MAJOR_OPERATION4AGE").withSharedCategory(PNA, "8888");
    builder.inQuestion("MAJOR_OPERATION4AGE").withSharedCategory(DNK, "9999");
    builder.inPage("36").withQuestion("MAJOR_OPERATION5OCC").setAnswerCondition("MAJOR_OPERATION5OCC_ACONDITION", "MAJOR_OPERATION4OCC", Y);
    builder.inQuestion("MAJOR_OPERATION5OCC").withSharedCategory(N, "0");
    builder.inQuestion("MAJOR_OPERATION5OCC").withSharedCategory(Y, "1");
    builder.inQuestion("MAJOR_OPERATION5OCC").withSharedCategory(PNA, "8");
    builder.inQuestion("MAJOR_OPERATION5OCC").withSharedCategory(DNK, "9");

    builder.inSection("A_POSTERIORI_VALUE").withPage("37").withQuestion("MAJOR_OPERATION5TYPE").setAnswerCondition("MAJOR_OPERATION5TYPE_ACONDITION", "MAJOR_OPERATION5OCC", Y);
    builder.inQuestion("MAJOR_OPERATION5TYPE").withSharedCategory(OPEN_AN);
    builder.inQuestion("MAJOR_OPERATION5TYPE").withSharedCategory(PNA, "8");
    builder.inQuestion("MAJOR_OPERATION5TYPE").withSharedCategory(DNK, "9");
    builder.inPage("37").withQuestion("MAJOR_OPERATION5AGE").setAnswerCondition("MAJOR_OPERATION5AGE_ACONDITION", "MAJOR_OPERATION5OCC", Y);
    builder.inQuestion("MAJOR_OPERATION5AGE").withSharedCategories(AGE, YEAR);
    builder.inQuestion("MAJOR_OPERATION5AGE").withSharedCategory(PNA, "8888");
    builder.inQuestion("MAJOR_OPERATION5AGE").withSharedCategory(DNK, "9999");

    return builder;
  }
}
