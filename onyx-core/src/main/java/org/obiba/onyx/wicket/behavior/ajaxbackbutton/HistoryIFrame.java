/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.behavior.ajaxbackbutton;

import org.apache.wicket.IPageMap;
import org.apache.wicket.markup.html.link.InlineFrame;

/**
 * 
 */
public class HistoryIFrame extends InlineFrame {

  private static final long serialVersionUID = 1L;

  public HistoryIFrame(final String wid, final IPageMap pageMap) {
    super(wid, pageMap, HistoryIFramePage.class);

    setOutputMarkupId(true);
    setMarkupId("historyIframe");
  }

}
