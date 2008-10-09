package org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

public class QuestionnaireBundleImpl implements QuestionnaireBundle {
  //
  // Constants
  //

  public static final String LANGUAGE_FILE_BASENAME = "questionnaire";

  public static final String LANGUAGE_FILE_EXTENSION = ".properties";

  private static final Logger log = LoggerFactory.getLogger(QuestionnaireBundleImpl.class);

  //
  // Instance Variables
  //

  private File bundleVersionDir;

  private Questionnaire questionnaire;

  private MessageSource messageSource;

  //
  // Constructors
  //

  public QuestionnaireBundleImpl(File bundleVersionDir, Questionnaire questionnaire) {
    if(bundleVersionDir == null) {
      throw new IllegalArgumentException("Null bundle version directory");
    }

    if(questionnaire == null) {
      throw new IllegalArgumentException("Null questionnaire");
    }

    this.bundleVersionDir = bundleVersionDir;
    this.questionnaire = questionnaire;

    // Initialize the message source.
    messageSource = new ReloadableResourceBundleMessageSource();
    ((ReloadableResourceBundleMessageSource) messageSource).setBasename(getMessageSourceBasename(bundleVersionDir));
  }

  //
  // QuestionnaireBundle Methods
  //

  public String getName() {
    return questionnaire.getName();
  }

  public Questionnaire getQuestionnaire() {
    return questionnaire;
  }

  public void setLanguage(Locale locale, Properties language) {
    FileOutputStream fos = null;

    try {
      language.store(fos = new FileOutputStream(new File(bundleVersionDir, "questionnaire_" + locale + LANGUAGE_FILE_EXTENSION)), null);
    } catch(IOException ex) {
      log.error("Failed to store language file", ex);
    } finally {
      if(fos != null) {
        try {
          fos.close();
        } catch(IOException ex) {
          log.error("Failed to close language file output stream", ex);
        }
      }
    }
  }

  public Properties getLanguage(Locale locale) {
    Properties language = null;

    File languageFile = new File(bundleVersionDir, "questionnaire_" + locale + LANGUAGE_FILE_EXTENSION);

    if(languageFile.exists()) {
      language = new Properties();

      FileInputStream fis = null;

      try {
        language.load(fis = new FileInputStream(languageFile));
      } catch(IOException ex) {
        language = null;
        log.error("Failed to load language file", ex);
      } finally {
        if(fis != null) {
          try {
            fis.close();
          } catch(IOException ex) {
            log.error("Failed to close language file input stream", ex);
          }
        }
      }
    }

    if(language == null) {
      QuestionnaireBuilder builder = QuestionnaireBuilder.getInstance(getQuestionnaire());
      language = builder.getProperties();
    }

    return language;
  }

  public Set<Locale> getAvailableLanguages() {
    final Set<Locale> languages = new HashSet<Locale>();

    // Iterate over all language files.
    bundleVersionDir.listFiles(new FileFilter() {
      public boolean accept(File file) {
        String fileName = file.getName();

        if(file.isFile() && fileName.startsWith(LANGUAGE_FILE_BASENAME + '_') && fileName.endsWith(LANGUAGE_FILE_EXTENSION)) {
          languages.add(new Locale(extractLocaleString(fileName)));
          return true;
        }

        return false;
      }
    });

    return languages;
  }

  public MessageSource getMessageSource() {
    return messageSource;
  }

  //
  // Methods
  //

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if(o instanceof QuestionnaireBundle) {
      return ((QuestionnaireBundle) o).getName().equals(getName());
    }

    return false;
  }

  /**
   * Given a bundle version directory, returns the base name used to configure the Spring message source.
   * 
   * @param bundleVersionDir bundle version directory
   * @return message source base name
   */
  private String getMessageSourceBasename(File bundleVersionDir) {
    StringBuffer baseName = new StringBuffer();

    // Append the bundle root directory.
    baseName.append(bundleVersionDir.getParentFile().getParentFile().getName());

    // Append the bundle directory.
    baseName.append('/');
    baseName.append(bundleVersionDir.getParentFile().getName());

    // Append the bundle version directory.
    baseName.append('/');
    baseName.append(bundleVersionDir.getName());

    // Append the resource bundle base name.
    baseName.append('/');
    baseName.append(LANGUAGE_FILE_BASENAME);

    return baseName.toString();
  }

  private String extractLocaleString(String fileName) {
    String localeString = null;

    int startIndex = fileName.indexOf('_');
    int endIndex = fileName.lastIndexOf(LANGUAGE_FILE_EXTENSION);

    localeString = fileName.substring(startIndex + 1, endIndex);

    return localeString;
  }
}