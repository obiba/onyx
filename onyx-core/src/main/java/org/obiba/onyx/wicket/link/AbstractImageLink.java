/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.link;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * 
 */
public abstract class AbstractImageLink extends Panel {

  private static final long serialVersionUID = 1L;

  /**
   * 
   * @param id
   * @param labelModel
   */
  public AbstractImageLink(String id, IModel labelModel) {
    this(id, labelModel, null);
  }

  /**
   * @param id
   * @param labelModel
   * @param descriptionModel
   */
  public AbstractImageLink(String id, IModel labelModel, IModel descriptionModel) {
    super(id, labelModel);

    AbstractLink link = newLink("link");

    link.add(new Label("label", labelModel));
    if(descriptionModel != null) {
      link.add(new Label("description", descriptionModel));
    } else {
      link.add(new Label("description"));
    }
    add(link);
  }

  protected abstract AbstractLink newLink(String id);

  /**
   * Get the wrapped link.
   * @return
   */
  public AbstractLink getLink() {
    return (AbstractLink) get("link");
  }

  /**
   * Called on link clicked.
   * @param target
   */
  public abstract void onClick(AjaxRequestTarget target);

}
