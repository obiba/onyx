/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant;

import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.participant.Participant;

public interface IParticipantReadListener {
  
  /**
   * Called when a {@link Participant} is read at line.
   * @param line
   * @param participant
   */
  public void onParticipantRead(int line, Participant participant) throws ValidationRuntimeException;
  
  /**
   * Called after last participant was read, at the end of the stream, recalling the last line.
   * @param line
   * @throws ValidationRuntimeException
   */
  public void onParticipantReadEnd(int line) throws ValidationRuntimeException;
  
}
