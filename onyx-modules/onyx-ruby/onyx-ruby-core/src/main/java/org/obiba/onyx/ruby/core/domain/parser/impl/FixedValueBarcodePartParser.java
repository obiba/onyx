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

import org.springframework.context.MessageSourceResolvable;

/**
 * Implementation for barcode part parser with acceptable values
 */
public class FixedValueBarcodePartParser extends DefaultBarcodePartParser {
  //
  // Constants
  //

  private static final String FIXED_VALUE_BARCODE_MISMATCH = "Ruby.Error.FixedValueMismatch";

  //
  // Instance Variables
  //

  private String fixedValue;

  //
  // Constructors
  //

  /**
   * The constructor that initialises acceptableValues
   */
  public FixedValueBarcodePartParser() {
    super();
  }

  //
  // DefaultBarcodePartParser Methods
  //

  public int getSize() {
    return fixedValue.length();
  }

  @Override
  protected MessageSourceResolvable validatePart(String part) {
    MessageSourceResolvable error = null;
    if(fixedValue.equals(part) == false) {
      // The part must be one of the acceptable values
      error = createBarcodeError(FIXED_VALUE_BARCODE_MISMATCH, new Object[] { part, fixedValue }, FIXED_VALUE_BARCODE_MISMATCH);
    }
    return error;
  }

  //
  // Methods
  //

  public void setFixedValue(String fixedValue) {
    this.fixedValue = fixedValue;
  }

  public String getFixedValue() {
    return fixedValue;
  }

  public boolean isKey() {
    return false;
  }
}
