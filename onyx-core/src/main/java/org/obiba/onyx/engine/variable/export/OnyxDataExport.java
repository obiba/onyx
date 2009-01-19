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
import java.util.Iterator;
import java.util.List;

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.variable.VariableDataSet;
import org.obiba.onyx.engine.variable.VariableDirectory;
import org.obiba.onyx.engine.variable.util.VariableStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class OnyxDataExport {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(OnyxDataExport.class);

  private EntityQueryService queryService;

  private VariableDirectory variableDirectory;

  private UserSessionService userSessionService;

  private IOnyxDataExportStrategy exportStrategy;

  private List<OnyxDataExportDestination> exportDestinations;

  public void setExportDestinations(List<OnyxDataExportDestination> exportDestinations) {
    this.exportDestinations = exportDestinations;
  }

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

  public void exportCompletedInterviews() throws Exception {

    Participant template = new Participant();
    template.setExported(false);
    List<Participant> participants = queryService.match(template);
    for(Iterator<Participant> iterator = participants.iterator(); iterator.hasNext();) {
      Participant participant = iterator.next();
      // Export completed interviews only
      Interview interview = participant.getInterview();
      if(interview == null || interview.getStatus() != InterviewStatus.COMPLETED) {
        iterator.remove();
      }
    }

    if(participants.size() > 0) {
      for(OnyxDataExportDestination destination : this.exportDestinations) {
        OnyxDataExportContext context = new OnyxDataExportContext(destination.getName(), userSessionService.getUser());
        try {
          exportStrategy.prepare(context);
          for(Participant participant : participants) {
            String entryName = participant.getBarcode() + ".xml";
            OutputStream os = exportStrategy.newEntry(entryName);
            VariableDataSet participantData = variableDirectory.getParticipantData(participant, destination);
            VariableStreamer.toXML(participantData, os);
            os.flush();
          }
        } catch(RuntimeException e) {
          context.fail();
          log.error("Error exporting data to destination {}:" + e.getMessage(), destination.getName());
        } finally {
          context.endExport();
          exportStrategy.terminate(context);
        }
      }
    }
  }

}
