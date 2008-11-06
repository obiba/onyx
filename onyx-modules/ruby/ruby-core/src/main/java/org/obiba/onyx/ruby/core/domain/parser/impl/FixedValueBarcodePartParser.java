/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.domain.parser.impl;

import java.util.List;

import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.ruby.core.domain.BarcodePart;
import org.springframework.context.MessageSourceResolvable;

/**
 * Implement barcode part validation with fixed value
 */
public class FixedValueBarcodePartParser extends FixedSizeBarcodePartParser {

  private String fixedValue;

  public BarcodePart eatAndValidatePart(StringBuilder barcodeFragment, ActiveInterviewService activeInterviewService, List<MessageSourceResolvable> errors) {
    try {
      String part = barcodeFragment.substring(0, size);
      List<MessageSourceResolvable> error = validatePart(part);
      BarcodePart barcodePart = null;
      if(error != null) {
        errors.addAll(error);
      } else if(!part.equals(fixedValue)) {
        setBarcodeError(errors, "barcodePartValueError", "Invalid Barcode Part Value.");
      } else {
        barcodePart = new BarcodePart(part);
      }
      return barcodePart;
    } catch(Exception e) {
      setBarcodeError(errors, "barcodePartLengthError", "Invalid Barcode Part Length.");
      return null;
    }
  }

  public void setFixedValue(String fixedValue) {
    this.fixedValue = fixedValue;
  }
}
