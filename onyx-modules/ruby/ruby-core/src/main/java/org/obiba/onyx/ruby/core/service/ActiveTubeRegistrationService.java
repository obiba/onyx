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

import java.util.List;

import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.Remark;
import org.springframework.context.MessageSourceResolvable;

/**
 * Interface for a tube registration service, used to register tubes for the current participant.
 */
public interface ActiveTubeRegistrationService {

  /**
   * Returns the expected number of tubes to be registered.
   * 
   * @return expected number of tubes to be registered
   */
  public int getExpectedTubeCount();

  /**
   * Returns the number of tubes presently registered for the current participant.
   * 
   * @return number of tubes registered for the current participant
   */
  public int getRegisteredTubeCount();

  /**
   * Registers a tube, with the specified barcode, for the current participant.
   * 
   * @param barcode the tube's barcode
   * @return error list, in case of barcode validation errors
   */
  public List<MessageSourceResolvable> registerTube(String barcode);

  /**
   * Un-registers a tube, with the specified barcode, for the current participant.
   * 
   * @param barcode the tube's barcode
   * @throws IllegalArgumentException if no tube with the specified barcode is registered
   */
  public void unregisterTube(String barcode);

  /**
   * Records a remark related to the specified tube registration.
   * 
   * @param barcode the tube's barcode
   * @param remark the remark to record (<code>null</code> to remove a previously recorded remark)
   * @throws IllegalArgumentException if no tube with the specified barcode is registered
   */
  public void setTubeRemark(String barcode, Remark remark);

  /**
   * Records a comment related to the specified tube registration.
   * 
   * @param barcode the tube's barcode
   * @param comment the comment to record (<code>null</code> to remove a previously recorded comment)
   * @throws IllegalArgumentException if no tube with the specified barcode is registered
   */
  public void setTubeComment(String barcode, String comment);

  /**
   * Returns true when at least one contraindication exists for the specified type.
   * @param type the type to check
   * @return true when at least one contraindication exists.
   */
  public boolean hasContraindications(Contraindication.Type type);

  /**
   * Finds the current ParticipantTubeRegistration and return it, it will create a new one if there is no
   * TubeRegistration for current Interview.
   * 
   * @return
   */
  public ParticipantTubeRegistration getParticipantTubeRegistration();

  /**
   * Persists current {@link ParticipantTubeRegistration}.
   */
  public void persistParticipantTubeRegistration();

  /**
   * Returns the selected contraindication or null if none is set.
   * @return
   */
  public Contraindication getContraindication();

  /**
   * Create the current {@link ParticipantTubeRegistration} and persist it.
   * @param participant
   * @return
   */
  public ParticipantTubeRegistration start(Participant participant);

  /**
   * Set the end date to the current {@link ParticipantTubeRegistration}.
   */
  public void end();

  /**
   * Deletes the current {@link ParticipantTubeRegistration} and all associated objects.
   */
  public void deleteParticipantTubeRegistration();
}
