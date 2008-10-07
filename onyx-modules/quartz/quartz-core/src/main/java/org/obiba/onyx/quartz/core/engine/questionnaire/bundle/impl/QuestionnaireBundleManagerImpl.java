package org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireStreamer;
import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

public class QuestionnaireBundleManagerImpl implements QuestionnaireBundleManager {
  //
  // Constants
  //
  
  private static final Logger log = LoggerFactory.getLogger(QuestionnaireBundleManager.class);
  
  //
  // Instance Variables
  //
  
  private File rootDirectory;
  
  private Set<QuestionnaireBundle> managedBundles;
  
  //
  // Constructors
  //
  
  public QuestionnaireBundleManagerImpl(File rootDirectory) {
    if (!isValidRootDirectory(rootDirectory)) {
      throw new IllegalArgumentException("Invalid root directory: "+rootDirectory);
    }
    
    this.rootDirectory = rootDirectory;
    
    managedBundles = new HashSet<QuestionnaireBundle>();
  }
  
  //
  // QuestionnaireBundleManager Methods
  //
  
  public File getRootDirectory() {
    return rootDirectory;
  }

  public void init() {
    // Clear all managed bundles, in case of repeated calls to this method.
    clearBundles();

    // Locate the latest version of each bundle under the root directory and register
    // it with the manager.
    File[] bundleDirs = rootDirectory.listFiles();
    
    for (File bundleDir : bundleDirs) {
      // Skip over any non-directories (i.e., files) that might be in the root directory.
      if (!bundleDir.isDirectory()) {
        continue;
      }
      
      // Determine the latest version of the bundle.
      String latestBundleVersion = getLatestBundleVersion(bundleDir);
      
      if (latestBundleVersion != null) {
        try {
          // Deserialize the bundle.
          QuestionnaireBundle bundle = deserializeBundle(new File(bundleDir, latestBundleVersion));
      
          // Register the bundle.
          registerBundle(bundle);
        }
        catch(Exception ex) {
          log.error("Failed to deserialize bundle "+bundleDir.getName()+" version "+latestBundleVersion, ex);
        }
      }
      else {
        // Log as an error the fact that a bundle exists with no versions.
        log.error("No version of questionnaire bundle "+bundleDir.getName()+" is available!");
      }
    }
  }

  public QuestionnaireBundle getBundle(String name) {
    QuestionnaireBundle bundle = null;
    
    for (QuestionnaireBundle aBundle : managedBundles) {
      if (aBundle.getName().equals(name)) {
        bundle = aBundle;
        break;
      }
    }
    
    return bundle;
  }
  
  public Set<QuestionnaireBundle> bundles() {
    return Collections.unmodifiableSet(managedBundles);
  }
  
  //
  // Methods
  //

  private boolean isValidRootDirectory(File directory) {
    return (directory != null && directory.isDirectory() && directory.canRead());
  }
  
  private void clearBundles() {
    managedBundles.clear();  
  }
  
  private void registerBundle(QuestionnaireBundle bundle) {
    managedBundles.add(bundle);
    log.info("Registered bundle "+bundle.getName());
  }
  
  private String getLatestBundleVersion(File bundleDir) {
    Version latestVersion = null;
    
    File[] versionDirs = bundleDir.listFiles();
    
    for (File versionDir : versionDirs) {
      // Skip over any non-directories (i.e., files) that might be in the bundle directory.
      if (!versionDir.isDirectory()) {
        continue;
      }
      
      // Keep track of latest version.
      Version version = new Version(versionDir.getName());
      
      if (latestVersion == null || (version.compareTo(latestVersion) > 0)) {
        latestVersion = version;
      }
    }
    
    return (latestVersion != null) ? latestVersion.toString() : null;
  }

  private QuestionnaireBundle deserializeBundle(File bundleVersionDir) throws FileNotFoundException {
    Questionnaire questionnaire = QuestionnaireStreamer.fromBundle(bundleVersionDir);
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    Set<Locale> languages = getBundleLanguages(bundleVersionDir);
    
    return new QuestionnaireBundleImpl(questionnaire, messageSource, languages);  
  }
  
  private Set<Locale> getBundleLanguages(File bundleVersionDir) {
    Set<Locale> languages = new HashSet<Locale>();
    
    File[] languageFiles = bundleVersionDir.listFiles(new FileFilter() {
      public boolean accept(File file) {
        return (file.isFile() && file.getName().startsWith("questionnaire_") && file.getName().endsWith(".properties"));
      }
    });

    for (File languageFile : languageFiles) {
      languages.add(new Locale(extractLanguageCode(languageFile.getName())));  
    }
    
    return languages;
  }
  
  private String extractLanguageCode(String languageFileName) {
    String languageCode = null;
    
    int separatorIndex = languageFileName.indexOf('_');
    languageCode = languageFileName.substring(separatorIndex+1, separatorIndex+3);
    
    return languageCode;
  }
  
  public static void main(String[] args) throws Exception {
    File rootDirectory = new File(System.getProperty("java.io.tmpdir"), "questionnaires");
    QuestionnaireBundleManager bundleManager = new QuestionnaireBundleManagerImpl(rootDirectory);
    
    bundleManager.init();
  }
}