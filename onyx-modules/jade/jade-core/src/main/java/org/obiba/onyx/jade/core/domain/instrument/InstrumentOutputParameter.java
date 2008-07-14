package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
//@DiscriminatorValue("InstrumentOutputParameter")
public class InstrumentOutputParameter extends InstrumentParameter {

  private static final long serialVersionUID = 1070862021923112847L;

  @ManyToOne
  @JoinColumn(name = "instrument_computed_output_parameter_id")
  private InstrumentComputedOutputParameter instrumentComputedOutputParameter;

  public InstrumentOutputParameter() {
    super();
  }

  public InstrumentComputedOutputParameter getInstrumentComputedOutputParameter() {
    return instrumentComputedOutputParameter;
  }

  public void setInstrumentComputedOutputParameter(InstrumentComputedOutputParameter instrumentComputedOutputParameter) {
    this.instrumentComputedOutputParameter = instrumentComputedOutputParameter;
  }

}
