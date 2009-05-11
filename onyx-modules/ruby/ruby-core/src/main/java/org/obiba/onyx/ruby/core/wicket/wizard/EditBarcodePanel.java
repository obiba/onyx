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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.wicket.tube.EditSamplePanel;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.reusable.Dialog;

public class EditBarcodePanel extends Panel {

  private static final long serialVersionUID = 1L;

  Dialog editSampleDialog;

  public EditBarcodePanel(String id, IModel rowModel, TubeRegistrationConfiguration tubeRegistrationConfiguration) {
    super(id, rowModel);
    addEditLink();
    addEditDialog(tubeRegistrationConfiguration);
  }

  @SuppressWarnings("serial")
  private void addEditDialog(TubeRegistrationConfiguration tubeRegistrationConfiguration) {
    editSampleDialog = new Dialog("editSampleDialog");
    editSampleDialog.setTitle(new ResourceModel("Edit"));
    editSampleDialog.setOptions(Dialog.Option.OK_CANCEL_OPTION, "Save");
    editSampleDialog.setInitialHeight(400);
    editSampleDialog.setInitialWidth(400);

    EditSamplePanel editSamplePanel = new EditSamplePanel(editSampleDialog, "content", getModel(), tubeRegistrationConfiguration);
    editSampleDialog.setContent(editSamplePanel);

    add(editSampleDialog);
  }

  @SuppressWarnings("serial")
  private void addEditLink() {
    AjaxLink editLink = new AjaxLink("link") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        editSampleDialog.show(target);
        target.appendJavascript("styleSelectedTubeRemark();");
      }

    };
    editLink.add(new Label("edit", new SpringStringResourceModel("Ruby.Edit")));
    add(editLink);
  }

}
