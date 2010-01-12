/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.integration.util;

import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.RangeValidator;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Returns the content for the Self Administered Health Questionnaire
 */
public class SelfAdminHealthQuestionnaireContentBuilder {

  private static final String QUESTIONNAIRE = "HealthQuestionnaireSelfAdministered";

  private static final String N = "N";

  private static final String Y = "Y";

  private static final String OTHER = "OTHER";

  private static final String PNA = "PNA";

  private static final String DNK = "DNK";

  private static final String AGE = "AGE";

  private static final String YEAR = "YEAR";

  private static final String CA = "CA";

  private static final String IT = "IT";

  private static final String FR = "FR";

  private static final String HT = "HT";

  private static final String LB = "LB";

  private static final String US = "US";

  private static final String CN = "CN";

  private static final String VN = "VN";

  private static final String PT = "PT";

  private static final String GR = "GR";

  private static final String MA = "MA";

  private static final String GB = "GB";

  private static final String ELSEWHERE = "ELSEWHERE";

  private static final String OPEN_N = "OPEN_N";

  private static final String ALCOHOL_DAY_QTY = "ALCOHOL_DAY_QTY";

  // Frequency static variables
  private static final String DAILY = "DAILY";

  private static final String _4TO5_WEEK = "4TO5_WEEK";

  private static final String _2TO3_WEEK = "2TO3_WEEK";

  private static final String WEEKLY = "WEEKLY";

  private static final String _2TO3_MONTH = "2TO3_MONTH";

  private static final String MONTHLY = "MONTHLY";

  private static final String LESS_MONTHLY = "LESS_MONTHLY";

  private static final String _6TO11_YEAR = "6TO11_YEAR";

  private static final String _1TO5_YEAR = "1TO5_YEAR";

  private static final String NEVER = "NEVER";

  public static QuestionnaireBuilder buildQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire(QUESTIONNAIRE, "1.0");

    builder.setSimplifiedUI();

    builder.withSection("ALCOHOL").withPage("0").withQuestion("ARRAY_OPEN", true).withCategory("MONDAY").withOpenAnswerDefinition("MONDAY_QUANTITY", DataType.INTEGER).setRequired(false);
    builder.inQuestion("ARRAY_OPEN").withCategory("TUESDAY").withOpenAnswerDefinition("TUESDAY_QUANTITY", DataType.INTEGER).setRequired(false);
    builder.inQuestion("ARRAY_OPEN").withQuestion("RED_WINE");
    builder.inQuestion("ARRAY_OPEN").withQuestion("WHITE_WINE");

    builder.withSection("A_ADMINISTRATION").withSection("ADMINISTRATIVE_DATA").withPage("1").withQuestion("A0");

    builder.withSection("B_DEMOGRAPHY").withSection("GENDER").withPage("2").withQuestion("SEX", "1").withCategory("MALE", "1");
    builder.inQuestion("SEX").withCategory("FEMALE", "2");
    builder.inQuestion("SEX").withCategory(OTHER, "3");
    builder.inQuestion("SEX").withSharedCategory(PNA, "8").setEscape(true);
    builder.inQuestion("SEX").withSharedCategory(DNK, "9").setEscape(true);

    builder.inSection("B_DEMOGRAPHY").withSection("FACE").withPage("3").withQuestion("PARTICIPANT_FACE", "3").withCategory("FACE1", "1");
    builder.inQuestion("PARTICIPANT_FACE").withCategory("FACE2", "2");
    builder.inQuestion("PARTICIPANT_FACE").withCategory("FACE3", "3");
    builder.inQuestion("PARTICIPANT_FACE").withCategory("FACE4", "4");
    builder.inQuestion("PARTICIPANT_FACE").withCategory("FACE5", "5");
    builder.inQuestion("PARTICIPANT_FACE").withSharedCategory(PNA, "88");
    builder.inQuestion("PARTICIPANT_FACE").withSharedCategory(DNK, "99");

    builder.inSection("B_DEMOGRAPHY").withSection("AGE_DATE_BIRTH").withPage("3_0").withQuestion("PARTICIPANT_AGE", "3_0").withCategory("PARTICIPANT_AGE").withOpenAnswerDefinition("PARTICIPANT_AGE", DataType.INTEGER).addValidator(new RangeValidator(40, 70));
    builder.inQuestion("PARTICIPANT_AGE").withSharedCategory(PNA, "88");
    builder.inQuestion("PARTICIPANT_AGE").withSharedCategory(DNK, "99");

