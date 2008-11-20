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
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.wicket.JavascriptEventConfirmation;
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

  @SpringBean
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

    Link deleteLink = new Link("link") {
      private static final long serialVersionUID = 1L;

      public void onClick() {
        activeTubeRegistrationService.unregisterTube(barcode);
      }
    };

    deleteLink.add(new Label("linkLabel", new StringResourceModel("Delete", this, null)));

    deleteLink.add(new JavascriptEventConfirmation("onclick", new SpringStringResourceModel("Ruby.Confirm.BarcodeDeletion", new Object[] { barcode }, null)));

    add(deleteLink);
  }
}