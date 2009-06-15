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
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.util.data.DataType;

/**
 * A variable that relies on the {@link IDataSource} on an existing variable.
 */
public class DataSourceVariable {

  private final String path;

  private final DataType dataType;

  private final IDataSource dataSource;

  public DataSourceVariable(String path, DataType dataType, IDataSource dataSource) {
    super();
    this.path = path;
    this.dataType = dataType;
    this.dataSource = dataSource;
  }

  public DataType getDataType() {
    return dataType;
  }

  public String getPath() {
    return path;
  }

  public VariableData getVariableData(Participant participant) {
    return new VariableData(path, dataSource.getData(participant));
  }
}
