/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.behavior;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;

/**
 * Provides a tooltip to an html element identified by an id. The tooltip JQuery plugin code used is provided by:
 * {@link http://bassistance.de/jquery-plugins/jquery-plugin-tooltip/}
 */
public class DisplayTooltipBehaviour extends AbstractBehavior {

  private static final long serialVersionUID = 7492802968802658076L;

  private String id;

  private String options = "";

  public DisplayTooltipBehaviour(String id) {
    this.id = id;
  }

  /**
   * Adds a tooltip to the html elements specified by the id. Options can be passed to the JQuery tooltip plugin.
   * @param id html element id.
   * @param options JQuery tooltip plugin options. {@link http://docs.jquery.com/Plugins/Tooltip/tooltip#options}
   */
  public DisplayTooltipBehaviour(String id, String options) {
    this.id = id;
    this.options = options;
  }

  public void renderHead(IHeaderResponse response) {
    response.renderJavascriptReference(new JavascriptResourceReference(DisplayTooltipBehaviour.class, "jquery.dimensions.js"));
    response.renderJavascriptReference(new JavascriptResourceReference(DisplayTooltipBehaviour.class, "jquery.tooltip.js"));
    response.renderCSSReference(new ResourceReference(DisplayTooltipBehaviour.class, "jquery.tooltip.css"));
    String script = "$('#" + id + "').tooltip(" + options + ");";
    response.renderOnLoadJavascript(script);
  }

  @Override
  public boolean isTemporary() {
    return true;
  }
}