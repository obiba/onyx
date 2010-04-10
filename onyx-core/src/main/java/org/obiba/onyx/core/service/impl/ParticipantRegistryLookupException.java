/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantRegistry;

/**
 * An error occurred while looking up a {@link Participant} in the {@link ParticipantRegistry}.
 */
public class ParticipantRegistryLookupException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  ParticipantRegistryLookupException() {
    super();
  }

  ParticipantRegistryLookupException(String message) {
    super(message);
  }

  ParticipantRegistryLookupException(Throwable cause) {
    super(cause);
  }

}
