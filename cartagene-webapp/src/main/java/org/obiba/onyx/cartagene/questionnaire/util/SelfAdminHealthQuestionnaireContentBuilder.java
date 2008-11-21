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

import java.util.Locale;

import org.apache.wicket.validation.validator.NumberValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ComparisionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ConditionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.DropDownQuestionPanelFactory;
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

  private static final String[] LIST_OF_COUNTRY = Locale.getISOCountries();

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

  public static QuestionnaireBuilder buildHealthQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaireSelfAdministered", "1.0");

    builder.withSection("A_ADMINISTRATION").withSection("ADMINISTRATIVE_DATA").withPage("1").withQuestion("A0");
    builder.inPage("1").addTimestamp("TS_START");

    builder.withSection("B_DEMOGRAPHY").withSection("GENDER").withPage("2").withQuestion("SEX").withCategory("MALE").setExportName("1");
    builder.inQuestion("SEX").withCategory("FEMALE").setExportName("2");
    builder.inQuestion("SEX").withSharedCategory(OTHER, "3");
    builder.inQuestion("SEX").withSharedCategory(PNA, "8");
    builder.inQuestion("SEX").withSharedCategory(DNK, "9");

    builder.inSection("B_DEMOGRAPHY").withSection("AGE_DATE_BIRTH").withPage("3").withQuestion("DATE_OF_BIRTH").withQuestion("DOB_YEAR").withCategory("DOB_YEAR").withOpenAnswerDefinition("DOB_YEAR", DataType.INTEGER).addValidator(new PatternValidator("\\d{4}"));
    builder.inQuestion("DOB_YEAR").withSharedCategory(PNA, "8888");
    builder.inQuestion("DOB_YEAR").withSharedCategory(DNK, "9999");
    builder.inQuestion("DATE_OF_BIRTH").withQuestion("DOB_MONTH", DropDownQuestionPanelFactory.class).withCategories("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");
    builder.inQuestion("DOB_MONTH").withSharedCategory(PNA, "88");
    builder.inQuestion("DOB_MONTH").withSharedCategory(DNK, "99");
    builder.inQuestion("DATE_OF_BIRTH").withQuestion("DOB_DAY", DropDownQuestionPanelFactory.class).withCategories("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31");
    builder.inQuestion("DOB_DAY").withSharedCategory(PNA, "88");
    builder.inQuestion("DOB_DAY").withSharedCategory(DNK, "99");
    builder.inPage("3").withQuestion("PARTICIPANT_AGE").withCategory("PARTICIPANT_AGE").withOpenAnswerDefinition("PARTICIPANT_AGE", DataType.INTEGER).addValidator(new NumberValidator.RangeValidator(40, 70));
    builder.inQuestion("PARTICIPANT_AGE").withSharedCategory(PNA, "88");
    builder.inQuestion("PARTICIPANT_AGE").withSharedCategory(DNK, "99");

    builder.inSection("B_DEMOGRAPHY").withSection("MARITAL_STATUS").withPage("4").withQuestion("MARITAL_STATUS").withCategory("MARRIED").setExportName("1");
    builder.inQuestion("MARITAL_STATUS").withCategory("DIVORCED").setExportName("2");
    builder.inQuestion("MARITAL_STATUS").withCategory("SEPARATED").setExportName("3");
    builder.inQuestion("MARITAL_STATUS").withCategory("WIDOWED").setExportName("4");
    builder.inQuestion("MARITAL_STATUS").withCategory("SINGLE").setExportName("5");
    builder.inQuestion("MARITAL_STATUS").withSharedCategory(PNA, "88");
    builder.inQuestion("MARITAL_STATUS").withSharedCategory(DNK, "99");

    builder.inSection("B_DEMOGRAPHY").withSection("SIBLING").withPage("5").withQuestion("NUMBER_SIBLINGS_ALL").withSharedCategory(OPEN_N).withOpenAnswerDefinition(OPEN_N, DataType.INTEGER).addValidator(new NumberValidator.MinimumValidator(0l));
    builder.inQuestion("NUMBER_SIBLINGS_ALL").withSharedCategory(PNA, "88");
    builder.inQuestion("NUMBER_SIBLINGS_ALL").withSharedCategory(DNK, "99");

    builder.inSection("B_DEMOGRAPHY").withSection("BIRTH_LOCATION").withPage("6").withQuestion("BL0");
    builder.inSection("BIRTH_LOCATION").withPage("7").withQuestion("COUNTRY_BIRTH", DropDownQuestionPanelFactory.class).withSharedCategories(LIST_OF_COUNTRY);
    builder.inQuestion("COUNTRY_BIRTH").withSharedCategory(PNA, "88");
    builder.inQuestion("COUNTRY_BIRTH").withSharedCategory(DNK, "99");
    builder.inSection("BIRTH_LOCATION").withPage("8").withQuestion("AGE_IMMIGRATION").withSharedCategory(AGE).withOpenAnswerDefinition(AGE, DataType.INTEGER).addValidator(new NumberValidator.MinimumValidator(0l)).addValidator(ComparisionOperator.le, "PARTICIPANT_AGE", "PARTICIPANT_AGE", "PARTICIPANT_AGE");
    builder.inQuestion("AGE_IMMIGRATION").withSharedCategory(YEAR).withOpenAnswerDefinition("YEAR", DataType.INTEGER).addValidator(ComparisionOperator.ge, "DOB_YEAR", "DOB_YEAR", "DOB_YEAR").addCurrentYearValidator(ComparisionOperator.le);
    builder.inQuestion("AGE_IMMIGRATION").withSharedCategory(PNA, "8888");
    builder.inQuestion("AGE_IMMIGRATION").withSharedCategory(DNK, "9999");
    builder.inQuestion("AGE_IMMIGRATION").setMultipleCondition("AGE_IMMIGRATION_MCONDITION", ConditionOperator.AND).withAnswerCondition("AGE_IMMIGRATION_ACONDITION_0", "COUNTRY_BIRTH");
    builder.inCondition("AGE_IMMIGRATION_MCONDITION").withNoAnswerCondition("AGE_IMMIGRATION_NCONDITION_0").withAnswerCondition("AGE_IMMIGRATION_ACONDITION_1", "COUNTRY_BIRTH", "CA");
    builder.inCondition("AGE_IMMIGRATION_MCONDITION").withNoAnswerCondition("AGE_IMMIGRATION_NCONDITION_1").withAnswerCondition("AGE_IMMIGRATION_ACONDITION_2", "COUNTRY_BIRTH", PNA);
    builder.inCondition("AGE_IMMIGRATION_MCONDITION").withNoAnswerCondition("AGE_IMMIGRATION_NCONDITION_2").withAnswerCondition("AGE_IMMIGRATION_ACONDITION_3", "COUNTRY_BIRTH", DNK);
    builder.inSection("BIRTH_LOCATION").withPage("9").withQuestion("MOTHER_COUNTRY_BIRTH", DropDownQuestionPanelFactory.class).withSharedCategories(LIST_OF_COUNTRY);
    builder.inQuestion("MOTHER_COUNTRY_BIRTH").withSharedCategory(PNA, "88");
    builder.inQuestion("MOTHER_COUNTRY_BIRTH").withSharedCategory(DNK, "99");
    builder.inSection("BIRTH_LOCATION").withPage("10").withQuestion("FATHER_COUNTRY_BIRTH", DropDownQuestionPanelFactory.class).withSharedCategories(LIST_OF_COUNTRY);
    builder.inQuestion("FATHER_COUNTRY_BIRTH").withSharedCategory(PNA, "88");
    builder.inQuestion("FATHER_COUNTRY_BIRTH").withSharedCategory(DNK, "99");

    builder.inSection("B_DEMOGRAPHY").withSection("RESIDENCE_HISTORY").withPage("11").withQuestion("CURRENT_RESIDENCE").withSharedCategory(OPEN_AN).withOpenAnswerDefinition(OPEN_AN, DataType.TEXT);
    builder.inQuestion("CURRENT_RESIDENCE").withSharedCategory(PNA, "88");
    builder.inQuestion("CURRENT_RESIDENCE").withSharedCategory(DNK, "99");
    builder.inSection("RESIDENCE_HISTORY").withPage("12").withQuestion("POSTAL_CODE").withSharedCategory(POSTAL_CODE3).withOpenAnswerDefinition(POSTAL_CODE3, DataType.TEXT).addValidator(new PatternValidator("[a-zA-Z]\\d[a-zA-Z]"));
    builder.inQuestion("POSTAL_CODE").withSharedCategory(PNA, "888");
    builder.inQuestion("POSTAL_CODE").withSharedCategory(DNK, "999");
    builder.inQuestion("POSTAL_CODE").setAnswerCondition("POSTAL_CODE_ACONDITION_0", "CURRENT_RESIDENCE", OPEN_AN);
    builder.inSection("RESIDENCE_HISTORY").withPage("13").withQuestion("AGE_CURRENT_RESIDENCE").withSharedCategory(AGE);
    builder.inQuestion("AGE_CURRENT_RESIDENCE").withSharedCategory(YEAR);
    builder.inQuestion("AGE_CURRENT_RESIDENCE").withSharedCategory(PNA, "88");
    builder.inQuestion("AGE_CURRENT_RESIDENCE").withSharedCategory(DNK, "99");
    builder.inQuestion("AGE_CURRENT_RESIDENCE").setAnswerCondition("AGE_CURRENT_RESIDENCE_ACONDITION_1", "CURRENT_RESIDENCE", OPEN_AN);
    builder.inSection("RESIDENCE_HISTORY").withPage("14").withQuestion("CURRENT_IS_LONGEST_TIME_LIVED").withSharedCategory(N, "0");
    builder.inQuestion("CURRENT_IS_LONGEST_TIME_LIVED").withSharedCategory(Y, "1");
    builder.inQuestion("CURRENT_IS_LONGEST_TIME_LIVED").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENT_IS_LONGEST_TIME_LIVED").withSharedCategory(DNK, "9");
    builder.inSection("RESIDENCE_HISTORY").withPage("15").withQuestion("LONGEST_TIME_COUNTRY", DropDownQuestionPanelFactory.class).withSharedCategories(LIST_OF_COUNTRY);
    builder.inQuestion("LONGEST_TIME_COUNTRY").withSharedCategory(PNA, "88");
    builder.inQuestion("LONGEST_TIME_COUNTRY").withSharedCategory(DNK, "99");
    builder.inQuestion("LONGEST_TIME_COUNTRY").setMultipleCondition("LONGEST_TIME_COUNTRY_MCONDITION", ConditionOperator.OR).withAnswerCondition("LONGEST_TIME_COUNTRY_ACONDITION_0", "CURRENT_IS_LONGEST_TIME_LIVED", N);
    builder.inCondition("LONGEST_TIME_COUNTRY_MCONDITION").withAnswerCondition("LONGEST_TIME_COUNTRY_ACONDITION_1", "CURRENT_IS_LONGEST_TIME_LIVED", DNK);
    builder.inSection("RESIDENCE_HISTORY").withPage("16").withQuestion("LONGEST_TIME_REGION").withSharedCategory(OPEN_AN);
    builder.inQuestion("LONGEST_TIME_REGION").withSharedCategory(PNA, "88");
    builder.inQuestion("LONGEST_TIME_REGION").withSharedCategory(DNK, "99");
    builder.inQuestion("LONGEST_TIME_REGION").setMultipleCondition("LONGEST_TIME_REGION_MCONDITION", ConditionOperator.AND).withAnswerCondition("LONGEST_TIME_REGION_ACONDITION_0", "LONGEST_TIME_COUNTRY");
    builder.inCondition("LONGEST_TIME_REGION_MCONDITION").withNoAnswerCondition("LONGEST_TIME_REGION_NCONDITION_0").withAnswerCondition("LONGEST_TIME_REGION_ACONDITION_1", "LONGEST_TIME_COUNTRY", PNA);
    builder.inCondition("LONGEST_TIME_REGION_MCONDITION").withNoAnswerCondition("LONGEST_TIME_REGION_NCONDITION_1").withAnswerCondition("LONGEST_TIME_REGION_ACONDITION_2", "LONGEST_TIME_COUNTRY", DNK);
    builder.inSection("RESIDENCE_HISTORY").withPage("17").withQuestion("LONGEST_TIME_LOCATION").withSharedCategory(OPEN_AN);
    builder.inQuestion("LONGEST_TIME_LOCATION").withSharedCategory(PNA, "88");
    builder.inQuestion("LONGEST_TIME_LOCATION").withSharedCategory(DNK, "99");
    builder.inQuestion("LONGEST_TIME_LOCATION").setAnswerCondition("LONGEST_TIME_LOCATION_ACONDITION", "LONGEST_TIME_REGION", OPEN_AN);
    builder.inSection("RESIDENCE_HISTORY").withPage("18").withQuestion("LONGEST_TIME_POSTAL_CODE").withSharedCategory(POSTAL_CODE3);
    builder.inQuestion("LONGEST_TIME_POSTAL_CODE").withSharedCategory(PNA, "888");
    builder.inQuestion("LONGEST_TIME_POSTAL_CODE").withSharedCategory(DNK, "999");
    builder.inQuestion("LONGEST_TIME_POSTAL_CODE").setAnswerCondition("LONGEST_TIME_POSTAL_CODE_ACONDITION", "LONGEST_TIME_COUNTRY", "CA");
    builder.inSection("RESIDENCE_HISTORY").withPage("19").withQuestion("LONGEST_TIME_STREET").withSharedCategory(OPEN_AN);
    builder.inQuestion("LONGEST_TIME_STREET").withSharedCategory(PNA, "88");
    builder.inQuestion("LONGEST_TIME_STREET").withSharedCategory(DNK, "99");
    builder.inQuestion("LONGEST_TIME_STREET").setMultipleCondition("LONGEST_TIME_STREET_MCONDITION", ConditionOperator.OR).withAnswerCondition("LONGEST_TIME_STREET.ACONDITION_0", "LONGEST_TIME_POSTAL_CODE", PNA);
    builder.inCondition("LONGEST_TIME_STREET_MCONDITION").withAnswerCondition("LONGEST_TIME_STREET.ACONDITION_1", "LONGEST_TIME_POSTAL_CODE", DNK);
    builder.inSection("RESIDENCE_HISTORY").withPage("20").withQuestion("LONGEST_TIME_CROSS_STREET").withSharedCategory(OPEN_AN);
    builder.inQuestion("LONGEST_TIME_CROSS_STREET").withSharedCategory(PNA, "88");
    builder.inQuestion("LONGEST_TIME_CROSS_STREET").withSharedCategory(DNK, "99");
    builder.inQuestion("LONGEST_TIME_CROSS_STREET").setAnswerCondition("LONGEST_TIME_CROSS_STREET_ACONDITION", "LONGEST_TIME_STREET", OPEN_AN);
    builder.inSection("RESIDENCE_HISTORY").withPage("21").withQuestion("LONGEST_TIME_AGE_STARTED_LIVING").withSharedCategory(AGE);
    builder.inQuestion("LONGEST_TIME_AGE_STARTED_LIVING").withSharedCategory(YEAR);
    builder.inQuestion("LONGEST_TIME_AGE_STARTED_LIVING").withSharedCategory(PNA, "8888");
    builder.inQuestion("LONGEST_TIME_AGE_STARTED_LIVING").withSharedCategory(DNK, "9999");
    builder.inQuestion("LONGEST_TIME_AGE_STARTED_LIVING").setMultipleCondition("LONGEST_TIME_AGE_STARTED_LIVING_MCONDITION", ConditionOperator.OR).withAnswerCondition("LONGEST_TIME_AGE_STARTED_LIVING_ACONDITION_0", "CURRENT_IS_LONGEST_TIME_LIVED", N);
    builder.inCondition("LONGEST_TIME_AGE_STARTED_LIVING_MCONDITION").withAnswerCondition("LONGEST_TIME_AGE_STARTED_LIVING_ACONDITION_1", "CURRENT_IS_LONGEST_TIME_LIVED", DNK);
    builder.inPage("21").addTimestamp("TS_DEM2");

    builder.inSection("B_DEMOGRAPHY").withSection("LANGUAGE").withPage("22").withQuestion("FIRST_LANGUAGE_LEARNED", true).withCategory("ENGLISH").setExportName("1");
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
    builder.inSection("EDUCATION_LEVEL").withPage("24").withQuestion("YEARS_EDUCATION").withSharedCategory(OPEN_N);
    builder.inQuestion("YEARS_EDUCATION").withSharedCategory(PNA, "8");
    builder.inQuestion("YEARS_EDUCATION").withSharedCategory(DNK, "9");

    builder.inSection("B_DEMOGRAPHY").withSection("WORKING_STATUS").withPage("25").withQuestion("WS0");
    builder.inSection("WORKING_STATUS").withPage("26").withQuestion("WORK_CURRENT_SITUATION_EMP").withSharedCategory(N, "0");
    builder.inQuestion("WORK_CURRENT_SITUATION_EMP").withCategory("FTE").setExportName("1");
    builder.inQuestion("WORK_CURRENT_SITUATION_EMP").withCategory("PTE").setExportName("2");
    builder.inQuestion("WORK_CURRENT_SITUATION_EMP").withSharedCategory(PNA, "8");
    builder.inQuestion("WORK_CURRENT_SITUATION_EMP").withSharedCategory(DNK, "9");
    builder.inQuestion("WORK_CURRENT_SITUATION_EMP").withQuestion("EMPLOYED");
    builder.inQuestion("WORK_CURRENT_SITUATION_EMP").withQuestion("SELF_EMPLOYED");
    builder.inPage("26").withQuestion("WORK_CURRENT_SITUATION_OTHER").withSharedCategory(N, "0");
    builder.inQuestion("WORK_CURRENT_SITUATION_OTHER").withSharedCategory(Y, "1");
    builder.inQuestion("WORK_CURRENT_SITUATION_OTHER").withSharedCategory(PNA, "8");
    builder.inQuestion("WORK_CURRENT_SITUATION_OTHER").withSharedCategory(DNK, "9");
    builder.inQuestion("WORK_CURRENT_SITUATION_OTHER").withQuestion("RETIRED");
    builder.inQuestion("WORK_CURRENT_SITUATION_OTHER").withQuestion("HOME_FAMILY");
    builder.inQuestion("WORK_CURRENT_SITUATION_OTHER").withQuestion("UNABLE_TO_WORK");
    builder.inQuestion("WORK_CURRENT_SITUATION_OTHER").withQuestion("UNEMPLOYED");
    builder.inQuestion("WORK_CURRENT_SITUATION_OTHER").withQuestion("UNPAID_WORK");
    builder.inQuestion("WORK_CURRENT_SITUATION_OTHER").withQuestion("STUDENT");
    builder.inSection("WORKING_STATUS").withPage("27").withQuestion("CURRENT_WORK_ISIC1", DropDownQuestionPanelFactory.class).withCategory("AGRICULTURE").setExportName("A");
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
    builder.inQuestion("CURRENT_WORK_ISIC1").setMultipleCondition("CURRENT_WORK_ISIC1_MCONDITION", ConditionOperator.OR).withAnswerCondition("CURRENT_WORK_ISIC1_ACONDITION_0", "EMPLOYED", "FTE");
    builder.inCondition("CURRENT_WORK_ISIC1_MCONDITION").withAnswerCondition("CURRENT_WORK_ISIC1_ACONDITION_1", "EMPLOYED", "PTE");
    builder.inCondition("CURRENT_WORK_ISIC1_MCONDITION").withAnswerCondition("CURRENT_WORK_ISIC1_ACONDITION_2", "SELF_EMPLOYED", "FTE");
    builder.inCondition("CURRENT_WORK_ISIC1_MCONDITION").withAnswerCondition("CURRENT_WORK_ISIC1_ACONDITION_3", "SELF_EMPLOYED", "PTE");
    builder.inSection("WORKING_STATUS").withPage("28").withQuestion("CURRENT_WORK_ISIC1_OTHER").withSharedCategory(OPEN_AN);
    builder.inQuestion("CURRENT_WORK_ISIC1_OTHER").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENT_WORK_ISIC1_OTHER").withSharedCategory(DNK, "9");
    builder.inQuestion("CURRENT_WORK_ISIC1_OTHER").setAnswerCondition("CURRENT_WORK_ISIC1_OTHER_ACONDITION", "CURRENT_WORK_ISIC1", OTHER);
    builder.inSection("WORKING_STATUS").withPage("29").withQuestion("IS_LONGEST_TIME_OCCUPATION").withSharedCategory(N, "0");
    builder.inQuestion("IS_LONGEST_TIME_OCCUPATION").withSharedCategory(Y, "1");
    builder.inQuestion("IS_LONGEST_TIME_OCCUPATION").withSharedCategory(PNA, "8");
    builder.inQuestion("IS_LONGEST_TIME_OCCUPATION").withSharedCategory(DNK, "9");
    builder.inSection("WORKING_STATUS").withPage("30").withQuestion("EVER_WORKED").withSharedCategory(N, "0");
    builder.inQuestion("EVER_WORKED").withSharedCategory(Y, "1");
    builder.inQuestion("EVER_WORKED").withSharedCategory(PNA, "8");
    builder.inQuestion("EVER_WORKED").withSharedCategory(DNK, "9");
    builder.inQuestion("EVER_WORKED").setMultipleCondition("EVER_WORKED_MCONDITION_0", ConditionOperator.AND).withAnswerCondition("EVER_WORKED_ACONDITION_0", "IS_LONGEST_TIME_OCCUPATION");
    builder.inCondition("EVER_WORKED_MCONDITION_0").withNoAnswerCondition("EVER_WORKED_NCONDITION_0").withAnswerCondition("EVER_WORKED_ACONDITION_1", "IS_LONGEST_TIME_OCCUPATION", Y);
    builder.inCondition("EVER_WORKED_MCONDITION_0").withNoAnswerCondition("EVER_WORKED_NCONDITION_1").withMultipleCondition("EVER_WORKED_MCONDITION_1", ConditionOperator.OR);
    builder.inCondition("EVER_WORKED_MCONDITION_1").withAnswerCondition("EVER_WORKED_ACONDITION_2", "EMPLOYED", "FTE");
    builder.inCondition("EVER_WORKED_MCONDITION_1").withAnswerCondition("EVER_WORKED_ACONDITION_3", "EMPLOYED", "PTE");
    builder.inCondition("EVER_WORKED_MCONDITION_1").withAnswerCondition("EVER_WORKED_ACONDITION_4", "SELF_EMPLOYED", "FTE");
    builder.inCondition("EVER_WORKED_MCONDITION_1").withAnswerCondition("EVER_WORKED_ACONDITION_5", "SELF_EMPLOYED", "PTE");
    builder.inSection("WORKING_STATUS").withPage("31").withQuestion("LONGEST_JOB_TITLE").withSharedCategory(OPEN_AN);
    builder.inQuestion("LONGEST_JOB_TITLE").withSharedCategory(PNA, "8");
    builder.inQuestion("LONGEST_JOB_TITLE").withSharedCategory(DNK, "9");
    builder.inQuestion("LONGEST_JOB_TITLE").setAnswerCondition("LONGEST_JOB_TITLE_ACONDITION", "EVER_WORKED", Y);

    builder.withSection("C_LIFE_HABITS").withSection("TOBACCO_USE").withPage("32").withQuestion("EVER_USED").withSharedCategory(N, "0");
    builder.inQuestion("EVER_USED").withSharedCategory(Y, "1");
    builder.inQuestion("EVER_USED").withSharedCategory(PNA, "8");
    builder.inQuestion("EVER_USED").withSharedCategory(DNK, "9");
    builder.inQuestion("EVER_USED").withQuestion("CIGARS_EVER");
    builder.inQuestion("EVER_USED").withQuestion("SMALL_CIGARS_EVER");
    builder.inQuestion("EVER_USED").withQuestion("PIPES_EVER");
    builder.inQuestion("EVER_USED").withQuestion("CHEWING_TOBACCO_SNUFF_EVER");
    builder.inQuestion("EVER_USED").withQuestion("BETEL_NUT_EVER");
    builder.inQuestion("EVER_USED").withQuestion("PAAN_EVER");
    builder.inQuestion("EVER_USED").withQuestion("SHEESHA_EVER");
    builder.inQuestion("EVER_USED").withQuestion("OTHER_NICOTIN_PRODUCT_EVER");
    builder.inSection("TOBACCO_USE").withPage("33").withQuestion("CURRENTLY_USE").withSharedCategory(N, "0");
    builder.inQuestion("CURRENTLY_USE").withSharedCategory(Y, "1");
    builder.inQuestion("CURRENTLY_USE").withSharedCategory(PNA, "8");
    builder.inQuestion("CURRENTLY_USE").withSharedCategory(DNK, "9");
    builder.inQuestion("CURRENTLY_USE").setMultipleCondition("CURRENTLY_USE_MCONDITION", ConditionOperator.OR).withAnswerCondition("CURRENTLY_USE_ACONDITION_0", "CIGARS_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_1", "CIGARS_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_2", "SMALL_CIGARS_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_3", "PIPES_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_4", "CHEWING_TOBACCO_SNUFF_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_5", "BETEL_NUT_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_6", "PAAN_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_7", "SHEESHA_EVER", Y);
    builder.inCondition("CURRENTLY_USE_MCONDITION").withAnswerCondition("CURRENTLY_USE_ACONDITION_8", "OTHER_NICOTIN_PRODUCT_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("CIGARS_CURRENT");
    builder.inQuestion("CIGARS_CURRENT").setAnswerCondition("CIGARS_CURRENT_ACONDITION", "CIGARS_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("SMALL_CIGARS_CURRENT");
    builder.inQuestion("SMALL_CIGARS_CURRENT").setAnswerCondition("SMALL_CIGARS_CURRENT_ACONDITION", "SMALL_CIGARS_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("PIPES_CURRENT");
    builder.inQuestion("PIPES_CURRENT").setAnswerCondition("PIPES_CURRENT_ACONDITION", "PIPES_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("CHEWING_TOBACCO_SNUFF_CURRENT");
    builder.inQuestion("CHEWING_TOBACCO_SNUFF_CURRENT").setAnswerCondition("CHEWING_TOBACCO_SNUFF_CURRENT_ACONDITION", "CHEWING_TOBACCO_SNUFF_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("BETEL_NUT_CURRENT");
    builder.inQuestion("BETEL_NUT_CURRENT").setAnswerCondition("BETEL_NUT_CURRENT_ACONDITION", "BETEL_NUT_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("PAAN_CURRENT");
    builder.inQuestion("PAAN_CURRENT").setAnswerCondition("PAAN_CURRENT_ACONDITION", "PAAN_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("SHEESHA_CURRENT");
    builder.inQuestion("SHEESHA_CURRENT").setAnswerCondition("SHEESHA_CURRENT_ACONDITION", "SHEESHA_EVER", Y);
    builder.inQuestion("CURRENTLY_USE").withQuestion("OTHER_NICOTIN_PRODUCT_CURRENT");
    builder.inQuestion("OTHER_NICOTIN_PRODUCT_CURRENT").setAnswerCondition("OTHER_NICOTIN_PRODUCT_CURRENT_ACONDITION", "OTHER_NICOTIN_PRODUCT_EVER", Y);
    builder.inPage("33").addTimestamp("TS_TU");

    builder.inSection("C_LIFE_HABITS").withSection("ALCOHOL_INTAKE").withPage("34").withQuestion("AI0");
    builder.inSection("ALCOHOL_INTAKE").withPage("35").withQuestion("EVER_DRUNK_ALCOHOL").withSharedCategory(N, "0");
    builder.inQuestion("EVER_DRUNK_ALCOHOL").withSharedCategory(Y, "1");
    builder.inQuestion("EVER_DRUNK_ALCOHOL").withSharedCategory(PNA, "8");
    builder.inQuestion("EVER_DRUNK_ALCOHOL").withSharedCategory(DNK, "9");
    builder.inSection("ALCOHOL_INTAKE").withPage("36").withQuestion("ALCOHOL_FREQUENCY").withSharedCategory(DAILY, "7");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(_4TO5_WEEK, "6");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(_2TO3_WEEK, "5");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(WEEKLY, "4");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(_2TO3_MONTH, "3");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(MONTHLY, "2");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(LESS_MONTHLY, "1");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(NEVER, "0");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(PNA, "88");
    builder.inQuestion("ALCOHOL_FREQUENCY").withSharedCategory(DNK, "99");
    builder.inQuestion("ALCOHOL_FREQUENCY").setAnswerCondition("ALCOHOL_FREQUENCY_ACONDITION", "EVER_DRUNK_ALCOHOL", Y);
    builder.inSection("ALCOHOL_INTAKE").withPage("37").withQuestion("RED_WINE_DAY_QTY").withSharedCategory(ALCOHOL_DAY_QTY).withOpenAnswerDefinition(ALCOHOL_DAY_QTY, DataType.INTEGER).addValidator(new NumberValidator.MinimumValidator(0l));
    builder.inQuestion("RED_WINE_DAY_QTY").withSharedCategory(PNA, "888");
    builder.inQuestion("RED_WINE_DAY_QTY").withSharedCategory(DNK, "999");
    builder.inQuestion("RED_WINE_DAY_QTY").setMultipleCondition("RED_WINE_DAY_QTY_MCONDITION", ConditionOperator.OR).withAnswerCondition("RED_WINE_DAY_QTY_ACONDITION_0", "ALCOHOL_FREQUENCY", DAILY);
    builder.inCondition("RED_WINE_DAY_QTY_MCONDITION").withAnswerCondition("RED_WINE_DAY_QTY_ACONDITION_1", "ALCOHOL_FREQUENCY", _4TO5_WEEK);
    builder.inCondition("RED_WINE_DAY_QTY_MCONDITION").withAnswerCondition("RED_WINE_DAY_QTY_ACONDITION_2", "ALCOHOL_FREQUENCY", _2TO3_WEEK);
    builder.inCondition("RED_WINE_DAY_QTY_MCONDITION").withAnswerCondition("RED_WINE_DAY_QTY_ACONDITION_3", "ALCOHOL_FREQUENCY", WEEKLY);
    builder.inSection("ALCOHOL_INTAKE").withPage("38").withQuestion("WHITE_WINE_DAY_QTY").withSharedCategory(ALCOHOL_DAY_QTY);
    builder.inQuestion("WHITE_WINE_DAY_QTY").withSharedCategory(PNA, "888");
    builder.inQuestion("WHITE_WINE_DAY_QTY").withSharedCategory(DNK, "999");
    builder.inQuestion("WHITE_WINE_DAY_QTY").setMultipleCondition("WHITE_WINE_DAY_QTY_MCONDITION", ConditionOperator.OR).withAnswerCondition("WHITE_WINE_DAY_QTY_ACONDITION_0", "ALCOHOL_FREQUENCY", DAILY);
    builder.inCondition("WHITE_WINE_DAY_QTY_MCONDITION").withAnswerCondition("WHITE_WINE_DAY_QTY_ACONDITION_1", "ALCOHOL_FREQUENCY", _4TO5_WEEK);
    builder.inCondition("WHITE_WINE_DAY_QTY_MCONDITION").withAnswerCondition("WHITE_WINE_DAY_QTY_ACONDITION_2", "ALCOHOL_FREQUENCY", _2TO3_WEEK);
    builder.inCondition("WHITE_WINE_DAY_QTY_MCONDITION").withAnswerCondition("WHITE_WINE_DAY_QTY_ACONDITION_3", "ALCOHOL_FREQUENCY", WEEKLY);
    builder.inSection("ALCOHOL_INTAKE").withPage("39").withQuestion("BEER_DAY_QTY").withSharedCategory(ALCOHOL_DAY_QTY);
    builder.inQuestion("BEER_DAY_QTY").withSharedCategory(PNA, "888");
    builder.inQuestion("BEER_DAY_QTY").withSharedCategory(DNK, "999");
    builder.inQuestion("BEER_DAY_QTY").setMultipleCondition("BEER_DAY_QTY_MCONDITION", ConditionOperator.OR).withAnswerCondition("BEER_DAY_QTY_ACONDITION_0", "ALCOHOL_FREQUENCY", DAILY);
    builder.inCondition("BEER_DAY_QTY_MCONDITION").withAnswerCondition("BEER_DAY_QTY_ACONDITION_1", "ALCOHOL_FREQUENCY", _4TO5_WEEK);
    builder.inCondition("BEER_DAY_QTY_MCONDITION").withAnswerCondition("BEER_DAY_QTY_ACONDITION_2", "ALCOHOL_FREQUENCY", _2TO3_WEEK);
    builder.inCondition("BEER_DAY_QTY_MCONDITION").withAnswerCondition("BEER_DAY_QTY_ACONDITION_3", "ALCOHOL_FREQUENCY", WEEKLY);
    builder.inSection("ALCOHOL_INTAKE").withPage("40").withQuestion("LIQUOR_DAY_QTY").withSharedCategory(ALCOHOL_DAY_QTY);
    builder.inQuestion("LIQUOR_DAY_QTY").withSharedCategory(PNA, "888");
    builder.inQuestion("LIQUOR_DAY_QTY").withSharedCategory(DNK, "999");
    builder.inQuestion("LIQUOR_DAY_QTY").setMultipleCondition("LIQUOR_DAY_QTY_MCONDITION", ConditionOperator.OR).withAnswerCondition("LIQUOR_DAY_QTY_ACONDITION_0", "ALCOHOL_FREQUENCY", DAILY);
    builder.inCondition("LIQUOR_DAY_QTY_MCONDITION").withAnswerCondition("LIQUOR_DAY_QTY_ACONDITION_1", "ALCOHOL_FREQUENCY", _4TO5_WEEK);
    builder.inCondition("LIQUOR_DAY_QTY_MCONDITION").withAnswerCondition("LIQUOR_DAY_QTY_ACONDITION_2", "ALCOHOL_FREQUENCY", _2TO3_WEEK);
    builder.inCondition("LIQUOR_DAY_QTY_MCONDITION").withAnswerCondition("LIQUOR_DAY_QTY_ACONDITION_3", "ALCOHOL_FREQUENCY", WEEKLY);
    builder.inSection("ALCOHOL_INTAKE").withPage("41").withQuestion("OTHER_ALCOHOL_DAY").withSharedCategory(N, "0");
    builder.inQuestion("OTHER_ALCOHOL_DAY").withSharedCategory(Y, "1");
    builder.inQuestion("OTHER_ALCOHOL_DAY").withSharedCategory(PNA, "8");
    builder.inQuestion("OTHER_ALCOHOL_DAY").withSharedCategory(DNK, "9");
    builder.inQuestion("OTHER_ALCOHOL_DAY").setMultipleCondition("OTHER_ALCOHOL_DAY_MCONDITION", ConditionOperator.OR).withAnswerCondition("OTHER_ALCOHOL_DAY_ACONDITION_0", "ALCOHOL_FREQUENCY", DAILY);
    builder.inCondition("OTHER_ALCOHOL_DAY_MCONDITION").withAnswerCondition("OTHER_ALCOHOL_DAY_ACONDITION_1", "ALCOHOL_FREQUENCY", _4TO5_WEEK);
    builder.inCondition("OTHER_ALCOHOL_DAY_MCONDITION").withAnswerCondition("OTHER_ALCOHOL_DAY_ACONDITION_2", "ALCOHOL_FREQUENCY", _2TO3_WEEK);
    builder.inCondition("OTHER_ALCOHOL_DAY_MCONDITION").withAnswerCondition("OTHER_ALCOHOL_DAY_ACONDITION_3", "ALCOHOL_FREQUENCY", WEEKLY);
    builder.inSection("ALCOHOL_INTAKE").withPage("42").withQuestion("OTHER_ALCOHOL_DAY_ID").withSharedCategory(OPEN_AN);
    builder.inQuestion("OTHER_ALCOHOL_DAY_ID").withSharedCategory(PNA, "88");
    builder.inQuestion("OTHER_ALCOHOL_DAY_ID").withSharedCategory(DNK, "99");
    builder.inQuestion("OTHER_ALCOHOL_DAY_ID").setAnswerCondition("OTHER_ALCOHOL_DAY_ID_ACONDITION", "OTHER_ALCOHOL_DAY", Y);
    builder.inSection("ALCOHOL_INTAKE").withPage("43").withQuestion("OTHER_ALCOHOL_DAY_QTY").withSharedCategory(ALCOHOL_DAY_QTY);
    builder.inQuestion("OTHER_ALCOHOL_DAY_QTY").withSharedCategory(PNA, "888");
    builder.inQuestion("OTHER_ALCOHOL_DAY_QTY").withSharedCategory(DNK, "999");
    builder.inQuestion("OTHER_ALCOHOL_DAY_QTY").setAnswerCondition("OTHER_ALCOHOL_DAY_QTY_ACONDITION", "OTHER_ALCOHOL_DAY", Y);
    builder.inSection("ALCOHOL_INTAKE").withPage("44").withQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ").withSharedCategory(DAILY, "8");
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
    builder.inQuestion("BINGE_DRINKING_MALE_HEAVY_FREQ").setMultipleCondition("BINGE_DRINKING_MALE_HEAVY_FREQ_MCONDITION_0", ConditionOperator.AND).withAnswerCondition("BINGE_DRINKING_MALE_HEAVY_FREQ_ACONDITION_0", "SEX", "MALE");
    builder.inCondition("BINGE_DRINKING_MALE_HEAVY_FREQ_MCONDITION_0").withMultipleCondition("BINGE_DRINKING_MALE_HEAVY_FREQ_MCONDITION_1", ConditionOperator.OR).withAnswerCondition("BINGE_DRINKING_MALE_HEAVY_FREQ_ACONDITION_1", "ALCOHOL_FREQUENCY", DAILY);
    builder.inCondition("BINGE_DRINKING_MALE_HEAVY_FREQ_MCONDITION_1").withAnswerCondition("BINGE_DRINKING_MALE_HEAVY_FREQ_ACONDITION_2", "ALCOHOL_FREQUENCY", _4TO5_WEEK);
    builder.inCondition("BINGE_DRINKING_MALE_HEAVY_FREQ_MCONDITION_1").withAnswerCondition("BINGE_DRINKING_MALE_HEAVY_FREQ_ACONDITION_3", "ALCOHOL_FREQUENCY", _2TO3_WEEK);
    builder.inCondition("BINGE_DRINKING_MALE_HEAVY_FREQ_MCONDITION_1").withAnswerCondition("BINGE_DRINKING_MALE_HEAVY_FREQ_ACONDITION_4", "ALCOHOL_FREQUENCY", WEEKLY);
    builder.inPage("44").withQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(_2TO3_MONTH, "4");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(MONTHLY, "3");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(_6TO11_YEAR, "2");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(_1TO5_YEAR, "1");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(NEVER, "0");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(PNA, "88");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").withSharedCategory(DNK, "99");
    builder.inQuestion("BINGE_DRINKING_MALE_MOD_FREQ").setMultipleCondition("BINGE_DRINKING_MALE_MOD_FREQ_MCONDITION_0", ConditionOperator.AND).withAnswerCondition("BINGE_DRINKING_MALE_MOD_FREQ_ACONDITION_0", "SEX", "MALE");
    builder.inCondition("BINGE_DRINKING_MALE_MOD_FREQ_MCONDITION_0").withMultipleCondition("BINGE_DRINKING_MALE_MOD_FREQ_MCONDITION_1", ConditionOperator.OR).withAnswerCondition("BINGE_DRINKING_MALE_MOD_FREQ_ACONDITION_1", "ALCOHOL_FREQUENCY", _2TO3_MONTH);
    builder.inCondition("BINGE_DRINKING_MALE_MOD_FREQ_MCONDITION_1").withAnswerCondition("BINGE_DRINKING_MALE_MOD_FREQ_ACONDITION_2", "ALCOHOL_FREQUENCY", MONTHLY);
    builder.inCondition("BINGE_DRINKING_MALE_MOD_FREQ_MCONDITION_1").withAnswerCondition("BINGE_DRINKING_MALE_MOD_FREQ_ACONDITION_3", "ALCOHOL_FREQUENCY", LESS_MONTHLY);
    builder.inSection("ALCOHOL_INTAKE").withPage("45").withQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ").withSharedCategory(DAILY, "8");
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
    builder.inQuestion("BINGE_DRINKING_FEMALE_HEAVY_FREQ").setMultipleCondition("BINGE_DRINKING_FEMALE_HEAVY_FREQ_MCONDITION_0", ConditionOperator.AND).withAnswerCondition("BINGE_DRINKING_FEMALE_HEAVY_FREQ_ACONDITION_0", "SEX", "MALE");
    builder.inCondition("BINGE_DRINKING_FEMALE_HEAVY_FREQ_MCONDITION_0").withMultipleCondition("BINGE_DRINKING_FEMALE_HEAVY_FREQ_MCONDITION_1", ConditionOperator.OR).withAnswerCondition("BINGE_DRINKING_FEMALE_HEAVY_FREQ_ACONDITION_1", "ALCOHOL_FREQUENCY", DAILY);
    builder.inCondition("BINGE_DRINKING_FEMALE_HEAVY_FREQ_MCONDITION_1").withAnswerCondition("BINGE_DRINKING_FEMALE_HEAVY_FREQ_ACONDITION_2", "ALCOHOL_FREQUENCY", _4TO5_WEEK);
    builder.inCondition("BINGE_DRINKING_FEMALE_HEAVY_FREQ_MCONDITION_1").withAnswerCondition("BINGE_DRINKING_FEMALE_HEAVY_FREQ_ACONDITION_3", "ALCOHOL_FREQUENCY", _2TO3_WEEK);
    builder.inCondition("BINGE_DRINKING_FEMALE_HEAVY_FREQ_MCONDITION_1").withAnswerCondition("BINGE_DRINKING_FEMALE_HEAVY_FREQ_ACONDITION_4", "ALCOHOL_FREQUENCY", WEEKLY);
    builder.inPage("45").withQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").withSharedCategory(_2TO3_MONTH, "4");
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").withSharedCategory(MONTHLY, "3");
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").withSharedCategory(_6TO11_YEAR, "2");
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").withSharedCategory(_1TO5_YEAR, "1");
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").withSharedCategory(NEVER, "0");
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").withSharedCategory(PNA, "88");
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").withSharedCategory(DNK, "99");
    builder.inQuestion("BINGE_DRINKING_FEMALE_MOD_FREQ").setMultipleCondition("BINGE_DRINKING_FEMALE_MOD_FREQ_MCONDITION_0", ConditionOperator.AND).withAnswerCondition("BINGE_DRINKING_FEMALE_MOD_FREQ_ACONDITION_0", "SEX", "MALE");
    builder.inCondition("BINGE_DRINKING_FEMALE_MOD_FREQ_MCONDITION_0").withMultipleCondition("BINGE_DRINKING_FEMALE_MOD_FREQ_MCONDITION_1", ConditionOperator.OR).withAnswerCondition("BINGE_DRINKING_FEMALE_MOD_FREQ_ACONDITION_1", "ALCOHOL_FREQUENCY", _2TO3_MONTH);
    builder.inCondition("BINGE_DRINKING_FEMALE_MOD_FREQ_MCONDITION_1").withAnswerCondition("BINGE_DRINKING_FEMALE_MOD_FREQ_ACONDITION_2", "ALCOHOL_FREQUENCY", MONTHLY);
    builder.inCondition("BINGE_DRINKING_FEMALE_MOD_FREQ_MCONDITION_1").withAnswerCondition("BINGE_DRINKING_FEMALE_MOD_FREQ_ACONDITION_3", "ALCOHOL_FREQUENCY", LESS_MONTHLY);
    builder.inPage("45").addTimestamp("TS_AI");

    return builder;
  }
}
