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
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.Model;

/**
 * 
 */
public class ButtonDisableBehavior extends AttributeAppender {

  private static final long serialVersionUID = -2793180600410649652L;

  public ButtonDisableBehavior() {
    super("class", new Model("ui-state-disabled"), " ");
  }

  /**
   * Overriden to enable the behaviour if the component is disabled. We want to append the attribute when the
   * component is disabled.
   */
  @Override
  public boolean isEnabled(Component component) {
    return component.isEnabled() == false;
  }
}
