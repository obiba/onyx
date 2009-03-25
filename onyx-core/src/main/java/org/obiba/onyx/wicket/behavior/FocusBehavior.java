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

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * Behaviour for setting the focus on a component.
 */
public class FocusBehavior extends AbstractBehavior {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  private Component component;

  //
  // AbstractBehavior Methods
  //

  public void bind(Component component) {
    this.component = component;
  }

  public void renderHead(IHeaderResponse iHeaderResponse) {
    super.renderHead(iHeaderResponse);
    iHeaderResponse.renderOnLoadJavascript("document.getElementById('" + component.getMarkupId() + "').focus();");
  }
}