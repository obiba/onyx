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
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.DialogBuilder;
import org.obiba.onyx.wicket.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.wicket.reusable.Dialog.Status;

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
    EditSamplePanel editSamplePanel = new EditSamplePanel("content", getModel(), tubeRegistrationConfiguration);
    editSampleDialog = DialogBuilder.buildDialog("editSampleDialog", new ResourceModel("Edit"), editSamplePanel).getDialog();
    editSampleDialog.setOptions(Dialog.Option.OK_CANCEL_OPTION, "Save");

    editSampleDialog.setCloseButtonCallback(new CloseButtonCallback() {

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {

        switch(status) {
        case SUCCESS:
          target.addComponent(EditBarcodePanel.this.findParent(OnyxEntityList.class));
          break;
        }
        return true;

      }

    });

    add(editSampleDialog);
  }

  @SuppressWarnings("serial")
  private void addEditLink() {
    AjaxLink editLink = new AjaxLink("link") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        editSampleDialog.show(target);
      }

    };
    editLink.add(new Label("edit", new SpringStringResourceModel("Ruby.Edit")));
    add(editLink);
  }

}
