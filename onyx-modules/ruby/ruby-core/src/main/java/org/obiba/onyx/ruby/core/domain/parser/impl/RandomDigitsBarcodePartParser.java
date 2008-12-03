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

import org.obiba.onyx.ruby.core.domain.parser.IBarcodePartParser;
import org.springframework.context.MessageSourceResolvable;

/**
 * A {@link IBarcodePartParser} that validates that the part is a series of digits.
 */
public class RandomDigitsBarcodePartParser extends FixedSizeBarcodePartParser {

  private static final String DIGIT_MISMATCH_BARCODE_ERROR = "Ruby.Error.DigitMismatch";

  @Override
  protected MessageSourceResolvable validatePart(String part) {
    for(int i = 0; i < part.length(); i++) {
      char c = part.charAt(i);
      if(Character.isDigit(c) == false) {
        return createBarcodeError(DIGIT_MISMATCH_BARCODE_ERROR, new Object[] { part, getSize(), c, new Integer(i + 1) }, "Invalid barcode part format.");
      }
    }
    return null;
  }
}
