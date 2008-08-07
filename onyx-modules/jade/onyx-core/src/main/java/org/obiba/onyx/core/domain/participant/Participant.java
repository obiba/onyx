package org.obiba.onyx.core.domain.participant;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;

@Entity
public class Participant extends AbstractEntity {

  private static final long serialVersionUID = 7720576329990574921L;

  private String firstName;

  private String lastName;
  
  @Enumerated(EnumType.STRING)
  private Gender gender;
  
  @Temporal(TemporalType.DATE)
  private Date birthDate;

  private String barcode;
  
  private String receptionComment;
  
  @OneToOne(mappedBy = "participant")
  private Interview interview;

  @OneToMany(mappedBy = "participant")
  private List<Appointment> appointments;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastAppointmentDate;

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public Date getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(Date birthDate) {
    this.birthDate = birthDate;
  }

  public Interview getInterview() {
    return interview;
  }

  public void setInterview(Interview interview) {
    this.interview = interview;
    this.interview.setParticipant(this);
  }

  public List<Appointment> getAppointments() {
    return appointments != null ? appointments : (appointments = new ArrayList<Appointment>());
  }

  public void addAppointment(Appointment appointment) {
    if(appointment != null) {
      getAppointments().add(appointment);
      appointment.setParticipant(this);
    }
  }

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  public Date getLastAppointmentDate() {
    return lastAppointmentDate;
  }

  public void setLastAppointmentDate(Date lastAppointmentDate) {
    this.lastAppointmentDate = lastAppointmentDate;
  }

  public String getFullName() {
    return getFirstName() + " " + getLastName();
  }

  public String getReceptionComment() {
    return receptionComment;
  }

  public void setReceptionComment(String receptionComment) {
    this.receptionComment = receptionComment;
  }

}
