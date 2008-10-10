package org.obiba.onyx.quartz.core.engine.questionnaire.util.localization;

import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.IQuestionnaireVisitor;

/**
 * Questionnaire visitor applied to localization.
 * @author Yannick Marcon
 *
 */
public interface IQuestionnaireLocalizationVisitor extends IQuestionnaireVisitor {

  /**
   * Set the property being looked up during the visit.
   */
  public void setProperty(String property);
  
  /**
   * Get the allowed properties for the visited questionnaire element.
   * @return
   */
  public List<String> getProperties();
  
}
