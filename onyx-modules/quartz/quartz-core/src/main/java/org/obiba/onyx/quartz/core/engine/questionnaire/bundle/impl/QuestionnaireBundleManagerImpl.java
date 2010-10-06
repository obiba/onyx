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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireStreamer;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

/**
 * A file system based implementation of <code>QuestionnaireBundleManager</code>.
 * 
 * @author cag-dspathis
 * 
 */
public class QuestionnaireBundleManagerImpl implements QuestionnaireBundleManager, ResourceLoaderAware, InitializingBean, ApplicationContextAware {
  //
  // Constants
  //

  public static final String QUESTIONNAIRE_BASE_NAME = "questionnaire";

  private static final Logger log = LoggerFactory.getLogger(QuestionnaireBundleManagerImpl.class);

  //
  // Instance Variables
  //

  private File rootDir;

  private Map<String, QuestionnaireBundle> bundleCache;

  private ResourceLoader resourceLoader;

  private IPropertyKeyProvider propertyKeyProvider;

  private ApplicationContext applicationContext;

  //
  // Constructors
  //

  /**
   * Creates a <code>QuestionnaireBundleManagerImpl</code>
   * 
   * @param rootDir root directory
   */
  public QuestionnaireBundleManagerImpl(File rootDir) {
    // Initialize the root directory here for tests. When running within Spring,
    // the root directory will be re-initialized, with the appropriate resource loader,
    // after properties are set (see afterPropertiesSet() method).
    this.rootDir = rootDir;

    bundleCache = new HashMap<String, QuestionnaireBundle>();
  }

  //
  // ResourceLoaderAware Methods
  //

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  //
  // InitializingBean Methods
  //

  @Override
  public void afterPropertiesSet() throws Exception {
    String resourcePath = rootDir.isAbsolute() ? "file:" + rootDir.getPath() : rootDir.getPath();

    rootDir = resourceLoader.getResource(resourcePath).getFile();

    if(!rootDir.exists()) {
      if(!rootDir.mkdirs()) {
        throw new IllegalArgumentException("Unable to create root directory: " + rootDir);
      }

    }

    if(!isValidRootDirectory(rootDir)) {
      throw new IllegalArgumentException("Invalid root directory: " + rootDir);
    }
  }

