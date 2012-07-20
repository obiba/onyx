/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service;

public interface PurgeParticipantDataService {

  /**
   * Retrieves the number of days after which an exported participant can be purged.
   *
   * @return Number of days
   */
  public String getPurgeDataOlderThanInDays();

  /**
   * @return True if participants can be interviewed several times, false otherwise
   */
  public boolean isMultipleInterview();

}
