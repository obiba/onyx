package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.util.data.Data;

@Entity
@DiscriminatorValue("OutputParameterSource")
public class OutputParameterSource extends InputSource {

  private static final long serialVersionUID = 79789789454360982L;

  @ManyToOne
  @JoinColumn(name = "instrument_type_id")
  private InstrumentType instrumentType;
  
  @Column(length = 200)
  private String parameterName;

  public OutputParameterSource() {
    super();
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  public InstrumentType getInstrumentType() {
    return instrumentType;
  }

  public void setInstrumentType(InstrumentType instrumentType) {
    this.instrumentType = instrumentType;
  }

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  @Override
  public Data getData(ParticipantInterview interview) {
    // TODO Auto-generated method stub
    return null;
  }

}
