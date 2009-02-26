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

import org.apache.wicket.validation.validator.NumberValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.core.data.ComputingDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanelFactory;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.DataType;

/**
 * Returns the content for the Self Administered Health Questionnaire
 */
public class SelfAdminHealthQuestionnaireContentBuilder {

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

  private static final String POSTAL_CODE3 = "POSTAL_CODE3";

  private static final String OPEN_N = "OPEN_N";

  private static final String OPEN_AN = "OPEN_AN";

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
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaireSelfAdministered", "1.0");

    builder.withSection("A_ADMINISTRATION").withSection("ADMINISTRATIVE_DATA").withPage("1").withQuestion("A0");
    builder.inPage("1").addTimestamp("TS_START");

    builder.withSection("B_DEMOGRAPHY").withSection("GENDER").withPage("2").withQuestion("SEX", "1").withCategory("MALE").setExportName("1");
    builder.inQuestion("SEX").withCategory("FEMALE").setExportName("2");
    builder.inQuestion("SEX").withSharedCategory(OTHER, "3");
    builder.inQuestion("SEX").withSharedCategory(PNA, "8").setEscape(true);
    builder.inQuestion("SEX").withSharedCategory(DNK, "9").setEscape(true);

    builder.inSection("B_DEMOGRAPHY").withSection("AGE_DATE_BIRTH").withPage("3").withQuestion("PARTICIPANT_AGE", "3").withCategory("PARTICIPANT_AGE").withOpenAnswerDefinition("PARTICIPANT_AGE", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(40, 70));
    builder.inQuestion("PARTICIPANT_AGE").withSharedCategory(PNA, "88");
    builder.inQuestion("PARTICIPANT_AGE").withSharedCategory(DNK, "99");

    builder.inSection("AGE_DATE_BIRTH").withPage("3_1").withQuestion("DATE_OF_BIRTH", "2").withQuestion("DOB_YEAR").withCategory("DOB_YEAR").withOpenAnswerDefinition("DOB_YEAR", DataType.INTEGER).addValidator(ComparisonOperator.le, new ComputingDataSource(DataType.INTEGER, "$currentYear - 40")).addValidator(ComparisonOperator.ge, new ComputingDataSource(DataType.INTEGER, "$currentYear - 70"));
    builder.inQuestion("DOB_YEAR").withSharedCategory(PNA, "8888");
    builder.inQuestion("DOB_YEAR").withSharedCategory(DNK, "9999");
    builder.inQuestion("DATE_OF_BIRTH").withQuestion("DOB_MONTH", DropDownQuestionPanelFactory.class).withCategories("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");
    builder.inQuestion("DOB_MONTH").withSharedCategory(PNA, "88");
    builder.inQuestion("DOB_MONTH").withSharedCategory(DNK, "99");
    builder.inQuestion("DATE_OF_BIRTH").withQuestion("DOB_DAY", DropDownQuestionPanelFactory.class).withCategories("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31");
    builder.inQuestion("DOB_DAY").withSharedCategory(PNA, "88");
    builder.inQuestion("DOB_DAY").withSharedCategory(DNK, "99");

    builder.inSection("B_DEMOGRAPHY").withSection("MARITAL_STATUS").withPage("4").withQuestion("MARITAL_STATUS", "4").withCategory("MARRIED").setExportName("1");
    builder.inQuestion("MARITAL_STATUS").withCategory("DIVORCED").setExportName("2");
    builder.inQuestion("MARITAL_STATUS").withCategory("SEPARATED").setExportName("3");
    builder.inQuestion("MARITAL_STATUS").withCategory("WIDOWED").setExportName("4");
    builder.inQuestion("MARITAL_STATUS").withCategory("SINGLE").setExportName("5");
    builder.inQuestion("MARITAL_STATUS").withSharedCategory(PNA, "88");
    builder.inQuestion("MARITAL_STATUS").withSharedCategory(DNK, "99");

    builder.inSection("B_DEMOGRAPHY").withSection("SIBLING").withPage("5").withQuestion("NUMBER_SIBLINGS_ALL", "5").withSharedCategory(OPEN_N).withOpenAnswerDefinition(OPEN_N, DataType.INTEGER).addValidator(new NumberValidator.MinimumValidator(0l));
    builder.inQuestion("NUMBER_SIBLINGS_ALL").withSharedCategory(PNA, "88");
    builder.inQuestion("NUMBER_SIBLINGS_ALL").withSharedCategory(DNK, "99");

