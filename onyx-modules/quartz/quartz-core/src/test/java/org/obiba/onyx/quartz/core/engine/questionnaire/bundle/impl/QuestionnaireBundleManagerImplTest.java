package org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

public class QuestionnaireBundleManagerImplTest {

  private QuestionnaireBundleManager bundleManager;

  private File rootDirectory;

  private File bundleRootDirectory;

  @Before
  public void setUp() throws IOException {
    // Create the root directory for the bundle manager tests. If it exists
    // already, delete it to start clean.
    rootDirectory = new File("target", "bundleManager-test");

    if(rootDirectory.exists()) {
      FileUtil.delete(rootDirectory);
    }

    // Create the bundle root directory (including any missing ancestors).
    bundleRootDirectory = new File(rootDirectory, "questionnaires");
    bundleRootDirectory.mkdirs();

    // Create the bundle manager.
    bundleManager = new QuestionnaireBundleManagerImpl(bundleRootDirectory);
  }

  @After
  public void tearDown() throws IOException {
    if(rootDirectory != null && rootDirectory.exists()) {
      FileUtil.delete(rootDirectory);
    }
  }

  @Test
  public void testCreateBundle() {
    Questionnaire questionnaire = new Questionnaire("q1", "1.0");

    // Create the bundle.
    try {
      bundleManager.createBundle(questionnaire);
    } catch(IOException ex) {
      Assert.fail("Failed to create bundle");
    }

    // Verify that the bundle version directory was created.
    File bundleVersionDir = new File(new File(bundleRootDirectory, questionnaire.getName()), questionnaire.getVersion());
    Assert.assertTrue(bundleVersionDir.isDirectory());

    // Verify that the questionnaire file was created.
    File questionnaireFile = new File(bundleVersionDir, "questionnaire.xml");
    Assert.assertTrue(questionnaireFile.isFile());
  }

  @Test
  public void testGetBundle() {
    // Create three versions of bundle "q1".
    Questionnaire q1v100 = new Questionnaire("q1", "1.0.0");
    Questionnaire q1v101 = new Questionnaire("q1", "1.0.1");
    Questionnaire q1v200 = new Questionnaire("q1", "2.0.0");

    try {
      bundleManager.createBundle(q1v100);
      bundleManager.createBundle(q1v101);
      bundleManager.createBundle(q1v200);
    } catch(IOException ex) {
      Assert.fail("Failed to create bundles");
    }

    // Now try to get the bundle that was created. The latest
    // version (2.0.0) should be returned.
    QuestionnaireBundle bundle = bundleManager.getBundle("q1");

    // Verify that the bundle was found.
    Assert.assertNotNull(bundle);
    Assert.assertEquals("q1", bundle.getName());
    Assert.assertEquals("2.0.0", bundle.getQuestionnaire().getVersion());
  }

  @Test
  public void testBundles() {
    // Create two versions of two different bundles.
    Questionnaire q1v100 = new Questionnaire("q1", "1.0.0");
    Questionnaire q1v101 = new Questionnaire("q1", "1.0.1");
    QuestionnaireBundle q1v101Bundle = null;

    Questionnaire q2v100 = new Questionnaire("q2", "1.0.0");
    Questionnaire q2v200 = new Questionnaire("q2", "2.0.0");
    QuestionnaireBundle q2v200Bundle = null;

    try {
      bundleManager.createBundle(q1v100);
      q1v101Bundle = bundleManager.createBundle(q1v101);
      bundleManager.createBundle(q2v100);
      q2v200Bundle = bundleManager.createBundle(q2v200);
    } catch(IOException ex) {
      Assert.fail("Failed to create bundles");
    }

    // Now try to get all the bundles. The latest versions
    // 1.0.1 for q1, and 2.0.0 for q2) should be returned.
    Set<QuestionnaireBundle> bundles = bundleManager.bundles();

    // Verify that the correct set of bundles was returned.
    Assert.assertNotNull(bundles);
    Assert.assertEquals(2, bundles.size());
    Assert.assertTrue(bundles.contains(q1v101Bundle));
    Assert.assertTrue(bundles.contains(q2v200Bundle));
  }
}
