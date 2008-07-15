package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("FixedSource")
public class FixedSource extends InputSource {

  private static final long serialVersionUID = -55114802454360982L;
  
  @Column(length = 200)
  private String value;

  public FixedSource() {
    super();
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

}
