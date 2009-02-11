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

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;

/**
 * abstract class used to modify the data (type and/or value)
 */
public abstract class AbstractDataSourceDataModifier extends AbstractDataSourceWrapper {

  public Data getData(Participant participant) {
    Data data = getWrapperSource().getData(participant);
    return modify(data);
  }

  public String getUnit() {
    return getWrapperSource().getUnit();
  }

  protected AbstractDataSourceDataModifier(IDataSource iDataSource) {
    setWrapperSource(iDataSource);
  }

  protected abstract Data modify(Data data);

}
