package org.obiba.onyx.core.domain.participant;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;

@Entity
public class Appointment extends AbstractEntity {

  private static final long serialVersionUID = 6009569414177913575L;

  private String appointmentCode;
  
  @Temporal(TemporalType.TIMESTAMP)
  public Date date;

  @OneToOne
  @JoinColumn(name = "participant_id")
  public Participant participant;

  public Appointment() {
  }
  
  public Appointment(Participant participant, Date date) {
    this.participant = participant;
    this.date = date;
  }

  public String getAppointmentCode() {
    return appointmentCode;
  }

  public void setAppointmentCode(String appointmentCode) {
    this.appointmentCode = appointmentCode;
  }
  
  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Participant getParticipant() {
    return participant;
  }

  public void setParticipant(Participant participant) {
    this.participant = participant;
  }

}
