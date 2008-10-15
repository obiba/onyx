package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;

/**
 * Handle the walk through each questionnaire element.
 * @author Yannick Marcon
 * @see QuestionnaireWalker
 *
 */
public interface IWalkerVisitor extends IVisitor {

  /**
   * Stop walking through the questionnaire hierarchy if false.
   * @return
   */
  public boolean visiteMore();
  
}
