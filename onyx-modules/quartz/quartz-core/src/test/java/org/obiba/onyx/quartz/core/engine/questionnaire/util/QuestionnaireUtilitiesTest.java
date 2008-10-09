package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

public class QuestionnaireUtilitiesTest {

  private static final String YES = "YES";

  private static final String NO = "NO";

  private static final String DONT_KNOW = "DONT_KNOW";

  private static final String OTHER_SPECIFY = "OTHER_SPECIFY";

  @Test
  public void testQuestionnaireBuilder() {
    QuestionnaireBuilder builder = buildQuestionnaire();
    System.out.println(QuestionnaireStreamer.toXML(builder.getElement()));
  }

  private QuestionnaireBuilder buildQuestionnaire() {
    QuestionnaireBuilder builder;
    try {
      builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "a");
      Assert.fail("Questionnaire version not checked.");
    } catch(Exception e) {
      builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");
    }

    builder.withSection("S1").withSection("S1_1").withPage("P1");
    builder.inPage("P1").withQuestion("Q1").withSharedCategories(YES, NO, DONT_KNOW);
    builder.inPage("P1").withQuestion("Q2").withCategories("1", "2").withSharedCategory(DONT_KNOW).setExportName("888");

    builder.inSection("S1_1").withPage("P2").withQuestion("Q3").withSharedCategory(YES).withSharedCategories(NO, DONT_KNOW);

    builder.inSection("S1").withSection("S1_2").withPage("P3");
    builder.inPage("P3").withQuestion("Q4").withSharedCategories(YES, NO, DONT_KNOW);

    builder.withSection("S2").withSection("S2_1").withPage("P4");
    builder.inPage("P4").withQuestion("Q5").withCategory("NAME").withOpenAnswerDefinition("AGE", DataType.INTEGER).setOpenAnswerDefinitionAbsoluteValues(DataBuilder.buildInteger(40), DataBuilder.buildInteger(70));
    builder.inQuestion("Q5").withCategory(OTHER_SPECIFY).withOpenAnswerDefinition("SPECIFY", DataType.TEXT).setOpenAnswerDefinitionDefaultData("Left", "Right").setOpenAnswerDefinitionUnit("kg").setOpenAnswerDefinitionFormat("[a-z,A-Z]+");

    return builder;
  }

  @Test
  public void testQuestionnaireStreamer() {
    QuestionnaireBuilder builder = buildQuestionnaire();

    // System.out.println(QuestionnaireStreamer.toXML(builder.getQuestionnaire()));

    // try {
    // File original = new File("target", "original.xml");
    // original.createNewFile();
    // QuestionnaireStreamer.toXML(builder.getQuestionnaire(), new FileOutputStream(original));
    // } catch(Exception e1) {
    // e1.printStackTrace();
    // }

    File bundleDirectory = new File("target", "bundle-test");
    if(bundleDirectory.exists()) {
      try {
        FileUtil.delete(bundleDirectory);
      } catch(IOException e) {
        e.printStackTrace();
      }
    }
    try {
      File bundle = QuestionnaireStreamer.makeBundle(builder.getElement(), bundleDirectory, Locale.FRENCH, Locale.ENGLISH);
      Assert.assertEquals(builder.getElement().getVersion(), bundle.getName());
      Assert.assertEquals(builder.getElement().getName(), bundle.getParentFile().getName());
      File file = new File(bundle, "questionnaire.xml");
      Assert.assertTrue("Questionnaire description file not created.", file.exists());
      file = new File(bundle, "questionnaire_fr.properties");
      Assert.assertTrue("Questionnaire fr properties file not created.", file.exists());
      file = new File(bundle, "questionnaire_en.properties");
      Assert.assertTrue("Questionnaire en properties file not created.", file.exists());

      Properties localizationProperties = new Properties();
      localizationProperties.load(new FileInputStream(file));
      Assert.assertTrue(localizationProperties.containsKey("Questionnaire.HealthQuestionnaire.description"));
      Assert.assertTrue(localizationProperties.containsKey("Section.S1.label"));
      Assert.assertTrue(localizationProperties.containsKey("Section.S1_1.label"));
      Assert.assertTrue(localizationProperties.containsKey("Page.P1.label"));
      Assert.assertTrue(localizationProperties.containsKey("Question.Q1.label"));
      Assert.assertTrue(localizationProperties.containsKey("Category.YES.label"));
      Assert.assertTrue(localizationProperties.containsKey("QuestionCategory.Q1.YES.label"));
      Assert.assertEquals("${Category.YES.label}", localizationProperties.getProperty("QuestionCategory.Q1.YES.label"));
      Assert.assertTrue(localizationProperties.containsKey("OpenAnswerDefinition.SPECIFY.Right"));

      Questionnaire fromDead = QuestionnaireStreamer.fromBundle(bundle);
      Assert.assertNotNull("Reloaded questionnaire is null", fromDead);
      Assert.assertTrue("Reloaded questionnaire has no sections", fromDead.getSections().size() > 0);
      Assert.assertTrue("Reloaded questionnaire has no pages", fromDead.getPages().size() > 0);

      // try {
      // File original = new File("target", "dead.xml");
      // original.createNewFile();
      // QuestionnaireStreamer.toXML(fromDead, new FileOutputStream(original));
      // } catch(Exception e1) {
      // e1.printStackTrace();
      // }

      // System.out.println(QuestionnaireStreamer.toXML(fromDead));
      // Assert.assertEquals("xml not equals", QuestionnaireStreamer.toXML(builder.getQuestionnaire()),
      // QuestionnaireStreamer.toXML(fromDead));

    } catch(IOException e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

}
