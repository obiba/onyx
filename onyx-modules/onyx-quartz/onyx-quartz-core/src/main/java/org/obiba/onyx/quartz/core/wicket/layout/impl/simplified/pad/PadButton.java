/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.pad;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.NoDragBehavior;
import org.obiba.onyx.wicket.link.AjaxImageLink;

/**
 * 
 */
public class PadButton extends Panel {

  private static final long serialVersionUID = 1L;

  /**
   * @param id
   */
  @SuppressWarnings("serial")
  public PadButton(String id, IModel model) {
    super(id, model);
    AjaxImageLink link = new AjaxImageLink("button", model) {

      @Override
      public void onClick(AjaxRequestTarget target) {
        IPadSelectionListener listener = PadButton.this.findParent(IPadSelectionListener.class);
        if(listener != null) {
          listener.onPadSelection(target, PadButton.this.getDefaultModel());
        }
      }

    };
    link.getLink().add(new NoDragBehavior());
    add(link);

  }

  /**
   * Enable or disable the inner component representing the button.
   * @param enabled
   * @return this for chaining
   */
  public PadButton setButtonEnabled(boolean enabled) {
    get("button").setEnabled(enabled);
    return this;
  }

}
