package org.obiba.onyx.quartz.core.wicket.layout;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * Page layout definition.
 * @author Yannick Marcon
 *
 */
public abstract class PageLayout extends Panel {

  public PageLayout(String id, IModel model) {
    super(id, model);
  }
  
  /**
   * Called when page is left to go to next page.
   */
  public abstract void onNext();
  
  /**
   * Called when page is left to go to previous page.
   */
  public abstract void onPrevious();
  
}
