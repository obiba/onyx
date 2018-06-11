/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.utils.tab;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.model.IModel;

/**
 *
 */
public abstract class HidableTab extends AbstractTab {

  private static final long serialVersionUID = 1L;

  private boolean visible;

  /**
   * @param title
   */
  public HidableTab(IModel<String> title) {
    super(title);
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

}
