package org.obiba.onyx.jade.core.domain.instrument;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("InstrumentComputedOutputParameter")
public class InstrumentComputedOutputParameter extends InstrumentOutputParameter {

  private static final long serialVersionUID = -4217317349036043214L;

  @OneToMany(mappedBy = "instrumentComputedOutputParameter")
  private List<InstrumentOutputParameter> instrumentOutputParameters;

  @Enumerated(EnumType.STRING)
  private InstrumentOutputParameterAlgorithm instrumentOutputParameterAlgorithm;

  public InstrumentComputedOutputParameter() {
    super();
  }

  public List<InstrumentOutputParameter> getInstrumentOutputParameters() {
    return instrumentOutputParameters != null ? instrumentOutputParameters : (instrumentOutputParameters = new ArrayList<InstrumentOutputParameter>());
  }

  public void addInstrumentOutputParameter(InstrumentOutputParameter instrumentOutputParameter) {
    if(instrumentOutputParameter != null) {
      getInstrumentOutputParameters().add(instrumentOutputParameter);
      instrumentOutputParameter.setInstrumentComputedOutputParameter(this);
    }
  }

  public InstrumentOutputParameterAlgorithm getInstrumentOutputParameterAlgorithm() {
    return instrumentOutputParameterAlgorithm;
  }

  public void setInstrumentOutputParameterAlgorithm(InstrumentOutputParameterAlgorithm instrumentOutputParameterAlgorithm) {
    this.instrumentOutputParameterAlgorithm = instrumentOutputParameterAlgorithm;
  }

}
