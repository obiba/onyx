/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.purge;

import java.util.Calendar;
import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * The goal of this Tasklet is to remove the Participant data which is no longer relevant to Onyx in order to protect
 * the confidentiality of the Participants.
 * 
 * It deletes all Participants that were exported before a specific date (based on the purgeDataOlderThanInDays
 * property). Participants associated with an Interview for which the status has not been configured to be exported to
 * any Destination are also deleted (ex: Interviews with status IN_PROGRESS, which are usually not exported).
 */
public class PurgeParticipantDataTasklet implements Tasklet {

  private ParticipantService participantService;

  private String purgeDataOlderThanInDays;

  public RepeatStatus execute(StepContribution stepContribution, ChunkContext context) {

    Calendar calendar = Calendar.getInstance();
    try {
      int purgeDataOlderThanInDaysValue = Integer.parseInt(purgeDataOlderThanInDays);
      if(purgeDataOlderThanInDaysValue < 0) {
        throw new RuntimeException("Value for purgeDataOlderThanInDays cannot be negative.");
      }
      calendar.add(Calendar.DATE, -1 * purgeDataOlderThanInDaysValue);
    } catch(Exception e) {
      throw new RuntimeException("The purge task has not been configured properly.  Please make sure that the value specified for the org.obiba.onyx.participant.purge property is valid.");
    }

    // Delete exported participants
    List<Participant> exportedParticipantsToBePurged = participantService.getExportedParticipants(calendar.getTime());
    for(Participant participant : exportedParticipantsToBePurged) {
      participantService.deleteParticipant(participant);
    }

    // Delete participants which are not exportable based on current configured export destinations
    List<Participant> nonExportableParticipantsToBePurged = participantService.getExportedParticipants(calendar.getTime());
    for(Participant participant : nonExportableParticipantsToBePurged) {
      participantService.deleteParticipant(participant);
    }

    return null;
  }

  public void setPurgeDataOlderThanInDays(String purgeDataOlderThanInDays) {
    this.purgeDataOlderThanInDays = purgeDataOlderThanInDays;
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

}
