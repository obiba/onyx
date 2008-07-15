package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("InstrumentOutputParameter")
public class InstrumentOutputParameter extends InstrumentParameter {

  private static final long serialVersionUID = 1070862021923112847L;

  public InstrumentOutputParameter() {
    super();
  }
}
