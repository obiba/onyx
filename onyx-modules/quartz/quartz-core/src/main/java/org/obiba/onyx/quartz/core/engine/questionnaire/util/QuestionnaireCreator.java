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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.obiba.core.util.FileUtil;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl.QuestionnaireBundleManagerImpl;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Utility class for creating and updating questionnaires on file system.
 */
public class QuestionnaireCreator {

  private File bundleRootDirectory = new File("target", "questionnaires");

  private File bundleSourceDirectory = new File("src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF" + File.separatorChar + "config" + File.separatorChar + "quartz" + File.separatorChar + "resources", "questionnaires");

  // private QuestionnaireVariableNameResolver variableNameResolver;

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
    new MagmaEngine().extend(new MagmaJsExtension()).extend(new MagmaXStreamExtension());
    if(bundleSourceDirectory.exists()) {
      FileUtil.copyDirectory(bundleSourceDirectory, bundleRootDirectory);
    }
  }

  /**
   * @throws QuestionnaireVariableNameNotUniqueException when non unique variable names are encountered.
   */
  public void createQuestionnaire(QuestionnaireBuilder builder, Locale... locales) {
    // Create the bundle manager.
    QuestionnaireBundleManager bundleManager = newBundleManager();

    // Create the bundle questionnaire.
    Questionnaire questionnaire = builder.getQuestionnaire();
    UniqueQuestionnaireElementNameBuilder.ensureQuestionnaireVariableNamesAreUnique(questionnaire);
    try {
      createBundle(bundleManager, questionnaire, locales);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Helper method for extracting a sub questionnaire from an existing questionnaire.
   * @param name original questionnaire name
   * @param newName extracted questionnaire name
   * @param fromPage pages from this page name
   * @param toPage pages to this page name (inclusive)
   */
  public void extractQuestionnaire(String name, String newName, String fromPage, String toPage) {
    QuestionnaireBundle bundle;

    // Create the bundle manager.
    QuestionnaireBundleManager bundleManager = newBundleManager();

    bundle = bundleManager.getPersistedBundle(name);

    Questionnaire questionnaire = bundle.getQuestionnaire();

    // QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire(newName, questionnaire.getVersion());
    questionnaire.setName(newName);

    QPos qpos = QPos.Before;
    List<Page> pagesToRemove = new ArrayList<Page>();
    for(Page page : questionnaire.getPages()) {
      if(qpos == QPos.Before && page.getName().equals(fromPage)) {
        qpos = QPos.In;
      }

      if(qpos == QPos.In) {
        System.out.println(page);
      } else {
        pagesToRemove.add(page);
      }

      if(qpos == QPos.In && page.getName().equals(toPage)) {
        qpos = QPos.After;
      }
    }

    // remove pages
    for(Page page : pagesToRemove) {
      questionnaire.removePage(page);
    }

    // remove empty sections
    List<Section> sectionsToRemove = new ArrayList<Section>();
    for(Section section : questionnaire.getSections()) {
      isSectionEmpty(section, sectionsToRemove);
    }
    for(Section section : sectionsToRemove) {
      if(section.getParentSection() != null) {
        section.getParentSection().removeSection(section);
      } else {
        questionnaire.removeSection(section);
      }
    }

    try {
      FileUtil.copyDirectory(new File(bundleRootDirectory, name), new File(bundleRootDirectory, newName));
      createBundle(bundleManager, questionnaire, questionnaire.getLocales().toArray(new Locale[questionnaire.getLocales().size()]));
    } catch(IOException e) {
      e.printStackTrace();
    }

  }

  private boolean isSectionEmpty(Section section, List<Section> toRemove) {
    for(Section subSection : section.getSections()) {
      if(!isSectionEmpty(subSection, toRemove)) {
        return false;
      }
    }

    if(section.getPages().size() > 0) {
      return false;
    }

    toRemove.add(section);
    return true;
  }

  private QuestionnaireBundleManager newBundleManager() {
    // Create the bundle manager.
    QuestionnaireBundleManager bundleManager = new QuestionnaireBundleManagerImpl(bundleRootDirectory);
    ((QuestionnaireBundleManagerImpl) bundleManager).setResourceLoader(new PathMatchingResourcePatternResolver());
    return bundleManager;
  }

  private void createBundle(QuestionnaireBundleManager bundleManager, Questionnaire questionnaire, Locale... locales) throws IOException {
    QuestionnaireBundle bundle = bundleManager.createBundle(questionnaire);
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
  }

  private enum QPos {
    Before, In, After
  }
}
