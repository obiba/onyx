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

/**
 * The specified {@code Participant} was not found.
 */
public class NoSuchParticipantException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  NoSuchParticipantException() {
    super();
  }

  NoSuchParticipantException(String message) {
    super(message);
  }

}
