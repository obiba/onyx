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
import org.apache.wicket.model.PropertyModel;
import org.obiba.onyx.ruby.core.domain.BarcodePart;
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.springframework.context.MessageSourceResolvable;

public class EditSamplePanel extends Panel {

  private static final long serialVersionUID = 1L;

  public EditSamplePanel(String id, IModel rowModel, TubeRegistrationConfiguration tubeRegistrationConfiguration) {
    super(id, rowModel);
    add(new Label("barcodeLabel", new SpringStringResourceModel("Ruby.Barcode")));
    add(new Label("barcode", new PropertyModel(rowModel, "barcode")));
    add(new Label("remarkLabel", new SpringStringResourceModel("Ruby.Remark")));
    add(new RemarkSelectorPanel("remark", rowModel, tubeRegistrationConfiguration));
    add(new Label("commentLabel", new SpringStringResourceModel("Ruby.Comment")));
    add(new CommentPanel("comment", rowModel));
    addBarcodeParts(rowModel, tubeRegistrationConfiguration);

  }

  private void addBarcodeParts(IModel rowModel, TubeRegistrationConfiguration tubeRegistrationConfiguration) {
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
