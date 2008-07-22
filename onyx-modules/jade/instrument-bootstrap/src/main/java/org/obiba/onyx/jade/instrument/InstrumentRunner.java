package org.obiba.onyx.jade.instrument;



public interface InstrumentRunner {
 
  // Initialization before instrument run.
  public void initialize();
  
  public void run();    
  
  // If the initialize method is executed (without error), the shutdown method is guaranteed
  // to be executed also.  Any exception thrown during shutdown will be ignored.
  public void shutdown(); 

}
