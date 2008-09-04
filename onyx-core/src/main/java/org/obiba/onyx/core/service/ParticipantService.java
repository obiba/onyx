package org.obiba.onyx.core.service;

import java.util.Date;
import java.util.List;

import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;

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
    
  public List<Participant> getParticipants(String barcode, String likeName, PagingClause paging, SortingClause... clauses);
  
  public int countParticipants(String barcode, String likeName);
  
  public List<Participant> getParticipants(String barcode, String likeName, InterviewStatus status, PagingClause paging, SortingClause... clauses);
  
  public int countParticipants(String barcode, String likeName, InterviewStatus status);
  
  public List<Participant> getParticipants(String barcode, String likeName, Date from, Date to, PagingClause paging, SortingClause... clauses);
  
  public int countParticipants(String barcode, String likeName, Date from, Date to);

  public void assignCodeToParticipant(Participant participant, String barcode, String receptionComment);
  
  public void updateParticipant(Participant participant);
}
