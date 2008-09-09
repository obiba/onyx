package org.obiba.onyx.core.service;

import java.util.Date;
import java.util.List;

import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;

public interface ParticipantService {

  /**
   * Persist the new participant from the given model.
   * @param model
   * @return
   */
  public Participant createParticipant(Participant model);

  /**
   * Add an appointment to participant.
   * @param participant
   * @param date
   * @return
   */
  public Appointment addAppointment(Participant participant, Date date);

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
  
  public List<Participant> getParticipantsByLastName(String likeName, PagingClause paging, SortingClause... clauses);
  
  public int countParticipantsByLastName(String likeName);

  public List<Participant> getParticipants(InterviewStatus status, PagingClause paging, SortingClause... clauses);

  public int countParticipants(InterviewStatus status);

  public List<Participant> getParticipants(Date from, Date to, PagingClause paging, SortingClause... clauses);

  public int countParticipants(Date from, Date to);

  public void assignCodeToParticipant(Participant participant, String barcode, String receptionComment, User user);

  public void updateParticipant(Participant participant);
}
