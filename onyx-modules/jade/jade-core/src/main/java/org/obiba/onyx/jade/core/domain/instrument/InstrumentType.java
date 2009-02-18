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

  /**
   * Returns all of the <code>InstrumentType</code>'s parameters of the specified type and having, or not having, a
   * data source.
   * 
   * @param parameterType type of parameter to return (i.e., the class)
   * @param hasDataSource indicates whether the parameters returned should include those with or without a data source
   * @return parameters of the specified type with or without a data source, as specified (or an empty list, if none)
   */
  @SuppressWarnings("unchecked")
  public <T extends InstrumentParameter> List<T> getInstrumentParameters(Class<T> parameterType, boolean hasDataSource) {
    List<T> inputParameters = new ArrayList<T>();

    for(InstrumentParameter parameter : getInstrumentParameters()) {
      if(parameterType.isInstance(parameter)) {
        if(!(parameter.getDataSource() != null ^ hasDataSource)) {
          inputParameters.add((T) parameter);
        }
      }
    }

    return inputParameters;
  }

  @Override
  public String toString() {
    return name;
  }
}
