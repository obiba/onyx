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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Participant extends AbstractEntity {
  //
  // Constants
  //

  private static final long serialVersionUID = 7720576329990574921L;

  public static final Map<String, String> essentialAttributeToFieldNameMap = new HashMap<String, String>() {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    {
      put(ParticipantMetadata.ENROLLMENT_ID_ATTRIBUTE_NAME, "enrollmentId");
      put(ParticipantMetadata.ASSESSMENT_CENTER_ID_ATTRIBUTE_NAME, "siteNo");
      put(ParticipantMetadata.FIRST_NAME_ATTRIBUTE_NAME, "firstName");
      put(ParticipantMetadata.LAST_NAME_ATTRIBUTE_NAME, "lastName");
      put(ParticipantMetadata.BIRTH_DATE_ATTRIBUTE_NAME, "birthDate");
      put(ParticipantMetadata.GENDER_ATTRIBUTE_NAME, "gender");
      put(ParticipantMetadata.PARTICIPANT_ID, "barcode");
      put(ParticipantMetadata.APPOINTMENT_TIME_ATTRIBUTE_NAME, "appointmentTime");
    }
  };

  //
  // Instance Variables
  //

  @Column(length = 250, nullable = false)
  private String firstName;

  @Column(length = 250, nullable = false)
  private String lastName;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Temporal(TemporalType.DATE)
  private Date birthDate;

  // ONYX-672:
  // Removed uniqueness constraints on nullable columns (barcode, enrollmentId), for portability. Different databases
  // (even different MySQL storage engines) treat null values differently, when enforcing these types of constraints.
  @Column(length = 250)
  private String barcode;

  private String enrollmentId;

  @Column(nullable = false)
  private String siteNo;

  @Enumerated(EnumType.STRING)
  private RecruitmentType recruitmentType;

  /**
   * List of values of configured participant attributes.
   */
  @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
  private List<ParticipantAttributeValue> configuredAttributeValues;

  @OneToOne(mappedBy = "participant", cascade = CascadeType.ALL)
  private Appointment appointment;

  @OneToOne(mappedBy = "participant", cascade = CascadeType.ALL)
  private Interview interview;

  //
  // Methods
  //

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public Data getFirstNameAsData() {
    return DataBuilder.buildText(getFirstName());
  }

  public void setFirstNameAsData(Data firstName) {
    setFirstName(firstName != null ? (String) firstName.getValue() : null);
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Data getLastNameAsData() {
    return DataBuilder.buildText(getLastName());
  }

  public void setLastNameAsData(Data lastName) {
    setLastName(lastName != null ? (String) lastName.getValue() : null);
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

  public Data getGenderAsData() {
    return DataBuilder.buildText(getGender() != null ? getGender().name() : null);
  }

  public void setGenderAsData(Data gender) {
    setGender(gender.getValue() != null ? Gender.valueOf((String) gender.getValue()) : null);
  }

  public Date getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(Date birthDate) {
    this.birthDate = birthDate;
  }

  public Data getBirthDateAsData() {
    return DataBuilder.buildDate(getBirthDate());
  }

  public void setBirthDateAsData(Data birthDate) {
    setBirthDate(birthDate != null ? (Date) birthDate.getValue() : null);
  }

  @Transient
  public Long getAge() {
    if(birthDate == null) return 0l;
    Calendar todayCal = Calendar.getInstance();
    Calendar birthCal = Calendar.getInstance();

    birthCal.setTime(birthDate);
    Long age = todayCal.getTimeInMillis() - birthCal.getTimeInMillis();
    Double ageDouble = SI.MILLI(SI.SECOND).getConverterTo(NonSI.YEAR).convert(Double.valueOf(age.toString()));
    ageDouble = Math.floor(ageDouble);
    age = Math.round(ageDouble);

    return (age);
  }

  @Transient
  public long getBirthYear() {
    if(birthDate == null) return 0;
    Calendar cal = Calendar.getInstance();
    cal.setTime(birthDate);
    return cal.get(Calendar.YEAR);
  }

  @Transient
  public long getBirthMonth() {
    if(birthDate == null) return 0;
    Calendar cal = Calendar.getInstance();
    cal.setTime(birthDate);
    return cal.get(Calendar.MONTH);
  }

  @Transient
  public long getBirthDay() {
    if(birthDate == null) return 0;
    Calendar cal = Calendar.getInstance();
    cal.setTime(birthDate);
    return cal.get(Calendar.DAY_OF_MONTH);
  }

  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  public Data getBarcodeAsData() {
    return DataBuilder.buildText(getBarcode());
  }

  public void setBarcodeAsData(Data barcode) {
    setBarcode(barcode != null ? (String) barcode.getValue() : null);
  }

  public String getEnrollmentId() {
    return enrollmentId;
  }

  public void setEnrollmentId(String enrollmentId) {
    this.enrollmentId = enrollmentId;
  }

  public Data getEnrollmentIdAsData() {
    return DataBuilder.buildText(getEnrollmentId());
  }

  public void setEnrollmentIdAsData(Data enrollmentId) {
    setEnrollmentId(enrollmentId != null ? (String) enrollmentId.getValue() : null);
  }

  public String getSiteNo() {
    return siteNo;
  }

  public void setSiteNo(String siteNo) {
    this.siteNo = siteNo;
  }

  public Data getSiteNoAsData() {
    return DataBuilder.buildText(getSiteNo());
  }

  public void setSiteNoAsData(Data siteNo) {
    setSiteNo(siteNo != null ? (String) siteNo.getValue() : null);
  }

  public Appointment getAppointment() {
    return appointment;
  }

  public void setAppointment(Appointment appointment) {
    this.appointment = appointment;
    this.appointment.setParticipant(this);
  }

  public Data getAppointmentTimeAsData() {
    if(getAppointment() != null) {
      return DataBuilder.buildDate(getAppointment().getDate());
    }
    return null;
  }

  public void setAppointmentTimeAsData(Data date) {
    if(getAppointment() != null) {
      getAppointment().setDate(date != null ? ((Date) date.getValue()) : null);
    } else {
      throw new RuntimeException("Cannot set the Appointment time because no Appointment exist for the Participant");
    }
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
    Assert.notNull("Null attribute name", attributeName);

    for(ParticipantAttributeValue attributeValue : getConfiguredAttributeValues()) {
      if(attributeValue.getAttributeName().equals(attributeName)) {
        return attributeValue;
      }
    }

    return null;
  }

  /**
   * Returns the value of an essential participant attribute.
   * @param attributeName The attribute name.
   * @return The value of the specified attribute (or <code>null</code> if none assigned).
   * @throws IllegalArgumentException if <code>attributeName</code> is <code>null</code>
   */
  public Data getEssentialAttributeValue(String attributeName) {
    Assert.notNull("Null attribute name", attributeName);

    BeanWrapper participantBean = new BeanWrapperImpl(this);
    String essentialAttributeFieldName = getEssentialAttributeDataFieldName(attributeName);
    if(essentialAttributeFieldName != null) {
      return (Data) participantBean.getPropertyValue(essentialAttributeFieldName);
    }
    return null;

  }

  public String getEssentialAttributeDataFieldName(String attributeName) {
    String fieldName = essentialAttributeToFieldNameMap.get(attributeName);
    if(fieldName != null) {
      fieldName = fieldName + "AsData";
    }

    return fieldName;
  }

  /**
   * Returns the value of a configured participant attribute.
   * 
   * @param attributeName attribute name
   * @return value of the specified attribute (or <code>null</code> if none assigned)
   * @throws IllegalArgumentException if <code>attributeName</code> is <code>null</code>
   */
  public Data getConfiguredAttributeValue(String attributeName) {
    Assert.notNull("Null attribute name", attributeName);

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
    Assert.notNull("Null attribute name", attributeName);

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
      getConfiguredAttributeValues().add(newParticipantAttributeValue(attributeName, data));
    }
  }

  /**
   * @param attributeName
   * @param data
   * @return
   */
  private ParticipantAttributeValue newParticipantAttributeValue(String attributeName, Data data) {
    ParticipantAttributeValue attributeValue = new ParticipantAttributeValue();
    attributeValue.setParticipant(this);
    attributeValue.setAttributeName(attributeName);
    attributeValue.setAttributeType(data.getType());
    attributeValue.setData(data);
    return attributeValue;
  }

  @Override
  public String toString() {
    return "Participant[" + barcode + "]";
  }

}
