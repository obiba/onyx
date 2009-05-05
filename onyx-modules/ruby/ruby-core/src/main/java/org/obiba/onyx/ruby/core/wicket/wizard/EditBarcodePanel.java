/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.wizard;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;

public class EditBarcodePanel extends Panel {

  private static final long serialVersionUID = 1L;

  public EditBarcodePanel(String id, IModel model) {
    super(id, model);
    addEditLink();
  }

  @SuppressWarnings("serial")
  private void addEditLink() {
    Link editLink = new Link("link") {

      @Override
      public void onClick() {
        System.out.println("Edit not implemented!");
      }

    };
    editLink.add(new Label("edit", new SpringStringResourceModel("Ruby.Edit")));
    add(editLink);
  }

}