    builder.inSection("AGE_DATE_BIRTH").withPage("3_1").withQuestion("DOB").withCategory("DOB").withOpenAnswerDefinition("DOB", DataType.DATE);
    builder.inOpenAnswerDefinition("DOB").withOpenAnswerDefinition("DOB_YEAR", DataType.INTEGER).setRequired(true).addValidator(ComparisonOperator.le, new ComputingDataSource(DataType.INTEGER, "$currentYear - 40")).addValidator(ComparisonOperator.ge, new ComputingDataSource(DataType.INTEGER, "$currentYear - 70")).setSize(4);
    builder.inOpenAnswerDefinition("DOB").withOpenAnswerDefinition("DOB_MONTH", DataType.INTEGER).setRequired(true).addValidator(ComparisonOperator.le, DataBuilder.buildInteger(12)).addValidator(ComparisonOperator.ge, DataBuilder.buildInteger(1));
    builder.inOpenAnswerDefinition("DOB").withOpenAnswerDefinition("DOB_DAY", DataType.INTEGER).setRequired(true).addValidator(ComparisonOperator.le, DataBuilder.buildInteger(31)).addValidator(ComparisonOperator.ge, DataBuilder.buildInteger(1));
    builder.inQuestion("DOB").withSharedCategory(PNA, "8888");
    builder.inQuestion("DOB").withSharedCategory(DNK, "9999");

    builder.inSection("B_DEMOGRAPHY").withSection("MARITAL_STATUS").withPage("4").withQuestion("MARITAL_STATUS", "4").withCategory("MARRIED", "1");
    builder.inQuestion("MARITAL_STATUS").withCategory("DIVORCED", "2");
    builder.inQuestion("MARITAL_STATUS").withCategory("SEPARATED", "3");
    builder.inQuestion("MARITAL_STATUS").withCategory("WIDOWED", "4");
    builder.inQuestion("MARITAL_STATUS").withCategory("SINGLE", "5");
    builder.inQuestion("MARITAL_STATUS").withSharedCategory(PNA, "88");
    builder.inQuestion("MARITAL_STATUS").withSharedCategory(DNK, "99");

    builder.inSection("B_DEMOGRAPHY").withSection("SIBLING").withPage("5").withQuestion("NUMBER_SIBLINGS_ALL", "5", true);
    builder.inQuestion("NUMBER_SIBLINGS_ALL").withCategory("BROTHER").withOpenAnswerDefinition("BROTHER_OPEN", DataType.INTEGER).addValidator(new MinimumValidator(0l));
    builder.inQuestion("NUMBER_SIBLINGS_ALL").withCategory("SISTER").withOpenAnswerDefinition("SISTER_OPEN", DataType.INTEGER).addValidator(new MinimumValidator(0l));
    builder.inQuestion("NUMBER_SIBLINGS_ALL").withSharedCategory(PNA, "88");
    builder.inQuestion("NUMBER_SIBLINGS_ALL").withSharedCategory(DNK, "99");

