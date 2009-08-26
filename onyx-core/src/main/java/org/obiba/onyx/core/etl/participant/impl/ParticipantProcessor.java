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

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.Validatable;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

/**
 * ParticipantProcessor is responsible for validating a participant item. It is also responsible for transforming the
 * Participant if necessary.
 */
public class ParticipantProcessor implements ItemProcessor<Participant, Participant> {

  protected static final Logger appointmentListUpdatelog = LoggerFactory.getLogger("appointmentListUpdate");

  private ApplicationConfigurationService applicationConfigurationService;

  private ParticipantService participantService;

  private ParticipantMetadata participantMetadata;

  private Set<String> enrollmentIds;

  private String idStr;

  public ParticipantProcessor() {
    enrollmentIds = new HashSet<String>();
  }

  public Participant process(Participant participantItem) throws Exception {
    setIdStr("Participant " + participantItem.getEnrollmentId() + " : ");

    // check enrollment id is unique in submitted participants list
    if(!checkUniqueEnrollmentId(participantItem)) {
      appointmentListUpdatelog.error("Duplicate {} {}.", AbstractParticipantReader.ENROLLMENT_ID_ATTRIBUTE_NAME, participantItem.getEnrollmentId());
      return null;
    }

    // validation loop for essential attributes
    for(ParticipantAttribute attribute : participantMetadata.getEssentialAttributes()) {
      Data dataToValidate = getDataToValidate(attribute, participantItem);
      if(!validateData(attribute, dataToValidate)) return null;
    }

    // validation loop for configurable attributes
    for(ParticipantAttribute attribute : participantMetadata.getConfiguredAttributes()) {
      if(!validateData(attribute, participantItem.getConfiguredAttributeValue(attribute.getName()))) return null;
    }

    return validateParticipant(participantItem);
  }

  private Participant validateParticipant(Participant participantItem) {
    Participant participant = null;

    // Validate site code
    if(!applicationConfigurationService.getApplicationConfiguration().getSiteNo().equals(participantItem.getSiteNo())) {
      appointmentListUpdatelog.warn("{}Ignoring participant for site {}.", idStr, participantItem.getSiteNo());

    } else {
      participant = new Participant();
      participant.setEnrollmentId(participantItem.getEnrollmentId());
      participant = participantService.getParticipant(participant);

      // Participant does not exist in database yet
      if(participant == null) {
        participant = participantItem;

        // log a warning for appointments scheduled in the past but add them to the system anyway
        if(!isNewAppointmentDateValid(participant)) {
          appointmentListUpdatelog.warn("{}Appointment date/time is in the past => adding appointment anyway.", idStr);
        }

        participant.setExported(false);

        // Participant exists already in database
      } else if(participant != null) {
        Interview interview = participant.getInterview();

        // Validate interview status
        if(interview != null && (interview.getStatus() == InterviewStatus.COMPLETED || interview.getStatus() == InterviewStatus.CANCELLED)) {
          appointmentListUpdatelog.warn("{}Interview {} => participant update ignored.", idStr, interview.getStatus().toString().toLowerCase());
        } else {

          // Validate appointment date
          if(!isNewAppointmentDateValid(participantItem)) {
            appointmentListUpdatelog.warn("{}Appointment date/time is in the past => participant update ignored.", idStr);
            return null;
          }

          Appointment appointment = participant.getAppointment();
          if(appointment == null) {
            // Use appointment obtained from the list
            participant.setAppointment(participantItem.getAppointment());
          } else {
            // appointment in database exists
            if(participantItem.getAppointment().getDate().equals(participant.getAppointment().getDate())) {
              appointmentListUpdatelog.warn("{}Appointment date for participant {} same in database => participant update ignored.", idStr, participantItem.getAppointment().getDate());
              return null;
            }
            participant.getAppointment().setDate(participantItem.getAppointment().getDate());
          }
        }
      }
    }

    return participant;
  }

