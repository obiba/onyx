/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.participant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.util.data.Data;

@Entity
public class Participant extends AbstractEntity {
  //
  // Constants
  //

  private static final long serialVersionUID = 7720576329990574921L;

  //
  // Instance Variables
  //

  @Column(length = 250)
  private String firstName;

  @Column(length = 250)
  private String lastName;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Temporal(TemporalType.DATE)
  private Date birthDate;

  @Column(length = 250)
  private String barcode;

  private String enrollmentId;

  private String siteNo;

  @Enumerated(EnumType.STRING)
  private RecruitmentType recruitmentType;

  /**
   * List of values of configured participant attributes.
   */
  @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
  private List<ParticipantAttributeValue> configuredAttributeValues;

  @OneToOne(mappedBy = "participant", cascade = CascadeType.ALL)
  private Appointment appointment;

  @OneToOne(mappedBy = "participant")
  private Interview interview;

  private Boolean exported;

  //
  // Methods
  //

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

  public String getFullName() {
    return getFirstName() + " " + getLastName();
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

  @Transient
  public long getBirthYear() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(birthDate);
    return (long) cal.get(Calendar.YEAR);
  }

  @Transient
  public long getBirthMonth() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(birthDate);
    return (long) cal.get(Calendar.MONTH);
  }

  @Transient
  public long getBirthDay() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(birthDate);
    return (long) cal.get(Calendar.DAY_OF_MONTH);
  }

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  public String getEnrollmentId() {
    return enrollmentId;
  }

  public void setEnrollmentId(String enrollmentId) {
    this.enrollmentId = enrollmentId;
  }

  public String getSiteNo() {
    return siteNo;
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

    if(interview != null) {
      this.interview.setParticipant(this);
    }
  }

  public void setSiteNo(String siteNo) {
    this.siteNo = siteNo;
  }

  public RecruitmentType getRecruitmentType() {
    return recruitmentType;
  }

  public void setRecruitmentType(RecruitmentType recruitmentType) {
    this.recruitmentType = recruitmentType;
  }

  /**
   * Returns the list of configured attribute values.
   * 
   * NOTE: If a configured attribute has the value <code>null</code>, it may or may not appear in this list. It
   * <i>will</i> appear in this list only if it had previously been assigned a non-<code>null</code> value.
   * 
   * @return list of configured attribute values
   */
  public List<ParticipantAttributeValue> getConfiguredAttributeValues() {
    if(configuredAttributeValues == null) {
      configuredAttributeValues = new ArrayList<ParticipantAttributeValue>();
    }

    return configuredAttributeValues;
  }

  @Transient
  /**
   * This method returns a map containing all the participant attributes. This is used by Spring to access the
   * attributes through the "javabeans" property standard.
   */
  public Map<String, Object> getAttributes() {
    Map<String, Object> attributesMap = new HashMap<String, Object>();

    for(ParticipantAttributeValue participantAttribute : getConfiguredAttributeValues()) {
      Object attributeValue = (participantAttribute.getData() != null) ? participantAttribute.getData().getValue() : null;
      attributesMap.put(participantAttribute.getAttributeName(), attributeValue);
    }
    return attributesMap;
  }

  /**
   * Updates the list of configured attribute values.
   * 
   * @param configuredAttributeValues list of configured attribute values
   */
  public void setConfiguredAttributeValues(List<ParticipantAttributeValue> configuredAttributeValues) {
    if(configuredAttributeValues != null) {
      getConfiguredAttributeValues().clear();
      getConfiguredAttributeValues().addAll(configuredAttributeValues);
    }
  }

  /**
   * Returns the ParticipantAttributeValue of a configured participant attribute.
   * 
   * @param attributeName attribute name
   * @return value of the specified attribute (or <code>null</code> if none assigned)
   * @throws IllegalArgumentException if <code>attributeName</code> is <code>null</code>
   */
  public ParticipantAttributeValue getParticipantAttributeValue(String attributeName) {
    if(attributeName == null) {
      throw new IllegalArgumentException("Null attribute name");
    }

    for(ParticipantAttributeValue attributeValue : getConfiguredAttributeValues()) {
      if(attributeValue.getAttributeName().equals(attributeName)) {
        return attributeValue;
      }
    }

    return null;
  }

  /**
   * Returns the value of a configured participant attribute.
   * 
   * @param attributeName attribute name
   * @return value of the specified attribute (or <code>null</code> if none assigned)
   * @throws IllegalArgumentException if <code>attributeName</code> is <code>null</code>
   */
  public Data getConfiguredAttributeValue(String attributeName) {
    if(attributeName == null) {
      throw new IllegalArgumentException("Null attribute name");
    }

    for(ParticipantAttributeValue attributeValue : getConfiguredAttributeValues()) {
      if(attributeValue.getAttributeName().equals(attributeName)) {
        Data data = attributeValue.getData();
        return (data != null && data.getValue() != null) ? data : null;
      }
    }

    return null;
  }

  /**
   * Updates the value of a configured participant attribute.
   * 
   * @param attributeName attribute name
   * @param data new value (or <code>null</code> to assign no value)
   * @throws IllegalArgumentException if <code>attributeName</code> is <code>null</code> that name is configured
   */
  public void setConfiguredAttributeValue(String attributeName, Data data) {
    if(attributeName == null) {
      throw new IllegalArgumentException("Null attribute name");
    }

    // If the list of configured attribute values contains a value for the specified
    // attribute, update that value.
    for(ParticipantAttributeValue attributeValue : getConfiguredAttributeValues()) {
      if(attributeValue.getAttributeName().equals(attributeName)) {
        if(data == null) {
          data = new Data(attributeValue.getAttributeType(), null);
        }
        attributeValue.setData(data);
        return;
      }
    }

    // The list of configured attribute values does NOT contain a value for the specified
    // attribute. So create a new ParticipantAttributeValue and append it to the list.
    //
    // ONYX-186: Don't add a new ParticipantAttributeValue in the case where the data argument
    // is null.
    if(data != null) {
      ParticipantAttributeValue attributeValue = new ParticipantAttributeValue();
      attributeValue.setParticipant(this);
      attributeValue.setAttributeName(attributeName);
      attributeValue.setAttributeType(data.getType());
      attributeValue.setData(data);

      getConfiguredAttributeValues().add(attributeValue);
    }
  }

  public Boolean getExported() {
    return exported;
  }

  public void setExported(Boolean exported) {
    this.exported = exported;
  }

  @Override
  public String toString() {
    return "Participant[" + barcode + "]";
  }

  /**
   * Defines if a participant and his interview are in the right states to allow the data export.
   * @return
   */
  public boolean isExportable() {
    if(exported != null && exported == true) return false;
    if(getInterview() == null) return false;
    InterviewStatus status = getInterview().getStatus();
    return (status == InterviewStatus.COMPLETED || status == InterviewStatus.CLOSED);
  }

}
