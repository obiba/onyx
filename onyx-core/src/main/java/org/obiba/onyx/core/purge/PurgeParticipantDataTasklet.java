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

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.PurgeParticipantDataService;
import org.obiba.onyx.engine.variable.export.OnyxDataPurge;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * The goal of this Tasklet is to remove the Participant data which is no longer relevant to Onyx in order to protect
 * the confidentiality of the Participants.
 * <p/>
 * Rules governing which Participants are to be purged can be found in purge.xml.
 */
public class PurgeParticipantDataTasklet implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(PurgeParticipantDataTasklet.class);

  public static final String TOTAL_DELETED = "totalDeleted";

  public static final String TOTAL_TIME = "totalTime";

  private ParticipantService participantService;

  private PurgeParticipantDataService purgeParticipantDataService;

  private OnyxDataPurge onyxDataPurge;

  public RepeatStatus execute(StepContribution stepContribution, ChunkContext context) {

    ExecutionContext execCtx = context.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
    if(execCtx.containsKey(TOTAL_DELETED) == false) {
      log.info("**** STARTING PARTICIPANT DATA PURGE ****");
      log.info("Current purge configuration is [{}] days", purgeParticipantDataService.getPurgeDataOlderThanInDays());
    }
    long start = System.currentTimeMillis();

    List<Participant> participantsToBePurged = onyxDataPurge.getParticipantsToPurge();

    if(participantsToBePurged.isEmpty() == false) {
      Participant participant = participantsToBePurged.iterator().next();
      participantService.deleteParticipant(participant);
      long duration = System.currentTimeMillis() - start;
      log.info("Participant [{}] was deleted in [{}] ms (remains {}).",
          new Object[] {participant.getBarcode(), duration, participantsToBePurged.size() - 1});
      int totalDeleted = 1;
      long totalTime = duration;
      if(execCtx.containsKey(TOTAL_DELETED)) {
        totalDeleted = totalDeleted + execCtx.getInt(TOTAL_DELETED);
        totalTime = totalTime + execCtx.getLong(TOTAL_TIME);
      }
      execCtx.put(TOTAL_DELETED, totalDeleted);
      execCtx.put(TOTAL_TIME, totalTime);
    }

    boolean continuable = participantsToBePurged.size() > 1;

    if(continuable == false) {
      log.info("**** PARTICIPANT DATA PURGE COMPLETED IN {}ms ****",
          execCtx.containsKey(TOTAL_TIME) ? execCtx.getLong(TOTAL_TIME) : 0);
    }

    return continuable ? RepeatStatus.CONTINUABLE : RepeatStatus.FINISHED;
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