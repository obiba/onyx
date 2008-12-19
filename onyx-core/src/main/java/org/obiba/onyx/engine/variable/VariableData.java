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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.obiba.onyx.util.data.Data;

/**
 * 
 */
public class VariableData implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final String ENCODING = "ISO-8859-1";

  public static final String QUERY = "?";

  public static final String QUERY_ELEMENT_SEPARATOR = "=";

  public static final String QUERY_SEPARATOR = "&";

  private Variable variable;

  private Data data;

  public VariableData() {
    super();
  }

  public VariableData(Data data) {
    super();
    this.data = data;
  }

  public String getPath() {
    String path = "";
    if(variable != null) {
      path = variable.getPath();
    }
    if(data != null) {
      if(path.length() > 0) {
        path += QUERY + "data" + QUERY_ELEMENT_SEPARATOR;
      }
      try {
        path += URLEncoder.encode(data.getValueAsString(), ENCODING);
      } catch(UnsupportedEncodingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return path;
  }

  public Variable getVariable() {
    return variable;
  }

  public void setVariable(Variable variable) {
    this.variable = variable;
  }

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }

}
