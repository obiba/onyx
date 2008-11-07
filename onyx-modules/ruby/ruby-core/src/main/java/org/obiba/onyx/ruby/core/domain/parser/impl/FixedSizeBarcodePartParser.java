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

import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.ruby.core.domain.BarcodePart;
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

  public BarcodePart eatAndValidatePart(StringBuilder barcodeFragment, ActiveInterviewService activeInterviewService, List<MessageSourceResolvable> errors) {
    BarcodePart barcodePart = null;

    try {

      String part = barcodeFragment.substring(0, size);
      barcodeFragment.delete(0, size);
      MessageSourceResolvable error = validatePart(part, activeInterviewService);

      if(error != null) {
        errors.add(error);
      } else {
        barcodePart = new BarcodePart(part);
      }

    } catch(Exception e) {
      errors.add(createBarcodeError("BarcodePartLengthError", "Invalid barcode part length."));
    }
    return barcodePart;
  }

  protected abstract MessageSourceResolvable validatePart(String part, ActiveInterviewService activeInterviewService);

  /**
   * @param code
   * @param defaultMsg
   */
  protected MessageSourceResolvable createBarcodeError(String code, String defaultMsg) {
    MessageSourceResolvable error = new ObjectError("Barcode", new String[] { code }, null, defaultMsg);
    return error;
  }

  public void setSize(int size) {
    this.size = size;
  }
}
