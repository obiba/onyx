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

import org.obiba.onyx.ruby.core.domain.BarcodePart;
import org.obiba.onyx.ruby.core.domain.parser.IBarcodePartParser;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

/**
 * Common barcode part Parser for parser implementations
 */
public abstract class DefaultBarcodePartParser implements IBarcodePartParser {
  //
  // Instance Variables
  //

  private MessageSourceResolvable partTitle;

  private String variableName;

  //
  // IBarcodePartParser Methods
  //

  public MessageSourceResolvable getPartTitle() {
    return partTitle;
  }

  public String getVariableName() {
    return variableName;
  }

  public BarcodePart eatAndValidatePart(StringBuilder barcodeFragment, List<MessageSourceResolvable> errors) {
    return eatPart(barcodeFragment, errors, true);
  }

  public BarcodePart eatPart(StringBuilder barcodeFragment) {
    return eatPart(barcodeFragment, null, false);
  }

  //
  // Methods
  //

  public void setPartTitle(MessageSourceResolvable partTitle) {
    this.partTitle = partTitle;
  }

  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  protected BarcodePart eatPart(StringBuilder barcodeFragment, List<MessageSourceResolvable> errors, boolean validate) {
    BarcodePart barcodePart = null;

    if(barcodeFragment != null && barcodeFragment.length() >= getSize()) {

      String part = barcodeFragment.substring(0, getSize());
      barcodeFragment.delete(0, getSize());

      MessageSourceResolvable error = null;
      if(validate) {
        error = validatePart(part);
      }

      if(error != null) {
        errors.add(error);
      } else {
        barcodePart = new BarcodePart(part);
        barcodePart.setPartTitle(partTitle);
      }

    } else {
      errors.add(createBarcodeError("BarcodePartSizeError", "Invalid barcode part size."));
    }
    return barcodePart;
  }

  /**
   * Barcode part validation based on different parser implementations
   * 
   * @param part
   * @return error message (<code>null</code> in case of no errors)
   */
  protected abstract MessageSourceResolvable validatePart(String part);

  /**
   * Creates error message
   * 
   * @param code
   * @param defaultMsg
   * @return
   */
  protected MessageSourceResolvable createBarcodeError(String code, String defaultMsg) {
    return createBarcodeError(code, null, defaultMsg);
  }

  protected MessageSourceResolvable createBarcodeError(String code, Object[] arguments, String defaultMsg) {
    MessageSourceResolvable error = new DefaultMessageSourceResolvable(new String[] { code }, arguments, defaultMsg);
    return error;
  }
}
