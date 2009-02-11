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

import org.obiba.onyx.core.domain.contraindication.Contraindication;

public class InstrumentType {

  private static final long serialVersionUID = 23414234L;

  private String name;

  private String description;

  private List<InstrumentParameter> instrumentParameters;

  private List<Contraindication> contraindications;

  public InstrumentType() {
  }

  public InstrumentType(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<Contraindication> getContraindications() {
    return contraindications != null ? contraindications : (contraindications = new ArrayList<Contraindication>());
  }

  /**
   * 
   * @param ci
   * @return this for chaining
   */
  public InstrumentType addContraindication(Contraindication ci) {
    if(ci != null) {
      getContraindications().add(ci);
    }
    return this;
  }

  public List<InstrumentParameter> getInstrumentParameters() {
    return instrumentParameters != null ? instrumentParameters : (instrumentParameters = new ArrayList<InstrumentParameter>());
  }

  /**
   * 
   * @param parameter
   * @return this for chaining
   */
  public InstrumentType addInstrumentParameter(InstrumentParameter parameter) {
    if(parameter != null) {
      getInstrumentParameters().add(parameter);
    }
    return this;
  }

  @Override
  public String toString() {
    return name;
  }
}
