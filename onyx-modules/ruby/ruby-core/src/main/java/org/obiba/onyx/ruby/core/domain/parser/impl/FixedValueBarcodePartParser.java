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

import org.obiba.onyx.core.service.ActiveInterviewService;
import org.springframework.context.MessageSourceResolvable;

/**
 * Implements barcode part validation with fixed value
 */
public class FixedValueBarcodePartParser extends FixedSizeBarcodePartParser {

  private String fixedValue;

  public void setFixedValue(String fixedValue) {
    this.fixedValue = fixedValue;
  }

  @Override
  protected MessageSourceResolvable validatePart(String part, ActiveInterviewService activeInterviewService) {
    MessageSourceResolvable error = null;
    if(!part.equals(fixedValue)) {
      error = createBarcodeError("BarcodePartValueError", "Invalid barcode part value.");
    }
    return error;
  }
}
