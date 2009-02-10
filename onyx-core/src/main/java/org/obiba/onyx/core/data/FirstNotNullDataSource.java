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

import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;

/**
 * 
 */
public class FirstNotNullDataSource extends AbstractMultipleDataSource {

  private IDataSource firstDataSource = null;

  public Data getData(Participant participant) {
    for(IDataSource dataSource : getIDataSources()) {
      Data data = dataSource.getData(participant);
      if(data != null && data.getValue() != null) {
        firstDataSource = dataSource;
        return data;
      }
    }
    return null;
  }

  public String getUnit() {
    return (firstDataSource != null) ? firstDataSource.getUnit() : null;
  }

  public FirstNotNullDataSource(List<IDataSource> iDataSources) {
    super(iDataSources);
  }
}
