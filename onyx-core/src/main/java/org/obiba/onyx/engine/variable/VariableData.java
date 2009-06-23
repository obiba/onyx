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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Variable data are container of data values and type related to a {@link Variable} definition. The {@link Variable} is
 * identified by its path. Dependencies between variables are solved by using key/reference variable links, expressed as
 * parameters in the variable path to ensure unique identification of a data.
 * @see IVariablePathNamingStrategy
 */
@XStreamAlias("variableData")
public class VariableData implements Serializable {

  private static final long serialVersionUID = 1L;

  @XStreamAsAttribute
  private String path;

  @XStreamAsAttribute
  private DataType type;

  @XStreamImplicit
  private List<Serializable> values;

  private List<VariableData> variableDatas;

  public VariableData(String variablePath) {
    super();
    this.path = variablePath;
  }

  public VariableData(String variablePath, Data data) {
    super();
    this.path = variablePath;
    addData(data);
  }

  /**
   * Get the path to the associated {@link Variable}.
   * @return
   * @see IVariablePathNamingStrategy
   */
  public String getVariablePath() {
    return path;
  }

  /**
   * Set the path to the associated {@link Variable}.
   * @param variablePath
   * @see IVariablePathNamingStrategy
   */
  public void setVariablePath(String variablePath) {
    this.path = variablePath;
  }

  private List<Serializable> getValues() {
    return values != null ? values : (values = new ArrayList<Serializable>());
  }

  /**
   * Get the datas.
   * @return
   */
  public List<Data> getDatas() {
    List<Data> datas = new ArrayList<Data>();

    if(type != null) {
      for(Serializable value : getValues()) {
        datas.add(new Data(type, value));
      }
    } else {
      for(Serializable value : getValues()) {
        datas.add(DataBuilder.build(value));
      }
    }

    return datas;
  }

  /**
   * Add a data to this variable data.
   * @param child
   * @return this for chaining
   */
  public VariableData addData(Data child) {
    if(child != null) {
      if(type != null && !type.equals(child.getType())) {
        throw new IllegalArgumentException("Cannot mix values from different data types: " + type + " is current, " + child.getType() + " is added.");
      }
      Serializable value = child.getValue();
      // make sure we do not export java.sql objects
      if(value instanceof Date && (value instanceof java.sql.Date || value instanceof java.sql.Timestamp || value instanceof java.sql.Time)) {
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) value);
        value = cal.getTime();
      }
      // make sure we do not send a null
      if(value != null) {
        getValues().add(value);
      }
      type = child.getType();
    }
    return this;
  }

  /**
   * Get the children variable data.
   * @return
   */
  public List<VariableData> getVariableDatas() {
    return variableDatas != null ? variableDatas : (variableDatas = new ArrayList<VariableData>());
  }

  /**
   * Add a child variable data.
   * @param child
   * @return this for chaining
   */
  public VariableData addVariableData(VariableData child) {
    if(child != null) {
      getVariableDatas().add(child);
    }
    return this;
  }

  /**
   * Add children variable data.
   * @param variableDatas
   * @return this for chaining
   */
  public VariableData addVariableDatas(VariableData... variableDatas) {
    if(variableDatas != null) {
      for(VariableData ref : variableDatas) {
        addVariableData(ref);
      }
    }
    return this;
  }

}
