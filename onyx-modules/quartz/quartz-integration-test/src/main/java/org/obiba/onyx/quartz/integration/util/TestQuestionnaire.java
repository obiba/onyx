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

import org.apache.wicket.validation.validator.RangeValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanelFactory;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class TestQuestionnaire {

  private static final String N = "N";

  private static final String Y = "Y";

  private static final String OTHER = "OTHER";

  private static final String PNA = "PNA";

  private static final String DNK = "DNK";

  public static QuestionnaireBuilder buildQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("Test", "1.0");

    builder.withSection("S0").withPage("P00").withQuestion("BP_QUESTION");
    builder.inPage("P00").withQuestion("REAL_QUESTION").withCategories(Y, N);

    builder.inSection("S0").withPage("P0").withQuestion("ARRAY_CONDITION").withCategories(Y, N);
    builder.inQuestion("ARRAY_CONDITION").withQuestion("A");
    builder.inQuestion("ARRAY_CONDITION").withQuestion("B").setCondition("A", "Y");

    builder.withSection("S1").withPage("P1").withQuestion("OPEN_QUESTION").withCategory("OPEN").withOpenAnswerDefinition("OPEN_MULTIPLE", DataType.TEXT);
    builder.inOpenAnswerDefinition("OPEN_MULTIPLE").withOpenAnswerDefinition("CHOICE1", DataType.TEXT).setDefaultData("A", "B", "C").setRequired(true);
    builder.inOpenAnswerDefinition("OPEN_MULTIPLE").withOpenAnswerDefinition("CHOICE2", DataType.TEXT).setDefaultData("X", "Y", "Z").setRequired(true);
    builder.inQuestion("OPEN_QUESTION").withSharedCategory(OTHER).withOpenAnswerDefinition("NUMERIC", DataType.INTEGER).addValidator(new RangeValidator(1, 4)).setSize(2);

    builder.inPage("P1").withQuestion("Q_MULTIPLE", true).withCategories("1", "2", "3");
    builder.inQuestion("Q_MULTIPLE").withSharedCategory(OTHER);
    builder.inQuestion("Q_MULTIPLE").withSharedCategory(PNA).setEscape(true).withSharedCategory(DNK).setEscape(true);
    builder.inQuestion("Q_MULTIPLE").withCategory("FOO").setEscape(true).withOpenAnswerDefinition("TEXT", DataType.TEXT);

    builder.inPage("P1").withQuestion("ARRAY").withCategories(Y, N).withSharedCategories(PNA, DNK);
    builder.inQuestion("ARRAY").withQuestion("Q1");
    builder.inQuestion("ARRAY").withQuestion("Q2");

    builder.inPage("P1").withQuestion("ARRAY_OPEN", true).withCategory("MONDAY").withOpenAnswerDefinition("MONDAY_QUANTITY", DataType.INTEGER).setSize(2);
    builder.inQuestion("ARRAY_OPEN").withCategory("TUESDAY").withOpenAnswerDefinition("TUESDAY_QUANTITY", DataType.INTEGER).setSize(2);
    builder.inQuestion("ARRAY_OPEN").withQuestion("RED_WINE", true);
    builder.inQuestion("ARRAY_OPEN").withQuestion("WHITE_WINE", true);
    builder.inQuestion("ARRAY_OPEN").withSharedCategory(PNA).setEscape(true);
    builder.inQuestion("ARRAY_OPEN").withSharedCategory(DNK).setEscape(true);

    builder.inPage("P1").withQuestion("Q_INNER_CONDITION").setCondition("$1 || $2>1 || $3>1", builder.newDataSource("Q_MULTIPLE", "PNA"), builder.newDataSource("OPEN_QUESTION", "OTHER", "NUMERIC"), builder.newDataSource("Q_MULTIPLE", "OTHER", "NUMERIC"));
    builder.inQuestion("Q_INNER_CONDITION").withCategories("1", "2", "3");

    builder.inSection("S1").withPage("P2").withQuestion("DATE_QUESTION").withCategory("DATE").withOpenAnswerDefinition("OPEN_DATE", DataType.DATE);

    builder.inSection("S1").withPage("P3").withQuestion("DD", DropDownQuestionPanelFactory.class);
    builder.inQuestion("DD").withCategories("A", "B", "C").withSharedCategory(OTHER);
    builder.inQuestion("DD").withSharedCategories(PNA, DNK);

    builder.inPage("P3").withQuestion("DD_DEPENDENT").withCategories("1", "2", "3");
    builder.inQuestion("DD_DEPENDENT").setCondition("$1 && (!$2 && !$3 && !$4)", builder.newDataSource("DD", "*"), builder.newDataSource("DD", "A"), builder.newDataSource("DD", PNA), builder.newDataSource("DD", DNK));

    builder.inSection("S1").withPage("P4").withQuestion("COUNTRY_BIRTH_LONG", DropDownQuestionPanelFactory.class);
    buildCountryListLong(builder, "COUNTRY_BIRTH_LONG");
    builder.inQuestion("COUNTRY_BIRTH_LONG").withSharedCategory(DNK, "9999");
    builder.inQuestion("COUNTRY_BIRTH_LONG").withSharedCategory(PNA, "8888");

    return builder;
  }

  private static void buildCountryListLong(QuestionnaireBuilder builder, String questionName) {

    builder.inQuestion(questionName).withSharedCategory(AFGHANISTAN, "3");
    builder.inQuestion(questionName).withSharedCategory(SOUTH_AFRICA, "244");
    builder.inQuestion(questionName).withSharedCategory(ALAND_ISLANDS, "16");
    builder.inQuestion(questionName).withSharedCategory(ALBANIA, "6");
    builder.inQuestion(questionName).withSharedCategory(ALGERIA, "61");
    builder.inQuestion(questionName).withSharedCategory(ANDORRA, "1");
    builder.inQuestion(questionName).withSharedCategory(ANGOLA, "9");
    builder.inQuestion(questionName).withSharedCategory(ANGUILLA, "5");
    builder.inQuestion(questionName).withSharedCategory(ANTARTICA, "10");
    builder.inQuestion(questionName).withSharedCategory(ANTIGUA_AND_BARBURA, "4");
    builder.inQuestion(questionName).withSharedCategory(NETHERLANDS_ANTILLES, "8");
    builder.inQuestion(questionName).withSharedCategory(SAUDI_ARABIA, "192");
    builder.inQuestion(questionName).withSharedCategory(ARGENTINA, "11");
    builder.inQuestion(questionName).withSharedCategory(ARMENIA, "7");
    builder.inQuestion(questionName).withSharedCategory(ARUBA, "15");
    builder.inQuestion(questionName).withSharedCategory(AUSTRALIA, "14");
    builder.inQuestion(questionName).withSharedCategory(AUSTRIA, "13");
    builder.inQuestion(questionName).withSharedCategory(AZERBAIJAN, "17");
    builder.inQuestion(questionName).withSharedCategory(BAHAMAS, "32");
    builder.inQuestion(questionName).withSharedCategory(BAHRAIN, "24");
    builder.inQuestion(questionName).withSharedCategory(BANGLADESH, "20");
    builder.inQuestion(questionName).withSharedCategory(BARBADOS, "19");
    builder.inQuestion(questionName).withSharedCategory(BELARUS, "36");
    builder.inQuestion(questionName).withSharedCategory(BELGIUM, "21");
    builder.inQuestion(questionName).withSharedCategory(BELIZE, "37");
    builder.inQuestion(questionName).withSharedCategory(BENIN, "26");
    builder.inQuestion(questionName).withSharedCategory(BERMUDA, "28");
    builder.inQuestion(questionName).withSharedCategory(BHUTAN, "33");
    builder.inQuestion(questionName).withSharedCategory(BOLIVIA, "30");
    builder.inQuestion(questionName).withSharedCategory(BOSNIA_AND_HERZEGOVINA, "18");
    builder.inQuestion(questionName).withSharedCategory(BOSTWANA, "35");
    builder.inQuestion(questionName).withSharedCategory(BOUVET_ISLAND, "34");
    builder.inQuestion(questionName).withSharedCategory(BRAZIL, "31");
    builder.inQuestion(questionName).withSharedCategory(BRUNEI_DARUSSALAM, "29");
    builder.inQuestion(questionName).withSharedCategory(BULGARIA, "23");
    builder.inQuestion(questionName).withSharedCategory(BURKINA_FASO, "22");
    builder.inQuestion(questionName).withSharedCategory(BURUNDI, "25");
    builder.inQuestion(questionName).withSharedCategory(CAYMAN_ISLANDS, "123");
    builder.inQuestion(questionName).withSharedCategory(CAMBODIA, "116");
    builder.inQuestion(questionName).withSharedCategory(CAMEROON, "47");
    builder.inQuestion(questionName).withSharedCategory(CAPE_VERDE, "52");
    builder.inQuestion(questionName).withSharedCategory(CENTRAL_AFRICAN_REPUBLIC, "41");
    builder.inQuestion(questionName).withSharedCategory(CHILE, "46");
    builder.inQuestion(questionName).withSharedCategory(CHRISTMAS_ISLAND, "53");
    builder.inQuestion(questionName).withSharedCategory(CYPRUS, "54");
    builder.inQuestion(questionName).withSharedCategory(COCOS_ISLANDS, "39");
    builder.inQuestion(questionName).withSharedCategory(COLOMBIA, "49");
    builder.inQuestion(questionName).withSharedCategory(COMOROS, "118");
    builder.inQuestion(questionName).withSharedCategory(CONGO, "42");
    builder.inQuestion(questionName).withSharedCategory(CONGO_DEMOCRATIC_REPUBLIC, "40");
    builder.inQuestion(questionName).withSharedCategory(COOK_ISLANDS, "45");
    builder.inQuestion(questionName).withSharedCategory(KOREA_REPUBLIC, "121");
    builder.inQuestion(questionName).withSharedCategory(KOREA_DEMOCRATIC_PEOPLES_REPUBLIC, "120");
    builder.inQuestion(questionName).withSharedCategory(COSTA_RICA, "50");
    builder.inQuestion(questionName).withSharedCategory(COTE_DIVOIRE, "44");
    builder.inQuestion(questionName).withSharedCategory(CROATIA, "97");
    builder.inQuestion(questionName).withSharedCategory(CUBA, "51");
    builder.inQuestion(questionName).withSharedCategory(DENMARK, "58");
    builder.inQuestion(questionName).withSharedCategory(DJIBOUTI, "57");
    builder.inQuestion(questionName).withSharedCategory(DOMINICAN_REPUBLIC, "60");
    builder.inQuestion(questionName).withSharedCategory(DOMINICA, "59");
    builder.inQuestion(questionName).withSharedCategory(EGYPT, "64");
    builder.inQuestion(questionName).withSharedCategory(EL_SALVADOR, "209");
    builder.inQuestion(questionName).withSharedCategory(UNITED_ARAB_EMIRATES, "2");
    builder.inQuestion(questionName).withSharedCategory(ECUADOR, "62");
    builder.inQuestion(questionName).withSharedCategory(ERITREA, "66");
    builder.inQuestion(questionName).withSharedCategory(SPAIN, "67");
    builder.inQuestion(questionName).withSharedCategory(ESTONIA, "63");
    builder.inQuestion(questionName).withSharedCategory(ETHIOPIA, "68");
    builder.inQuestion(questionName).withSharedCategory(FALKLAND_ISLANDS, "71");
    builder.inQuestion(questionName).withSharedCategory(FAROE_ISLANDS, "73");
    builder.inQuestion(questionName).withSharedCategory(FIJI, "70");
    builder.inQuestion(questionName).withSharedCategory(FINLAND, "69");
    builder.inQuestion(questionName).withSharedCategory(GABON, "75");
    builder.inQuestion(questionName).withSharedCategory(GAMBIA, "84");
    builder.inQuestion(questionName).withSharedCategory(GEORGIA, "78");
    builder.inQuestion(questionName).withSharedCategory(SOUTH_GEORGIA_SANDWICH_ISLANDS, "89");
    builder.inQuestion(questionName).withSharedCategory(GHANA, "81");
    builder.inQuestion(questionName).withSharedCategory(GIBRALTAR, "82");
    builder.inQuestion(questionName).withSharedCategory(GRENADA, "77");
    builder.inQuestion(questionName).withSharedCategory(GREENLAND, "83");
    builder.inQuestion(questionName).withSharedCategory(GUADELOUPE, "86");
    builder.inQuestion(questionName).withSharedCategory(GUAM, "91");
    builder.inQuestion(questionName).withSharedCategory(GUATEMALA, "90");
    builder.inQuestion(questionName).withSharedCategory(GUERNSEY, "80");
    builder.inQuestion(questionName).withSharedCategory(GUINEA, "85");
    builder.inQuestion(questionName).withSharedCategory(EQUATORIAL_GUINEA, "87");
    builder.inQuestion(questionName).withSharedCategory(GUINEA_BISSAU, "92");
    builder.inQuestion(questionName).withSharedCategory(GUYANA, "93");
    builder.inQuestion(questionName).withSharedCategory(FRENCH_GUIANA, "79");
    builder.inQuestion(questionName).withSharedCategory(HAITI, "98");
    builder.inQuestion(questionName).withSharedCategory(HEARD_MCDONALD_ISLANDS, "95");
    builder.inQuestion(questionName).withSharedCategory(HONDURAS, "96");
    builder.inQuestion(questionName).withSharedCategory(HONG_KONG, "94");
    builder.inQuestion(questionName).withSharedCategory(HUNGARY, "99");
    builder.inQuestion(questionName).withSharedCategory(ISLE_OFMAN, "103");
    builder.inQuestion(questionName).withSharedCategory(UNITED_STATES_MINOR_OUTLYING_ISLANDS, "230");
    builder.inQuestion(questionName).withSharedCategory(VIRGIN_ISLANDS_BRITISH, "236");
    builder.inQuestion(questionName).withSharedCategory(VIRGIN_ISLANDS_US, "237");
    builder.inQuestion(questionName).withSharedCategory(INDONESIA, "100");
    builder.inQuestion(questionName).withSharedCategory(IRAN_ISLAMIC_REPUBLIC, "107");
    builder.inQuestion(questionName).withSharedCategory(IRAQ, "106");
    builder.inQuestion(questionName).withSharedCategory(ICELAND, "108");
    builder.inQuestion(questionName).withSharedCategory(ISRAEL, "102");
    builder.inQuestion(questionName).withSharedCategory(JAPAN, "113");
    builder.inQuestion(questionName).withSharedCategory(JERSEY, "110");
    builder.inQuestion(questionName).withSharedCategory(JORDAN, "112");
    builder.inQuestion(questionName).withSharedCategory(KAZAKHSTAN, "124");
    builder.inQuestion(questionName).withSharedCategory(KENYA, "114");
    builder.inQuestion(questionName).withSharedCategory(KYRGYZSTAN, "115");
    builder.inQuestion(questionName).withSharedCategory(KIRIBATI, "117");
    builder.inQuestion(questionName).withSharedCategory(KUWAIT, "122");
    builder.inQuestion(questionName).withSharedCategory(LAO_PEOPLES_DEMOCRATIC_REPUBLIC, "125");
    builder.inQuestion(questionName).withSharedCategory(LESOTHO, "131");
    builder.inQuestion(questionName).withSharedCategory(LATVIA, "134");
    builder.inQuestion(questionName).withSharedCategory(LEBANON, "126");
    builder.inQuestion(questionName).withSharedCategory(LIBERIA, "130");
    builder.inQuestion(questionName).withSharedCategory(LIBYAN_ARAB_JAMAHIRIYA, "135");
    builder.inQuestion(questionName).withSharedCategory(LIECHTENSTEIN, "128");
    builder.inQuestion(questionName).withSharedCategory(LITHUANIA, "132");
    builder.inQuestion(questionName).withSharedCategory(LUXEMBOURG, "133");
    builder.inQuestion(questionName).withSharedCategory(MACAO, "147");
    builder.inQuestion(questionName).withSharedCategory(MACEDONIA_THE_FORMER_YUGOSLAV_REPUBLIC, "143");
    builder.inQuestion(questionName).withSharedCategory(MADAGASCAR, "141");
    builder.inQuestion(questionName).withSharedCategory(MALAYSIA, "157");
    builder.inQuestion(questionName).withSharedCategory(MALAWI, "155");
    builder.inQuestion(questionName).withSharedCategory(MALDIVES, "154");
    builder.inQuestion(questionName).withSharedCategory(MALI, "144");
    builder.inQuestion(questionName).withSharedCategory(MALTA, "152");
    builder.inQuestion(questionName).withSharedCategory(NORTHERN_MARIANA_ISLANDS, "148");
    builder.inQuestion(questionName).withSharedCategory(MOROCCO, "136");
    builder.inQuestion(questionName).withSharedCategory(MARSHALL_ISLANDS, "142");
    builder.inQuestion(questionName).withSharedCategory(MARTINIQUE, "149");
    builder.inQuestion(questionName).withSharedCategory(MAURITUS, "153");
    builder.inQuestion(questionName).withSharedCategory(MAURITANIA, "150");
    builder.inQuestion(questionName).withSharedCategory(MAYOTTE, "243");
    builder.inQuestion(questionName).withSharedCategory(MEXICO, "156");
    builder.inQuestion(questionName).withSharedCategory(MICRONESIA_FEDERATED_STATES, "72");
    builder.inQuestion(questionName).withSharedCategory(MOLDOVA, "138");
    builder.inQuestion(questionName).withSharedCategory(MONACO, "137");
    builder.inQuestion(questionName).withSharedCategory(MONGOLIA, "146");
    builder.inQuestion(questionName).withSharedCategory(MONTENEGRO, "139");
    builder.inQuestion(questionName).withSharedCategory(MONTSERRAT, "151");
    builder.inQuestion(questionName).withSharedCategory(MOZAMBIQUE, "158");
    builder.inQuestion(questionName).withSharedCategory(MYANMAR, "145");
    builder.inQuestion(questionName).withSharedCategory(NAMIBIA, "159");
    builder.inQuestion(questionName).withSharedCategory(NAURU, "168");
    builder.inQuestion(questionName).withSharedCategory(NEPAL, "167");
    builder.inQuestion(questionName).withSharedCategory(NICARAGUA, "164");
    builder.inQuestion(questionName).withSharedCategory(NIGER, "161");
    builder.inQuestion(questionName).withSharedCategory(NIGERIA, "163");
    builder.inQuestion(questionName).withSharedCategory(NIUE, "169");
    builder.inQuestion(questionName).withSharedCategory(NORFOLK_ISLAND, "162");
    builder.inQuestion(questionName).withSharedCategory(NORWAY, "166");
    builder.inQuestion(questionName).withSharedCategory(NEW_CLAEDONIA, "160");
    builder.inQuestion(questionName).withSharedCategory(NEW_ZEALAND, "170");
    builder.inQuestion(questionName).withSharedCategory(BRITISH_INDIAN_OCEAN_TERRITORY, "105");
    builder.inQuestion(questionName).withSharedCategory(OMAN, "171");
    builder.inQuestion(questionName).withSharedCategory(UGANDA, "229");
    builder.inQuestion(questionName).withSharedCategory(UZBEKISTAN, "233");
    builder.inQuestion(questionName).withSharedCategory(PAKISTAN, "177");
    builder.inQuestion(questionName).withSharedCategory(PALAU, "184");
    builder.inQuestion(questionName).withSharedCategory(PALESTINIAN_TERRITORY_OCCUPIED, "182");
    builder.inQuestion(questionName).withSharedCategory(PANAMA, "172");
    builder.inQuestion(questionName).withSharedCategory(PAPUA_NEW_GUINEA, "175");
    builder.inQuestion(questionName).withSharedCategory(PARAGUAY, "185");
    builder.inQuestion(questionName).withSharedCategory(NETHERLANDS, "165");
    builder.inQuestion(questionName).withSharedCategory(PERU, "173");
    builder.inQuestion(questionName).withSharedCategory(PITCAIRN, "180");
    builder.inQuestion(questionName).withSharedCategory(FRENCH_POLYNESIA, "174");
    builder.inQuestion(questionName).withSharedCategory(PUERTO_RICO, "181");
    builder.inQuestion(questionName).withSharedCategory(QATAR, "186");
    builder.inQuestion(questionName).withSharedCategory(REUNION, "187");
    builder.inQuestion(questionName).withSharedCategory(ROMANIA, "188");
    builder.inQuestion(questionName).withSharedCategory(RWANDA, "191");
    builder.inQuestion(questionName).withSharedCategory(WESTERN_SAHARA, "65");
    builder.inQuestion(questionName).withSharedCategory(SAINT_BATHELEMY, "27");
    builder.inQuestion(questionName).withSharedCategory(SAINT_HELENA, "199");
    builder.inQuestion(questionName).withSharedCategory(SAINT_LUCIA, "127");
    builder.inQuestion(questionName).withSharedCategory(SAINT_KITTS_AND_NEVIS, "119");
    builder.inQuestion(questionName).withSharedCategory(SAN_MARINO, "204");
    builder.inQuestion(questionName).withSharedCategory(SAINT_MARTIN, "140");
    builder.inQuestion(questionName).withSharedCategory(SAINT_PIERRE_AND_MIQUELON, "179");
    builder.inQuestion(questionName).withSharedCategory(SAINT_VINCENT_AND_GRENADINES, "234");
    builder.inQuestion(questionName).withSharedCategory(SOLOMON_ISLANDS, "193");
    builder.inQuestion(questionName).withSharedCategory(SAMOA, "241");
    builder.inQuestion(questionName).withSharedCategory(AMERICAN_SAMOA, "12");
    builder.inQuestion(questionName).withSharedCategory(SAO_TOME_AND_PRICINPE, "208");
    builder.inQuestion(questionName).withSharedCategory(SENEGAL, "205");
    builder.inQuestion(questionName).withSharedCategory(SERBIA, "189");
    builder.inQuestion(questionName).withSharedCategory(SEYCHELLES, "194");
    builder.inQuestion(questionName).withSharedCategory(SIERRA_LEONE, "203");
    builder.inQuestion(questionName).withSharedCategory(SINGAPORE, "198");
    builder.inQuestion(questionName).withSharedCategory(SLOVAKIA, "202");
    builder.inQuestion(questionName).withSharedCategory(SLOVENIA, "200");
    builder.inQuestion(questionName).withSharedCategory(SOMALIA, "206");
    builder.inQuestion(questionName).withSharedCategory(SUDAN, "195");
    builder.inQuestion(questionName).withSharedCategory(SRI_LANKA, "129");
    builder.inQuestion(questionName).withSharedCategory(SWEDEN, "196");
    builder.inQuestion(questionName).withSharedCategory(SWITZERLAND, "43");
    builder.inQuestion(questionName).withSharedCategory(SURINAME, "207");
    builder.inQuestion(questionName).withSharedCategory(SVALBARD_AND_JAN_MAYEN, "201");
    builder.inQuestion(questionName).withSharedCategory(SWAZILAND, "211");
    builder.inQuestion(questionName).withSharedCategory(SYRIAN_ARAB_RAPUBLIC, "210");
    builder.inQuestion(questionName).withSharedCategory(TAJIKISTAN, "217");
    builder.inQuestion(questionName).withSharedCategory(TAWAIN_PROVINCE_CHINA, "226");
    builder.inQuestion(questionName).withSharedCategory(TANZANIA_UNITED_REPUBLIC, "227");
    builder.inQuestion(questionName).withSharedCategory(CHAD, "213");
    builder.inQuestion(questionName).withSharedCategory(CZECH_REPUBLIC, "55");
    builder.inQuestion(questionName).withSharedCategory(FRENCH_SOUTHERN_TERRITORIES, "214");
    builder.inQuestion(questionName).withSharedCategory(THAILAND, "216");
    builder.inQuestion(questionName).withSharedCategory(TIMOR_LESTE, "219");
    builder.inQuestion(questionName).withSharedCategory(TOGO, "215");
    builder.inQuestion(questionName).withSharedCategory(TOKELAU, "218");
    builder.inQuestion(questionName).withSharedCategory(TONGA, "222");
    builder.inQuestion(questionName).withSharedCategory(TRINIDAD_AND_TOBAGO, "224");
    builder.inQuestion(questionName).withSharedCategory(TUNISIA, "221");
    builder.inQuestion(questionName).withSharedCategory(TURKMENISTAN, "220");
    builder.inQuestion(questionName).withSharedCategory(TURKS_AND_CAICOS_ISLANDS, "212");
    builder.inQuestion(questionName).withSharedCategory(TURKEY, "223");
    builder.inQuestion(questionName).withSharedCategory(TUVALU, "225");
    builder.inQuestion(questionName).withSharedCategory(UKRAINE, "228");
    builder.inQuestion(questionName).withSharedCategory(URUGUAY, "232");
    builder.inQuestion(questionName).withSharedCategory(VANUATU, "239");
    builder.inQuestion(questionName).withSharedCategory(VATICAN_CITY_STATE, "197");
    builder.inQuestion(questionName).withSharedCategory(VENEZUELA, "235");
    builder.inQuestion(questionName).withSharedCategory(VIET_NAM, "238");
    builder.inQuestion(questionName).withSharedCategory(WALLIS_AND_FUTUNA, "240");
    builder.inQuestion(questionName).withSharedCategory(YEMEN, "242");
    builder.inQuestion(questionName).withSharedCategory(ZAMBIA, "245");
    builder.inQuestion(questionName).withSharedCategory(ZIMBABWE, "246");

  }

  private final static String AFGHANISTAN = "AFGHANISTAN";

  private final static String SOUTH_AFRICA = "SOUTH_AFRICA";

  private final static String ALAND_ISLANDS = "ALAND_ISLANDS";

  private final static String ALBANIA = "ALBANIA";

  private final static String ALGERIA = "ALGERIA";

  private final static String GERMANY = "GERMANY";

  private final static String ANDORRA = "ANDORRA";

  private final static String ANGOLA = "ANGOLA";

  private final static String ANGUILLA = "ANGUILLA";

  private final static String ANTARTICA = "ANTARTICA";

  private final static String ANTIGUA_AND_BARBURA = "ANTIGUA_AND_BARBURA";

  private final static String NETHERLANDS_ANTILLES = "NETHERLANDS_ANTILLES";

  private final static String SAUDI_ARABIA = "SAUDI_ARABIA";

  private final static String ARGENTINA = "ARGENTINA";

  private final static String ARMENIA = "ARMENIA";

  private final static String ARUBA = "ARUBA";

  private final static String AUSTRALIA = "AUSTRALIA";

  private final static String AUSTRIA = "AUSTRIA";

  private final static String AZERBAIJAN = "AZERBAIJAN";

  private final static String BAHAMAS = "BAHAMAS";

  private final static String BAHRAIN = "BAHRAIN";

  private final static String BANGLADESH = "BANGLADESH";

  private final static String BARBADOS = "BARBADOS";

  private final static String BELARUS = "BELARUS";

  private final static String BELGIUM = "BELGIUM";

  private final static String BELIZE = "BELIZE";

  private final static String BENIN = "BENIN";

  private final static String BERMUDA = "BERMUDA";

  private final static String BHUTAN = "BHUTAN";

  private final static String BOLIVIA = "BOLIVIA";

  private final static String BOSNIA_AND_HERZEGOVINA = "BOSNIA_AND_HERZEGOVINA";

  private final static String BOSTWANA = "BOSTWANA";

  private final static String BOUVET_ISLAND = "BOUVET_ISLAND";

  private final static String BRAZIL = "BRAZIL";

  private final static String BRUNEI_DARUSSALAM = "BRUNEI_DARUSSALAM";

  private final static String BULGARIA = "BULGARIA";

  private final static String BURKINA_FASO = "BURKINA_FASO";

  private final static String BURUNDI = "BURUNDI";

  private final static String CAYMAN_ISLANDS = "CAYMAN_ISLANDS";

  private final static String CAMBODIA = "CAMBODIA";

  private final static String CAMEROON = "CAMEROON";

  private final static String CANADA = "CANADA";

  private final static String CAPE_VERDE = "CAPE_VERDE";

  private final static String CENTRAL_AFRICAN_REPUBLIC = "CENTRAL_AFRICAN_REPUBLIC";

  private final static String CHILE = "CHILE";

  private final static String CHINA = "CHINA";

  private final static String CHRISTMAS_ISLAND = "CHRISTMAS_ISLAND";

  private final static String CYPRUS = "CYPRUS";

  private final static String COCOS_ISLANDS = "COCOS_ISLANDS";

  private final static String COLOMBIA = "COLOMBIA";

  private final static String COMOROS = "COMOROS";

  private final static String CONGO = "CONGO";

  private final static String CONGO_DEMOCRATIC_REPUBLIC = "CONGO_DEMOCRATIC_REPUBLIC";

  private final static String COOK_ISLANDS = "COOK_ISLANDS";

  private final static String KOREA_REPUBLIC = "KOREA_REPUBLIC";

  private final static String KOREA_DEMOCRATIC_PEOPLES_REPUBLIC = "KOREA_DEMOCRATIC_PEOPLES_REPUBLIC";

  private final static String COSTA_RICA = "COSTA_RICA";

  private final static String COTE_DIVOIRE = "COTE_DIVOIRE";

  private final static String CROATIA = "CROATIA";

  private final static String CUBA = "CUBA";

  private final static String DENMARK = "DENMARK";

  private final static String DJIBOUTI = "DJIBOUTI";

  private final static String DOMINICAN_REPUBLIC = "DOMINICAN_REPUBLIC";

  private final static String DOMINICA = "DOMINICA";

  private final static String EGYPT = "EGYPT";

  private final static String EL_SALVADOR = "EL_SALVADOR";

  private final static String UNITED_ARAB_EMIRATES = "UNITED_ARAB_EMIRATES";

  private final static String ECUADOR = "ECUADOR";

  private final static String ERITREA = "ERITREA";

  private final static String SPAIN = "SPAIN";

  private final static String ESTONIA = "ESTONIA";

  private final static String UNITED_STATES = "UNITED_STATES";

  private final static String ETHIOPIA = "ETHIOPIA";

  private final static String FALKLAND_ISLANDS = "FALKLAND_ISLANDS";

  private final static String FAROE_ISLANDS = "FAROE_ISLANDS";

  private final static String FIJI = "FIJI";

  private final static String FINLAND = "FINLAND";

  private final static String FRANCE = "FRANCE";

  private final static String GABON = "GABON";

  private final static String GAMBIA = "GAMBIA";

  private final static String GEORGIA = "GEORGIA";

  private final static String SOUTH_GEORGIA_SANDWICH_ISLANDS = "SOUTH_GEORGIA_SANDWICH_ISLANDS";

  private final static String GHANA = "GHANA";

  private final static String GIBRALTAR = "GIBRALTAR";

  private final static String GREECE = "GREECE";

  private final static String GRENADA = "GRENADA";

  private final static String GREENLAND = "GREENLAND";

  private final static String GUADELOUPE = "GUADELOUPE";

  private final static String GUAM = "GUAM";

  private final static String GUATEMALA = "GUATEMALA";

  private final static String GUERNSEY = "GUERNSEY";

  private final static String GUINEA = "GUINEA";

  private final static String EQUATORIAL_GUINEA = "EQUATORIAL_GUINEA";

  private final static String GUINEA_BISSAU = "GUINEA_BISSAU";

  private final static String GUYANA = "GUYANA";

  private final static String FRENCH_GUIANA = "FRENCH_GUIANA";

  private final static String HAITI = "HAITI";

  private final static String HEARD_MCDONALD_ISLANDS = "HEARD_MCDONALD_ISLANDS";

  private final static String HONDURAS = "HONDURAS";

  private final static String HONG_KONG = "HONG_KONG";

  private final static String HUNGARY = "HUNGARY";

  private final static String ISLE_OFMAN = "ISLE_OFMAN";

  private final static String UNITED_STATES_MINOR_OUTLYING_ISLANDS = "UNITED_STATES_MINOR_OUTLYING_ISLANDS";

  private final static String VIRGIN_ISLANDS_BRITISH = "VIRGIN_ISLANDS_BRITISH";

  private final static String VIRGIN_ISLANDS_US = "VIRGIN_ISLANDS_US";

  private final static String INDIA = "INDIA";

  private final static String INDONESIA = "INDONESIA";

  private final static String IRAN_ISLAMIC_REPUBLIC = "IRAN_ISLAMIC_REPUBLIC";

  private final static String IRAQ = "IRAQ";

  private final static String IRELAND = "IRELAND";

  private final static String ICELAND = "ICELAND";

  private final static String ISRAEL = "ISRAEL";

  private final static String ITALY = "ITALY";

  private final static String JAMAICA = "JAMAICA";

  private final static String JAPAN = "JAPAN";

  private final static String JERSEY = "JERSEY";

  private final static String JORDAN = "JORDAN";

  private final static String KAZAKHSTAN = "KAZAKHSTAN";

  private final static String KENYA = "NKENYA";

  private final static String KYRGYZSTAN = "KYRGYZSTAN";

  private final static String KIRIBATI = "KIRIBATI";

  private final static String KUWAIT = "KUWAIT";

  private final static String LAO_PEOPLES_DEMOCRATIC_REPUBLIC = "LAO_PEOPLES_DEMOCRATIC_REPUBLIC";

  private final static String LESOTHO = "LESOTHO";

  private final static String LATVIA = "LATVIA";

  private final static String LEBANON = "LEBANON";

  private final static String LIBERIA = "LIBERIA";

  private final static String LIBYAN_ARAB_JAMAHIRIYA = "LIBYAN_ARAB_JAMAHIRIYA";

  private final static String LIECHTENSTEIN = "LIECHTENSTEIN";

  private final static String LITHUANIA = "LITHUANIA";

  private final static String LUXEMBOURG = "LUXEMBOURG";

  private final static String MACAO = "MACAO";

  private final static String MACEDONIA_THE_FORMER_YUGOSLAV_REPUBLIC = "MACEDONIA_THE_FORMER_YUGOSLAV_REPUBLIC";

  private final static String MADAGASCAR = "MADAGASCAR";

  private final static String MALAYSIA = "MALAYSIA";

  private final static String MALAWI = "MALAWI";

  private final static String MALDIVES = "MALDIVES";

  private final static String MALI = "MALI";

  private final static String MALTA = "MALTA";

  private final static String NORTHERN_MARIANA_ISLANDS = "NORTHERN_MARIANA_ISLANDS";

  private final static String MOROCCO = "MOROCCO";

  private final static String MARSHALL_ISLANDS = "MARSHALL_ISLANDS";

  private final static String MARTINIQUE = "MARTINIQUE";

  private final static String MAURITUS = "MAURITUS";

  private final static String MAURITANIA = "MAURITANIA";

  private final static String MAYOTTE = "MAYOTTE";

  private final static String MEXICO = "MEXICO";

  private final static String MICRONESIA_FEDERATED_STATES = "MICRONESIA_FEDERATED_STATES";

  private final static String MOLDOVA = "MOLDOVA";

  private final static String MONACO = "MONACO";

  private final static String MONGOLIA = "MONGOLIA";

  private final static String MONTENEGRO = "MONTENEGRO";

  private final static String MONTSERRAT = "MONTSERRAT";

  private final static String MOZAMBIQUE = "MOZAMBIQUE";

  private final static String MYANMAR = "MYANMAR";

  private final static String NAMIBIA = "NAMIBIA";

  private final static String NAURU = "NAURU";

  private final static String NEPAL = "NEPAL";

  private final static String NICARAGUA = "NICARAGUA";

  private final static String NIGER = "NIGER";

  private final static String NIGERIA = "NIGERIA";

  private final static String NIUE = "NIUE";

  private final static String NORFOLK_ISLAND = "NORFOLK_ISLAND";

  private final static String NORWAY = "NORWAY";

  private final static String NEW_CLAEDONIA = "NEW_CLAEDONIA";

  private final static String NEW_ZEALAND = "NEW_ZEALAND";

  private final static String BRITISH_INDIAN_OCEAN_TERRITORY = "BRITISH_INDIAN_OCEAN_TERRITORY";

  private final static String OMAN = "OMAN";

  private final static String UGANDA = "UGANDA";

  private final static String UZBEKISTAN = "UZBEKISTAN";

  private final static String PAKISTAN = "PAKISTAN";

  private final static String PALAU = "PALAU";

  private final static String PALESTINIAN_TERRITORY_OCCUPIED = "PALESTINIAN_TERRITORY_OCCUPIED";

  private final static String PANAMA = "PANAMA";

  private final static String PAPUA_NEW_GUINEA = "PAPUA_NEW_GUINEA";

  private final static String PARAGUAY = "PARAGUAY";

  private final static String NETHERLANDS = "NETHERLANDS";

  private final static String PERU = "PERU";

  private final static String PHILIPPINES = "PHILIPPINES";

  private final static String PITCAIRN = "PITCAIRN";

  private final static String POLAND = "POLAND";

  private final static String FRENCH_POLYNESIA = "FRENCH_POLYNESIA";

  private final static String PUERTO_RICO = "PUERTO_RICO";

  private final static String PORTUGAL = "PORTUGAL";

  private final static String QATAR = "QATAR";

  private final static String REUNION = "REUNION";

  private final static String ROMANIA = "ROMANIA";

  private final static String UNITED_KINGDOM = "UNITED_KINGDOM";

  private final static String RUSSIAN_FEDERATION = "RUSSIAN_FEDERATION";

  private final static String RWANDA = "RWANDA";

  private final static String WESTERN_SAHARA = "WESTERN_SAHARA";

  private final static String SAINT_BATHELEMY = "SAINT_BATHELEMY";

  private final static String SAINT_HELENA = "SAINT_HELENA";

  private final static String SAINT_LUCIA = "SAINT_LUCIA";

  private final static String SAINT_KITTS_AND_NEVIS = "SAINT_KITTS_AND_NEVIS";

  private final static String SAN_MARINO = "SAN_MARINO";

  private final static String SAINT_MARTIN = "SAINT_MARTIN";

  private final static String SAINT_PIERRE_AND_MIQUELON = "SAINT_PIERRE_AND_MIQUELON";

  private final static String SAINT_VINCENT_AND_GRENADINES = "SAINT_VINCENT_AND_GRENADINES";

  private final static String SOLOMON_ISLANDS = "SOLOMON_ISLANDS";

  private final static String SAMOA = "SAMOA";

  private final static String AMERICAN_SAMOA = "AMERICAN_SAMOA";

  private final static String SAO_TOME_AND_PRICINPE = "SAO_TOME_AND_PRICINPE";

  private final static String SENEGAL = "SENEGAL";

  private final static String SERBIA = "SERBIA";

  private final static String SEYCHELLES = "SEYCHELLES";

  private final static String SIERRA_LEONE = "SIERRA_LEONE";

  private final static String SINGAPORE = "SINGAPORE";

  private final static String SLOVAKIA = "SLOVAKIA";

  private final static String SLOVENIA = "SLOVENIA";

  private final static String SOMALIA = "SOMALIA";

  private final static String SUDAN = "SUDAN";

  private final static String SRI_LANKA = "SRI_LANKA";

  private final static String SWEDEN = "SWEDEN";

  private final static String SWITZERLAND = "SWITZERLAND";

  private final static String SURINAME = "SURINAME";

  private final static String SVALBARD_AND_JAN_MAYEN = "SVALBARD_AND_JAN_MAYEN";

  private final static String SWAZILAND = "SWAZILAND";

  private final static String SYRIAN_ARAB_RAPUBLIC = "SYRIAN_ARAB_RAPUBLIC";

  private final static String TAJIKISTAN = "TAJIKISTAN";

  private final static String TAWAIN_PROVINCE_CHINA = "TAWAIN_PROVINCE_CHINA";

  private final static String TANZANIA_UNITED_REPUBLIC = "TANZANIA_UNITED_REPUBLIC";

  private final static String CHAD = "CHAD";

  private final static String CZECH_REPUBLIC = "CZECH_REPUBLIC";

  private final static String FRENCH_SOUTHERN_TERRITORIES = "FRENCH_SOUTHERN_TERRITORIES";

  private final static String THAILAND = "THAILAND";

  private final static String TIMOR_LESTE = "TIMOR_LESTE";

  private final static String TOGO = "TOGO";

  private final static String TOKELAU = "TOKELAU";

  private final static String TONGA = "TONGA";

  private final static String TRINIDAD_AND_TOBAGO = "TRINIDAD_AND_TOBAGO";

  private final static String TUNISIA = "TUNISIA";

  private final static String TURKMENISTAN = "TURKMENISTAN";

  private final static String TURKS_AND_CAICOS_ISLANDS = "TURKS_AND_CAICOS_ISLANDS";

  private final static String TURKEY = "TURKEY";

  private final static String TUVALU = "TUVALU";

  private final static String UKRAINE = "UKRAINE";

  private final static String URUGUAY = "URUGUAY";

  private final static String VANUATU = "VANUATU";

  private final static String VATICAN_CITY_STATE = "VATICAN_CITY_STATE";

  private final static String VENEZUELA = "VENEZUELA";

  private final static String VIET_NAM = "VIET_NAM";

  private final static String WALLIS_AND_FUTUNA = "WALLIS_AND_FUTUNA";

  private final static String YEMEN = "YEMEN";

  private final static String ZAMBIA = "ZAMBIA";

  private final static String ZIMBABWE = "ZIMBABWE";
}
