package org.obiba.onyx.jade.instrument;

import java.util.Collection;


public interface InstrumentRunner {
  
  // Delete any previous measurements persisted locally (i.e. in local DB, output file, ...)  
  public void deleteOldMeasurements();
  
  // Set input parameters and configuration instrument before a measurement.   
  public void setInput();    
  
  // Retrieve output from external software once measurements have been taken.
  public Collection retrieveOutput(); 

}
