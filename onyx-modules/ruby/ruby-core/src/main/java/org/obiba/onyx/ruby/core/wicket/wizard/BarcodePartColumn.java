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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.ruby.core.domain.BarcodePart;
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.springframework.context.MessageSourceResolvable;

public class BarcodePartColumn extends AbstractColumn {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean
  private TubeRegistrationConfiguration tubeRegistrationConfiguration;

  //
  // Constructors
  //

  public BarcodePartColumn(IModel displayModel) {
    super(displayModel);
    InjectorHolder.getInjector().inject(this);
  }

  //
  // AbstractColumn Methods
  //

  public void populateItem(Item cellItem, String componentId, IModel rowModel) {
    BarcodeStructure barcodeStructure = tubeRegistrationConfiguration.getBarcodeStructure();

    RegisteredParticipantTube registeredParticipantTube = (RegisteredParticipantTube) rowModel.getObject();
    String barcode = registeredParticipantTube.getBarcode();
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();

    List<BarcodePart> barcodePartList = barcodeStructure.parseBarcode(barcode, errors);
    BarcodePart barcodePart = barcodePartList.get(cellItem.getIndex() - 1);
    MessageSourceResolvable barcodePartLabel = barcodePart.getPartLabel();

    cellItem.add(new Label(componentId, new SpringStringResourceModel(barcodePartLabel.getCodes()[0], barcodePartLabel.getArguments(), barcodePartLabel.getDefaultMessage())));
  }
}