    builder.inSection("B_DEMOGRAPHY").withSection("BIRTH_LOCATION").withPage("6").withQuestion("BL0");
    builder.inSection("BIRTH_LOCATION").withPage("7").withQuestion("COUNTRY_BIRTH", "6", DropDownQuestionPanelFactory.class).withSharedCategories(CA, IT, FR, HT, LB, US, CN, VN, PT, GR, MA, GB);
    builder.inQuestion("COUNTRY_BIRTH").withSharedCategory(ELSEWHERE, "77").setEscape(true);
    builder.inQuestion("COUNTRY_BIRTH").withSharedCategory(PNA, "88");
    builder.inQuestion("COUNTRY_BIRTH").withSharedCategory(DNK, "99");
    builder.inSection("BIRTH_LOCATION").withPage("8").withQuestion("AGE_IMMIGRATION", "7").withSharedCategory(AGE).withOpenAnswerDefinition(AGE, DataType.INTEGER).addValidator(new NumberValidator.MinimumValidator(0l)).addValidator(ComparisonOperator.le, "PARTICIPANT_AGE", "PARTICIPANT_AGE", "PARTICIPANT_AGE");
    builder.inQuestion("AGE_IMMIGRATION").withSharedCategory(YEAR).withOpenAnswerDefinition("YEAR", DataType.INTEGER).addValidator(ComparisonOperator.ge, "DOB_YEAR", "DOB_YEAR", "DOB_YEAR").addCurrentYearValidator(ComparisonOperator.le);
    builder.inQuestion("AGE_IMMIGRATION").withSharedCategory(PNA, "8888");
    builder.inQuestion("AGE_IMMIGRATION").withSharedCategory(DNK, "9999");
    builder.inQuestion("AGE_IMMIGRATION").setCondition("$1 && !$2 && !$3 && !$4", builder.newDataSource("COUNTRY_BIRTH"), builder.newDataSource("COUNTRY_BIRTH", "CA"), builder.newDataSource("COUNTRY_BIRTH", PNA), builder.newDataSource("COUNTRY_BIRTH", DNK));
    builder.inSection("BIRTH_LOCATION").withPage("9").withQuestion("MOTHER_COUNTRY_BIRTH", "8", DropDownQuestionPanelFactory.class).withSharedCategories(CA, IT, FR, HT, LB, US, CN, VN, PT, GR, MA, GB);
    builder.inQuestion("MOTHER_COUNTRY_BIRTH").withSharedCategory(ELSEWHERE, "77");
    builder.inQuestion("MOTHER_COUNTRY_BIRTH").withSharedCategory(PNA, "88");
    builder.inQuestion("MOTHER_COUNTRY_BIRTH").withSharedCategory(DNK, "99");
    builder.inSection("BIRTH_LOCATION").withPage("10").withQuestion("FATHER_COUNTRY_BIRTH", "9", DropDownQuestionPanelFactory.class).withSharedCategories(CA, IT, FR, HT, LB, US, CN, VN, PT, GR, MA, GB);
    builder.inQuestion("FATHER_COUNTRY_BIRTH").withSharedCategory(ELSEWHERE, "77");
    builder.inQuestion("FATHER_COUNTRY_BIRTH").withSharedCategory(PNA, "88");
    builder.inQuestion("FATHER_COUNTRY_BIRTH").withSharedCategory(DNK, "99");

