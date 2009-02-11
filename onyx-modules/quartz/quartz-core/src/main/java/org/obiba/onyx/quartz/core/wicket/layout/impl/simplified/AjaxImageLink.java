/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;

/**
 * 
 */
public abstract class AjaxImageLink extends AbstractImageLink {

  private static final long serialVersionUID = 1L;

  /**
   * 
   * @param id
   * @param labelModel
   */
  public AjaxImageLink(String id, IModel labelModel) {
    super(id, labelModel, null);
  }

  /**
   * @param id
   * @param labelModel
   * @param descriptionModel
   */
  public AjaxImageLink(String id, IModel labelModel, IModel descriptionModel) {
    super(id, labelModel, descriptionModel);
  }

  @Override
  protected AbstractLink newLink(String id) {
    return new AjaxLink(id) {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        AjaxImageLink.this.onClick(target);
      }

    };

  }

  /**
   * @param target
   */
  public abstract void onClick(AjaxRequestTarget target);

}
