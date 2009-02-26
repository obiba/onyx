/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.obiba.core.util.FileUtil;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl.QuestionnaireBundleManagerImpl;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Utility class for creating and updating questionnaires on file system.
 */
public class QuestionnaireCreator {

  private File bundleRootDirectory = new File("target", "questionnaires");

  private File bundleSourceDirectory = new File("src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF" + File.separatorChar + "config" + File.separatorChar + "quartz" + File.separatorChar + "resources", "questionnaires");

  public QuestionnaireCreator() throws IOException {
    super();
    initialize();
  }

  public QuestionnaireCreator(File workingDir) throws IOException {
    super();
    if(workingDir != null) {
      bundleRootDirectory = new File(workingDir, bundleRootDirectory.getPath());
      bundleSourceDirectory = new File(workingDir, bundleSourceDirectory.getPath());
    }
    initialize();
  }

  public QuestionnaireCreator(File bundleRootDirectory, File bundleSourceDirectory) throws IOException {
    super();
    this.bundleRootDirectory = bundleRootDirectory;
    this.bundleSourceDirectory = bundleSourceDirectory;
    initialize();
  }

  private void initialize() throws IOException {
    if(bundleSourceDirectory.exists()) {
      FileUtil.copyDirectory(bundleSourceDirectory, bundleRootDirectory);
    }
  }

  public void createQuestionnaire(QuestionnaireBuilder builder, Locale... locales) {
    QuestionnaireBundle bundle;

    // Create the bundle manager.
    QuestionnaireBundleManager bundleManager = new QuestionnaireBundleManagerImpl(bundleRootDirectory);
    ((QuestionnaireBundleManagerImpl) bundleManager).setPropertyKeyProvider(builder.getPropertyKeyProvider());
    ((QuestionnaireBundleManagerImpl) bundleManager).setResourceLoader(new PathMatchingResourcePatternResolver());

    // Create the bundle questionnaire.
    try {
      bundle = bundleManager.createBundle(builder.getQuestionnaire());
      if(locales != null) {
        for(Locale locale : locales) {
          Properties properties = bundle.getLanguage(locale);

          if(properties != null) {
            bundle.setLanguage(locale, properties);
          } else {
            bundle.setLanguage(locale, new Properties());
          }
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }

  }
}
