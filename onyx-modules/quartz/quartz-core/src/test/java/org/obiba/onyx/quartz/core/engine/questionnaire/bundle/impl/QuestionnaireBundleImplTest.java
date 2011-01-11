/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class QuestionnaireBundleImplTest {

  private QuestionnaireBundleManager bundleManager;

  private File rootDirectory;

  private File bundleRootDirectory;

  private QuestionnaireBundle bundle;

  private Questionnaire questionnaire;

  @Before
  public void setUp() throws IOException {
    // Create the root directory for the bundle manager tests. If it exists
    // already, delete it to start clean.
    rootDirectory = new File("target", "bundleImpl-test");

    if(rootDirectory.exists()) {
      FileUtil.delete(rootDirectory);
    }

    // Create the bundle root directory (including any missing ancestors).
    bundleRootDirectory = new File(rootDirectory, "questionnaires");
    bundleRootDirectory.mkdirs();

    // Create the test questionnaire.
    questionnaire = new Questionnaire("q1", "1.0.0");

    // Create the bundle manager.
    bundleManager = new QuestionnaireBundleManagerImpl(bundleRootDirectory);
    ((QuestionnaireBundleManagerImpl) bundleManager).setResourceLoader(new PathMatchingResourcePatternResolver());

    // Create the test bundle.
    bundle = bundleManager.createBundle(questionnaire);
  }

  @After
  public void tearDown() throws IOException {
    if(rootDirectory != null && rootDirectory.exists()) {
      FileUtil.delete(rootDirectory);
    }
  }

  @Test
  public void testSetLanguage() {
    File bundleVersionDir = new File(new File(bundleRootDirectory, bundle.getName()), bundle.getQuestionnaire().getVersion());
    Locale enLocale = new Locale("en");
    Properties enLanguage = new Properties();

    // Set the language for the "en" locale.
    bundle.setLanguage(enLocale, enLanguage);

    // Verify that the language file was created.
    File enLanguageFile = new File(bundleVersionDir, "language_" + enLocale + ".properties");
    Assert.assertTrue(enLanguageFile.isFile());
  }

  @Test
  public void testGetLanguage() {
    //
    // Set languages for two locales, "en" and "fr".
    //
    Locale enLocale = new Locale("en");
    Properties enLanguage = new Properties();
    enLanguage.setProperty("Questionnaire.q1.label", "value");
    bundle.setLanguage(enLocale, enLanguage);

    Locale frLocale = new Locale("fr");
    Properties frLanguage = new Properties();
    frLanguage.setProperty("Questionnaire.q1.label", "valeur");
    bundle.setLanguage(frLocale, frLanguage);

    // Get the language for the "fr" locale.
    Properties testLanguage = bundle.getLanguage(frLocale);

    // Verify that the correct language (French) was returned.
    Assert.assertNotNull(testLanguage);
    Assert.assertEquals("valeur", testLanguage.getProperty("Questionnaire.q1.label"));
  }

  @Test
  public void testGetAvailableLanguages() {
    //
    // Set languages for two locales, "en" and "fr".
    //
    Locale enLocale = new Locale("en");
    Properties enLanguage = new Properties();
    enLanguage.setProperty("key", "value");
    bundle.setLanguage(enLocale, enLanguage);

    Locale frLocale = new Locale("fr");
    Properties frLanguage = new Properties();
    frLanguage.setProperty("key", "valeur");
    bundle.setLanguage(frLocale, frLanguage);

    // Get all available languages.
    List<Locale> languages = bundle.getAvailableLanguages();

    // Verify that the correct set of languages (locales) was returned.
    Assert.assertNotNull(languages);
    Assert.assertEquals(2, languages.size());
    Assert.assertTrue(languages.contains(enLocale));
    Assert.assertTrue(languages.contains(frLocale));
  }
}
