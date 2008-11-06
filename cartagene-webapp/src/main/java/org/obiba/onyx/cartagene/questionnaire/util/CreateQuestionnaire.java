/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.cartagene.questionnaire.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.obiba.core.util.FileUtil;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl.QuestionnaireBundleManagerImpl;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.wicket.layout.impl.DropDownQuestionPanelFactory;
import org.obiba.onyx.util.data.DataType;

public class CreateQuestionnaire {

  private static File bundleRootDirectory = new File("target", "questionnaires");

  private static File bundleSourceDirectory = new File("src" + File.separatorChar + "main" + File.separatorChar + "webapp", "questionnaires");

  private QuestionnaireBundle bundle;

  private static final String NO = "NO";

  private static final String YES = "YES";

  private static final String OTHER = "OTHER";

  private static final String FULL_TIME = "FULL_TIME";

  private static final String PART_TIME = "PART_TIME";

  private static final String NO_ANSWER = "NO_ANSWER";

  private static final String DONT_KNOW = "DONT_KNOW";

  public static void main(String args[]) {
    CreateQuestionnaire c = new CreateQuestionnaire();

    if(bundleSourceDirectory.exists()) {
      try {
        FileUtil.copyDirectory(bundleSourceDirectory, bundleRootDirectory);
      } catch(IOException e) {
        e.printStackTrace();
      }

    }
    c.createQuestionnaire();
  }

  public CreateQuestionnaire() {
  }

  public void createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("S_ARRAY").withPage("P_ARRAY").withQuestion("SHARED_CATEGORIES_QUESTION_ARRAY").withCategories("1", "2").withSharedCategories(DONT_KNOW, NO_ANSWER).withCategory("OTHER_OPINION").withOpenAnswerDefinition("SPECIFY_OPINION", DataType.TEXT);
    builder.inQuestion("SHARED_CATEGORIES_QUESTION_ARRAY").withQuestion("SHARED_CATEGORIES_QUESTION_1");
    builder.inQuestion("SHARED_CATEGORIES_QUESTION_ARRAY").withQuestion("SHARED_CATEGORIES_QUESTION_2");

    builder.inPage("P_ARRAY").withQuestion("SHARED_MULTIPLE_CATEGORIES_QUESTION_ARRAY", true).withCategories("BLUE", "RED", "YELLOW").withCategory("OTHER_COLOR").withOpenAnswerDefinition("SPECIFY_COLOR", DataType.TEXT);
    builder.inQuestion("SHARED_MULTIPLE_CATEGORIES_QUESTION_ARRAY").withQuestion("SHARED_MULTIPLE_CATEGORIES_QUESTION_1");
    builder.inQuestion("SHARED_MULTIPLE_CATEGORIES_QUESTION_ARRAY").withQuestion("SHARED_MULTIPLE_CATEGORIES_QUESTION_2");

    builder.withSection("SB").withSection("GENDER").withPage("P1").withQuestion("Q1").withCategories("1", "2");
    builder.inQuestion("Q1").withSharedCategory(OTHER).setExportName("3");
    builder.inQuestion("Q1").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q1").withSharedCategory(DONT_KNOW).setExportName("9");

    builder.inSection("SB").withSection("BIRTHDATE").withPage("P2").withQuestion("Q2", DropDownQuestionPanelFactory.class).withCategory("1").withOpenAnswerDefinition("year", DataType.INTEGER).setOpenAnswerDefinitionFormat("YYYY");
    builder.inQuestion("Q2").withSharedCategory(NO_ANSWER).setExportName("8888");
    builder.inQuestion("Q2").withSharedCategory(DONT_KNOW).setExportName("9999");
    builder.inSection("BIRTHDATE").withPage("P3").withQuestion("Q3").withCategory("1").withOpenAnswerDefinition("month", DataType.INTEGER).setOpenAnswerDefinitionFormat("MM");
    builder.inQuestion("Q3").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q3").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("BIRTHDATE").withPage("P4").withQuestion("Q4").withCategory("1").withOpenAnswerDefinition("age", DataType.INTEGER).setOpenAnswerDefinitionAbsoluteValues("40", "70");
    builder.inQuestion("Q4").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q4").withSharedCategory(DONT_KNOW).setExportName("99");

    builder.inSection("SB").withSection("MARITALSTATUS").withPage("P5").withQuestion("Q5").withCategories("1", "2", "3", "4", "5");
    builder.inQuestion("Q5").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q5").withSharedCategory(DONT_KNOW).setExportName("99");

    builder.inSection("SB").withSection("HOUSEHOLDSTATUS").withPage("P6").withQuestion("Q6").withCategory("1").withOpenAnswerDefinition("adults", DataType.INTEGER).setOpenAnswerDefinitionAbsoluteValues("1", "100");
    builder.inQuestion("Q6").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q6").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("HOUSEHOLDSTATUS").withPage("P7").withQuestion("Q7").withCategory("1").withOpenAnswerDefinition("children", DataType.INTEGER).setOpenAnswerDefinitionAbsoluteValues("0", "100");
    builder.inQuestion("Q7").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q7").withSharedCategory(DONT_KNOW).setExportName("99");

