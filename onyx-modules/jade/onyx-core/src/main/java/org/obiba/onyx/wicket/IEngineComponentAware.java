package org.obiba.onyx.wicket;

import org.obiba.onyx.wicket.action.ActionWindow;

/**
 * An interface to inject to stage module component some usefull tools.
 * @author Yannick Marcon
 *
 */
public interface IEngineComponentAware {
  
  /**
   * Set the action window that can be used by the stage component to perform interactive actions.
   * @param window
   */
  public void setActionWindwon(ActionWindow window);
  
}
