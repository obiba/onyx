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

import java.io.Serializable;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;

/**
 * Get a data with its unit associated to a Participant.
 */
public interface IDataSource extends Serializable {

  /**
   * 
   * @param participant
   * @return null if no data found
   */
  public Data getData(Participant participant);

  /**
   * Optional unit to be applied to data value.
   * @return
   */
  public String getUnit();

}
