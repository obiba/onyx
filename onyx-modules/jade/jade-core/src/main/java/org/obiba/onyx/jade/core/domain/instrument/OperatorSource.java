package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.util.data.Data;

@Entity
@DiscriminatorValue("OperatorSource")
public class OperatorSource extends InputSource {

  private static final long serialVersionUID = -5502454360982L;

  public OperatorSource() {
    super();
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public Data getData(ParticipantInterview interview) {
    // TODO Auto-generated method stub
    return null;
  }

}
