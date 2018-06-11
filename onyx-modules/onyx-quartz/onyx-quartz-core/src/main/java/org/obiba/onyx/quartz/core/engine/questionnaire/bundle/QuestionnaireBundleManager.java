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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

public interface QuestionnaireBundleManager {
  /**
   * Creates a bundle for the given questionnaire.
   * 
   * @param questionnaire questionnaire
   * @return questionnaire bundle created
   * @throws IOException on any I/O-related error
   */
  public QuestionnaireBundle createBundle(Questionnaire questionnaire) throws IOException;

  /**
   * Same that createBundle(Questionnaire questionnaire)
   * 
   * @param questionnaire
   * @param reloadProperties Set reloadProperties to false to do not fetch properties files to add to questionnaire
   * locales.
   * @return
   * @throws IOException
   */
  QuestionnaireBundle createBundle(Questionnaire questionnaire, boolean reloadProperties) throws IOException;

  /**
   * Returns the latest version of the specified questionnaire bundle.
   * 
   * @param name questionnaire bundle name
   * @return questionnaire bundle (or <code>null</code> if no bundle with the specified name was found)
   */
  public QuestionnaireBundle getBundle(String name);

  /**
   * Returns the latest version of the specified questionnaire bundle. (from xml file, not the cached)
   * 
   * @param name
   * @return
   */
  public QuestionnaireBundle getPersistedBundle(String name);

  /**
   * Returns the latest (in cache) versions of all questionnaire bundles.
   * 
   * @return managed questionnaire bundles
   */
  public Set<QuestionnaireBundle> bundles();

  /**
   * @return
   */
  File getRootDir();

  /**
   * @param name
   * @return
   * @throws IOException
   */
  File generateBundleZip(String name) throws IOException;

  /**
   * @param bundle
   */
  void cacheBundle(QuestionnaireBundle bundle);

  /**
   * Returns the latest (in persisted files) versions of all questionnaire bundles.
   * 
   * @return managed questionnaire bundles
   */
  Set<QuestionnaireBundle> persistedBundles();

  /**
   * @return
   */
  int countQuestionnaires();

  /**
   * @param inputStream
   * @return
   */
  Questionnaire load(InputStream inputStream);

  /**
   * @param questionnaire
   * @param localeProperties
   * @return
   * @throws IOException
   */
  QuestionnaireBundle createBundle(Questionnaire questionnaire, File... localeProperties) throws IOException;

  /**
   * @param questionnaire
   */
  void deleteBundle(Questionnaire questionnaire);

  /**
   * @param bundle
   */
  void flushBundle(QuestionnaireBundle bundle);

}