    builder.inSection("B_DEMOGRAPHY").withSection("BIRTH_LOCATION").withPage("6").withQuestion("BL0");
    builder.inSection("BIRTH_LOCATION").withPage("7").withQuestion("COUNTRY_BIRTH", "6").withSharedCategories(CA, IT, FR, HT, LB, US, CN, VN, PT, GR, MA, GB);
    builder.inQuestion("COUNTRY_BIRTH").withSharedCategory(ELSEWHERE, "77").setEscape(true);
    builder.inQuestion("COUNTRY_BIRTH").withSharedCategory(PNA, "88");
    builder.inQuestion("COUNTRY_BIRTH").withSharedCategory(DNK, "99");
    builder.inQuestion("COUNTRY_BIRTH").setRowCount(4);
    builder.inSection("BIRTH_LOCATION").withPage("8").withQuestion("AGE_IMMIGRATION", "7").withSharedCategory(AGE).withOpenAnswerDefinition(AGE, DataType.INTEGER).addValidator(new MinimumValidator(0l)).addValidator(ComparisonOperator.le, "PARTICIPANT_AGE", "PARTICIPANT_AGE", "PARTICIPANT_AGE");
    builder.inQuestion("AGE_IMMIGRATION").withSharedCategory(YEAR).withOpenAnswerDefinition("YEAR", DataType.INTEGER).addValidator(ComparisonOperator.ge, "DOB", "DOB", "DOB_YEAR").addCurrentYearValidator(ComparisonOperator.le);
    builder.inQuestion("AGE_IMMIGRATION").withSharedCategory(PNA, "8888");
    builder.inQuestion("AGE_IMMIGRATION").withSharedCategory(DNK, "9999");
    builder.inQuestion("AGE_IMMIGRATION").setCondition("$1 && !$2 && !$3 && !$4", builder.newDataSource("COUNTRY_BIRTH"), builder.newDataSource("COUNTRY_BIRTH", "CA"), builder.newDataSource("COUNTRY_BIRTH", PNA), builder.newDataSource("COUNTRY_BIRTH", DNK));
    builder.inSection("BIRTH_LOCATION").withPage("9").withQuestion("MOTHER_COUNTRY_BIRTH", "8").withSharedCategories(CA, IT, FR, HT, LB, US, CN, VN, PT, GR, MA, GB);
    builder.inQuestion("MOTHER_COUNTRY_BIRTH").withSharedCategory(ELSEWHERE, "77");
    builder.inQuestion("MOTHER_COUNTRY_BIRTH").withSharedCategory(PNA, "88");
    builder.inQuestion("MOTHER_COUNTRY_BIRTH").withSharedCategory(DNK, "99");
    builder.inQuestion("MOTHER_COUNTRY_BIRTH").setRowCount(4);
    builder.inSection("BIRTH_LOCATION").withPage("10").withQuestion("FATHER_COUNTRY_BIRTH", "9").withSharedCategories(CA, IT, FR, HT, LB, US, CN, VN, PT, GR, MA, GB);
    builder.inQuestion("FATHER_COUNTRY_BIRTH").withSharedCategory(ELSEWHERE, "77");
    builder.inQuestion("FATHER_COUNTRY_BIRTH").withSharedCategory(PNA, "88");
    builder.inQuestion("FATHER_COUNTRY_BIRTH").withSharedCategory(DNK, "99");
    builder.inQuestion("FATHER_COUNTRY_BIRTH").setRowCount(4);

    builder.inSection("B_DEMOGRAPHY").withSection("LANGUAGE").withPage("22").withQuestion("FIRST_LANGUAGE_LEARNED", "21", true).setAnswerCount(1, 4);
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("ENGLISH", "1");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("FRENCH", "2");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("ARABIC", "3");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("CHINESE", "4");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("ABORIGINAL", "5");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("GERMAN", "6");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("GREEK", "7");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("ITALIAN", "9");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("KOREAN", "10");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("POLISH", "12");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("PORTUGUESE", "13");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("SPANISH", "15");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("UKRAINIAN", "17");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("VIETNAMESE", "18");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("RUSSIAN", "21");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withCategory("OTHER_LANGUAGE", "23").setEscape(true);
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withSharedCategory(PNA, "88");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withSharedCategory(DNK, "99");

    builder.inSection("B_DEMOGRAPHY").withSection("EDUCATION_LEVEL").withPage("23").withQuestion("EL0");
    builder.inSection("EDUCATION_LEVEL").withPage("24").withQuestion("YEARS_EDUCATION", "22").withSharedCategory(OPEN_N).withOpenAnswerDefinition(OPEN_N, DataType.INTEGER).addValidator(new MinimumValidator(0l));
    builder.inQuestion("YEARS_EDUCATION").withSharedCategory(PNA, "8");
    builder.inQuestion("YEARS_EDUCATION").withSharedCategory(DNK, "9");

