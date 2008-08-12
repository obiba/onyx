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
