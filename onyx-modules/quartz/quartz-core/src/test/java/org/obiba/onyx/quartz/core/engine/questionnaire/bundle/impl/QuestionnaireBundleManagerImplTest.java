package org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireStreamer;

public class QuestionnaireBundleManagerImplTest {
   
  private File rootDirectory;
  
  private File bundleRootDirectory;
  
  private Locale enLocale;
  
  private Locale frLocale;
  
  @Before
  public void setUp() throws IOException {
    rootDirectory = new File("target", "bundleManager-test");
    
    if (rootDirectory.exists()) {
      FileUtil.delete(rootDirectory);
    }
    
    bundleRootDirectory = new File(rootDirectory, "questionnaires");
    
    enLocale = new Locale("en");
    frLocale = new Locale("fr");
    
    // Create two versions of q1 questionnaire.
    Questionnaire q1v100 = new Questionnaire("q1", "1.0.0");
    QuestionnaireStreamer.makeBundle(q1v100, bundleRootDirectory, enLocale, frLocale);    
    
    Questionnaire q1v101 = new Questionnaire("q1", "1.0.1");
    QuestionnaireStreamer.makeBundle(q1v101, bundleRootDirectory, enLocale, frLocale);
    
    // Create test versions of q2 questionnaire.
    Questionnaire q2v100 = new Questionnaire("q2", "1.0.0");
    QuestionnaireStreamer.makeBundle(q2v100, bundleRootDirectory, enLocale, frLocale);    
    
    Questionnaire q2v200 = new Questionnaire("q2", "2.0.0");
    QuestionnaireStreamer.makeBundle(q2v200, bundleRootDirectory, frLocale);
  }
  
  @After
  public void tearDown() throws IOException {
    if (rootDirectory != null && rootDirectory.exists()) {
      FileUtil.delete(rootDirectory);
    }
  }
  
  @Test
  public void testInit() {
    QuestionnaireBundleManager bundleManager = new QuestionnaireBundleManagerImpl(bundleRootDirectory);
    
    bundleManager.init();
    
    // Verify number of bundles registered with the manager.
    Assert.assertEquals(2, bundleManager.bundles().size());
    
    // Verify that the first bundle was registered, that the bundle contains the
    // latest version of the questionnaire, and the correct set of languages.
    QuestionnaireBundle q1Bundle = bundleManager.getBundle("q1");
    Assert.assertNotNull(q1Bundle);
    Assert.assertEquals("q1", q1Bundle.getName());
    Assert.assertEquals("1.0.1", q1Bundle.getQuestionnaire().getVersion());
    
    Set<Locale> q1Languages = q1Bundle.getAvailableLanguages();
    Assert.assertEquals(2, q1Languages.size());
    Assert.assertTrue(q1Languages.contains(enLocale));
    Assert.assertTrue(q1Languages.contains(frLocale));
    
    // Verify that the second bundle was registered, that the bundle contains the
    // latest version of the questionnaire, and the correct set of languages.
    QuestionnaireBundle q2Bundle = bundleManager.getBundle("q2");
    Assert.assertNotNull(q2Bundle);
    Assert.assertEquals("q2", q2Bundle.getName());
    Assert.assertEquals("2.0.0", q2Bundle.getQuestionnaire().getVersion());
    
    Set<Locale> q2Languages = q2Bundle.getAvailableLanguages();
    Assert.assertEquals(1, q2Languages.size());
    Assert.assertTrue(q2Languages.contains(frLocale));
  }
}
