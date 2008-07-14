package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("InstrumentInputParameter")
public class InstrumentInputParameter extends InstrumentParameter {

  private static final long serialVersionUID = -5035544856948727535L;
  
  @ManyToOne
  @JoinColumn(name = "input_source_id")
  private InputSource inputSource;

  public InstrumentInputParameter() {
    super();
  }

  public InputSource getInputSource() {
    return inputSource;
  }

  public void setInputSource(InputSource inputSource) {
    this.inputSource = inputSource;
  }

}
