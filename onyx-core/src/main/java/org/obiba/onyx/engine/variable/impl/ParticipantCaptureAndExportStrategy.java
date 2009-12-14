/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.impl;

import java.util.Date;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.core.service.ExportLogService;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.variable.CaptureAndExportStrategy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Strategy for determining a Participant's capture date range.
 */
public class ParticipantCaptureAndExportStrategy implements CaptureAndExportStrategy {
  //
  // Instance Variables
  //

  @Autowired(required = true)
  private ParticipantService participantService;

  @Autowired(required = true)
  private ExportLogService exportLogService;

  //
  // CaptureAndExportStrategy Methods
  //

  public String getEntityType() {
    return "Participant";
  }

  public Date getCaptureStartDate(String entityIdentifier) {
    Interview interview = getInterview(entityIdentifier);
    return (interview != null) ? interview.getStartDate() : null;
  }

  public Date getCaptureEndDate(String entityIdentifier) {
    Interview interview = getInterview(entityIdentifier);
    return (interview != null) ? interview.getEndDate() : null;
  }

  public boolean isExported(String entityIdentifier) {
    return isExported(entityIdentifier, null);
  }

  public boolean isExported(String entityIdentifier, String destinationName) {
    if(entityIdentifier != null) {
      ExportLog exportLog = (destinationName != null) ? exportLogService.getLastExportLog("Participant", entityIdentifier, destinationName) : exportLogService.getLastExportLog("Participant", entityIdentifier);
      return exportLog != null;
    }
    return false;
  }

  //
  // Methods
  //

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public void setExportLogService(ExportLogService exportLogService) {
    this.exportLogService = exportLogService;
  }

  private Interview getInterview(String participantBarcode) {
    Participant template = new Participant();
    template.setBarcode(participantBarcode);
    Participant participant = participantService.getParticipant(template);
    if(participant != null) {
      return participant.getInterview();
    }
    throw new IllegalArgumentException("No such participant entity");
  }
}
