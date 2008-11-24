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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.MessageSourceResolvable;

/**
 * Implementation for barcode part parser with acceptable values
 */
public class AcceptableValuesBarcodePartParser extends DefaultBarcodePartParser {
  //
  // Constants
  //

  private static final String UNKNOWN_TUBETYPE_SEQUENCENUMBER_BARCODE_ERROR = "Ruby.Error.UnknownTubeTypeSequenceNumber";

  //
  // Instance Variables
  //

  private Set<String> acceptableValues;

  //
  // Constructors
  //

  /**
   * The constructor that initialises acceptableValues
   */
  public AcceptableValuesBarcodePartParser() {
    super();
    this.acceptableValues = new HashSet<String>();
  }

  //
  // DefaultBarcodePartParser Methods
  //

  public int getSize() {
    if(acceptableValues.isEmpty()) {
      return 0;
    } else {
      return acceptableValues.iterator().next().length();
    }
  }

  @Override
  protected MessageSourceResolvable validatePart(String part) {
    MessageSourceResolvable error = null;
    if(!acceptableValues.contains(part)) {
      // The part must be one of the acceptable values
      error = createBarcodeError(UNKNOWN_TUBETYPE_SEQUENCENUMBER_BARCODE_ERROR, new Object[] { part }, UNKNOWN_TUBETYPE_SEQUENCENUMBER_BARCODE_ERROR);
    }
    return error;
  }

  //
  // Methods
  //

  /**
   * Validates and sets the acceptable values' copy
   * 
   * @param acceptableValues
   */
  public void setAcceptableValues(Set<String> acceptableValues) {
    if(acceptableValues != null && !acceptableValues.isEmpty()) {

      int valueLength = acceptableValues.iterator().next().length();
      Set<String> values = new HashSet<String>();

      for(String value : acceptableValues) {
        if(value.length() != valueLength) {
          throw new IllegalArgumentException("The acceptable values have different size.");
        }
        values.add(value);
      }

      this.acceptableValues = values;
    }
  }

  public Set<String> getAcceptableValues() {
    return Collections.unmodifiableSet(acceptableValues);
  }
}
