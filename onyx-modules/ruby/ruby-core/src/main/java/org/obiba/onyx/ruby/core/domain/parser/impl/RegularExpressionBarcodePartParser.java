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
 * A {@link IBarcodePartParser} that validates that the part matches a given regular expression.
 */
public class RegularExpressionBarcodePartParser extends FixedSizeBarcodePartParser {
  //
  // Constants
  //

  protected static final String REGULAR_EXPRESSION_MISMATCH_BARCODE_ERROR = "Ruby.Error.RegularExpressionMismatch";

  /**
   * the regular expression used to validate the part
   */
  private String expression;

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

  @Override
  protected MessageSourceResolvable validatePart(String part) {
    MessageSourceResolvable error = null;
    if(!part.matches(expression)) {
      // The code must match the given pattern
      error = createBarcodeError(REGULAR_EXPRESSION_MISMATCH_BARCODE_ERROR, new Object[] { part, expression }, "Invalid barcode part format.");
    }
    return error;
  }

  public boolean isKey() {
    return false;
  }
}
