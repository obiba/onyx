/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.obiba.onyx.ruby.core.domain.parser.IBarcodePartParser;
import org.springframework.context.MessageSourceResolvable;

public class BarcodeStructure {
  //
  // Instance Variables
  //

  private List<IBarcodePartParser> parserList;

  //
  // Constructors
  //

  public BarcodeStructure() {
    parserList = new ArrayList<IBarcodePartParser>();
  }

  //
  // Methods
  //

  public void setParsers(List<IBarcodePartParser> parserList) {
    this.parserList.clear();

    if(parserList != null) {
      this.parserList.addAll(parserList);
    }
  }

  public List<IBarcodePartParser> getParsers() {
    return Collections.unmodifiableList(parserList);
  }

  public List<BarcodePart> parseBarcode(String barcode, List<MessageSourceResolvable> errors) {
    List<BarcodePart> barcodePartList = new ArrayList<BarcodePart>();

    StringBuilder barcodeFragment = new StringBuilder(barcode);

    for(IBarcodePartParser parser : parserList) {
      barcodePartList.add(parser.eatAndValidatePart(barcodeFragment, errors));

      if(!errors.isEmpty()) {
        break;
      }
    }

    return barcodePartList;
  }
}
