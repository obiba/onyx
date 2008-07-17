package org.obiba.onyx.jade.instrument;

import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;

public abstract class AbstractInstrumentRunner implements InstrumentRunner {

  protected InstrumentExecutionService instrumentExecutionService;

  public InstrumentExecutionService getInstrumentExecutionService() {
    return instrumentExecutionService;
  }

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

}
