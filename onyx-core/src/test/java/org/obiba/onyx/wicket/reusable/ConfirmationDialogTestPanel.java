/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.reusable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * A panel to hold the {@link ConfirmationDialog} under test.
 */
public class ConfirmationDialogTestPanel extends Panel {

  private static final long serialVersionUID = 1L;

  final ConfirmationDialog confirmationDialog;

  public ConfirmationDialogTestPanel(String id) {
    super(id);

    confirmationDialog = new ConfirmationDialog("confirmationDialog");

    add(confirmationDialog);
    add(new AjaxLink("openConfirmationDialogLink") {
      private static final long serialVersionUID = 1L;

      public void onClick(AjaxRequestTarget target) {
        confirmationDialog.show(target);
      }
    });
  }

  ConfirmationDialog getConfirmationDialog() {
    return confirmationDialog;
  }

}
