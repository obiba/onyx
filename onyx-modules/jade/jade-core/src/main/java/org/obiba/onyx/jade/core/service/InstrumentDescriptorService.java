package org.obiba.onyx.jade.core.service;

public interface InstrumentDescriptorService {

  /**
   * Get the codebase of a specific instrument using its unique barcode.
   * 
   * @param instrumentBarCode
   * @return
   */
  public String getCodeBase( String instrumentBarCode );
  
  /**
   * Stores the codebase (base path used in Jnlp) for a specific instrument. 
   * The barcode is used as unique identifier for the instrument.
   * 
   * @param instrumentBarCode
   * @param codeBase
   */
  public void addCodeBase( String instrumentBarCode, String codeBase  );
  
}
