/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.configurable;

import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.engine.variable.Variable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * A variable that relies on the {@link IDataSource} on an existing variable.
 */
@XStreamAlias("dataSourceVariable")
public class DataSourceVariable {

  private Variable variable;

  @XStreamAsAttribute
  private final String parent;

  private final IDataSource dataSource;

  public DataSourceVariable(String parent, Variable variable, IDataSource dataSource) {
    super();
    this.parent = parent;
    this.variable = variable;
    this.dataSource = dataSource;
  }

  public String getParentPath() {
    return parent;
  }

  public Variable getVariable() {
    return variable;
  }

  public IDataSource getDataSource() {
    return dataSource;
  }
}
