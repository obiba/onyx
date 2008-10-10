package org.obiba.onyx.quartz.core.engine.questionnaire.question;

/**
 * A localizable element is visitable for providing the localization key for each of its properties.
 * @author Yannick Marcon
 * 
 */
public interface ILocalizable extends IVisitable {

  /**
   * Unique identifier for the localizable element.
   * @return
   */
  public String getName();
}