    builder.inSection("B_DEMOGRAPHY").withSection("WORKING_STATUS").withPage("25").withQuestion("WS0");
    builder.inSection("WORKING_STATUS").withPage("26").withQuestion("CURRENT_SITUATION", "23").withSharedCategory(N, "0");
    builder.inQuestion("CURRENT_SITUATION").withSharedCategory(Y, "1");
    builder.inQuestion("CURRENT_SITUATION").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENT_SITUATION").withSharedCategory(DNK, "9");
    builder.inQuestion("CURRENT_SITUATION").withQuestion("EMPLOYED", "1.1");
    builder.inQuestion("CURRENT_SITUATION").withQuestion("RETIRED", "1.3");
    builder.inQuestion("CURRENT_SITUATION").withQuestion("HOME_FAMILY", "1.4");
    builder.inQuestion("CURRENT_SITUATION").withQuestion("UNABLE_TO_WORK", "1.5");
    builder.inQuestion("CURRENT_SITUATION").withQuestion("UNPAID_WORK", "1.7");

    builder.inSection("WORKING_STATUS").withPage("26a").withQuestion("EMPLOYED_FULL_TIME", "24").withSharedCategory(Y, "1");
    builder.inQuestion("EMPLOYED_FULL_TIME").withSharedCategory(N, "2");
    builder.inQuestion("EMPLOYED_FULL_TIME").setCondition("EMPLOYED", Y);

    builder.inSection("WORKING_STATUS").withPage("27").withQuestion("CURRENT_WORK_ISIC1", "26").withCategory("AGRICULTURE", "A");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("FISHING", "B");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("MINING", "C");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("MANUFACTURING", "D");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("ELECTRICITY", "E");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("CONSTRUCTION", "F");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("TRADE", "G");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("RESTAURATION", "H");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("TRANSPORT", "I");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("FINANCIAL", "J");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("REAL_ESTATE", "K");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("PUBLIC_ADMIN", "L");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("EDUCATION", "M");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("HEALTH_SOCIAL", "N");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("OTHER_COMMUNITY", "O");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("OTHER_WORK", "77").setEscape(true);
    builder.inQuestion("CURRENT_WORK_ISIC1").withSharedCategory(PNA, "88");
    builder.inQuestion("CURRENT_WORK_ISIC1").withSharedCategory(DNK, "99");
    builder.inQuestion("CURRENT_WORK_ISIC1").setCondition("EMPLOYED", Y);

    builder.inSection("WORKING_STATUS").withPage("29").withQuestion("IS_LONGEST_TIME_OCCUPATION", "28").withSharedCategory(N, "0");
    builder.inQuestion("IS_LONGEST_TIME_OCCUPATION").withSharedCategory(Y, "1");
    builder.inQuestion("IS_LONGEST_TIME_OCCUPATION").withSharedCategory(PNA, "8");
    builder.inQuestion("IS_LONGEST_TIME_OCCUPATION").withSharedCategory(DNK, "9");
    builder.inSection("WORKING_STATUS").withPage("30").withQuestion("EVER_WORKED", "29").withSharedCategory(N, "0");
    builder.inQuestion("EVER_WORKED").withSharedCategory(Y, "1");
    builder.inQuestion("EVER_WORKED").withSharedCategory(PNA, "8");
    builder.inQuestion("EVER_WORKED").withSharedCategory(DNK, "9");
    builder.inQuestion("EVER_WORKED").setCondition("$1 && !$2 && !$3", builder.newDataSource("IS_LONGEST_TIME_OCCUPATION"), builder.newDataSource("IS_LONGEST_TIME_OCCUPATION", Y), builder.newDataSource("EMPLOYED", Y));

    builder.inSection("WORKING_STATUS").withPage("29_1").withQuestion("COMPUTER_TIME");
    builder.inQuestion("COMPUTER_TIME").withCategory("HOUR_DAY").withOpenAnswerDefinition("HOUR_DAY", DataType.INTEGER).addValidator(new RangeValidator(1, 24));
    builder.inQuestion("COMPUTER_TIME").withCategory("HOUR_WEEK").withOpenAnswerDefinition("HOUR_WEEK", DataType.INTEGER).addValidator(new RangeValidator(1, 24 * 7));
    builder.inQuestion("COMPUTER_TIME").withCategory("HOUR_MONTH").withOpenAnswerDefinition("HOUR_MONTH", DataType.INTEGER).addValidator(new RangeValidator(1, 24 * 31));
    builder.inQuestion("COMPUTER_TIME").withCategory("DONT_USE");
    builder.inQuestion("COMPUTER_TIME").withSharedCategory(PNA, "8");
    builder.inQuestion("COMPUTER_TIME").withSharedCategory(DNK, "9");

