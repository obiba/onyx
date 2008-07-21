package org.obiba.onyx.jade.core;

import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;

public class JadeStage extends Stage {

  private InstrumentType instrumentType;

  public JadeStage(Module module, InstrumentType instrumentType) {
    this.instrumentType = instrumentType;
    setName(instrumentType.getName());
    setDescription(instrumentType.getDescription());
    setModule(module.getName());
  }

  public InstrumentType getInstrumentType() {
    return instrumentType;
  }

}
