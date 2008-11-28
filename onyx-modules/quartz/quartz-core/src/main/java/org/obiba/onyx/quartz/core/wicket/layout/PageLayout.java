/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * Page layout definition.
 * @author Yannick Marcon
 * 
 */
public abstract class PageLayout extends Panel {

  public PageLayout(String id, IModel pageModel) {
    super(id, pageModel);
  }

  /**
   * Called when page is left to go to next page.
   */
  public abstract void onNext(AjaxRequestTarget target);

  /**
   * Called when page is left to go to previous page.
   */
  public abstract void onPrevious(AjaxRequestTarget target);

}
