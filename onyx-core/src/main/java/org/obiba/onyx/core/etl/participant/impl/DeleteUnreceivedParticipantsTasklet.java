/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant.impl;

import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.etl.participant.IParticipantReader;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.magma.MagmaInstanceProvider;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * Spring Batch Tasklet called in DeleteUnreceivedParticipantsStep to delete the participants without interview
 */
public class DeleteUnreceivedParticipantsTasklet implements Tasklet {

  private ParticipantService participantService;

  private IParticipantReader participantReader;

  private MagmaInstanceProvider magmaInstanceProvider;

  public RepeatStatus execute(StepContribution stepContribution, ChunkContext context) throws Exception {
    if(participantReader.isUpdateAvailable()) {
      List<Participant> participants = participantService.getNotReceivedParticipants();

      for(Participant participant: participants) {
        magmaInstanceProvider.evictCachedValues(participant);
      }

      participantService.cleanUpAppointment();
      stepContribution.setExitStatus(new ExitStatus("UPDATE"));
    } else {
      stepContribution.setExitStatus(new ExitStatus("NO UPDATE"));
    }
    return RepeatStatus.FINISHED;
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public void setParticipantReader(IParticipantReader participantReader) {
    this.participantReader = participantReader;
  }

  public void setMagmaInstanceProvider(MagmaInstanceProvider magmaInstanceProvider) {
    this.magmaInstanceProvider = magmaInstanceProvider;
  }
}
