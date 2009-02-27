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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.obiba.onyx.core.io.support.LocalizedResourceLoader;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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

  /** Used to handle the language bundles which can be handled like any other localized resource */
  private LocalizedResourceLoader messageBundleLoader;

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

    this.messageBundleLoader = new LocalizedResourceLoader();
    this.messageBundleLoader.setResourceName(LANGUAGE_FILE_BASENAME);
    this.messageBundleLoader.setResourceExtension(LANGUAGE_FILE_EXTENSION);
    this.messageBundleLoader.setResourcePath(bundleVersionDir);
    this.messageBundleLoader.setResourceLoader(resourceLoader);

    // Initialize the message source.
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource() {
      @Override
      protected MessageFormat createMessageFormat(String msg, Locale locale) {
        return new StringReferenceCompatibleMessageFormat((msg != null ? msg : ""), locale);
      }
    };
    messageSource.setBasename(this.messageBundleLoader.getResourceBasename());

    if(resourceLoader != null) {
      messageSource.setResourceLoader(resourceLoader);
    }

    this.messageSource = messageSource;

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

  public Resource getImageResource(String imageId) {
    File imageDir = new File(bundleVersionDir, "images");
    return new FileSystemResource(new File(imageDir, imageId));
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

    Resource messageBundle = messageBundleLoader.getLocalizedResource(locale);
    if(messageBundle != null) {
      language = new Properties();
      InputStream is = null;
      try {
        language.load(is = messageBundle.getInputStream());
      } catch(IOException ex) {
        language = null;
        log.error("Failed to load language resource", ex);
      } finally {
        if(is != null) {
          try {
            is.close();
          } catch(IOException ex) {
            log.error("Failed to close language input stream", ex);
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

  public List<Locale> getAvailableLanguages() {
    return messageBundleLoader.getAvailableLocales();
  }

  public MessageSource getMessageSource() {
    return messageSource;
  }

  public String getPropertyKey(IQuestionnaireElement localizable, String property) {
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

}