  /**
   * Gets the essential attributes and transform them to Data so the validation can be done
   * @param attribute
   * @param participant
   * @return
   */
  private Data getDataToValidate(ParticipantAttribute attribute, Participant participant) {
    Data data = null;

    if(attribute.getName().equals(AbstractParticipantReader.ENROLLMENT_ID_ATTRIBUTE_NAME)) data = DataBuilder.buildText(participant.getEnrollmentId());
    if(attribute.getName().equals(AbstractParticipantReader.ASSESSMENT_CENTER_ID_ATTRIBUTE_NAME)) return DataBuilder.buildText(participant.getSiteNo());
    if(attribute.getName().equals(AbstractParticipantReader.FIRST_NAME_ATTRIBUTE_NAME)) return DataBuilder.buildText(participant.getFirstName());
    if(attribute.getName().equals(AbstractParticipantReader.LAST_NAME_ATTRIBUTE_NAME)) return DataBuilder.buildText(participant.getLastName());
    if(attribute.getName().equals(AbstractParticipantReader.BIRTH_DATE_ATTRIBUTE_NAME)) return DataBuilder.buildDate(participant.getBirthDate());
    if(attribute.getName().equals(AbstractParticipantReader.GENDER_ATTRIBUTE_NAME)) return DataBuilder.buildText(String.valueOf(participant.getGender()));
    if(attribute.getName().equals(AbstractParticipantReader.APPOINTMENT_TIME_ATTRIBUTE_NAME)) return DataBuilder.buildDate(participant.getAppointment().getDate());

    return data;
  }

  private boolean validateData(ParticipantAttribute attribute, Data data) {

    // check if the attribute is mandatory and not null
    if(!checkMandatoryCondition(attribute, data)) {
      appointmentListUpdatelog.error("{}No value for mandatory field {}.", idStr, attribute.getName());
      return false;
    }

    // For TEXT-type attributes, if the attribute has a list of allowed values, validate that the value
    // is within that list.
    if(attribute.getType().equals(DataType.TEXT) && data != null) {
      if(!checkValueAllowed(attribute, data)) {
        appointmentListUpdatelog.error("{}Value not allowed for field {}", idStr, attribute.getName() + ": " + data.getValueAsString());
        return false;
      }
    }

    // For non-null attribute values, execute the attribute's validators.
    if(data != null && data.getValue() != null) {
      Validatable validatableData = new Validatable(data);

      for(IValidator validator : attribute.getValidators()) {
        validator.validate(validatableData);
      }

      // In case of any errors, substitute null.
      if(!validatableData.getErrors().isEmpty()) {
        data.setValue(null);
      }
    }

    return true;
  }

  private boolean checkMandatoryCondition(ParticipantAttribute attribute, Data attributeValue) {
    if(attribute.isMandatoryAtEnrollment()) {
      if(attributeValue == null || attributeValue.getValue() == null) {
        return false;
      }
    }
    return true;
  }

  private boolean checkValueAllowed(ParticipantAttribute attribute, Data data) {
    Set<String> allowedValues = attribute.getAllowedValues();

    if(!allowedValues.isEmpty()) {
      String textValue = data.getValue();

      Iterator<String> iter = allowedValues.iterator();
      String matchingValue = null;

      while(iter.hasNext()) {
        String s = iter.next();
        if(s.equalsIgnoreCase(textValue)) {
          matchingValue = s;
          data.setValue(matchingValue);
          break;
        }
      }

      if(matchingValue == null) {
        if(attribute.isMandatoryAtEnrollment()) {
          return false;
        } else {
          data.setValue("");
        }
      }
    }

    return true;
  }

  private boolean checkUniqueEnrollmentId(Participant participant) {
    if(enrollmentIds.contains(participant.getEnrollmentId())) {
      return false;
    }

    enrollmentIds.add(participant.getEnrollmentId());
    return true;
  }

  private boolean isNewAppointmentDateValid(Participant participant) {
    Date newAppointmentDate = participant.getAppointment().getDate();
    if(newAppointmentDate != null && newAppointmentDate.compareTo(new Date()) < 0) return false;
    return true;

  }

  public void setParticipantMetadata(ParticipantMetadata participantMetadata) {
    this.participantMetadata = participantMetadata;
  }

  public void setIdStr(String idStr) {
    this.idStr = idStr;
  }

  public void setApplicationConfigurationService(ApplicationConfigurationService applicationConfigurationService) {
    this.applicationConfigurationService = applicationConfigurationService;
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

}
