package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.obiba.onyx.jade.core.service.InputSourceVisitor;

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
  public void accept(InputSourceVisitor visitor) {
    visitor.visit(this);
  }

}
