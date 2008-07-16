package org.obiba.onyx.jade.instrument;

import org.obiba.onyx.jade.remote.RemoteService;

public abstract class AbstractInstrumentRunner implements InstrumentRunner {

  protected RemoteService instrRemoteService;      
  
  public RemoteService getInstrRemoteService() {
    return instrRemoteService;
  }

  public void setInstrRemoteService(RemoteService instrRemoteService) {
    this.instrRemoteService = instrRemoteService;
  }

}
