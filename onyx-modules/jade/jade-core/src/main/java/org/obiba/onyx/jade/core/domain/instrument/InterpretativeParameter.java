package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.obiba.onyx.util.data.DataType;

@Entity
@DiscriminatorValue("InterpretativeParameter")
public class InterpretativeParameter extends InstrumentParameter {

  private static final long serialVersionUID = -5035544856948727535L;

  @Enumerated(EnumType.STRING)
  private ParticipantInteractionType type;

  public InterpretativeParameter() {
    super();
    setDataType(DataType.TEXT);
    setCaptureMethod(InstrumentParameterCaptureMethod.MANUAL);
  }

  public ParticipantInteractionType getType() {
    return type;
  }

  public void setType(ParticipantInteractionType type) {
    this.type = type;
  }

}