    builder.inSection("SB").withSection("SIBLING").withPage("P8").withQuestion("Q8").withCategory("1").withOpenAnswerDefinition("siblings", DataType.INTEGER).setOpenAnswerDefinitionAbsoluteValues("0", "20");
    builder.inQuestion("Q8").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q8").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("SIBLING").withPage("P9").withQuestion("Q9").withCategory("1").withOpenAnswerDefinition("olderSiblings", DataType.INTEGER).setOpenAnswerDefinitionAbsoluteValues("0", "20");
    builder.inQuestion("Q9").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q9").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("SIBLING").withPage("P10").withQuestion("Q10").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q10").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q10").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q10").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("SIBLING").withPage("P11").withQuestion("Q11").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q11").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q11").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q11").withSharedCategory(DONT_KNOW).setExportName("9");

    builder.inSection("SB").withSection("EDUCATIONLEVEL").withPage("P12").withQuestion("Q12");
    builder.inSection("EDUCATIONLEVEL").withPage("P13").withQuestion("Q13").withCategory("1").withOpenAnswerDefinition("years", DataType.INTEGER).setOpenAnswerDefinitionAbsoluteValues("0", "20");
    builder.inQuestion("Q13").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q13").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("EDUCATIONLEVEL").withPage("P14").withQuestion("Q14").withCategories("1", "2", "3", "4", "5", "6", "7", "8");
    builder.inQuestion("Q14").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q14").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("EDUCATIONLEVEL").withPage("P15").withQuestion("Q15").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q15").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q15").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q15").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("SB").withSection("WORKINGSTATUS").withPage("P16").withQuestion("Q16");
    builder.inSection("WORKINGSTATUS").withPage("P17_1").withQuestion("Q17").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q17").withSharedCategory(FULL_TIME).setExportName("1");
    builder.inQuestion("Q17").withSharedCategory(PART_TIME).setExportName("2");
    builder.inQuestion("Q17").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q17").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P17_2").withQuestion("Q18").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q18").withSharedCategory(FULL_TIME).setExportName("1");
    builder.inQuestion("Q18").withSharedCategory(PART_TIME).setExportName("2");
    builder.inQuestion("Q18").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q18").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P17_3").withQuestion("Q19").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q19").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q19").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q19").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P17_4").withQuestion("Q20").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q20").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q20").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q20").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P17_5").withQuestion("Q21").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q21").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q21").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q21").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P17_6").withQuestion("Q22").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q22").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q22").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q22").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P17_7").withQuestion("Q23").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q23").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q23").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q23").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P17_8").withQuestion("Q24").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q24").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q24").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q24").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P18").withQuestion("Q25").withCategory("1").withOpenAnswerDefinition("job", DataType.TEXT);
    builder.inQuestion("Q25").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q25").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("WORKINGSTATUS").withPage("P19").withQuestion("Q26").withCategories("1", "2", "3", "4", "5", "6", "7", "8");
    builder.inQuestion("Q26").withSharedCategory(OTHER).setExportName("9");
    builder.inQuestion("Q26").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q26").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("WORKINGSTATUS").withPage("P20").withQuestion("Q27").withCategory("1").withOpenAnswerDefinition("specify", DataType.TEXT);
    builder.inQuestion("Q27").withSharedCategory(NO_ANSWER).setExportName("88");
    builder.inQuestion("Q27").withSharedCategory(DONT_KNOW).setExportName("99");
    builder.inSection("WORKINGSTATUS").withPage("P21").withQuestion("Q28").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q28").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q28").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q28").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inSection("WORKINGSTATUS").withPage("P22").withQuestion("Q29").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q29").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q29").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q29").withSharedCategory(DONT_KNOW).setExportName("9");

    // Add Timestamps to pages
    List<Page> pages = builder.getElement().getPages();
    for(Page page : pages) {
      builder.inPage(page.getName()).addTimestamp();
    }

    // Create the bundle manager.
    QuestionnaireBundleManager bundleManager = new QuestionnaireBundleManagerImpl(bundleRootDirectory);
    ((QuestionnaireBundleManagerImpl) bundleManager).setPropertyKeyProvider(new DefaultPropertyKeyProviderImpl());

    // Create the bundle questionnaire.
    try {
      bundle = bundleManager.createBundle(builder.getQuestionnaire());
    } catch(IOException e) {
      e.printStackTrace();
    }

    setBundleProperties(Locale.FRENCH);
    setBundleProperties(Locale.ENGLISH);
  }

  private void setBundleProperties(Locale language) {
    Properties properties = bundle.getLanguage(language);

    if(properties != null) {
      bundle.setLanguage(language, properties);
    } else {
      bundle.setLanguage(language, new Properties());
    }
  }
}
