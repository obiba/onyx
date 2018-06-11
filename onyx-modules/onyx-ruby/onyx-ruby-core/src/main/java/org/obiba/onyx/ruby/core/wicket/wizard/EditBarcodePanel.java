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
import org.obiba.onyx.wicket.reusable.Dialog.CloseButtonCallback;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.reusable.Dialog.WindowClosedCallback;

public class EditBarcodePanel extends Panel {

  private static final long serialVersionUID = 1L;

  Dialog editSampleDialog;

  EditSamplePanel editSamplePanel;

  public EditBarcodePanel(String id, IModel rowModel, TubeRegistrationConfiguration tubeRegistrationConfiguration) {
    super(id, rowModel);
    addEditLink();
    addEditDialog(tubeRegistrationConfiguration);
    addDialogCallbacks();
  }

  @SuppressWarnings("serial")
  private void addEditDialog(TubeRegistrationConfiguration tubeRegistrationConfiguration) {
    editSampleDialog = new Dialog("editSampleDialog");
    editSampleDialog.setTitle(new ResourceModel("Edit"));
    editSampleDialog.setOptions(Dialog.Option.OK_CANCEL_OPTION, "Save");
    editSampleDialog.setHeightUnit("em");
    editSampleDialog.setWidthUnit("em");
    editSampleDialog.setInitialHeight(36);
    editSampleDialog.setInitialWidth(34);

    editSamplePanel = new EditSamplePanel("content", getDefaultModel(), tubeRegistrationConfiguration);
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

  @SuppressWarnings("serial")
  private void addDialogCallbacks() {
    editSampleDialog.setWindowClosedCallback(new WindowClosedCallback() {

      public void onClose(AjaxRequestTarget target, Status status) {
        if(status.equals(Status.SUCCESS)) {
          target.addComponent(EditBarcodePanel.this.findParent(TubeRegistrationPanel.class));
        }
      }

    });

    editSampleDialog.setCloseButtonCallback(new CloseButtonCallback() {

      public boolean onCloseButtonClicked(AjaxRequestTarget target, Status status) {

        switch(status) {
        case SUCCESS:
          editSamplePanel.updateSample();
          break;
        case ERROR:
          editSamplePanel.displayFeedbackWindow(target);
          return false;

        }
        return true;

      }

    });

  }

}
