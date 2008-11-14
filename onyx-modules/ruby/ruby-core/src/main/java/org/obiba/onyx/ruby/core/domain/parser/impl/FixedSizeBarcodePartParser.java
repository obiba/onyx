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

/**
 * Common for barcode part parser with fixed size, defines size getter and setter methods
 */
public abstract class FixedSizeBarcodePartParser extends DefaultBarcodePartParser {

  private int size;

  public void setSize(int size) {
    this.size = size;
  }

  public int getSize() {
    return size;
  }

}
