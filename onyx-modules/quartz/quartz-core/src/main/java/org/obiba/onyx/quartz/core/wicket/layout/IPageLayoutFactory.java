package org.obiba.onyx.quartz.core.wicket.layout;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;

/**
 * Page layout factory.
 * @author Yannick Marcon
 *
 */
public interface IPageLayoutFactory {

   public PageLayout createLayout(String id, Page page);
  
}
