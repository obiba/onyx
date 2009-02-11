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

public class InstrumentComputedOutputParameter extends InstrumentOutputParameter {

  private static final long serialVersionUID = -4217317349036043214L;

  private List<InstrumentParameter> outputs;

  private InstrumentOutputParameterAlgorithm algorithm;

  public InstrumentComputedOutputParameter() {
    super();
  }

  public List<InstrumentParameter> getInstrumentParameters() {
    return outputs != null ? outputs : (outputs = new ArrayList<InstrumentParameter>());
  }

  public void addInstrumentParameter(InstrumentParameter instrumentParameter) {
    if(instrumentParameter != null) {
      getInstrumentParameters().add(instrumentParameter);
    }
  }

  public InstrumentOutputParameterAlgorithm getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(InstrumentOutputParameterAlgorithm algorithm) {
    this.algorithm = algorithm;
  }

}
