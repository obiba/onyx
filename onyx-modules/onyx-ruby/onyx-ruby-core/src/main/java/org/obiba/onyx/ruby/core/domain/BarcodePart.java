/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.domain;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

/**
 * 
 */
public class BarcodePart {
  //
  // Instance Variables
  //

  private String part;

  private MessageSourceResolvable title;

  //
  // Constructors
  //

  public BarcodePart(String part) {
    this.part = part;
  }

  public BarcodePart(String part, MessageSourceResolvable title) {
    this.part = part;
    this.title = title;
  }

  //
  // Methods
  //

  public void setPartTitle(MessageSourceResolvable title) {
    this.title = title;
  }

  public MessageSourceResolvable getPartTitle() {
    return title;
  }

  /**
   * @return part value wrapped in <code>DefaultMessageSourceResolvable</code>
   */
  public MessageSourceResolvable getPartLabel() {
    String partCode = (title != null) ? title.getCodes()[0] + "." + part : part;
    // Default value will be the actual part value
    MessageSourceResolvable partLabel = new DefaultMessageSourceResolvable(new String[] { partCode }, part);

    return partLabel;
  }

  /**
   * Returns the non-localized barcode part (i.e., the part as it appears in the barcode).
   * 
   * Note: This value serves as the variable data for barcode part variables.
   * 
   * @return non-localized barcode part
   */
  public String getPartValue() {
    return part;
  }
}