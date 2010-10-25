/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.core.etl.participant.util.XmlParticipantInput;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

/**
 * ItemReader for reading Participant items from an xml file. To use this reader: configure the participant-reader.xml
 * file under the cohort project
 */
public class XmlParticipantReader extends AbstractParticipantReader {

  /**
   * Maps attribute names to tag names.
   */
  private Map<String, String> attributeNameToTagMap;

  private Iterator<XmlParticipantInput> iterator;

  //
  // Constructor
  //
  public XmlParticipantReader() {
    columnNameToAttributeNameMap = new HashMap<String, String>();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void open(ExecutionContext context) throws ItemStreamException {
    super.open(context);

    XStream xstream = new XStream();
    initializeXstream(xstream);

    List<XmlParticipantInput> participants = new ArrayList<XmlParticipantInput>();

    try {
      participants.addAll((List<XmlParticipantInput>) xstream.fromXML(getFileInputStream()));
      iterator = participants.iterator();
    } catch(XStreamException exception) {
      throw new IllegalArgumentException("Invalid appointment list file");
    }

    setAttributeNameToTagMap();

  }

  public Participant read() throws Exception, UnexpectedInputException, ParseException {
    Participant participant = null;
    XmlParticipantInput participantInput = (iterator.hasNext()) ? iterator.next() : null;
    if(participantInput == null || participantInput.getAttributes().size() == 0 || participantInput.containsWhitespaceOnly()) return null;

    try {
      participantInput.setAttributesMap();
      participant = processParticipant(participantInput);
      participant.setAppointment(processAppointment(participantInput));
    } catch(IllegalArgumentException ex) {
      throw new IllegalArgumentException(ex.getMessage());
    }

    return participant;
  }

  @Override
  public String getFilePattern() {
    return ".xml";
  }

  //
  // Local methods
  //
  private void initializeXstream(XStream xstream) {
    xstream.setMode(XStream.ID_REFERENCES);

    xstream.alias("participants", List.class);
    xstream.alias("participant", XmlParticipantInput.class);
    xstream.alias("attribute", XmlParticipantInput.Attribute.class);

    xstream.useAttributeFor(XmlParticipantInput.Attribute.class, "key");
    xstream.useAttributeFor(XmlParticipantInput.Attribute.class, "value");

    xstream.addImplicitCollection(XmlParticipantInput.class, "attributes");
  }

  @SuppressWarnings("unchecked")
  private void setAttributeNameToTagMap() {
    attributeNameToTagMap = new HashMap<String, String>();
    if(columnNameToAttributeNameMap != null) {
      for(Entry entry : columnNameToAttributeNameMap.entrySet()) {
        attributeNameToTagMap.put(entry.getValue().toString().toUpperCase(), entry.getKey().toString());
      }
    }
  }

  private Participant processParticipant(XmlParticipantInput participantInput) {
    Participant participant = new Participant();

    setParticipantEssentialAttributes(participant, participantInput);
    setParticipantConfiguredAttributes(participant, participantInput);

    return participant;
  }

  private Appointment processAppointment(XmlParticipantInput participantInput) {
    Appointment appointment = new Appointment();
    Data data = null;

    data = getEssentialAttributeValue(ParticipantMetadata.ENROLLMENT_ID_ATTRIBUTE_NAME, participantInput.getAttributesMap().get(attributeNameToTagMap.get(ParticipantMetadata.ENROLLMENT_ID_ATTRIBUTE_NAME.toUpperCase())));
    String enrollmentId = (String) ((data != null) ? data.getValue() : null);
    appointment.setAppointmentCode(enrollmentId);

    data = getEssentialAttributeValue(ParticipantMetadata.APPOINTMENT_TIME_ATTRIBUTE_NAME, participantInput.getAttributesMap().get(attributeNameToTagMap.get(ParticipantMetadata.APPOINTMENT_TIME_ATTRIBUTE_NAME.toUpperCase())));
    Date appointmentTime = (data != null) ? (Date) data.getValue() : null;
    appointment.setDate(appointmentTime);

    return appointment;
  }

  private void setParticipantEssentialAttributes(Participant participant, XmlParticipantInput participantInput) {
    participant.setRecruitmentType(RecruitmentType.ENROLLED);

    Data data = null;

    Map<String, String> attributes = participantInput.getAttributesMap();
    data = getEssentialAttributeValue(ParticipantMetadata.ENROLLMENT_ID_ATTRIBUTE_NAME, attributes.get(attributeNameToTagMap.get(ParticipantMetadata.ENROLLMENT_ID_ATTRIBUTE_NAME.toUpperCase())));
    String enrollmentId = (String) ((data != null) ? data.getValue() : null);
    participant.setEnrollmentId(enrollmentId);

    data = getEssentialAttributeValue(ParticipantMetadata.ASSESSMENT_CENTER_ID_ATTRIBUTE_NAME, attributes.get(attributeNameToTagMap.get(ParticipantMetadata.ASSESSMENT_CENTER_ID_ATTRIBUTE_NAME.toUpperCase())));
    String assessmentCenterId = (String) ((data != null) ? data.getValue() : null);
    participant.setSiteNo(assessmentCenterId);

    data = getEssentialAttributeValue(ParticipantMetadata.FIRST_NAME_ATTRIBUTE_NAME, attributes.get(attributeNameToTagMap.get(ParticipantMetadata.FIRST_NAME_ATTRIBUTE_NAME.toUpperCase())));
    String firstName = (String) ((data != null) ? data.getValue() : null);
    participant.setFirstName(firstName);

    data = getEssentialAttributeValue(ParticipantMetadata.LAST_NAME_ATTRIBUTE_NAME, attributes.get(attributeNameToTagMap.get(ParticipantMetadata.LAST_NAME_ATTRIBUTE_NAME.toUpperCase())));
    String lastName = (String) ((data != null) ? data.getValue() : null);
    participant.setLastName(lastName);

    if(attributeNameToTagMap.containsKey(ParticipantMetadata.BIRTH_DATE_ATTRIBUTE_NAME.toUpperCase())) {
      data = getEssentialAttributeValue(ParticipantMetadata.BIRTH_DATE_ATTRIBUTE_NAME, attributes.get(attributeNameToTagMap.get(ParticipantMetadata.BIRTH_DATE_ATTRIBUTE_NAME.toUpperCase())));
      Date birthDate = (data != null) ? (Date) data.getValue() : null;
      participant.setBirthDate(birthDate);
    }

    if(attributeNameToTagMap.containsKey(ParticipantMetadata.GENDER_ATTRIBUTE_NAME.toUpperCase())) {
      data = getEssentialAttributeValue(ParticipantMetadata.GENDER_ATTRIBUTE_NAME, attributes.get(attributeNameToTagMap.get(ParticipantMetadata.GENDER_ATTRIBUTE_NAME.toUpperCase())));
      String gender = (data != null) ? data.getValueAsString() : "";
      if(gender.equals("M")) {
        participant.setGender(Gender.MALE);
      } else if(gender.equals("F")) {
        participant.setGender(Gender.FEMALE);
      } else {
        participant.setGender(null);
      }
    }
  }

  private void setParticipantConfiguredAttributes(Participant participant, XmlParticipantInput participantInput) {
    Map<String, String> attributes = participantInput.getAttributesMap();

    for(Map.Entry<String, String> attr : attributes.entrySet()) {
    }

    for(ParticipantAttribute configuredAttribute : getParticipantMetadata().getConfiguredAttributes()) {
      if(configuredAttribute.isAssignableAtEnrollment() && attributeNameToTagMap.containsKey(configuredAttribute.getName().toUpperCase())) {
        setConfiguredAttribute(participant, configuredAttribute, attributes.get(attributeNameToTagMap.get(configuredAttribute.getName().toUpperCase())));
      }
    }
  }

  private void setConfiguredAttribute(Participant participant, ParticipantAttribute attribute, String value) {
    Data data = getAttributeValue(attribute, value);
    participant.setConfiguredAttributeValue(attribute.getName(), data);
  }

  private Data getEssentialAttributeValue(String attributeName, String value) {
    ParticipantAttribute attribute = getParticipantMetadata().getEssentialAttribute(attributeName);
    Data data = getAttributeValue(attribute, value);

    return data;
  }

  /**
   * Returns the value of the participant attribute stored in the specified data cell.
   * 
   * @param attribute participant attribute
   * @param cell data cell
   * @param evaluator cell evaluator
   * @return attribute value (or <code>null</code> if none)
   * @throws IllegalArgumentException if the cell type is not compatible with the attribute type, or if the attribute is
   * mandatory but its value is <code>null</code>
   */
  @SuppressWarnings("incomplete-switch")
  private Data getAttributeValue(ParticipantAttribute attribute, String value) {
    if(value == null) return null;
    Data data = null;

    try {
      switch(attribute.getType()) {
      case DECIMAL:
        data = DataBuilder.buildDecimal(value);
        break;
      case INTEGER:
        data = DataBuilder.buildInteger(Long.valueOf(value));
        break;
      case DATE:
        data = DataBuilder.buildDate(value);
        break;
      case TEXT:
        String textValue = value;

        if(textValue.trim().length() != 0) {
          data = DataBuilder.buildText(textValue);
        }

        break;
      }
    } catch(IllegalArgumentException ex) {
      if(attribute.isMandatoryAtEnrollment()) {
        throw new IllegalArgumentException("Wrong data type value for field '" + attribute.getName() + "': " + value);
      } else {
        return null;
      }
    }

    return data;
  }

  public Iterator<XmlParticipantInput> getIterator() {
    return iterator;
  }
}
