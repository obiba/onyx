/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.editor.utils.tab.HidableTab;

/**
 *
 */
public abstract class SavableHidableTab extends HidableTab {

  private static final long serialVersionUID = 1L;

  /**
   * @param title
   */
  public SavableHidableTab(IModel<String> title) {
    super(title);
  }

  public abstract void save();

}
