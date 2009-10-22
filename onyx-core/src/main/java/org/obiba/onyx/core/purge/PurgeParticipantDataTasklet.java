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

import java.util.Date;
import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.PurgeParticipantDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger log = LoggerFactory.getLogger(PurgeParticipantDataTasklet.class);

  private ParticipantService participantService;

  private PurgeParticipantDataService purgeParticipantDataService;

  public RepeatStatus execute(StepContribution stepContribution, ChunkContext context) {

    log.info("**** STARTING PARTICIPANT DATA PURGE ****");
    log.info("Current purge configuration is [{}] days", purgeParticipantDataService.getPurgeDataOlderThanInDays());
    long start = System.currentTimeMillis();

    Date maxDateForDeletion = purgeParticipantDataService.getMaxDateForDeletion();

    // Delete exported participants
    List<Participant> exportedParticipantsToBePurged = participantService.getExportedParticipants(maxDateForDeletion);
    for(Participant participant : exportedParticipantsToBePurged) {
      log.info("Deleting Participant id = [{}] (exported participant) and related data  :  ", participant.getId());
      participantService.deleteParticipant(participant);
    }

    // Delete participants which are not exportable based on current configured export destinations
    List<Participant> nonExportableParticipantsToBePurged = participantService.getNonExportableParticipants(maxDateForDeletion);
    for(Participant participant : nonExportableParticipantsToBePurged) {
      participantService.deleteParticipant(participant);
      log.info("Deleting Participant id = [{}] (non-exportable participant) and related data :  ", participant.getId());
    }

    int totalDeleted = exportedParticipantsToBePurged.size() + nonExportableParticipantsToBePurged.size();
    context.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("totalDeleted", totalDeleted);

    long end = System.currentTimeMillis();

    log.info("A total of [{}] Participants were deleted in [{}] ms.", totalDeleted, end - start);

    log.info("**** PARTICIPANT DATA PURGE COMPLETED ****");

    return RepeatStatus.FINISHED;
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public void setPurgeParticipantDataService(PurgeParticipantDataService purgeParticipantDataService) {
    this.purgeParticipantDataService = purgeParticipantDataService;
  }

}
