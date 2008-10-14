package org.obiba.onyx.quartz.core.engine.questionnaire;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

/**
 * Visitable {@link Questionnaire} element.
 * @author Yannick Marcon
 *
 */
public interface IVisitable {

  /**
   * Accept the visit.
   * @param visitor
   */
  public void accept(IVisitor visitor);
  
}
