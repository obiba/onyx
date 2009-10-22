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

import java.util.Date;

/**
 * 
 */
public interface PurgeParticipantDataService {

  /**
   * Counts the total number of participants which would be deleted if the purge was launched right now.
   * 
   * @return Participant count
   */
  public int countParticipantsToBeDeleted();

  /**
   * Calculates the max date for participant deletion. This date is determined using the purgeDataOlderThanInDays
   * setting. See getPurgeDataOlderThanInDays() method below.
   * 
   * @return Max date for deletion
   */
  public Date getMaxDateForDeletion();

  /**
   * Retrieves the number of days after which an exported participant can be purged.
   * 
   * @return Number of days
   */
  public String getPurgeDataOlderThanInDays();

}
