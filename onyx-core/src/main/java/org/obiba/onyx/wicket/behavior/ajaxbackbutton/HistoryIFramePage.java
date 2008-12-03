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

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;

/**
 * 
 */
public class HistoryIFramePage extends WebPage implements IHeaderContributor {

  public HistoryIFramePage() {
  }

  public void renderHead(IHeaderResponse response) {
    response.renderJavascriptReference(new ResourceReference(HistoryAjaxBehavior.class, "jquery.js"));
  }

}
