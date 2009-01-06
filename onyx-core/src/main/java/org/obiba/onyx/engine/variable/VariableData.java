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
import java.util.List;

import org.obiba.onyx.util.data.Data;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * 
 */
@XStreamAlias("variableData")
public class VariableData implements Serializable {

  private static final long serialVersionUID = 1L;

  // public static final String ENCODING = "ISO-8859-1";
  //
  // public static final String QUERY = "/data?";
  //
  // public static final String QUERY_KEY_VALUE_SEPARATOR = "=";
  //
  // public static final String QUERY_STATEMENT_SEPARATOR = "&";

  @XStreamAsAttribute
  private String variablePath;

  private Data data;

  @XStreamImplicit
  private List<VariableData> references;

  public VariableData(String variablePath) {
    super();
  }

  public VariableData(String variablePath, Data data) {
    super();
    this.variablePath = variablePath;
    this.data = data;
  }

  // public String getPath() {
  // String path = "";
  // if(variablePath != null) {
  // path = variablePath;
  // }
  // if(data != null) {
  // if(path.length() > 0) {
  // path += QUERY + "value" + QUERY_KEY_VALUE_SEPARATOR;
  // }
  // try {
  // path += URLEncoder.encode(data.getValueAsString(), ENCODING);
  // } catch(UnsupportedEncodingException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // }
  // return path;
  // }

  public String getVariablePath() {
    return variablePath;
  }

  public void setVariablePath(String variablePath) {
    this.variablePath = variablePath;
  }

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }

  public List<VariableData> getReferences() {
    return references != null ? references : (references = new ArrayList<VariableData>());
  }

  /**
   * Cross referring variable data.
   * @param reference
   * @return this for chaining
   */
  public VariableData addReference(VariableData reference) {
    if(reference != null) {
      getReferences().add(reference);
      reference.getReferences().add(this);
    }
    return this;
  }

  public VariableData addReferences(VariableData... reference) {
    if(reference != null) {
      for(VariableData ref : reference) {
        addReference(ref);
      }
    }
    return this;
  }

}
