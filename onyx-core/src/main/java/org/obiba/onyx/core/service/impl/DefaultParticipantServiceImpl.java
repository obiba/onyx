package org.obiba.onyx.core.service.impl;

import java.util.Date;

import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;

/**
 * Default implementation (non hibernate specific) of Participant Service
 * @see#ParticipantServiceHibernateImpl.
 * @author Yannick Marcon
 * 
 */
public abstract class DefaultParticipantServiceImpl extends PersistenceManagerAwareService implements ParticipantService {

  public Participant createParticipant(Participant model) {
    if(model.getId() != null) throw new IllegalArgumentException("Participant model must not be an already persisted entity.");

    return getPersistenceManager().save(model);
  }

  public Appointment addAppointment(Participant participant, Date date) {
    Appointment appointment = new Appointment();
    appointment.setDate(date);
    appointment.setParticipant(participant);
    appointment = getPersistenceManager().save(appointment);

    getPersistenceManager().save(participant);

    return appointment;
  }

  public void assignCodeToParticipant(Participant participant, String barcode, String receptionComment) {
    participant.setBarcode(barcode);
    participant.setReceptionComment(receptionComment);
    persistenceManager.save(participant);
  }

  public void updateParticipant(Participant participant) {
    Participant p = getPersistenceManager().get(Participant.class, participant.getId());
    p.setFirstName(participant.getFirstName());
    p.setLastName(participant.getLastName());
    p.setGender(participant.getGender());
    p.setBirthDate(participant.getBirthDate());
    p.setStreet(participant.getStreet());
    p.setApartment(participant.getApartment());
    p.setCity(participant.getCity());
    p.setProvince(participant.getProvince());
    p.setCountry(participant.getCountry());
    p.setPostalCode(participant.getPostalCode());
    p.setPhone(participant.getPhone());

    persistenceManager.save(p);
  }
}