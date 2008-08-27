package org.obiba.onyx.core.service.impl;

import java.util.Date;

import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;

/**
 * Default implementation (non hibernate specific) of Participant Service @see#ParticipantServiceHibernateImpl.
 * @author Yannick Marcon
 *
 */
public abstract class DefaultParticipantServiceImpl extends PersistenceManagerAwareService implements ParticipantService {

  public Participant createParticipant(Participant model) {
    if (model.getId() != null)
      throw new IllegalArgumentException("Participant model must not be an already persisted entity.");
    
    return getPersistenceManager().save(model);
  }
  
  public Appointment addAppointment(Participant participant, Date date) {
    Appointment appointment = new Appointment();
    appointment.setDate(date);
    appointment.setParticipant(participant);
    appointment = getPersistenceManager().save(appointment);
    
    participant.setLastAppointmentDate(getLatestAppointment(participant).getDate());
    getPersistenceManager().save(participant);
    
    return appointment;
  }

  public Appointment getLatestAppointment(Participant participant) {
    Appointment template = new Appointment();
    template.setParticipant(participant);
    
    return getPersistenceManager().matchOne(template, SortingClause.create("date", false));
  }

  public void assignCodeToParticipant(Participant participant, String barcode, String receptionComment) {
    participant.setBarcode(barcode);
    participant.setReceptionComment(receptionComment);
    persistenceManager.save(participant);
  }

  
}
