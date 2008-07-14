package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.Entity;

@Entity
//@DiscriminatorValue("InstrumentInputParameter")
public class InstrumentInputParameter extends InstrumentParameter {

  private static final long serialVersionUID = -5035544856948727535L;

  public InstrumentInputParameter() {
    super();
  }

}
