package org.obiba.onyx.quartz.core.engine.questionnaire.bundle;

import java.io.IOException;
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
   * Returns the latest version of the specified questionnaire bundle.
   *  
   * @param name questionnaire bundle name
   * @return questionnaire bundle (or <code>null</code> if no bundle with the specified name was found)
   */
  public QuestionnaireBundle getBundle(String name);

  /**
   * Returns the latest versions of all questionnaire bundles.
   * 
   * @return managed questionnaire bundles
   */
  public Set<QuestionnaireBundle> bundles();
}