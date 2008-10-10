package org.obiba.onyx.quartz.core.engine.questionnaire.question;

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
  public void accept(IQuestionnaireVisitor visitor);
  
}
