/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.instrument;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
@DiscriminatorValue("InstrumentComputedOutputParameter")
public class InstrumentComputedOutputParameter extends InstrumentOutputParameter {

  private static final long serialVersionUID = -4217317349036043214L;

  @ManyToMany
  @JoinTable(name = "instrument_computed_output", joinColumns = @JoinColumn(name = "instrument_computed_output_parameter_id"), inverseJoinColumns = @JoinColumn(name = "instrument_output_parameter_id"))
  private List<InstrumentOutputParameter> outputs;

  @Enumerated(EnumType.STRING)
  private InstrumentOutputParameterAlgorithm algorithm;

  public InstrumentComputedOutputParameter() {
    super();
  }

  public List<InstrumentOutputParameter> getInstrumentOutputParameters() {
    return outputs != null ? outputs : (outputs = new ArrayList<InstrumentOutputParameter>());
  }

  public void addInstrumentOutputParameter(InstrumentOutputParameter instrumentOutputParameter) {
    if(instrumentOutputParameter != null) {
      getInstrumentOutputParameters().add(instrumentOutputParameter);
    }
  }

  public InstrumentOutputParameterAlgorithm getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(InstrumentOutputParameterAlgorithm algorithm) {
    this.algorithm = algorithm;
  }

}
