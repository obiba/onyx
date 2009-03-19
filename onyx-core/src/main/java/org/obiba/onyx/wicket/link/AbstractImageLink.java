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

import org.apache.wicket.AttributeModifier;
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

  private AbstractLink link;

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

    initialize(labelModel, descriptionModel);
    addDecorator(imageDecorator);
  }

  /**
   * 
   * @param id
   * @param labelModel
   * @param descriptionModel
   * @param imageDecoratorModel image src attribute value
   */
  public AbstractImageLink(String id, IModel labelModel, IModel descriptionModel, IModel imageDecoratorModel) {
    super(id, labelModel);

    initialize(labelModel, descriptionModel);
    addDecorator(imageDecoratorModel);
  }

  /**
   * @param id
   * @param labelModel
   * @param descriptionModel
   * @param imageDecorator
   */
  public AbstractImageLink(String id, IModel labelModel, IModel descriptionModel) {
    super(id, labelModel);

    initialize(labelModel, descriptionModel);
    addNoDecorator();
  }

  private void initialize(IModel labelModel, IModel descriptionModel) {
    link = newLink("link");

    link.add(new Label("label", labelModel).setEscapeModelStrings(false));
    if(descriptionModel != null) {
      link.add(new Label("description", descriptionModel).setEscapeModelStrings(false));
    } else {
      link.add(new Label("description"));
    }
    add(link);
  }

  /**
   * Add image decorator to image link.
   * 
   * @param imageDecorator image resource
   */
  private void addDecorator(ResourceReference imageDecorator) {
    if(imageDecorator != null) {
      Image image = new Image("decorator", imageDecorator);
      link.add(image);
    } else {
      addNoDecorator();
    }
  }

  /**
   * Add image decorator to image link.
   * 
   * @param imageDecoratorModel image src attribute value
   */
  private void addDecorator(IModel imageDecoratorModel) {
    if(imageDecoratorModel != null) {
      Image image = new Image("decorator");
      image.add(new AttributeModifier("src", imageDecoratorModel));
      link.add(image);
    } else {
      addNoDecorator();
    }
  }

  /**
   * Hide image tag.
   */
  private void addNoDecorator() {
    Image image = new Image("decorator");
    image.setVisible(false);
    link.add(image);
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
