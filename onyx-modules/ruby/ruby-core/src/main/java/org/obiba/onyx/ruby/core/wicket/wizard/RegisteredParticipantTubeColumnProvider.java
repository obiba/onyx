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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.domain.parser.IBarcodePartParser;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;

class RegisteredParticipantTubeColumnProvider implements IColumnProvider, Serializable {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(RegisteredParticipantTubeColumnProvider.class);

  //
  // Instance Variables
  //

  private List<IColumn> columns = new ArrayList<IColumn>();

  private List<IColumn> additional = new ArrayList<IColumn>();

  private int firstBarcodePartColumnIndex;

  //
  // Constructors
  //

  @SuppressWarnings("serial")
  public RegisteredParticipantTubeColumnProvider(TubeRegistrationConfiguration tubeRegistrationConfiguration) {
    addDeleteColumn();
    addBarcodeColumn();
    addBarcodePartColumns(tubeRegistrationConfiguration);
  }

  //
  // IColumnProvider Methods
  //

  public List<IColumn> getAdditionalColumns() {
    return additional;
  }

  public List<String> getColumnHeaderNames() {
    return null;
  }

  public List<IColumn> getDefaultColumns() {
    return columns;
  }

  public List<IColumn> getRequiredColumns() {
    return columns;
  }

  //
  // Methods
  //

  private void addDeleteColumn() {
    columns.add(new AbstractColumn(new Model("")) {
      private static final long serialVersionUID = 1L;

      public void populateItem(Item cellItem, String componentId, IModel rowModel) {
        cellItem.add(new DeleteBarcodePanel(componentId, rowModel));
      }
    });

    firstBarcodePartColumnIndex++;
  }

  private void addBarcodeColumn() {
    columns.add(new AbstractColumn(new SpringStringResourceModel("Ruby.Barcode")) {
      private static final long serialVersionUID = 1L;

      public void populateItem(Item cellItem, String componentId, IModel rowModel) {
        cellItem.add(new Label(componentId, new PropertyModel(rowModel, "barcode")));
      }
    });

    firstBarcodePartColumnIndex++;
  }

  private void addBarcodePartColumns(TubeRegistrationConfiguration tubeRegistrationConfiguration) {
    BarcodeStructure barcodeStructure = tubeRegistrationConfiguration.getBarcodeStructure();

    List<IBarcodePartParser> parserList = barcodeStructure.getParsers();

    for(final IBarcodePartParser parser : parserList) {
      MessageSourceResolvable partTitle = parser.getPartTitle();

      if(partTitle != null) {
        BarcodePartColumn partColumn = new BarcodePartColumn(new SpringStringResourceModel(parser.getPartTitle().getCodes()[0]), firstBarcodePartColumnIndex);
        columns.add(partColumn);
      }
    }
  }

  public int getFirstBarcodePartColumnIndex() {
    return firstBarcodePartColumnIndex;
  }
}