    builder.inSection("B_DEMOGRAPHY").withSection("RESIDENCE_HISTORY").withPage("11").withQuestion("CURRENT_RESIDENCE", "10").withSharedCategory(OPEN_AN).withOpenAnswerDefinition(OPEN_AN, DataType.TEXT);
    builder.inQuestion("CURRENT_RESIDENCE").withSharedCategory(PNA, "88");
    builder.inQuestion("CURRENT_RESIDENCE").withSharedCategory(DNK, "99");
    builder.inSection("RESIDENCE_HISTORY").withPage("12").withQuestion("POSTAL_CODE", "11").withSharedCategory(POSTAL_CODE3).withOpenAnswerDefinition(POSTAL_CODE3, DataType.TEXT).addValidator(new PatternValidator("[a-zA-Z]\\d[a-zA-Z]"));
    builder.inQuestion("POSTAL_CODE").withSharedCategory(PNA, "888");
    builder.inQuestion("POSTAL_CODE").withSharedCategory(DNK, "999");
    builder.inQuestion("POSTAL_CODE").setCondition("CURRENT_RESIDENCE", OPEN_AN);
    builder.inSection("RESIDENCE_HISTORY").withPage("13").withQuestion("AGE_CURRENT_RESIDENCE", "12").withSharedCategory(AGE);
    builder.inQuestion("AGE_CURRENT_RESIDENCE").withSharedCategory(YEAR);
    builder.inQuestion("AGE_CURRENT_RESIDENCE").withSharedCategory(PNA, "88");
    builder.inQuestion("AGE_CURRENT_RESIDENCE").withSharedCategory(DNK, "99");
    builder.inQuestion("AGE_CURRENT_RESIDENCE").setCondition("CURRENT_RESIDENCE", OPEN_AN);
    builder.inSection("RESIDENCE_HISTORY").withPage("14").withQuestion("CURRENT_IS_LONGEST_TIME_LIVED", "13").withSharedCategory(N, "0");
    builder.inQuestion("CURRENT_IS_LONGEST_TIME_LIVED").withSharedCategory(Y, "1");
    builder.inQuestion("CURRENT_IS_LONGEST_TIME_LIVED").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENT_IS_LONGEST_TIME_LIVED").withSharedCategory(DNK, "9");
    builder.inSection("RESIDENCE_HISTORY").withPage("15").withQuestion("LONGEST_TIME_COUNTRY", "14", DropDownQuestionPanelFactory.class).withSharedCategories(CA, IT, FR, HT, LB, US, CN, VN, PT, GR, MA, GB);
    builder.inQuestion("LONGEST_TIME_COUNTRY").withSharedCategory(ELSEWHERE, "77");
    builder.inQuestion("LONGEST_TIME_COUNTRY").withSharedCategory(PNA, "88");
    builder.inQuestion("LONGEST_TIME_COUNTRY").withSharedCategory(DNK, "99");
    builder.inQuestion("LONGEST_TIME_COUNTRY").setCondition("$1 || $2", builder.newDataSource("CURRENT_IS_LONGEST_TIME_LIVED", N), builder.newDataSource("CURRENT_IS_LONGEST_TIME_LIVED", DNK));
    builder.inSection("RESIDENCE_HISTORY").withPage("16").withQuestion("LONGEST_TIME_REGION", "15").withSharedCategory(OPEN_AN);
    builder.inQuestion("LONGEST_TIME_REGION").withSharedCategory(PNA, "88");
    builder.inQuestion("LONGEST_TIME_REGION").withSharedCategory(DNK, "99");
    builder.inQuestion("LONGEST_TIME_REGION").setCondition("$1 && !$2 && !$2", builder.newDataSource("LONGEST_TIME_COUNTRY"), builder.newDataSource("LONGEST_TIME_COUNTRY", PNA), builder.newDataSource("LONGEST_TIME_COUNTRY", DNK));
    builder.inSection("RESIDENCE_HISTORY").withPage("17").withQuestion("LONGEST_TIME_LOCATION", "16").withSharedCategory(OPEN_AN);
    builder.inQuestion("LONGEST_TIME_LOCATION").withSharedCategory(PNA, "88");
    builder.inQuestion("LONGEST_TIME_LOCATION").withSharedCategory(DNK, "99");
    builder.inQuestion("LONGEST_TIME_LOCATION").setCondition("LONGEST_TIME_REGION", OPEN_AN);
    builder.inSection("RESIDENCE_HISTORY").withPage("18").withQuestion("LONGEST_TIME_POSTAL_CODE", "17").withSharedCategory(POSTAL_CODE3);
    builder.inQuestion("LONGEST_TIME_POSTAL_CODE").withSharedCategory(PNA, "888");
    builder.inQuestion("LONGEST_TIME_POSTAL_CODE").withSharedCategory(DNK, "999");
    builder.inQuestion("LONGEST_TIME_POSTAL_CODE").setCondition("$1 && $2", builder.newDataSource("LONGEST_TIME_LOCATION", OPEN_AN), builder.newDataSource("LONGEST_TIME_COUNTRY", "CA"));
    builder.inSection("RESIDENCE_HISTORY").withPage("19").withQuestion("LONGEST_TIME_STREET", "18").withSharedCategory(OPEN_AN);
    builder.inQuestion("LONGEST_TIME_STREET").withSharedCategory(PNA, "88");
    builder.inQuestion("LONGEST_TIME_STREET").withSharedCategory(DNK, "99");
    builder.inQuestion("LONGEST_TIME_STREET").setCondition("$1 || $2", builder.newDataSource("LONGEST_TIME_POSTAL_CODE", PNA), builder.newDataSource("LONGEST_TIME_POSTAL_CODE", DNK));
    builder.inSection("RESIDENCE_HISTORY").withPage("20").withQuestion("LONGEST_TIME_CROSS_STREET", "19").withSharedCategory(OPEN_AN);
    builder.inQuestion("LONGEST_TIME_CROSS_STREET").withSharedCategory(PNA, "88");
    builder.inQuestion("LONGEST_TIME_CROSS_STREET").withSharedCategory(DNK, "99");
    builder.inQuestion("LONGEST_TIME_CROSS_STREET").setCondition("LONGEST_TIME_STREET", OPEN_AN);
    builder.inSection("RESIDENCE_HISTORY").withPage("21").withQuestion("LONGEST_TIME_AGE_STARTED_LIVING", "20").withSharedCategory(AGE);
    builder.inQuestion("LONGEST_TIME_AGE_STARTED_LIVING").withSharedCategory(YEAR);
    builder.inQuestion("LONGEST_TIME_AGE_STARTED_LIVING").withSharedCategory(PNA, "8888");
    builder.inQuestion("LONGEST_TIME_AGE_STARTED_LIVING").withSharedCategory(DNK, "9999");
    builder.inQuestion("LONGEST_TIME_AGE_STARTED_LIVING").setCondition("$1 && !$2 && !$3", builder.newDataSource("LONGEST_TIME_COUNTRY"), builder.newDataSource("COUNTRY_BIRTH", PNA), builder.newDataSource("COUNTRY_BIRTH", DNK));

