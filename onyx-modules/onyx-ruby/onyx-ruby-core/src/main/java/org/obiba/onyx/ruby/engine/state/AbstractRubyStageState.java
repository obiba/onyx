/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.engine.state;

import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.ruby.core.service.ParticipantTubeRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Ruby states.
 */
public abstract class AbstractRubyStageState extends AbstractStageState {
  //
  // Constants
  //

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractRubyStageState.class);

  private ParticipantTubeRegistrationService participantTubeRegistrationService;

  public void setParticipantTubeRegistrationService(ParticipantTubeRegistrationService participantTubeRegistrationService) {
    this.participantTubeRegistrationService = participantTubeRegistrationService;
  }

  protected void deleteParticipantTubeRegistration() {
    getParticipantTubeRegistrationService().deleteParticipantTubeRegistration(activeInterviewService.getParticipant(), getStage().getName());
  }

  protected ParticipantTubeRegistrationService getParticipantTubeRegistrationService() {
    return participantTubeRegistrationService;
  }

}
