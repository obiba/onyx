package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

public class QuestionnaireBuilderTest {

  private static final String YES = "YES";

  private static final String NO = "NO";

  private static final String DONT_KNOW = "DONT_KNOW";

  private static final String OTHER_SPECIFY = "OTHER_SPECIFY";

  @Test
  public void testQuestionnaireBuilder() {
    QuestionnaireBuilder builder;
    try {
      builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "a");
      Assert.fail("Questionnaire version not checked.");
    } catch(Exception e) {
      builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");
    }

    try {
      builder.withSection("Section 1");
      Assert.fail("Section name not checked.");
    } catch(Exception e) {
    }

    builder.withSection("S1").withSection("S1_1");
    
    Section section = builder.getQuestionnaire().findSection("S1");
    Assert.assertNotNull("Section not found", section);
    section = builder.getQuestionnaire().findSection("S1_1");
    Assert.assertNotNull("Section not found", section);
    
    builder.inSection("S1_1").withPage("P1").withQuestion("Q1").withSharedCategories(YES, NO, DONT_KNOW);
    builder.inPage("P1").withQuestion("Q2").withCategories("1", "2").withSharedCategory(DONT_KNOW).setExportName("888");

    builder.inSection("S1_1").withPage("P2").withQuestion("Q3").withSharedCategory(YES).withSharedCategories(NO, DONT_KNOW);

    builder.inSection("S1").withSection("S1_2").withPage("P3");
    builder.inPage("P3").withQuestion("Q4").withSharedCategories(YES, NO, DONT_KNOW);

    builder.withSection("S2").withSection("S2_1").withPage("P4");
    builder.inPage("P4").withQuestion("Q5").withCategory("NAME").withOpenAnswerDefinition("AGE", DataType.INTEGER).setOpenAnswerDefinitionAbsoluteValues(DataBuilder.buildInteger(40), DataBuilder.buildInteger(70));
    builder.inQuestion("Q5").withCategory(OTHER_SPECIFY).withOpenAnswerDefinition("SPECIFY", DataType.TEXT).setOpenAnswerDefinitionDefaultData("Left", "Right").setOpenAnswerDefinitionUnit("kg").setOpenAnswerDefinitionFormat("[a-z,A-Z]+");

    //System.out.println(QuestionnaireStreamer.toXML(builder.getQuestionnaire()));
    
    //System.out.println(builder.getProperties());
  }

}
