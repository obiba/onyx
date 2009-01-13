/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import java.io.OutputStream;
import java.util.List;

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.variable.VariableDataSet;
import org.obiba.onyx.engine.variable.VariableDirectory;
import org.obiba.onyx.engine.variable.util.VariableStreamer;

/**
 * 
 */
public class OnyxDataExport {

  EntityQueryService queryService;

  VariableDirectory variableDirectory;

  UserSessionService userSessionService;

  IOnyxDataExportStrategy exportStrategy;

  public void setExportStrategy(IOnyxDataExportStrategy exportStrategy) {
    this.exportStrategy = exportStrategy;
  }

  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }

  public void setUserSessionService(UserSessionService userSessionService) {
    this.userSessionService = userSessionService;
  }

  public void setVariableDirectory(VariableDirectory variableDirectory) {
    this.variableDirectory = variableDirectory;
  }

  public void exportCompletedInterviews(OnyxDataExportDestination destination) throws Exception {

    OnyxDataExportContext context = new OnyxDataExportContext(destination.getName(), userSessionService.getUser());

    Participant template = new Participant();
    template.setExported(false);
    List<Participant> participants = queryService.match(template);
    if(participants.size() > 0) {
      exportStrategy.prepare(context);
      for(Participant participant : participants) {
        if(participant.getInterview().getStatus() == InterviewStatus.COMPLETED) {
          String entryName = participant.getBarcode() + ".xml";
          OutputStream os = exportStrategy.newEntry(entryName);
          VariableDataSet participantData = variableDirectory.getParticipantData(participant, destination);
          VariableStreamer.toXML(participantData, os);
        }
      }
      context.endExport();
      exportStrategy.terminate(context);
    }
  }

}
