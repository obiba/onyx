/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.core.service.ExportLogService;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.variable.export.OnyxDataPurge;
import org.obiba.onyx.magma.MagmaInstanceProvider;

/**
 * Model to obtain details of the Participants that are scheduled to be purged. The load method obtains all the
 * Participants to be purged. Other methods provide access to high level details about those Participants.
 */
public class OnyxDataPurgeModel extends SpringDetachableModel<List<Participant>> {
  private static final long serialVersionUID = 1L;

  private int totalInterviews;

  private transient List<Participant> exportedInterviews;

  private transient List<Participant> unexportedInterviews;

  @SpringBean
  private OnyxDataPurge onyxDataPurge;

  @SpringBean
  private ParticipantService participantService;

  @SpringBean
  private ExportLogService exportLogService;

  public OnyxDataPurgeModel() {
  }

  @Override
  protected List<Participant> load() {
    List<Participant> participantsToPurge = onyxDataPurge.getParticipantsToPurge();
    totalInterviews = participantService.countParticipants(null, null);

    exportedInterviews = new ArrayList<Participant>();
    unexportedInterviews = new ArrayList<Participant>();
    for(Participant participant : participantsToPurge) {
      List<ExportLog> exportLogs = exportLogService.getExportLogs(MagmaInstanceProvider.PARTICIPANT_ENTITY_TYPE, participant.getBarcode(), null, true);
      if(exportLogs == null || exportLogs.isEmpty()) {
        unexportedInterviews.add(participant);
      } else {
        exportedInterviews.add(participant);
      }
    }
    return participantsToPurge;
  }

  @Override
  protected void onDetach() {
    super.onDetach();
    totalInterviews = 0;
    exportedInterviews = null;
    unexportedInterviews = null;
  }

  public boolean hasInterviewsToPurge() {
    return super.getObject().size() > 0;
  }

  public String getTotalInterviewsToPurge() {
    return Integer.toString(super.getObject().size());
  }

  public String getTotalInterviews() {
    return Integer.toString(totalInterviews);
  }

  public String getTotalExportedInterviewsToPurge() {
    return Integer.toString(exportedInterviews.size());
  }

  public String getTotalUnexportedInterviewsToPurge() {
    return Integer.toString(unexportedInterviews.size());
  }

  public String getTotalUnexportedInterviewsWithStatus(InterviewStatus status) {
    int result = 0;
    for(Participant participant : unexportedInterviews) {
      if(participant.getInterview().getStatus() == status) result++;
    }
    return Integer.toString(result);
  }

  public String getTotalExportedInterviewsWithStatus(InterviewStatus status) {
    int result = 0;
    for(Participant participant : exportedInterviews) {
      if(participant.getInterview().getStatus() == status) result++;
    }
    return Integer.toString(result);
  }

}
