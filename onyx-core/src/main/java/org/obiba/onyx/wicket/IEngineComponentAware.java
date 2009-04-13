/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket;

import org.obiba.onyx.core.reusable.FeedbackWindow;
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
  public void setActionWindow(ActionWindow window);

  /**
   * The place to display feedback messages.
   * @param feedback
   */
  public void setFeedbackWindow(FeedbackWindow feedbackWindow);
}
