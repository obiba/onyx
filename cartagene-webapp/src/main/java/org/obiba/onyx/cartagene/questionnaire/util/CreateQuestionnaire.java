package org.obiba.onyx.cartagene.questionnaire.util;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl.QuestionnaireBundleManagerImpl;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.util.data.DataType;

public class CreateQuestionnaire {

  private File bundleRootDirectory = new File("target", "questionnaires");

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
    c.createQuestionnaire();
  }

  public CreateQuestionnaire() {
  }

  public void createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("SB").withSection("GENDER").withPage("P1").withQuestion("Q1").withCategories("1", "2");
    builder.inQuestion("Q1").withSharedCategory(OTHER).setExportName("3");
    builder.inQuestion("Q1").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q1").withSharedCategory(DONT_KNOW).setExportName("9");

    builder.inSection("SB").withSection("BIRTHDATE").withPage("P2").withQuestion("Q2").withCategory("1").withOpenAnswerDefinition("year", DataType.INTEGER).setOpenAnswerDefinitionFormat("YYYY");
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
    builder.inSection("WORKINGSTATUS").withPage("P17").withQuestion("GLOBAL");
    builder.inQuestion("GLOBAL").withQuestion("Q17").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q17").withSharedCategory(FULL_TIME).setExportName("1");
    builder.inQuestion("Q17").withSharedCategory(PART_TIME).setExportName("2");
    builder.inQuestion("Q17").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q17").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inQuestion("GLOBAL").withQuestion("Q18").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q18").withSharedCategory(FULL_TIME).setExportName("1");
    builder.inQuestion("Q18").withSharedCategory(PART_TIME).setExportName("2");
    builder.inQuestion("Q18").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q18").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inQuestion("GLOBAL").withQuestion("Q19").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q19").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q19").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q19").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inQuestion("GLOBAL").withQuestion("Q20").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q20").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q20").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q20").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inQuestion("GLOBAL").withQuestion("Q21").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q21").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q21").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q21").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inQuestion("GLOBAL").withQuestion("Q22").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q22").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q22").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q22").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inQuestion("GLOBAL").withQuestion("Q23").withSharedCategory(NO).setExportName("0");
    builder.inQuestion("Q23").withSharedCategory(YES).setExportName("1");
    builder.inQuestion("Q23").withSharedCategory(NO_ANSWER).setExportName("8");
    builder.inQuestion("Q23").withSharedCategory(DONT_KNOW).setExportName("9");
    builder.inQuestion("GLOBAL").withQuestion("Q24").withSharedCategory(NO).setExportName("0");
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

    // Create the bundle manager.
    QuestionnaireBundleManager bundleManager = new QuestionnaireBundleManagerImpl(bundleRootDirectory);
    ((QuestionnaireBundleManagerImpl) bundleManager).setPropertyKeyProvider(new DefaultPropertyKeyProviderImpl());
    
    // Create the bundle questionnaire.
    try {
      bundle = bundleManager.createBundle(builder.getQuestionnaire());
    } catch(IOException e) {
      e.printStackTrace();
    }
    
    bundle.setLanguage(Locale.ENGLISH, new Properties());
    bundle.setLanguage(Locale.FRENCH, new Properties());
    
  }
}
