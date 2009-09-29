/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.participant;

public enum InterviewStatus {
  /** Interview has begun, but is not yet complete. */
  IN_PROGRESS,
  /** Participant left before completing interview. */
  CLOSED,
  /** The participant has withdrawn from the study. */
  CANCELLED,
  /** Interview complete. Conclusion executed. */
  COMPLETED
}
