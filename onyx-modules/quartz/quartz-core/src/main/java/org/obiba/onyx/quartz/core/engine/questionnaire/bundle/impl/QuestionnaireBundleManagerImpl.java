package org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import com.thoughtworks.xstream.XStream;

/**
 * A file system based implementation of <code>QuestionnaireBundleManager</code>.
 * 
 * @author cag-dspathis
 * 
 */
public class QuestionnaireBundleManagerImpl implements QuestionnaireBundleManager, ResourceLoaderAware, InitializingBean {
  //
  // Constants
  //

  public static final String QUESTIONNAIRE_BASE_NAME = "questionnaire";

  private static final Logger log = LoggerFactory.getLogger(QuestionnaireBundleManagerImpl.class);

  //
  // Instance Variables
  //

  private File rootDir;

  private Set<QuestionnaireBundle> bundleCache;

  private ResourceLoader resourceLoader;

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

    bundleCache = new HashSet<QuestionnaireBundle>();
  }

  //
  // ResourceLoaderAware Methods
  //

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  //
  // InitializingBean Methods
  //

  public void afterPropertiesSet() throws Exception {
    String resourcePath = rootDir.isAbsolute() ? "file:"+rootDir.getPath() : rootDir.getPath();
    
    rootDir = resourceLoader.getResource(resourcePath).getFile();

    if(!rootDir.exists()) {
      rootDir.mkdirs();
    }

    if(!isValidRootDirectory(rootDir)) {
      throw new IllegalArgumentException("Invalid root directory: " + rootDir);
    }
  }

  //
  // QuestionnaireBundleManager Methods
  //

  public QuestionnaireBundle createBundle(Questionnaire questionnaire) throws IOException {
    File bundleVersionDir = new File(new File(rootDir, questionnaire.getName()), questionnaire.getVersion());

    // Create the bundle object.
    QuestionnaireBundle bundle = new QuestionnaireBundleImpl(bundleVersionDir, questionnaire);

    // Serialize it.
    serializeBundle(bundle);

    return bundle;
  }

  public QuestionnaireBundle getBundle(String name) {
    QuestionnaireBundle bundle = null;

    // Look for the bundle in the cache.
    for(QuestionnaireBundle aBundle : bundleCache) {
      if(aBundle.getName().equals(name)) {
        bundle = aBundle;
        break;
      }
    }

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

  public Set<QuestionnaireBundle> bundles() {
    final Set<QuestionnaireBundle> bundles = new HashSet<QuestionnaireBundle>();

    // Iterate over all bundle directories.
    rootDir.listFiles(new FileFilter() {
      public boolean accept(File file) {
        if(file.isDirectory()) {
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
   * @return <code>true</code> if the directory is not <code>null</code>, is indeed a directory (i.e., not a file),
   * and is readable
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
    bundleVersionDir.mkdirs();

    // Create the questionnaire file.
    File questionnaireFile = new File(bundleVersionDir, QUESTIONNAIRE_BASE_NAME + ".xml");
    if(!questionnaireFile.exists()) {
      questionnaireFile.createNewFile();
    }

    // Serialize the questionnaire to the file.
    (new QuestionnaireSerializer()).serialize(bundle.getQuestionnaire(), questionnaireFile);
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
    Questionnaire questionnaire = (new QuestionnaireSerializer()).deserialize(bundleVersionDir);

    // Create the bundle.
    QuestionnaireBundle bundle = new QuestionnaireBundleImpl(bundleVersionDir, questionnaire);

    return bundle;
  }

  private void cacheBundle(QuestionnaireBundle bundle) {
    bundleCache.add(bundle);
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

  class QuestionnaireSerializer {

    private XStream xstream;

    private QuestionnaireSerializer() {
      initializeXStream();
    }

    private void initializeXStream() {
      xstream = new XStream();
      xstream.setMode(XStream.ID_REFERENCES);
      xstream.alias("questionnaire", Questionnaire.class);
      xstream.useAttributeFor(Questionnaire.class, "name");
      xstream.useAttributeFor(Questionnaire.class, "version");
      xstream.alias("section", Section.class);
      xstream.useAttributeFor(Section.class, "name");
      xstream.alias("page", Page.class);
      xstream.useAttributeFor(Page.class, "name");
      xstream.alias("question", Question.class);
      xstream.useAttributeFor(Question.class, "name");
      xstream.useAttributeFor(Question.class, "number");
      xstream.useAttributeFor(Question.class, "required");
      xstream.useAttributeFor(Question.class, "multiple");
      xstream.useAttributeFor(Question.class, "minCount");
      xstream.useAttributeFor(Question.class, "maxCount");
      xstream.alias("category", Category.class);
      xstream.useAttributeFor(Category.class, "name");
      xstream.alias("questionCategory", QuestionCategory.class);
      xstream.useAttributeFor(QuestionCategory.class, "repeatable");
      xstream.useAttributeFor(QuestionCategory.class, "selected");
      xstream.useAttributeFor(QuestionCategory.class, "exportName");
    }

    public void serialize(Questionnaire questionnaire, File questionnaireFile) throws IOException {
      FileOutputStream fos = null;

      try {
        fos = new FileOutputStream(questionnaireFile);
        xstream.toXML(questionnaire, fos);
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

    public Questionnaire deserialize(File bundleVersionDir) throws IOException {
      Questionnaire questionnaire = null;
      File questionnaireFile = new File(bundleVersionDir, QUESTIONNAIRE_BASE_NAME + ".xml");

      FileInputStream fis = null;

      try {
        fis = new FileInputStream(questionnaireFile);
        questionnaire = (Questionnaire) xstream.fromXML(new FileInputStream(questionnaireFile));
      } finally {
        if(fis != null) {
          try {
            fis.close();
          } catch(IOException ex) {
            log.error("Failed to close questionnaire file input stream", ex);
          }
        }
      }

      return questionnaire;
    }
  }

  class VersionFileFilter implements FileFilter {
    private Version latestVersion;
    private String latestVersionFilename;

    public boolean accept(File file) {
      if(file.isDirectory()) {
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
}