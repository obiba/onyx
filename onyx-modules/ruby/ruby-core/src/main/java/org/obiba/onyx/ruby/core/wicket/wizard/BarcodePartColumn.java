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
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;

public class BarcodePartColumn extends AbstractColumn {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(BarcodePartColumn.class);

  //
  // Instance Variables
  //

  @SpringBean
  private TubeRegistrationConfiguration tubeRegistrationConfiguration;

  private int firstBarcodePartColumnIndex;

  //
  // Constructors
  //

  public BarcodePartColumn(IModel displayModel, int firstBarcodePartColumnIndex) {
    super(displayModel);

    this.firstBarcodePartColumnIndex = firstBarcodePartColumnIndex;

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

    // From barcodePartList, extract the list of DISPLAYED barcode parts
    // (i.e., those with a title). Only these barcode parts have corresponding
    // columns.
    List<BarcodePart> displayedBarcodePartList = new ArrayList<BarcodePart>();

    for(BarcodePart barcodePart : barcodePartList) {
      if(barcodePart.getPartTitle() != null) {
        displayedBarcodePartList.add(barcodePart);
      }
    }

    // From displayedBarcodePartList, get the barcode part to be rendered the current
    // cell. Need to subtract firstBarcodePartColumnIndex from the cell item index, because
    // there are additional columns in the table (the Delete column and the Barcode column).
    int barcodePartIndex = cellItem.getIndex() - firstBarcodePartColumnIndex;
    BarcodePart barcodePart = displayedBarcodePartList.get(barcodePartIndex);

    MessageSourceResolvable barcodePartLabel = barcodePart.getPartLabel();
    cellItem.add(new Label(componentId, new MessageSourceResolvableStringModel(barcodePartLabel)));
  }
}
