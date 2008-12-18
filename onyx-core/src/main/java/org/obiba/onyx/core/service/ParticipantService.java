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

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;

public interface ParticipantService {

  /**
   * 
   * @param code participant code or appointment code
   * @param paging
   * @param clauses
   * @return
   */
  public List<Participant> getParticipantsByCode(String code, PagingClause paging, SortingClause... clauses);

  /**
   * 
   * @param code participant code or appointment code
   * @return
   */
  public int countParticipantsByCode(String code);

  /**
   * Get the list of participants by like-comparing the first and last name to the given name.
   * @param likeName
   * @param paging
   * @param clauses
   * @return
   */
  public List<Participant> getParticipantsByName(String likeName, PagingClause paging, SortingClause... clauses);

  /**
   * Count the participants by like-comparing the first and last name to the given name.
   * @param likeName
   * @return
   */
  public int countParticipantsByName(String likeName);

  /**
   * Get the list of participants by their interview status.
   * @param status
   * @param paging
   * @param clauses
   * @return
   */
  public List<Participant> getParticipants(InterviewStatus status, PagingClause paging, SortingClause... clauses);

  /**
   * Count the participants by their interview status.
   * @param status
   * @return
   */
  public int countParticipants(InterviewStatus status);

  /**
   * Get the list of participants having an appointment between the given dates.
   * @param from
   * @param to
   * @param paging
   * @param clauses
   * @return
   */
  public List<Participant> getParticipants(Date from, Date to, PagingClause paging, SortingClause... clauses);

  /**
   * Count the participants having an appointment between the given dates.
   * @param from
   * @param to
   * @return
   */
  public int countParticipants(Date from, Date to);

  /**
   * Assign a barcode to a participant.
   * @param participant
   * @param barcode
   * @param receptionComment
   * @param user
   */
  public void assignCodeToParticipant(Participant participant, String barcode, String receptionComment, User user);

  /**
   * Save the participant, its appointment and metadata.
   * @param participant
   */
  public void updateParticipant(Participant participant);

  /**
   * Look in the special directory the participant list and add/update the participant/appointment list.
   */
  public void updateParticipantList(User user) throws ValidationRuntimeException;

  /**
   * Update participants and their appointment from the given participants file.
   * @param participantsList
   */
  public void updateParticipants(InputStream participantsListStream) throws ValidationRuntimeException;

}
