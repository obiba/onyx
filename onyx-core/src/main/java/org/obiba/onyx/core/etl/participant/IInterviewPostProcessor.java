package org.obiba.onyx.core.etl.participant;

import org.obiba.onyx.core.domain.participant.Interview;

/**
 * Interview processor, called at different stage of an interview life cycle.
 */
public interface IInterviewPostProcessor {

  /**
   * Called at participant reception after an interview is created.
   *
   * @param interview
   */
  void onCreation(Interview interview);

  /**
   * Called after an interview lock is obtained.
   *
   * @param interview
   */
  void onProgress(Interview interview);

}
