/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.bundle;

import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;

public interface QuestionnaireBundle {
  /**
   * Returns the name of the bundle.
   * 
   * This is identical with the name of the questionnaire.
   * 
   * @return bundle name
   */
  public String getName();

  /**
   * Returns the bundle's questionnaire.
   * 
   * Exactly one questionnaire exists per bundle.
   * 
   * @return questionnaire
   */
  public Questionnaire getQuestionnaire();

  /**
   * Returns the specified image resource.
   * 
   * @param imageId image id
   * @return image resource (or <code>null</code> if no such image exists in the bundle)
   */
  public Resource getImageResource(String imageId);

  /**
   * Specifies the language in which the questionnaire will be administered in the given locale.
   * 
   * Note: If a language was previously specified for the locale, the new language overwrites the previous one.
   * 
   * @param locale locale
   * @param language language, as a set of key-value entries
   */
  public void setLanguage(Locale locale, Properties language);

  /**
   * Returns the language in which the questionnaire will be administered in the specified locale.
   * 
   * Note: If no language was previously specified for the locale, the language returned will just the keys required
   * (values will be empty).
   * 
   * @param locale locale
   * @return language, as a set of key-value entries
   */
  public Properties getLanguage(Locale locale);

  /**
   * Returns all of the languages in which the bundle's questionnaire may be administered, as a set of locales.
   * 
   * The languages returned are determined by the presence of corresponding resource bundles in the questionnaire
   * bundle. For example, if the following resource bundles exist
   * 
   * <ul>
   * <li>questionnaire_en.properties</li>
   * <li>questionnaire_fr.properties</li>
   * </ul>
   * 
   * a set containing two locales shall be returned -- <code>Locale("en")</code> and <code>Locale("fr")</code>.
   * 
   * @return all languages in which the bundle's questionnaire may be administered
   */
  public List<Locale> getAvailableLanguages();

  /**
   * Returns the Spring <code>MessageSource</code> associated with the bundle.
   * 
   * The <code>MessageSource</code> can be used to localize questionnaire text to any of the languages returned by
   * <code>getAvailableLanguages()</code>.
   * 
   * @return bundle message source
   */
  public MessageSource getMessageSource();

  /**
   * Given a localizable (a questionnaire, or a questionnaire element) and a property, returns the property's key.
   * 
   * @param localizable a questionnaire, or any element of a questionnaire that implements the <code>ILocalizable</code>
   * interface
   * @param property property name
   * @return property key
   */
  public String getPropertyKey(IQuestionnaireElement localizable, String property);

  /**
   * {@link org.springframework.context.support.ReloadableResourceBundleMessageSource#clearCache}
   */
  void clearMessageSourceCache();

  /**
   * Delete language file associated to given locale
   * @param locale
   */
  void deleteLanguage(Locale locale);

  /**
   * Put language in language loaded from file
   * @param locale
   * @param language
   */
  void updateLanguage(Locale locale, Properties language);
}
