package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

public class QuestionnaireUtilitiesTest {

  @Test
  public void testQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    Section parentSection = builder.withSection("S1").getSection();

    Category YES = new Category("YES");
    Category NO = new Category("NO");
    Category DONT_KNOW = new Category("DONT_KNOW");

    builder.withSection(parentSection, "S1.1").withPage("P1");
    builder.withQuestion("Q1").withCategories(YES, NO, DONT_KNOW);
    builder.withQuestion("Q2").withCategories("1", "2").withCategory(DONT_KNOW, "888");

    builder.withPage("P2");
    builder.withQuestion("Q3").withCategories(YES, NO, DONT_KNOW);

    builder.withSection(parentSection, "S1.2").withPage("P3");
    builder.withQuestion("Q4").withCategories(YES, NO, DONT_KNOW);

    parentSection = builder.withSection("S2").getSection();
    builder.withSection(parentSection, "S2.1").withPage("P4");
    builder.withQuestion("Q5").withCategories(YES, NO, DONT_KNOW);

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
      File bundle = QuestionnaireStreamer.makeBundle(builder.getQuestionnaire(), bundleDirectory, Locale.FRENCH, Locale.ENGLISH);
      Assert.assertEquals(builder.getQuestionnaire().getVersion(), bundle.getName());
      Assert.assertEquals(builder.getQuestionnaire().getName(), bundle.getParentFile().getName());
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
      Assert.assertTrue(localizationProperties.containsKey("Section.S1.1.label"));
      Assert.assertTrue(localizationProperties.containsKey("Page.P1.label"));
      Assert.assertTrue(localizationProperties.containsKey("Question.Q1.label"));
      Assert.assertTrue(localizationProperties.containsKey("Category.YES.label"));
      Assert.assertTrue(localizationProperties.containsKey("QuestionCategory.Q1.YES.label"));
      Assert.assertEquals("${Category.YES.label}", localizationProperties.getProperty("QuestionCategory.Q1.YES.label"));

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
