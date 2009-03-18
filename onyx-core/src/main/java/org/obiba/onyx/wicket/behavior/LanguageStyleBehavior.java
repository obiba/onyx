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
import org.apache.wicket.markup.ComponentTag;

/**
 * Set a dynamic css class representing the language of the component, for per locale styling.
 */
public class LanguageStyleBehavior extends AbstractBehavior {

  private static final long serialVersionUID = 2990673242056411361L;

  @Override
  public void onComponentTag(Component component, ComponentTag tag) {
    String cssClass = "obiba-lang-" + component.getLocale();

    if(tag.getAttributes().containsKey("class")) {
      cssClass += " " + tag.getAttributes().getString("class");
    }
    tag.getAttributes().put("class", cssClass);
  }

}
