package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.obiba.onyx.jade.core.service.InputSourceVisitor;

@Entity
@DiscriminatorValue("ParticipantPropertySource")
public class ParticipantPropertySource extends InputSource {

  private static final long serialVersionUID = -5505114802454360982L;
  
  @Column(length = 200)
  private String property;

  public ParticipantPropertySource() {
    super();
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }
  
  @Override
  public void accept(InputSourceVisitor visitor) {
    visitor.visit(this);
  }
}
