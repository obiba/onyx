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

import java.util.Date;
import java.util.List;

import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.exception.NonUniqueParticipantException;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.util.data.Data;

public interface ParticipantService {

  /**
   * search by code, appointment code or name
   * 
   * @param inputField
   * @param paging
   * @param clauses
   * @return
   */
  public List<Participant> getParticipantsByInputField(String inputField, PagingClause paging, SortingClause... clauses);

  /**
   * search by code, appointment code or name
   * 
   * @param inputField
   * @return
   */
  public int countParticipantsByInputField(String inputField);

  /**
   * Get the list of participants by their interview status.
   * 
   * @param status
   * @param paging
   * @param clauses
   * @return
   */
  public List<Participant> getParticipants(InterviewStatus status, PagingClause paging, SortingClause... clauses);

  /**
   * Count the participants by their interview status.
   * 
   * @param status
   * @return
   */
  public int countParticipants(InterviewStatus status);

  /**
   * Get the list of participants having an appointment between the given dates.
   * 
   * @param from
   * @param to
   * @param paging
   * @param clauses
   * @return
   */
  public List<Participant> getParticipants(Date from, Date to, PagingClause paging, SortingClause... clauses);

  /**
   * Count the participants having an appointment between the given dates.
   * 
   * @param from
   * @param to
   * @return
   */
  public int countParticipants(Date from, Date to);

  /**
   * Assign a barcode to a participant.
   * 
   * @param participant
   * @param barcode
   * @param receptionComment
   * @param userName
   */
  public void assignCodeToParticipant(Participant participant, String barcode, String receptionComment, String userName);

  /**
   * Save the participant, its appointment and metadata.
   * 
   * @param participant
   */
  public void updateParticipant(Participant participant);

  /**
   * Get Participant actions.
   * 
   * @param participant
   * @return
   */
  public List<Action> getActions(Participant participant);

  /**
   * Get Participant actions on a stage.
   * 
   * @param participant
   * @param stage
   * @return
   */
  public List<Action> getActions(Participant participant, String stage);

  /**
   * Get the data for the participant configured attribute.
   * 
   * @param participant
   * @param attributeName
   * @return null if no attribute with the given name
   */
  public Data getConfiguredAttributeValue(Participant participant, String attributeName);

  /**
   * Get the participant corresponding to the specified template
   * 
   * @param participant
   * @return
   */
  public Participant getParticipant(Participant participant);

  /**
   * Lookup for a Participant based on both of its unique identifiers (barcode and enrollmentId).
   * 
   * @param participantIdentifier The Participant identifier to look for.
   * @return A Participant matching the specified identifier.
   * @throws NonUniqueParticipantException If multiple Participant are found for the specified identifier (this is the
   * case where ParticipantA.barcode == PArticipantB.enrollmentId) an exception is thrown.
   */
  public Participant getParticipant(String participantIdentifier) throws NonUniqueParticipantException;

  /**
   * Delete all appointments that have not been received
   */
  public void cleanUpAppointment();

  /**
   * Deletes the participant and his data (including the data stored by each {@link Module} of Onyx)
   * 
   * @param participant Participant to delete.
   */
  public void deleteParticipant(Participant participant);

  /**
   * Returns whether the participant (identified by enrollment Id) has been purged.
   * 
   * @return
   */
  public boolean isParticipantPurged(Participant participant);

  /**
   * Returns whether the participant (identified by participant Id) has been purged.
   * 
   * @return
   */
  public boolean isInterviewPurged(Participant participant);

}