    builder.withSection("C_LIFE_HABITS").withSection("TOBACCO_USE").withPage("32").withQuestion("EVER_USED", "31").withSharedCategory(N, "0");
    builder.inQuestion("EVER_USED").withSharedCategory(Y, "1");
    builder.inQuestion("EVER_USED").withSharedCategory(PNA, "8");
    builder.inQuestion("EVER_USED").withSharedCategory(DNK, "9");
    builder.inQuestion("EVER_USED").withQuestion("CIGARS_EVER", "1.1");
    builder.inQuestion("EVER_USED").withQuestion("SMALL_CIGARS_EVER", "1.2");
    builder.inQuestion("EVER_USED").withQuestion("PIPES_EVER", "1.3");
    builder.inQuestion("EVER_USED").withQuestion("OTHER_NICOTIN_PRODUCT_EVER", "1.8");
    builder.inSection("TOBACCO_USE").withPage("33").withQuestion("CURRENTLY_USE", "32").withSharedCategory(N, "0");
    builder.inQuestion("CURRENTLY_USE").withSharedCategory(Y, "1");
    builder.inQuestion("CURRENTLY_USE").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENTLY_USE").withSharedCategory(DNK, "9");
    builder.inQuestion("CURRENTLY_USE").setCondition("$1 || $2 || $3 || $4", builder.newDataSource("CIGARS_EVER", Y), builder.newDataSource("SMALL_CIGARS_EVER", Y), builder.newDataSource("PIPES_EVER", Y), builder.newDataSource("OTHER_NICOTIN_PRODUCT_EVER", Y));
    builder.inQuestion("CURRENTLY_USE").withQuestion("CIGARS_CURRENT", "1.1");
    builder.inQuestion("CIGARS_CURRENT").setCondition("CIGARS_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("SMALL_CIGARS_CURRENT", "1.2");
    builder.inQuestion("SMALL_CIGARS_CURRENT").setCondition("SMALL_CIGARS_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("PIPES_CURRENT", "1.3");
    builder.inQuestion("PIPES_CURRENT").setCondition("PIPES_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("OTHER_NICOTIN_PRODUCT_CURRENT", "1.8");
    builder.inQuestion("OTHER_NICOTIN_PRODUCT_CURRENT").setCondition("OTHER_NICOTIN_PRODUCT_EVER", Y);
    builder.inPage("33").addTimestamp("TS_TU");

    builder.inSection("C_LIFE_HABITS").withSection("ALCOHOL_INTAKE").withPage("34").withQuestion("AI0");
    builder.inSection("ALCOHOL_INTAKE").withPage("35").withQuestion("EVER_DRUNK_ALCOHOL", "33").withSharedCategory(N, "0");
    builder.inQuestion("EVER_DRUNK_ALCOHOL").withSharedCategory(Y, "1");
    builder.inQuestion("EVER_DRUNK_ALCOHOL").withSharedCategory(PNA, "8");
    builder.inQuestion("EVER_DRUNK_ALCOHOL").withSharedCategory(DNK, "9");
    builder.inSection("ALCOHOL_INTAKE").withPage("36").withQuestion("ALCOHOL_FREQUENCY", "34").setRowCount(3);
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(DAILY, "7");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(WEEKLY, "4");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(_2TO3_WEEK, "5");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(_4TO5_WEEK, "6");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(LESS_MONTHLY, "1");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(MONTHLY, "2");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(NEVER, "0").setEscape(true);
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(PNA, "88");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(DNK, "99");
    builder.inQuestion("ALCOHOL_FREQUENCY").setCondition("EVER_DRUNK_ALCOHOL", Y);
    builder.inSection("ALCOHOL_INTAKE").withPage("37").withQuestion("RED_WINE_DAY_QTY", "35").withSharedCategory(ALCOHOL_DAY_QTY).withOpenAnswerDefinition(ALCOHOL_DAY_QTY, DataType.INTEGER).addValidator(new MinimumValidator(0l));
    builder.inQuestion("RED_WINE_DAY_QTY").withSharedCategory(PNA, "888");
    builder.inQuestion("RED_WINE_DAY_QTY").withSharedCategory(DNK, "999");
    builder.inQuestion("RED_WINE_DAY_QTY").setCondition("$1 || $2 || $3 || $4", builder.newDataSource("ALCOHOL_FREQUENCY", DAILY), builder.newDataSource("ALCOHOL_FREQUENCY", _4TO5_WEEK), builder.newDataSource("ALCOHOL_FREQUENCY", _2TO3_WEEK), builder.newDataSource("ALCOHOL_FREQUENCY", WEEKLY));
    builder.inSection("ALCOHOL_INTAKE").withPage("38").withQuestion("WHITE_WINE_DAY_QTY", "36").withSharedCategory(ALCOHOL_DAY_QTY);
    builder.inQuestion("WHITE_WINE_DAY_QTY").withSharedCategory(PNA, "888");
    builder.inQuestion("WHITE_WINE_DAY_QTY").withSharedCategory(DNK, "999");
    builder.inQuestion("WHITE_WINE_DAY_QTY").setCondition("RED_WINE_DAY_QTY");
    builder.inSection("ALCOHOL_INTAKE").withPage("39").withQuestion("BEER_DAY_QTY", "37").withSharedCategory(ALCOHOL_DAY_QTY);
    builder.inQuestion("BEER_DAY_QTY").withSharedCategory(PNA, "888");
    builder.inQuestion("BEER_DAY_QTY").withSharedCategory(DNK, "999");
    builder.inQuestion("BEER_DAY_QTY").setCondition("RED_WINE_DAY_QTY");
    builder.inSection("ALCOHOL_INTAKE").withPage("40").withQuestion("LIQUOR_DAY_QTY", "38").withSharedCategory(ALCOHOL_DAY_QTY);
    builder.inQuestion("LIQUOR_DAY_QTY").withSharedCategory(PNA, "888");
    builder.inQuestion("LIQUOR_DAY_QTY").withSharedCategory(DNK, "999");
    builder.inQuestion("LIQUOR_DAY_QTY").setCondition("RED_WINE_DAY_QTY");
    builder.inSection("ALCOHOL_INTAKE").withPage("41").withQuestion("OTHER_ALCOHOL_DAY", "39").withSharedCategory(N, "0");
    builder.inQuestion("OTHER_ALCOHOL_DAY").withSharedCategory(Y, "1");
    builder.inQuestion("OTHER_ALCOHOL_DAY").withSharedCategory(PNA, "8");
    builder.inQuestion("OTHER_ALCOHOL_DAY").withSharedCategory(DNK, "9");
    builder.inQuestion("OTHER_ALCOHOL_DAY").setCondition("RED_WINE_DAY_QTY");

    builder.inSection("ALCOHOL_INTAKE").withPage("43").withQuestion("OTHER_ALCOHOL_DAY_QTY", "41").withSharedCategory(ALCOHOL_DAY_QTY);
    builder.inQuestion("OTHER_ALCOHOL_DAY_QTY").withSharedCategory(PNA, "888");
    builder.inQuestion("OTHER_ALCOHOL_DAY_QTY").withSharedCategory(DNK, "999");
    builder.inQuestion("OTHER_ALCOHOL_DAY_QTY").setCondition("OTHER_ALCOHOL_DAY", Y);
    builder.inSection("ALCOHOL_INTAKE").withPage("44").withQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ", "42").setRowCount(4).withSharedCategory(DAILY, "8");
    builder.inQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ").withSharedCategory(_4TO5_WEEK, "7");
    builder.inQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ").withSharedCategory(_2TO3_WEEK, "6");
    builder.inQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ").withSharedCategory(WEEKLY, "5");
    builder.inQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ").withSharedCategory(_2TO3_MONTH, "4");
    builder.inQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ").withSharedCategory(MONTHLY, "3");
    builder.inQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ").withSharedCategory(_6TO11_YEAR, "2");
    builder.inQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ").withSharedCategory(_1TO5_YEAR, "1");
    builder.inQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ").withSharedCategory(NEVER, "0");
    builder.inQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ").withSharedCategory(PNA, "88");
    builder.inQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ").withSharedCategory(DNK, "99");
    builder.inQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ").setCondition("$1 && ($2 || $3 || $4 || $5)", builder.newDataSource("SEX", "MALE"), builder.newDataSource("ALCOHOL_FREQUENCY", DAILY), builder.newDataSource("ALCOHOL_FREQUENCY", _4TO5_WEEK), builder.newDataSource("ALCOHOL_FREQUENCY", _2TO3_WEEK), builder.newDataSource("ALCOHOL_FREQUENCY", WEEKLY));
    builder.inPage("44").withQuestion("BINGE_DRINKING_MALE_MOD_FREQ", "43").setRowCount(4).withSharedCategory(_2TO3_MONTH, "4");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(MONTHLY, "3");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(_6TO11_YEAR, "2");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(_1TO5_YEAR, "1");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(NEVER, "0");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(PNA, "88");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(DNK, "99");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").setCondition("$1 && ($2 || $3)", builder.newDataSource("SEX", "MALE"), builder.newDataSource("ALCOHOL_FREQUENCY", MONTHLY), builder.newDataSource("ALCOHOL_FREQUENCY", LESS_MONTHLY));
    builder.inSection("ALCOHOL_INTAKE").withPage("45").withQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ", "44").setRowCount(4).withSharedCategory(DAILY, "8");
    builder.inQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ").withSharedCategory(_4TO5_WEEK, "7");
    builder.inQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ").withSharedCategory(_2TO3_WEEK, "6");
    builder.inQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ").withSharedCategory(WEEKLY, "5");
    builder.inQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ").withSharedCategory(_2TO3_MONTH, "4");
    builder.inQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ").withSharedCategory(MONTHLY, "3");
    builder.inQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ").withSharedCategory(_6TO11_YEAR, "2");
    builder.inQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ").withSharedCategory(_1TO5_YEAR, "1");
    builder.inQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ").withSharedCategory(NEVER, "0");
    builder.inQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ").withSharedCategory(PNA, "88");
    builder.inQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ").withSharedCategory(DNK, "99");
    builder.inQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ").setCondition("$1 && ($2 || $3 || $4 || $5)", builder.newDataSource("SEX", "FEMALE"), builder.newDataSource("ALCOHOL_FREQUENCY", DAILY), builder.newDataSource("ALCOHOL_FREQUENCY", _4TO5_WEEK), builder.newDataSource("ALCOHOL_FREQUENCY", _2TO3_WEEK), builder.newDataSource("ALCOHOL_FREQUENCY", WEEKLY));
    builder.inPage("45").withQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ", "45").withSharedCategory(_2TO3_MONTH, "4");
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").withSharedCategory(MONTHLY, "3");
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").withSharedCategory(_6TO11_YEAR, "2");
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").withSharedCategory(_1TO5_YEAR, "1");
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").withSharedCategory(NEVER, "0");
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").withSharedCategory(PNA, "88");
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").withSharedCategory(DNK, "99");
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").setCondition("$1 && ($2 || $3)", builder.newDataSource("SEX", "FEMALE"), builder.newDataSource("ALCOHOL_FREQUENCY", MONTHLY), builder.newDataSource("ALCOHOL_FREQUENCY", LESS_MONTHLY));
    builder.inPage("45").addTimestamp("TS_AI");

    builder.inSection("C_LIFE_HABITS").withSection("FEELINGS").withPage("46").withQuestion("FEELINGS_BOILERPLATE");
    builder.inSection("FEELINGS").withPage("47").withQuestion("FEELINGS");
    builder.inQuestion("FEELINGS").withCategories("NOT_AT_ALL", "SEVERAL_DAYS", "MORE_THAN_HALF_A_DAY", "NEARLY_EVERY_DAY");
    builder.inQuestion("FEELINGS").withSharedCategories(PNA, DNK);
    builder.inQuestion("FEELINGS").withQuestion("NERVOUS");
    builder.inQuestion("FEELINGS").withQuestion("WORRY_CONTROL");
    builder.inQuestion("FEELINGS").withQuestion("WOORY_TOO_MUCH");
    builder.inQuestion("FEELINGS").withQuestion("TROUBLE_RELAXING");
    builder.inQuestion("FEELINGS").withQuestion("RESTLESS");
    builder.inQuestion("FEELINGS").withQuestion("IRRITABLE");
    builder.inQuestion("FEELINGS").withQuestion("AFRAID");

    builder.withSection("CONCLUSION").withPage("48").withQuestion("CONCLUSION");

    return builder;
  }
}
