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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog.OnYesCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteBarcodePanel extends Panel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DeleteBarcodePanel.class);

  //
  // Instance Variables
  //

  @SpringBean(name = "activeTubeRegistrationService")
  private ActiveTubeRegistrationService activeTubeRegistrationService;

  //
  // Constructors
  //

  public DeleteBarcodePanel(String id, IModel registeredParticipantTubeModel) {
    super(id, registeredParticipantTubeModel);

    addDeleteLink();
  }

  //
  // Methods
  //

  private void addDeleteLink() {
    RegisteredParticipantTube registeredParticipantTube = (RegisteredParticipantTube) DeleteBarcodePanel.this.getModelObject();
    final String barcode = registeredParticipantTube.getBarcode();

    final ConfirmationDialog confirmationDialog = new ConfirmationDialog("confirmation-dialog");
    add(confirmationDialog);

    AjaxLink deleteLink = new AjaxLink("link") {
      private static final long serialVersionUID = 1L;

      public void onClick(AjaxRequestTarget target) {

        Label label = new Label("content", new SpringStringResourceModel("Ruby.Confirm.BarcodeDeletion", new Object[] { barcode }, null));
        label.add(new AttributeModifier("class", true, new Model("confirmation-dialog-content")));

        confirmationDialog.setContent(label);
        confirmationDialog.setTitle(new SpringStringResourceModel("Ruby.TubeDeletion"));
        confirmationDialog.setYesButtonCallback(new OnYesCallback() {

          private static final long serialVersionUID = -6691702933562884991L;

          public void onYesButtonClicked(AjaxRequestTarget target) {
            activeTubeRegistrationService.unregisterTube(barcode);
            target.addComponent(DeleteBarcodePanel.this.findParent(TubeRegistrationPanel.class));
          }

        });
        confirmationDialog.show(target);
      }
    };

    deleteLink.add(new Label("delete", new SpringStringResourceModel("Ruby.Delete")));
    add(deleteLink);
  }
}