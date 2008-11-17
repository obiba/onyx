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

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.springframework.context.MessageSourceResolvable;

public class BarcodePartColumn extends AbstractColumn {
  //
  // Instance Variables
  //

  private MessageSourceResolvable title;

  //
  // Constructors
  //

  public BarcodePartColumn(IModel displayModel) {
    super(displayModel);
  }

  //
  // AbstractColumn Methods
  //

  public void populateItem(Item cellItem, String componentId, IModel rowModel) {
    // TODO Auto-generated method stub

  }

  //
  // Methods
  //

}
