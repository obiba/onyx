/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.service;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;

/**
 *
 */
public interface ParticipantTubeRegistrationService {

  /**
   * 
   * @param tubeSetName the associated tube set (i.e., Ruby stage name)
   * @return
   */
  public ParticipantTubeRegistration getParticipantTubeRegistration(Participant participant, String tubeSetName);

  /**
   * Deletes the current {@link ParticipantTubeRegistration} and all associated objects, of the specified Ruby stage.
   * 
   * @param tubeSetName the associated Ruby stage
   */
  public void deleteParticipantTubeRegistration(Participant participant, String tubeSetName);

  /**
   * Deletes the {@link ParticipantTubeRegistration}s and associated objects of a specific {@link Participant}.
   * 
   * @param participant The participant.
   */
  public void deleteAllParticipantTubeRegistrations(Participant participant);

  /**
   * Create the current {@link ParticipantTubeRegistration} and persist it.
   * @param participant
   * @param tubeSetName the associated tube set (i.e., Ruby stage name)
   * @return
   */
  public ParticipantTubeRegistration start(Participant participant, String tubeSetName);

  /**
   * Resume an interrupted participant tube registration.
   * 
   * @param participant participant
   * @param tubeSetName the associated tube set (i.e., Ruby stage name)
   */
  public void resume(Participant participant, String tubeSetName);

  /**
   * 
   * @param registration
   */
  public void end(ParticipantTubeRegistration registration);

}
