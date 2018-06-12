/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.tube;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.ruby.core.domain.BarcodePart;
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.springframework.context.MessageSourceResolvable;

@SuppressWarnings("serial")
public class BarcodePartsPanel extends Panel {

  public BarcodePartsPanel(String id, IModel rowModel, TubeRegistrationConfiguration tubeRegistrationConfiguration) {
    super(id, rowModel);
    BarcodeStructure barcodeStructure = tubeRegistrationConfiguration.getBarcodeStructure();

    RegisteredParticipantTube registeredParticipantTube = (RegisteredParticipantTube) rowModel.getObject();
    String barcode = registeredParticipantTube.getBarcode();
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();

    List<BarcodePart> barcodePartList = barcodeStructure.parseBarcode(barcode, errors);

    RepeatingView barcodeParts = new RepeatingView("barcodeParts");
    WebMarkupContainer container;

    for(BarcodePart barcodePart : barcodePartList) {
      if(barcodePart.getPartTitle() != null) {
        container = new WebMarkupContainer(barcodeParts.newChildId());
        container.add(new Label("barcodePart", new MessageSourceResolvableStringModel(barcodePart.getPartLabel())));
        container.add(new Label("barcodePartLabel", new SpringStringResourceModel(barcodePart.getPartTitle().getCodes()[0])));
        barcodeParts.add(container);
      }
    }

    add(barcodeParts);
  }

}
