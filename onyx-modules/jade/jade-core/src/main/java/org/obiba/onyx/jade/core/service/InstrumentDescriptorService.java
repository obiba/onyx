/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service;

public interface InstrumentDescriptorService {

  /**
   * Get the codebase of a specific instrument using its unique barcode.
   * 
   * @param instrumentBarCode
   * @return
   */
  public String getCodeBase(String instrumentBarCode);

  /**
   * Stores the codebase (base path used in Jnlp) for a specific instrument. The barcode is used as unique identifier
   * for the instrument.
   * 
   * @param instrumentBarCode
   * @param codeBase
   */
  public void setCodeBase(String instrumentBarCode, String codeBase);

}
