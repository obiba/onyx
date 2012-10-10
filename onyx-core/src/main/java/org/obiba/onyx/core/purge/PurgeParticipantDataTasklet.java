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

import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.PurgeParticipantDataService;
import org.obiba.onyx.engine.variable.export.OnyxDataPurge;
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
 * Rules governing which Participants are to be purged can be found in purge.xml.
 */
public class PurgeParticipantDataTasklet implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(PurgeParticipantDataTasklet.class);

  private ParticipantService participantService;

  private PurgeParticipantDataService purgeParticipantDataService;

  private OnyxDataPurge onyxDataPurge;

  public RepeatStatus execute(StepContribution stepContribution, ChunkContext context) {

    log.info("**** STARTING PARTICIPANT DATA PURGE ****");
    log.info("Current purge configuration is [{}] days", purgeParticipantDataService.getPurgeDataOlderThanInDays());
    long start = System.currentTimeMillis();

    List<Participant> participantsToBePurged = onyxDataPurge.getParticipantsToPurge();
    for(Participant participant : participantsToBePurged) {
      participantService.deleteParticipant(participant);
      log.info("Deleted Participant id = [{}] and related data :  ", participant.getId());
    }

    context.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("totalDeleted", participantsToBePurged.size());

    long end = System.currentTimeMillis();

    log.info("A total of [{}] Participants were deleted in [{}] ms.", participantsToBePurged.size(), end - start);

    log.info("**** PARTICIPANT DATA PURGE COMPLETED ****");

    return RepeatStatus.FINISHED;
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public void setPurgeParticipantDataService(PurgeParticipantDataService purgeParticipantDataService) {
    this.purgeParticipantDataService = purgeParticipantDataService;
  }

  public void setOnyxDataPurge(OnyxDataPurge onyxDataPurge) {
    this.onyxDataPurge = onyxDataPurge;
  }

}