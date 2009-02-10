/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.data;

import java.util.ArrayList;
import java.util.List;

/**
 * holds a list of data sources
 */
public abstract class AbstractMultipleDataSource implements IDataSource {

  private List<IDataSource> iDataSources;

  public List<IDataSource> getIDataSources() {
    return (iDataSources != null) ? iDataSources : new ArrayList<IDataSource>();
  }

  public void setIDataSources(List<IDataSource> iDataSources) {
    this.iDataSources = iDataSources;
  }

  public AbstractMultipleDataSource(List<IDataSource> iDataSources) {
    this.iDataSources = iDataSources;
  }

}
