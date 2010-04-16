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

import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * The Behavior is useful to run small amounts of JavaScript that can be specified inline.
 */
public class ExecuteJavaScriptBehaviour extends AbstractBehavior {

  private static final long serialVersionUID = 7492802968802658076L;

  private final boolean temporary;

  private final String javaScript;

  /**
   * The supplied javaScript will be run every time the Component is rendered.
   * @param javaScript The script to run.
   */
  public ExecuteJavaScriptBehaviour(String javaScript) {
    this(javaScript, false);
  }

  /**
   * The supplied javaScript will be run when the Component is rendered, either once or everytime depending on the value
   * of temporary.
   * @param javaScript The script to run.
   * @param temporary Set to true if the script should only be run once.
   */
  public ExecuteJavaScriptBehaviour(String javaScript, boolean temporary) {
    this.javaScript = javaScript;
    this.temporary = temporary;
  }

  public void renderHead(IHeaderResponse response) {
    response.renderOnLoadJavascript(javaScript);
  }

  @Override
  public boolean isTemporary() {
    return temporary;
  }
}