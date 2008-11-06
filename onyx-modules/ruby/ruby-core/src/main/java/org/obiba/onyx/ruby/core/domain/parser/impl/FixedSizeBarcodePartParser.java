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

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.ruby.core.domain.parser.IBarcodePartParser;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.ObjectError;

/**
 *
 */
public abstract class FixedSizeBarcodePartParser implements IBarcodePartParser {

  protected int size;

  public MessageSourceResolvable getPartTitle() {

    return null;
  }

  protected List<MessageSourceResolvable> validatePart(String part) {
    if(part == null || part.length() != size) {
      List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
      setBarcodeError(errors, "barcodePartLengthError", "Invalid Barcode Part Length.");
      return errors;
    } else {
      return null;
    }
  }

  /**
   * @param errors
   * @param code
   * @param defaultMsg
   */
  protected void setBarcodeError(List<MessageSourceResolvable> errors, String code, String defaultMsg) {
    MessageSourceResolvable valueError = new ObjectError("Barcode", new String[] { code }, null, defaultMsg);
    errors.add(valueError);
  }

  public void setSize(int size) {
    this.size = size;
  }
}
