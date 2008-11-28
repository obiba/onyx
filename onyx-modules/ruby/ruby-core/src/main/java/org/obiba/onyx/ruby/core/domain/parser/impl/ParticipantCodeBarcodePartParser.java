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
 * Implementation for Participant barcode part parser
 */
public class ParticipantCodeBarcodePartParser extends FixedSizeBarcodePartParser {
  //
  // Constants
  //

  private static final String PARTICIPANT_MISMATCH_BARCODE_ERROR = "Ruby.Error.ParticipantMismatch";

  //
  // Instance Variables
  //

  private ActiveInterviewService activeInterviewService;

  //
  // FixedSizeBarcodePartParser Methods
  //

  @Override
  protected MessageSourceResolvable validatePart(String part) {
    MessageSourceResolvable error = null;

    Participant participant = activeInterviewService.getParticipant();
    if(participant == null) {
      error = createBarcodeError("ParticipantNotFoundError", "Current participant could not be found.");
    } else if(!part.equals(participant.getBarcode())) {

      // The code must match the current participant code
      error = createBarcodeError(PARTICIPANT_MISMATCH_BARCODE_ERROR, new Object[] { part, participant.getBarcode() }, null);
    }
    return error;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public ActiveInterviewService getActiveInterviewService() {
    return this.activeInterviewService;
  }
}
