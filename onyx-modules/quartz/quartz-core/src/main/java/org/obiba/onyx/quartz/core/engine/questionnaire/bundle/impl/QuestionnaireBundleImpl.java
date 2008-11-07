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
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireStreamer;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.onyx.util.StringReferenceCompatibleMessageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ResourceLoader;

public class QuestionnaireBundleImpl implements QuestionnaireBundle {

  //
  // Constants
  //

  public static final String LANGUAGE_FILE_BASENAME = "language";

  public static final String LANGUAGE_FILE_EXTENSION = ".properties";

  private static final Logger log = LoggerFactory.getLogger(QuestionnaireBundleImpl.class);

  //
  // Instance Variables
  //

  private File bundleVersionDir;

  private Questionnaire questionnaire;

  private MessageSource messageSource;

  private IPropertyKeyProvider propertyKeyProvider;

  //
  // Constructors
  //

  public QuestionnaireBundleImpl(ResourceLoader resourceLoader, File bundleVersionDir, Questionnaire questionnaire, IPropertyKeyProvider propertyKeyProvider) {
    if(bundleVersionDir == null) {
      throw new IllegalArgumentException("Null bundle version directory");
    }

    if(questionnaire == null) {
      throw new IllegalArgumentException("Null questionnaire");
    }

    if(propertyKeyProvider == null) {
      throw new IllegalArgumentException("Null property key provider");
    }

    this.bundleVersionDir = bundleVersionDir;
    this.questionnaire = questionnaire;
    this.propertyKeyProvider = propertyKeyProvider;

    // Initialize the message source.
    messageSource = new ReloadableResourceBundleMessageSource() {
      @Override
      protected MessageFormat createMessageFormat(String msg, Locale locale) {
        return new StringReferenceCompatibleMessageFormat((msg != null ? msg : ""), locale);
      }
    };
    ((ReloadableResourceBundleMessageSource) messageSource).setBasename(getMessageSourceBasename(bundleVersionDir));

    if(resourceLoader != null) {
      ((ReloadableResourceBundleMessageSource) messageSource).setResourceLoader(resourceLoader);
    }

    for(Locale locale : getAvailableLanguages()) {
      if(!this.questionnaire.getLocales().contains(locale)) this.questionnaire.addLocale(locale);
    }
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
      QuestionnaireStreamer.storeLanguage(getQuestionnaire(), locale, language, propertyKeyProvider, fos = new FileOutputStream(new File(bundleVersionDir, LANGUAGE_FILE_BASENAME + '_' + locale + LANGUAGE_FILE_EXTENSION)));
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

    File languageFile = new File(bundleVersionDir, LANGUAGE_FILE_BASENAME + '_' + locale + LANGUAGE_FILE_EXTENSION);

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
      language = builder.getProperties(propertyKeyProvider);
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

  public String getPropertyKey(ILocalizable localizable, String property) {
    return propertyKeyProvider.getPropertyKey(localizable, property);
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
