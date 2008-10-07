package org.obiba.onyx.quartz.core.engine.questionnaire.bundle;

import java.io.File;
import java.util.Set;

public interface QuestionnaireBundleManager {

  /**
   * Returns the root directory of managed questionnaire bundles.
   * 
   * @return questionnaire bundle root directory
   */
  public File getRootDirectory();

  /**
   * Initializes the bundle manager.
   * 
   * Calling this method loads all questionnaire bundles found under the root directory.
   */
  public void init();
  
  /**
   * Returns the latest version of the specified questionnaire bundle.
   * 
   * @param name questionnaire bundle name
   * @return questionnaire bundle (or <code>null</code> if no bundle with the specified name was found)
   */
  public QuestionnaireBundle getBundle(String name);

  /**
   * Returns the set of managed questionnaire bundles.
   *  
   * @return managed questionnaire bundles
   */
  public Set<QuestionnaireBundle> bundles();
}