    builder.inPage("21").addTimestamp("TS_DEM2");

    builder.inSection("B_DEMOGRAPHY").withSection("LANGUAGE").withPage("22").withQuestion("FIRST_LANGUAGE_LEARNED", "21", true).setAnswerCount(1, 4).setRowCount(8);
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
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withSharedCategory(OTHER, "23");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withSharedCategory(PNA, "88");
    builder.inQuestion("FIRST_LANGUAGE_LEARNED").withSharedCategory(DNK, "99");

    builder.inSection("B_DEMOGRAPHY").withSection("EDUCATION_LEVEL").withPage("23").withQuestion("EL0");
    builder.inSection("EDUCATION_LEVEL").withPage("24").withQuestion("YEARS_EDUCATION", "22").withSharedCategory(OPEN_N);
    builder.inQuestion("YEARS_EDUCATION").withSharedCategory(PNA, "8");
    builder.inQuestion("YEARS_EDUCATION").withSharedCategory(DNK, "9");

    builder.inSection("B_DEMOGRAPHY").withSection("WORKING_STATUS").withPage("25").withQuestion("WS0");
    builder.inSection("WORKING_STATUS").withPage("26").withQuestion("CURRENT_SITUATION", "23").withSharedCategory(N, "0");
    builder.inQuestion("CURRENT_SITUATION").withSharedCategory(Y, "1");
    builder.inQuestion("CURRENT_SITUATION").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENT_SITUATION").withSharedCategory(DNK, "9");
    builder.inQuestion("CURRENT_SITUATION").withQuestion("EMPLOYED", "1.1");
    builder.inQuestion("CURRENT_SITUATION").withQuestion("SELF_EMPLOYED", "1.2");
    builder.inQuestion("CURRENT_SITUATION").withQuestion("RETIRED", "1.3");
    builder.inQuestion("CURRENT_SITUATION").withQuestion("HOME_FAMILY", "1.4");
    builder.inQuestion("CURRENT_SITUATION").withQuestion("UNABLE_TO_WORK", "1.5");
    builder.inQuestion("CURRENT_SITUATION").withQuestion("UNEMPLOYED", "1.6");
    builder.inQuestion("CURRENT_SITUATION").withQuestion("UNPAID_WORK", "1.7");
    builder.inQuestion("CURRENT_SITUATION").withQuestion("STUDENT", "1.8");

