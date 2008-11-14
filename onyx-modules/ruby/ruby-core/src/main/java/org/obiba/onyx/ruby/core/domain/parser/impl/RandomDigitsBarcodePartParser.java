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
 * Implement random barcode part validation
 */
public class RandomDigitsBarcodePartParser extends FixedSizeBarcodePartParser {
  /**
   * it's a regular expression that part need to match.
   */
  private String format;

  public void setFormat(String format) {
    this.format = format;
  }

  public String getFormat() {
    return format;
  }

  @Override
  protected MessageSourceResolvable validatePart(String part) {
    MessageSourceResolvable error = null;
    if(!part.matches(format)) {
      // The code must match the given pattern
      error = createBarcodeError("BarcodePartFormatError", "Invalid barcode part format.");
    }
    return error;
  }
}
