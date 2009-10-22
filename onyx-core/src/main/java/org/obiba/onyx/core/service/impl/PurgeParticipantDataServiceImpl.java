/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.PurgeParticipantDataService;

public class PurgeParticipantDataServiceImpl implements PurgeParticipantDataService {

  private ParticipantService participantService;

  private String purgeDataOlderThanInDays;

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public int countParticipantsToBeDeleted() {
    Date maxDateForDeletion = getMaxDateForDeletion();
    List<Participant> exportedParticipantsToBePurged = participantService.getExportedParticipants(maxDateForDeletion);
    List<Participant> nonExportableParticipantsToBePurged = participantService.getNonExportableParticipants(maxDateForDeletion);
    return exportedParticipantsToBePurged.size() + nonExportableParticipantsToBePurged.size();
  }

  public Date getMaxDateForDeletion() {
    Calendar calendar = Calendar.getInstance();
    try {
      int purgeDataOlderThanInDaysValue = Integer.parseInt(purgeDataOlderThanInDays);
      if(purgeDataOlderThanInDaysValue < 0) {
        throw new RuntimeException("Value for purgeDataOlderThanInDays cannot be negative.");
      }
      calendar.add(Calendar.DATE, -1 * purgeDataOlderThanInDaysValue);
    } catch(Exception e) {
      throw new RuntimeException("The purge task has not been configured properly.  Please make sure that the value specified for the org.obiba.onyx.participant.purge property is valid.", e);
    }
    return calendar.getTime();
  }

  public void setPurgeDataOlderThanInDays(String purgeDataOlderThanInDays) {
    this.purgeDataOlderThanInDays = purgeDataOlderThanInDays;
  }

  public String getPurgeDataOlderThanInDays() {
    return purgeDataOlderThanInDays;
  }

}
