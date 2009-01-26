/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.example.questionnaire.util;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.obiba.core.util.FileUtil;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl.QuestionnaireBundleManagerImpl;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class CreateQuestionnaire {

  private static File bundleRootDirectory = new File("target", "questionnaires");

  private static File bundleSourceDirectory = new File("src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF" + File.separatorChar + "config" + File.separatorChar + "quartz" + File.separatorChar + "resources", "questionnaires");

  private QuestionnaireBundle bundle;

  public static void main(String args[]) {
    CreateQuestionnaire c = new CreateQuestionnaire();

    if(bundleSourceDirectory.exists()) {
      try {
        FileUtil.copyDirectory(bundleSourceDirectory, bundleRootDirectory);
      } catch(IOException e) {
        e.printStackTrace();
      }
    }

    // Select the questionnaire you wish to create
    c.createQuestionnaire(TestQuestionnaireContentBuilder.buildTestQuestionnaire(), false);
    c.createQuestionnaire(SelfAdminHealthQuestionnaireContentBuilder.buildHealthQuestionnaire(), false);
    c.createQuestionnaire(AssistedHealthQuestionnaireContentBuilder.buildHealthQuestionnaire(), false);
    c.createQuestionnaire(CIPreliminaryQuestionnaireContentBuilder.buildCIPreliminaryQuestionnaire(), true);
  }

  public CreateQuestionnaire() {
  }

  public void createQuestionnaire(QuestionnaireBuilder builder, boolean englishOnly) {
    // Create the bundle manager.
    QuestionnaireBundleManager bundleManager = new QuestionnaireBundleManagerImpl(bundleRootDirectory);
    ((QuestionnaireBundleManagerImpl) bundleManager).setPropertyKeyProvider(new DefaultPropertyKeyProviderImpl());
    ((QuestionnaireBundleManagerImpl) bundleManager).setResourceLoader(new PathMatchingResourcePatternResolver());

    // Create the bundle questionnaire.
    try {
      bundle = bundleManager.createBundle(builder.getQuestionnaire());
    } catch(IOException e) {
      e.printStackTrace();
    }

    if(!englishOnly) setBundleProperties(Locale.FRENCH);
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