    builder.inSection("WORKING_STATUS").withPage("26a").withQuestion("EMPLOYED_FULL_TIME", "24").withSharedCategory(Y, "1");
    builder.inQuestion("EMPLOYED_FULL_TIME").withSharedCategory(N, "2");
    builder.inQuestion("EMPLOYED_FULL_TIME").setCondition("EMPLOYED", Y);
    builder.inPage("26a").withQuestion("SELF_EMPLOYED_FULL_TIME", "25").withSharedCategory(Y, "1");
    builder.inQuestion("SELF_EMPLOYED_FULL_TIME").withSharedCategory(N, "2");
    builder.inQuestion("SELF_EMPLOYED_FULL_TIME").setCondition("SELF_EMPLOYED", Y);

    builder.inSection("WORKING_STATUS").withPage("27").withQuestion("CURRENT_WORK_ISIC1", "26", DropDownQuestionPanelFactory.class).withCategory("AGRICULTURE").setExportName("A");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("FISHING").setExportName("B");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("MINING").setExportName("C");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("MANUFACTURING").setExportName("D");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("ELECTRICITY").setExportName("E");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("CONSTRUCTION").setExportName("F");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("TRADE").setExportName("G");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("RESTAURATION").setExportName("H");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("TRANSPORT").setExportName("I");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("FINANCIAL").setExportName("J");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("REAL_ESTATE").setExportName("K");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("PUBLIC_ADMIN").setExportName("L");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("EDUCATION").setExportName("M");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("HEALTH_SOCIAL").setExportName("N");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("OTHER_COMMUNITY").setExportName("O");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("PRIVATE_HOUSEHOLD").setExportName("P");
    builder.inQuestion("CURRENT_WORK_ISIC1").withCategory("EXTRATERRITORIAL").setExportName("Q");
    builder.inQuestion("CURRENT_WORK_ISIC1").withSharedCategory(OTHER, "77");
    builder.inQuestion("CURRENT_WORK_ISIC1").withSharedCategory(PNA, "88");
    builder.inQuestion("CURRENT_WORK_ISIC1").withSharedCategory(DNK, "99");
    builder.inQuestion("CURRENT_WORK_ISIC1").setCondition("$1 || $2", builder.newDataSource("EMPLOYED", Y), builder.newDataSource("SELF_EMPLOYED", Y));
    builder.inSection("WORKING_STATUS").withPage("28").withQuestion("CURRENT_WORK_ISIC1_OTHER", "27").withSharedCategory(OPEN_AN);
    builder.inQuestion("CURRENT_WORK_ISIC1_OTHER").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENT_WORK_ISIC1_OTHER").withSharedCategory(DNK, "9");
    builder.inQuestion("CURRENT_WORK_ISIC1_OTHER").setCondition("CURRENT_WORK_ISIC1", OTHER);
    builder.inSection("WORKING_STATUS").withPage("29").withQuestion("IS_LONGEST_TIME_OCCUPATION", "28").withSharedCategory(N, "0");
    builder.inQuestion("IS_LONGEST_TIME_OCCUPATION").withSharedCategory(Y, "1");
    builder.inQuestion("IS_LONGEST_TIME_OCCUPATION").withSharedCategory(PNA, "8");
    builder.inQuestion("IS_LONGEST_TIME_OCCUPATION").withSharedCategory(DNK, "9");
    builder.inSection("WORKING_STATUS").withPage("30").withQuestion("EVER_WORKED", "29").withSharedCategory(N, "0");
    builder.inQuestion("EVER_WORKED").withSharedCategory(Y, "1");
    builder.inQuestion("EVER_WORKED").withSharedCategory(PNA, "8");
    builder.inQuestion("EVER_WORKED").withSharedCategory(DNK, "9");
    builder.inQuestion("EVER_WORKED").setCondition("$1 && !$2 && !($3 || $4)", builder.newDataSource("IS_LONGEST_TIME_OCCUPATION"), builder.newDataSource("IS_LONGEST_TIME_OCCUPATION", Y), builder.newDataSource("EMPLOYED", Y), builder.newDataSource("SELF_EMPLOYED", Y));
    builder.inSection("WORKING_STATUS").withPage("31").withQuestion("LONGEST_JOB_TITLE", "30").withSharedCategory(OPEN_AN);
    builder.inQuestion("LONGEST_JOB_TITLE").withSharedCategory(PNA, "8");
    builder.inQuestion("LONGEST_JOB_TITLE").withSharedCategory(DNK, "9");
    builder.inQuestion("LONGEST_JOB_TITLE").setCondition("$1 || ($2 && ($3 || $4))", builder.newDataSource("EVER_WORKED", Y), builder.newDataSource("IS_LONGEST_TIME_OCCUPATION", N), builder.newDataSource("EMPLOYED", Y), builder.newDataSource("SELF_EMPLOYED", Y));

