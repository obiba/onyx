package org.obiba.onyx.jade.instrument.gehealthcare;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;

public class CardiosoftInstrumentRunner implements InstrumentRunner {

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  // Injected by spring.
  ExternalAppLauncherHelper externalAppHelper;

  public InstrumentExecutionService getInstrumentExecutionService() {
    return instrumentExecutionService;
  }

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

  public ExternalAppLauncherHelper getExternalAppHelper() {
    return externalAppHelper;
  }

  public void setExternalAppHelper(ExternalAppLauncherHelper externalAppHelper) {
    this.externalAppHelper = externalAppHelper;
  }

  public void initialize() {
    System.out.println( "*** Initializing Cardiosoft Runner ***" );
  }

  public void run() {
    System.out.println( "*** Running Cardiosoft Runner ***" );    
    externalAppHelper.launch();
  }

  public void shutdown() {
    System.out.println( "*** Shutdown Cardiosoft Runner ***" );
  }

}
