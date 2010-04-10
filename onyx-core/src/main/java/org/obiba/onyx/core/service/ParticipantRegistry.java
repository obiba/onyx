/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.impl.NoSuchParticipantException;
import org.obiba.onyx.core.service.impl.ParticipantRegistryLookupException;

/**
 * Provides a registry of {@link Participant}s which can be retrieved using the unique id of the {@code Participant}.
 */
public interface ParticipantRegistry {

  /**
   * Returns a {@link Participant} that matches the provided uniqueId.
   * @param uniqueId The unique id of the {@code Participant} to be retrieved.
   * @return A {@code Participant} matching the provided uniqueId.
   * @throws NoSuchParticipantException if the specified {@code Participant} cannot be found.
   * @throws ParticipantRegistryLookupException if an error occurs during the lookup.
   */
  public Participant lookupParticipant(String uniqueId) throws NoSuchParticipantException, ParticipantRegistryLookupException;
}
