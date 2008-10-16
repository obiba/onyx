/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.instrument;

/**
 * Instrument descriptor holds instrument specific configuration information. 
 * @author Yannick Marcon
 *
 */
public class InstrumentDescriptor {

  /**
   * The barcod of the instrument it is associated with.
   */
  private String instrumentBarCode;
  
  private String codeBase;
  
  public InstrumentDescriptor(String instrumentBarCode) {
    this.instrumentBarCode = instrumentBarCode;
  }

  public String getCodeBase() {
    return codeBase;
  }

  public void setCodeBase(String codeBase) {
    this.codeBase = codeBase;
  }

  public String getInstrumentBarCode() {
    return instrumentBarCode;
  }

}
