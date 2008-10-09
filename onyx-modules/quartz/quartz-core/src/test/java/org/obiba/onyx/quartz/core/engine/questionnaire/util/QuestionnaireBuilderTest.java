package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
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
    } catch(IllegalArgumentException e) {
      builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");
    }

    try {
      builder.withSection("Section 1");
      Assert.fail("Section name not checked.");
    } catch(IllegalArgumentException e) {
    }

    builder.withSection("S1").withSection("S1_1");
    
    Section section = builder.getQuestionnaire().findSection("S1");
    Assert.assertNotNull("Section not found", section);
    section = builder.getQuestionnaire().findSection("S1_1");
    Assert.assertNotNull("Section not found", section);
    Assert.assertEquals("S1", section.getParentSection().getName());
    
    try {
      builder.inSection("S1_2");
      Assert.fail("Exception must be thrown if going to an invalid questionnaire element");
    } catch(IllegalStateException e) {
    }
    
    builder.inSection("S1_1").withPage("P1").withQuestion("Q1").withSharedCategories(YES, NO, DONT_KNOW);
    
    Page page = builder.getQuestionnaire().findPage("P1");
    Assert.assertNotNull(page);
    Assert.assertEquals(1, page.getQuestions().size());
    Question question = builder.getQuestionnaire().findQuestion("Q1");
    Assert.assertNotNull(question);
    Assert.assertEquals(3, question.getQuestionCategories().size());
    Assert.assertEquals(3, question.getCategories().size());
    Category category = builder.getQuestionnaire().findCategory(DONT_KNOW);
    Assert.assertNotNull(category);
    
    builder.inPage("P1").withQuestion("Q2").withCategories("1", "2", "3").withSharedCategory(DONT_KNOW).setExportName("888").setRepeatable(false).setSelected(true);
    question = builder.getQuestionnaire().findQuestion("Q2");
    Assert.assertEquals(4, question.getCategories().size());
    Assert.assertEquals(category, question.findCategory(DONT_KNOW));
    QuestionCategory qCategory = question.findQuestionCategory(DONT_KNOW);
    Assert.assertEquals("888", qCategory.getExportName());
    Assert.assertEquals(false, qCategory.isRepeatable());
    Assert.assertEquals(true, qCategory.isSelected());
    
    builder.inSection("S1_1").withPage("P2").withQuestion("Q3").withSharedCategory(YES).withSharedCategories(NO, DONT_KNOW);
    Assert.assertEquals(2, builder.getQuestionnaire().findSection("S1_1").getPages().size());
    Assert.assertEquals(3, builder.getQuestionnaire().findQuestion("Q3").getCategories().size());

    builder.inSection("S1").withSection("S1_2").withPage("P3");
    builder.inPage("P3").withQuestion("Q4");
    try {
      builder.inQuestion("Q4").withCategories("1", "1");
      Assert.fail("Exception must be thrown if attempting to add categories with the same name in a question.");
    } catch(IllegalArgumentException e) {
    }
    Assert.assertEquals(1, builder.getQuestionnaire().findQuestion("Q4").getCategories().size());
    
    builder.withSection("S2").withSection("S2_1").withPage("P4");
    builder.inPage("P4").withQuestion("Q5").withCategory("NAME").withOpenAnswerDefinition("AGE", DataType.INTEGER).setOpenAnswerDefinitionAbsoluteValues(DataBuilder.buildInteger(40), DataBuilder.buildInteger(70));
    category = builder.getQuestionnaire().findCategory("NAME");
    
    builder.inQuestion("Q5").withCategory(OTHER_SPECIFY).withOpenAnswerDefinition("SPECIFY", DataType.TEXT).setOpenAnswerDefinitionDefaultData("Left", "Right").setOpenAnswerDefinitionUnit("kg").setOpenAnswerDefinitionFormat("[a-z,A-Z]+");

    System.out.println(QuestionnaireStreamer.toXML(builder.getQuestionnaire()));
    
    //System.out.println(builder.getProperties());
  }

}
