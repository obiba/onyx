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
 * Scroll to bottom of element. This behaviour is temporary and will be executed only once.
 */
public class ScrollToBottomBehaviour extends AbstractBehavior {

  private static final long serialVersionUID = 7492802968802658076L;

  private String id;

  public ScrollToBottomBehaviour(String id) {
    this.id = id;
  }

  public void renderHead(IHeaderResponse response) {
    String script = "$('" + id + "').attr({ scrollTop: $('" + id + "').attr('scrollHeight') });";
    response.renderOnLoadJavascript(script);
  }

  @Override
  public boolean isTemporary() {
    return true;
  }
}
