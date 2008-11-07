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
import java.util.Set;

import org.obiba.onyx.ruby.core.domain.BarcodePart;
import org.obiba.onyx.ruby.core.domain.parser.IBarcodePartParser;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.ObjectError;

/**
 * Implements barcode part parser with acceptable values
 */
public class AcceptableValuesBarcodePartParser implements IBarcodePartParser {

  private Set<String> acceptableValues;

  public BarcodePart eatAndValidatePart(StringBuilder barcodeFragment, List<MessageSourceResolvable> errors) {
    BarcodePart barcodePart = null;

    try {

      String part = barcodeFragment.substring(0, getSize());
      barcodeFragment.delete(0, getSize());

      MessageSourceResolvable error = validatePart(part);

      if(error != null) {
        errors.add(error);
      } else {
        barcodePart = new BarcodePart(part);
      }

    } catch(Exception e) {
      errors.add(createBarcodeError("BarcodePartSizeError", "Invalid barcode part size."));
    }
    return barcodePart;
  }

  public MessageSourceResolvable getPartTitle() {
    return null;
  }

  public void setAcceptableValues(Set<String> acceptableValues) {
    this.acceptableValues = acceptableValues;
  }

  public Set<String> getAcceptableValues() {
    return acceptableValues;
  }

  public int getSize() {

    if(acceptableValues == null || acceptableValues.size() == 0) {
      return 0;
    } else {
      return acceptableValues.iterator().next().length();
    }
  }

  /**
   * @param code
   * @param defaultMsg
   */
  private MessageSourceResolvable createBarcodeError(String code, String defaultMsg) {
    MessageSourceResolvable error = new ObjectError("Barcode", new String[] { code }, null, defaultMsg);
    return error;
  }

  private MessageSourceResolvable validatePart(String part) {
    MessageSourceResolvable error = null;
    if(acceptableValues == null) {
      error = createBarcodeError("NoAcceptableValueError", "No acceptable barcode part values found.");
    } else if(!acceptableValues.contains(part)) {
      error = createBarcodeError("BarcodePartValueError", "Invalid barcode part value.");
    }
    return error;
  }

}
