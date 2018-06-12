/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.domain.parser;

import java.util.List;

import org.obiba.onyx.ruby.core.domain.BarcodePart;
import org.springframework.context.MessageSourceResolvable;

/**
 * Interface for <code>BarcodePart</code> parsers.
 */
public interface IBarcodePartParser {

  /**
   * Returns the part's title, if it has one.
   * 
   * @return part title (<code>null</code> if none)
   */
  public MessageSourceResolvable getPartTitle();

  /**
   * Returns the barcode part's variable name, if it has been configured with one.
   * 
   * @return barcode part variable name (may be <code>null</code>)
   */
  public String getVariableName();

  /**
   * <p>
   * Consumes the appropriate (parser-dependent) number of characters in the given barcode fragment, and creates a
   * <code>BarcodePart</code> based on those characters.
   * </p>
   * 
   * <p>
   * The consumed characters are validated. Any errors found are added to the provided error list.
   * </p>
   * 
   * <p>
   * Note: Characters are consumed from left to right.
   * </p>
   * 
   * @param barcodeFragment a barcode fragment (for the first parser in a <code>BarcodeStructure</code> this will be a
   * complete barcode; for subsequent parsers, this will be some fragment of the barcode -- i.e., whatever has not been
   * consumed by earlier parsers)
   * @param errors error list
   * @return consumed barcode part (<code>null</code> in case of errors)
   */
  public BarcodePart eatAndValidatePart(StringBuilder barcodeFragment, List<MessageSourceResolvable> errors);

  /**
   * <p>
   * Consumes the appropriate (parser-dependent) number of characters in the given barcode fragment, and creates a
   * <code>BarcodePart</code> based on those characters.
   * </p>
   * 
   * <p>
   * This method does <em>not</em> validate the consumed characters. However, if the required <em>number</em> of
   * characters cannot be consumed, a value of <code>null</code> is returned.
   * </p>
   * 
   * @param barcodeFragment a barcode fragment (for the first parser in a <code>BarcodeStructure</code> this will be a
   * complete barcode; for subsequent parsers, this will be some fragment of the barcode -- i.e., whatever has not been
   * consumed by earlier parsers)
   * @return consumed barcode part (<code>null</code> in case of errors)
   */
  public BarcodePart eatPart(StringBuilder barcodeFragment);

  /**
   * @return consumed barcode part size
   */
  public int getSize();

  /**
   * Defines if whether or not the parser produces variables that should be marked as "keys".
   * 
   * @return true if key, false if not
   */
  public boolean isKey();

}
