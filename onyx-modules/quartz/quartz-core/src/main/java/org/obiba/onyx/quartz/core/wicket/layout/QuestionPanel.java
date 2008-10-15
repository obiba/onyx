package org.obiba.onyx.quartz.core.wicket.layout;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * Base class for question display.
 * @author Yannick Marcon
 *
 */
public abstract class QuestionPanel extends Panel {
  
  public QuestionPanel(String id, IModel model) {
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
