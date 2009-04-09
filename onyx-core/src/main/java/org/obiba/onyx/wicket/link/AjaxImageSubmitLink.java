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
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;

/**
 * 
 */
public abstract class AjaxImageSubmitLink extends AbstractImageLink {

  private static final long serialVersionUID = 1L;

  /**
   * 
   * @param id
   * @param labelModel
   */
  public AjaxImageSubmitLink(String id, IModel labelModel) {
    super(id, labelModel, null);
  }

  /**
   * @param id
   * @param labelModel
   * @param descriptionModel
   */
  public AjaxImageSubmitLink(String id, IModel labelModel, IModel descriptionModel) {
    super(id, labelModel, descriptionModel);
  }

  /**
   * @param id
   * @param labelModel
   * @param descriptionModel
   * @param imageDecorator
   */
  public AjaxImageSubmitLink(String id, IModel labelModel, IModel descriptionModel, ResourceReference imageDecorator) {
    super(id, labelModel, descriptionModel, imageDecorator);
  }

  /**
   * 
   * @param id
   * @param labelModel
   * @param descriptionModel
   * @param imageDecoratorModel image src attribute value
   * @param contextRelativeImage indicates whether the image path in <code>imageDecoratorModel</code> is relative to
   * the context root
   */
  public AjaxImageSubmitLink(String id, IModel labelModel, IModel descriptionModel, IModel imageDecoratorModel, boolean contextRelativeImage) {
    super(id, labelModel, descriptionModel, imageDecoratorModel, contextRelativeImage);
  }

  public AjaxImageSubmitLink(String id, IModel labelModel, IModel descriptionModel, IModel imageDecoratorModel) {
    this(id, labelModel, descriptionModel, imageDecoratorModel, false);
  }

  @Override
  protected AbstractLink newLink(String id) {
    return new AjaxSubmitLink(id) {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        AjaxImageSubmitLink.this.onSubmit(target, form);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        AjaxImageSubmitLink.this.onError(target, form);
      }

    };

  }

  /**
   * Called after form submition and validation occured.
   * @param target
   */
  public abstract void onSubmit(AjaxRequestTarget target, Form form);

  /**
   * Called when an error occures.
   * @param target
   * @param form
   */
  public abstract void onError(AjaxRequestTarget target, Form form);

}
