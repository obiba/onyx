package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("OutputParameterSource")
public class OutputParameterSource extends InputSource {

  private static final long serialVersionUID = 79789789454360982L;

  public OutputParameterSource() {
    super();
  }

}
