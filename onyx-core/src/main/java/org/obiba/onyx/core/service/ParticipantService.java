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

  public List<Participant> getParticipantsByName(String likeName, PagingClause paging, SortingClause... clauses);

  public int countParticipantsByName(String likeName);

  public List<Participant> getParticipants(InterviewStatus status, PagingClause paging, SortingClause... clauses);

  public int countParticipants(InterviewStatus status);

  public List<Participant> getParticipants(Date from, Date to, PagingClause paging, SortingClause... clauses);

  public int countParticipants(Date from, Date to);

  public void assignCodeToParticipant(Participant participant, String barcode, String receptionComment, User user);

  public void updateParticipant(Participant participant);

  /**
   * Look in the special directory the participant list and add/update the participant/appointment list.
   */
  public void updateParticipantList() throws ValidationRuntimeException;

  /**
   * Update participants and their appointment from the given participants file.
   * @param participantsList
   */
  public void updateParticipants(InputStream participantsListStream) throws ValidationRuntimeException;
}
