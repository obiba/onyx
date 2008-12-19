/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable;

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class Variable extends Entity {

  private static final long serialVersionUID = 1L;

  private DataType type;

  private String unit;

  private List<VariableData> variableDatas;

  public Variable(String name) {
    super(name);
  }

  /**
   * @param name
   * @param parent
   */
  public Variable(String name, Entity parent) {
    super(name, parent);
  }

  public DataType getType() {
    return type;
  }

  public Variable setType(DataType type) {
    this.type = type;
    return this;
  }

  public String getUnit() {
    return unit;
  }

  public Variable setUnit(String unit) {
    this.unit = unit;
    return this;
  }

  public List<VariableData> getVariableDatas() {
    return variableDatas != null ? variableDatas : (variableDatas = new ArrayList<VariableData>());
  }

  public VariableData addVariableData(VariableData variableData) {
    if(variableData != null) {
      getVariableDatas().add(variableData);
      variableData.setVariable(this);
    }
    return variableData;
  }

}
