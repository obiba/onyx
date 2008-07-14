package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("OperatorSource")
public class OperatorSource extends InputSource {

  private static final long serialVersionUID = -5502454360982L;

  public OperatorSource() {
    super();
  }

}
