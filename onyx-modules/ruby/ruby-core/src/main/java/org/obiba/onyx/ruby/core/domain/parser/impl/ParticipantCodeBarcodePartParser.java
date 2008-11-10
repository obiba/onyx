/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.domain.parser.impl;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.springframework.context.MessageSourceResolvable;

/**
 *
 */
public class ParticipantCodeBarcodePartParser extends FixedSizeBarcodePartParser {

  /**
   * it's a regular expression that part need to match.
   */
  private String format;

  private ActiveInterviewService activeInterviewService;

  @Override
  protected MessageSourceResolvable validatePart(String part) {
    MessageSourceResolvable error = null;
    if(!part.matches(format)) {
      error = createBarcodeError("ParticipantCodeFormatError", "Invalid participant barcode format.");
    } else {
      Participant participant = activeInterviewService.getParticipant();
      if(participant == null) {
        error = createBarcodeError("ParticipantNotFoundError", "Current participant could not be found.");
      } else if(!part.equals(participant.getBarcode())) {
        error = createBarcodeError("ParticipantCodeMatchError", "Participant code does not match the current one.");
      }
    }
    return error;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getFormat() {
    return format;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public ActiveInterviewService getActiveInterviewService() {
    return this.activeInterviewService;
  }
}