  public void setPropertyKeyProvider(IPropertyKeyProvider propertyKeyProvider) {
    this.propertyKeyProvider = propertyKeyProvider;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  //
  // QuestionnaireBundleManager Methods
  //

  @Override
  public QuestionnaireBundle createBundle(Questionnaire questionnaire) throws IOException {
    File bundleVersionDir = new File(new File(rootDir, questionnaire.getName()), questionnaire.getVersion());

    // Create the bundle object.
    QuestionnaireBundle bundle = new QuestionnaireBundleImpl(resourceLoader, bundleVersionDir, questionnaire, propertyKeyProvider);

    // Serialize it.
    serializeBundle(bundle);

    return bundle;
  }

  @Override
  public QuestionnaireBundle getBundle(String name) {
    // Look for the bundle in the cache.
    QuestionnaireBundle bundle = bundleCache.get(name);

    // If not found, load the latest version of the bundle from the file system.
    if(bundle == null) {
      try {
        bundle = loadBundle(name);
      } catch(IOException ex) {
        log.error("Failed to load questionnaire bundle " + name);
      }
    }
    return bundle;
  }

  @Override
  public QuestionnaireBundle getClearedMessageSourceCacheBundle(String name) {
    QuestionnaireBundle bundle = getBundle(name);
    if(bundle != null) bundle.clearMessageSourceCache();
    return bundle;
  }

  @Override
  public Set<QuestionnaireBundle> bundles() {
    final Set<QuestionnaireBundle> bundles = new HashSet<QuestionnaireBundle>();

    // Iterate over all bundle directories.
    rootDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        if(file.isDirectory() && file.getName().charAt(0) != '.') {
          bundles.add(getBundle(file.getName()));
          return true;
        }

        return false;
      }
    });

    return bundles;
  }

  //
  // Methods
  //

  /**
   * Indicates whether the specified root directory is valid.
   * 
   * @param directory root directory
   * @return <code>true</code> if the directory is not <code>null</code>, is indeed a directory (i.e., not a file), and
   * is readable
   */
  private boolean isValidRootDirectory(File directory) {
    return (directory != null && directory.isDirectory() && directory.canRead());
  }

  /**
   * Loads the latest version of the specified questionnaire bundle from the file system.
   * 
   * @param name bundle name
   * @return bundle (or <code>null</code> if not found
   * @throws IOException on any I/O error
   */
  private QuestionnaireBundle loadBundle(String name) throws IOException {
    QuestionnaireBundle bundle = null;

    // Determine the latest version of the bundle.
    File bundleDir = new File(rootDir, name);
    String latestBundleVersion = getLatestBundleVersion(bundleDir);

    if(latestBundleVersion != null) {
      try {
        // Load the bundle.
        bundle = deserializeBundle(new File(bundleDir, latestBundleVersion));

        // Put it in the cache.
        cacheBundle(bundle);
      } catch(Exception ex) {
        log.error("Failed to deserialize bundle " + bundleDir.getName() + " version " + latestBundleVersion, ex);
      }
    } else {
      // Log as an error the fact that a bundle exists with no versions.
      log.error("No version exists of questionnaire bundle " + bundleDir.getName() + "!");
    }

    return bundle;
  }

  /**
   * Serializes a bundle.
   * 
   * This method creates the necessary directory structure for the bundle, then serializes the bundle's questionnaire in
   * the appropriate directory, as follows:
   * 
   * <code>{rootDir}/{bundleDir}/{bundleVersionDir}/questionnaire.xml</code>
   * 
   * @param bundle questionnaire bundle
   * @throws IOException on any I/O error
   */
  private void serializeBundle(QuestionnaireBundle bundle) throws IOException {
    // Create the bundle version directory, which will contain the questionnaire.
    File bundleVersionDir = new File(new File(rootDir, bundle.getQuestionnaire().getName()), bundle.getQuestionnaire().getVersion());
    if(!bundleVersionDir.exists()) {
      if(!bundleVersionDir.mkdirs()) {
        throw new IllegalArgumentException("Failed creating bundle version directory: " + bundleVersionDir);
      }
    }

    // Create the questionnaire file.
    File questionnaireFile = new File(bundleVersionDir, QUESTIONNAIRE_BASE_NAME + ".xml");
    if(!questionnaireFile.exists()) {
      if(!questionnaireFile.createNewFile()) {
        throw new IllegalArgumentException("Failed creating questionnaire file: " + questionnaireFile);
      }
    }

    // Serialize the questionnaire to the file.
    FileOutputStream fos = null;

    try {
      fos = new FileOutputStream(questionnaireFile);
      QuestionnaireStreamer.toXML(bundle.getQuestionnaire(), new FileOutputStream(questionnaireFile));
    } finally {
      if(fos != null) {
        try {
          fos.close();
        } catch(IOException ex) {
          log.error("Failed to close questionnaire file output stream", ex);
        }
      }
    }
  }

  /**
   * Deserializes a bundle.
   * 
   * This method first loads the bundle's questionnaire from the file system then instantiates and returns a
   * <code>QuestionnaireBundle</code> containing that questionnaire.
   * 
   * @param bundleVersionDir bundle version directory
   * @return questionnaire bundle
   * @throws IOException on any I/O error
   */
  private QuestionnaireBundle deserializeBundle(File bundleVersionDir) throws IOException {
    // Deserialize the questionnaire.
    Questionnaire questionnaire = null;
    FileInputStream fis = null;

    try {
      fis = new FileInputStream(new File(bundleVersionDir, QUESTIONNAIRE_BASE_NAME + ".xml"));
      questionnaire = QuestionnaireStreamer.fromBundle(fis, applicationContext);
    } finally {
      if(fis != null) {
        try {
          fis.close();
        } catch(IOException ex) {
          log.error("Failed to close questionnaire file input stream", ex);
        }
      }
    }

    // Create the bundle.
    QuestionnaireBundle bundle = new QuestionnaireBundleImpl(resourceLoader, bundleVersionDir, questionnaire, propertyKeyProvider);

    return bundle;
  }

  private void cacheBundle(QuestionnaireBundle bundle) {
    bundleCache.put(bundle.getQuestionnaire().getName(), bundle);
    log.debug("Added bundle " + bundle.getName() + " to the cache");
  }

  private String getLatestBundleVersion(File bundleDir) {
    VersionFileFilter versionFileFilter = new VersionFileFilter();

    bundleDir.listFiles(versionFileFilter);
    String latestVersion = versionFileFilter.getLatestVersion();

    return latestVersion;
  }

  //
  // Inner Classes
  //

  class VersionFileFilter implements FileFilter {
    private Version latestVersion;

    private String latestVersionFilename;

    @Override
    public boolean accept(File file) {
      if(file.isDirectory() && file.getName().charAt(0) != '.') {
        Version version = new Version(file.getName());

        if(latestVersion == null || (version.compareTo(latestVersion) > 0)) {
          latestVersion = version;
          latestVersionFilename = file.getName();
        }

        return true;
      }

      return false;
    }

    public String getLatestVersion() {
      return latestVersionFilename;
    }
  }

  @Override
  public File getRootDir() {
    return rootDir;
  }

}
