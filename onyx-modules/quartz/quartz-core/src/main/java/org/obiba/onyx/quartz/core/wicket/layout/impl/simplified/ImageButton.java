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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * 
 */
public abstract class ImageButton extends Panel {

  private static final long serialVersionUID = 1L;

  public ImageButton(String id, IModel labelModel) {
    this(id, labelModel, null);
  }

  /**
   * @param id
   * @param labelModel
   * @param descriptionModel
   */
  public ImageButton(String id, IModel labelModel, IModel descriptionModel) {
    super(id, labelModel);

    AjaxLink link = new AjaxLink("link") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        ImageButton.this.onClick(target);
      }

    };

    link.add(new Label("label", labelModel));
    if(descriptionModel != null) {
      link.add(new Label("description", descriptionModel));
    } else {
      link.add(new Label("description"));
    }
    link.add(new QuestionCategorySelectionBehavior());
    add(link);
  }

  /**
   * @param target
   */
  public abstract void onClick(AjaxRequestTarget target);

}
