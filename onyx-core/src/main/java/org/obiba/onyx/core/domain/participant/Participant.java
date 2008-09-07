package org.obiba.onyx.core.domain.participant;

import java.util.Calendar;
import java.util.Date;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

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

  private String street;

  private String apartment;

  private String city;

  private String province;

  private String country;

  private String postalCode;

  private String phone;

  @OneToOne(mappedBy = "participant")
  private Appointment appointment;

  @OneToOne(mappedBy = "participant")
  private Interview interview;

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

  public Appointment getAppointment() {
    return appointment;
  }

  public void setAppointment(Appointment appointment) {
    this.appointment = appointment;
    this.appointment.setParticipant(this);
  }

  public Interview getInterview() {
    return interview;
  }

  public void setInterview(Interview interview) {
    this.interview = interview;
    this.interview.setParticipant(this);
  }

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
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

  @Transient
  public Long getAge() {
    Calendar todayCal = Calendar.getInstance();
    Calendar birthCal = Calendar.getInstance();

    birthCal.setTime((Date) getBirthDate());
    Long age = todayCal.getTimeInMillis() - birthCal.getTimeInMillis();
    Double ageDouble = SI.MILLI(SI.SECOND).getConverterTo(NonSI.YEAR).convert(Double.valueOf(age.toString()));
    ageDouble = Math.floor(ageDouble);
    age = Math.round(ageDouble);

    return (age);
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getApartment() {
    return apartment;
  }

  public void setApartment(String apartment) {
    this.apartment = apartment;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

}
