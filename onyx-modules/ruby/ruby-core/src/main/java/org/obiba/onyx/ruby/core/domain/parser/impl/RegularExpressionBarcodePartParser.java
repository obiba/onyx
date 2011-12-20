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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obiba.onyx.ruby.core.domain.BarcodePart;
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
  protected BarcodePart eatPart(StringBuilder barcodeFragment, List<MessageSourceResolvable> errors, boolean validate) {
    Pattern p = Pattern.compile(expression);
    Matcher m = p.matcher(barcodeFragment);

    BarcodePart part = null;
    if(m.matches()) {
      part = new BarcodePart(barcodeFragment.substring(0, m.end()));
      barcodeFragment.delete(0, m.end());
    } else if(validate) {
      errors.add(createBarcodeError(REGULAR_EXPRESSION_MISMATCH_BARCODE_ERROR, new Object[] { barcodeFragment, expression }, "Invalid barcode part format."));
    }
    return part;
  }

  @Override
  protected MessageSourceResolvable validatePart(String part) {
    // We use eatPart instead to allow barcodes of varying-size
    return null;
  }

  public boolean isKey() {
    return false;
  }
}