    builder.withSection("C_LIFE_HABITS").withSection("TOBACCO_USE").withPage("32").withQuestion("EVER_USED", "31").withSharedCategory(N, "0");
    builder.inQuestion("EVER_USED").withSharedCategory(Y, "1");
    builder.inQuestion("EVER_USED").withSharedCategory(PNA, "8");
    builder.inQuestion("EVER_USED").withSharedCategory(DNK, "9");
    builder.inQuestion("EVER_USED").withQuestion("CIGARS_EVER", "1.1");
    builder.inQuestion("EVER_USED").withQuestion("SMALL_CIGARS_EVER", "1.2");
    builder.inQuestion("EVER_USED").withQuestion("PIPES_EVER", "1.3");
    builder.inQuestion("EVER_USED").withQuestion("CHEWING_TOBACCO_SNUFF_EVER", "1.4");
    builder.inQuestion("EVER_USED").withQuestion("BETEL_NUT_EVER", "1.5");
    builder.inQuestion("EVER_USED").withQuestion("PAAN_EVER", "1.6");
    builder.inQuestion("EVER_USED").withQuestion("SHEESHA_EVER", "1.7");
    builder.inQuestion("EVER_USED").withQuestion("OTHER_NICOTIN_PRODUCT_EVER", "1.8");
    builder.inSection("TOBACCO_USE").withPage("33").withQuestion("CURRENTLY_USE", "32").withSharedCategory(N, "0");
    builder.inQuestion("CURRENTLY_USE").withSharedCategory(Y, "1");
    builder.inQuestion("CURRENTLY_USE").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENTLY_USE").withSharedCategory(DNK, "9");
    builder.inQuestion("CURRENTLY_USE").setCondition("$1 || $2 || $3 || $4 || $5 || $6 || $7 || $8 || $9", builder.newDataSource("CIGARS_EVER", Y), builder.newDataSource("CIGARS_EVER", Y), builder.newDataSource("SMALL_CIGARS_EVER", Y), builder.newDataSource("PIPES_EVER", Y), builder.newDataSource("CHEWING_TOBACCO_SNUFF_EVER", Y), builder.newDataSource("BETEL_NUT_EVER", Y), builder.newDataSource("PAAN_EVER", Y), builder.newDataSource("SHEESHA_EVER", Y), builder.newDataSource("OTHER_NICOTIN_PRODUCT_EVER", Y));
    builder.inQuestion("CURRENTLY_USE").withQuestion("CIGARS_CURRENT", "1.1");
    builder.inQuestion("CIGARS_CURRENT").setCondition("CIGARS_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("SMALL_CIGARS_CURRENT", "1.2");
    builder.inQuestion("SMALL_CIGARS_CURRENT").setCondition("SMALL_CIGARS_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("PIPES_CURRENT", "1.3");
    builder.inQuestion("PIPES_CURRENT").setCondition("PIPES_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("CHEWING_TOBACCO_SNUFF_CURRENT", "1.4");
    builder.inQuestion("CHEWING_TOBACCO_SNUFF_CURRENT").setCondition("CHEWING_TOBACCO_SNUFF_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("BETEL_NUT_CURRENT", "1.5");
    builder.inQuestion("BETEL_NUT_CURRENT").setCondition("BETEL_NUT_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("PAAN_CURRENT", "1.6");
    builder.inQuestion("PAAN_CURRENT").setCondition("PAAN_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("SHEESHA_CURRENT", "1.7");
    builder.inQuestion("SHEESHA_CURRENT").setCondition("SHEESHA_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("OTHER_NICOTIN_PRODUCT_CURRENT", "1.8");
    builder.inQuestion("OTHER_NICOTIN_PRODUCT_CURRENT").setCondition("OTHER_NICOTIN_PRODUCT_EVER", Y);
    builder.inPage("33").addTimestamp("TS_TU");

    builder.inSection("C_LIFE_HABITS").withSection("ALCOHOL_INTAKE").withPage("34").withQuestion("AI0");
    builder.inSection("ALCOHOL_INTAKE").withPage("35").withQuestion("EVER_DRUNK_ALCOHOL", "33").withSharedCategory(N, "0");
    builder.inQuestion("EVER_DRUNK_ALCOHOL").withSharedCategory(Y, "1");
    builder.inQuestion("EVER_DRUNK_ALCOHOL").withSharedCategory(PNA, "8");
    builder.inQuestion("EVER_DRUNK_ALCOHOL").withSharedCategory(DNK, "9");
    builder.inSection("ALCOHOL_INTAKE").withPage("36").withQuestion("ALCOHOL_FREQUENCY", "34").withSharedCategory(DAILY, "7");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(_4TO5_WEEK, "6");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(_2TO3_WEEK, "5");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(WEEKLY, "4");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(_2TO3_MONTH, "3");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(MONTHLY, "2");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(LESS_MONTHLY, "1");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(NEVER, "0");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(PNA, "88");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(DNK, "99");
    builder.inQuestion("ALCOHOL_FREQUENCY").setCondition("EVER_DRUNK_ALCOHOL", Y);
    builder.inSection("ALCOHOL_INTAKE").withPage("37").withQuestion("RED_WINE_DAY_QTY", "35").withSharedCategory(ALCOHOL_DAY_QTY).withOpenAnswerDefinition(ALCOHOL_DAY_QTY, DataType.INTEGER).addValidator(new NumberValidator.MinimumValidator(0l));
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
    builder.inSection("ALCOHOL_INTAKE").withPage("42").withQuestion("OTHER_ALCOHOL_DAY_ID", "40").withSharedCategory(OPEN_AN);
    builder.inQuestion("OTHER_ALCOHOL_DAY_ID").withSharedCategory(PNA, "88");
    builder.inQuestion("OTHER_ALCOHOL_DAY_ID").withSharedCategory(DNK, "99");
    builder.inQuestion("OTHER_ALCOHOL_DAY_ID").setCondition("OTHER_ALCOHOL_DAY", Y);
    builder.inSection("ALCOHOL_INTAKE").withPage("43").withQuestion("OTHER_ALCOHOL_DAY_QTY", "41").withSharedCategory(ALCOHOL_DAY_QTY);
    builder.inQuestion("OTHER_ALCOHOL_DAY_QTY").withSharedCategory(PNA, "888");
    builder.inQuestion("OTHER_ALCOHOL_DAY_QTY").withSharedCategory(DNK, "999");
    builder.inQuestion("OTHER_ALCOHOL_DAY_QTY").setCondition("OTHER_ALCOHOL_DAY", Y);
    builder.inSection("ALCOHOL_INTAKE").withPage("44").withQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ", "42").withSharedCategory(DAILY, "8");
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
    builder.inPage("44").withQuestion("BINGE_DRINKING_MALE_MOD_FREQ", "43").withSharedCategory(_2TO3_MONTH, "4");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(MONTHLY, "3");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(_6TO11_YEAR, "2");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(_1TO5_YEAR, "1");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(NEVER, "0");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(PNA, "88");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(DNK, "99");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").setCondition("$1 && ($2 || $3 || $4)", builder.newDataSource("SEX", "MALE"), builder.newDataSource("ALCOHOL_FREQUENCY", _2TO3_MONTH), builder.newDataSource("ALCOHOL_FREQUENCY", MONTHLY), builder.newDataSource("ALCOHOL_FREQUENCY", LESS_MONTHLY));
    builder.inSection("ALCOHOL_INTAKE").withPage("45").withQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ", "44").withSharedCategory(DAILY, "8");
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
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").setCondition("$1 && ($2 || $3 || $4)", builder.newDataSource("SEX", "FEMALE"), builder.newDataSource("ALCOHOL_FREQUENCY", _2TO3_MONTH), builder.newDataSource("ALCOHOL_FREQUENCY", MONTHLY), builder.newDataSource("ALCOHOL_FREQUENCY", LESS_MONTHLY));
    builder.inPage("45").addTimestamp("TS_AI");

    return builder;
  }
}
