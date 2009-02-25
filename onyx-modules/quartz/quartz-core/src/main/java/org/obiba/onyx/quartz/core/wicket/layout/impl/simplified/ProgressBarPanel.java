/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 * This is a simple wicket component which can be used to display a progress bar.
 */
public class ProgressBarPanel extends Panel {

  private static final long serialVersionUID = 1L;

  WebComponent progressBar;

  public ProgressBarPanel(String id) {
    super(id);
    add(progressBar = new WebComponent("progress"));
  }

  public ProgressBarPanel(String id, int progressPercentage) {
    this(id);
    setProgressPercentage(progressPercentage);
  }

  /**
   * Sets the percentage to be displayed on the progress bar.
   * 
   * @param progressPercentage The percentage to be displayed.
   */
  public void setProgressPercentage(int progressPercentage) {
    progressBar.add(new AttributeAppender("style", new Model("width:" + progressPercentage + "%"), ";"));
    addOrReplace(new Label("progressLabel", new Model(progressPercentage)));
  }

}
