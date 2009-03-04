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

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
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
  public AbstractImageLink(String id, IModel labelModel, IModel descriptionModel, ResourceReference imageDecorator) {
    super(id, labelModel);

    AbstractLink link = newLink("link");

    link.add(new Label("label", labelModel).setEscapeModelStrings(false));
    if(descriptionModel != null) {
      link.add(new Label("description", descriptionModel).setEscapeModelStrings(false));
    } else {
      link.add(new Label("description"));
    }

    addDecorator(imageDecorator, link);

    add(link);

  }

  /**
   * Add image decorator to image link.
   * 
   * @param imageDecorator Image to add as a decorator.
   * @param link
   */
  private void addDecorator(ResourceReference imageDecorator, AbstractLink link) {
    Image image;
    if(imageDecorator != null) {
      image = new Image("decorator", imageDecorator);
      link.add(image);
    } else {
      image = new Image("decorator");
      image.setVisible(false);
    }
    link.add(image);
  }

  /**
   * @param id
   * @param labelModel
   * @param descriptionModel
   * @param imageDecorator
   */
  public AbstractImageLink(String id, IModel labelModel, IModel descriptionModel) {
    this(id, labelModel, descriptionModel, null);
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
   * Get the label inside the link.
   * @return
   */
  public Label getLabel() {
    return (Label) getLink().get("label");
  }

  /**
   * Get the description inside the link.
   * @return
   */
  public Label getDescription() {
    return (Label) getLink().get("description");
  }

  /**
   * Called on link clicked.
   * @param target
   */
  public abstract void onClick(AjaxRequestTarget target);

}
