package org.obiba.onyx.quartz.core.service.impl;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;

/**
 * This class provides a set of convenience methods for <code>INavigationStrategy</code> implementors.
 * 
 * @author cag-dspathis
 * 
 */
public class NavigationStrategySupport {

  /**
   * Indicates whether the specified page contains at least one unanswered (and answerable) question.
   * 
   * @param service
   * @param page
   * @return
   */
  public static boolean hasUnansweredQuestion(ActiveQuestionnaireAdministrationService service, Page page) {
    // TODO: Implement!
    return false;
  }

  /**
   * Indicates whether the specified page contains at least one question with an active answer.
   * 
   * @param service
   * @param page
   * @return
   */
  public static boolean hasActiveAnswer(ActiveQuestionnaireAdministrationService service, Page page) {
    // TODO: Implement!    
    return false;
  }

  /**
   * Indicates whether the specified page contains at least one question with a inactive answer.
   * 
   * @param service
   * @param page
   * @return
   */
  public static boolean hasInactiveAnswer(ActiveQuestionnaireAdministrationService service, Page page) {
    // TODO: Implement!
    return false;
  }
}