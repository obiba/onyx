/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.base.panel;

import org.apache.wicket.markup.html.panel.Panel;

/**
 * Base page menu bar.
 * @author ymarcon
 * 
 */
public class MenuBar extends Panel {

  private static final long serialVersionUID = 1L;

  public MenuBar(String id) {
    super(id);
    setMarkupId("menuBar");
    setOutputMarkupId(true);

    buildMenus();
    buildAddOns();
  }

  protected void buildMenus() {
    MenuBuilder.build(this);
  }

  protected void buildAddOns() {
    ;
  }
}
