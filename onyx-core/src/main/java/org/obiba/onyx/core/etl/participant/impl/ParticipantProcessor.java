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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.Validatable;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateLog;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;

/**
 * ParticipantProcessor is responsible for validating a participant item. It is also responsible for transforming the
 * Participant if necessary.
 */
public class ParticipantProcessor implements ItemProcessor<Participant, Participant>, StepExecutionListener {

  private static Set<String> enrollmentIds = new HashSet<String>();

  private static List<AppointmentUpdateLog> log = new ArrayList<AppointmentUpdateLog>();

  private ApplicationConfigurationService applicationConfigurationService;

  private ParticipantService participantService;

  private ParticipantMetadata participantMetadata;

  private String participantId;

  public Participant process(Participant participantItem) throws Exception {
    setParticipantId(participantItem.getEnrollmentId());

    // check enrollment id is unique in submitted participants list
    if(!checkUniqueEnrollmentId(participantItem)) {
      log.add(new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.ERROR, "Duplicate " + ParticipantMetadata.ENROLLMENT_ID_ATTRIBUTE_NAME + " " + participantId));
      return null;
    }

    // validation loop for essential attributes
    for(ParticipantAttribute attribute : participantMetadata.getEssentialAttributes()) {
      Data dataToValidate = participantItem.getEssentialAttributeValue(attribute.getName());
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
      log.add(new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.WARN, participantId, "Ignoring participant for site " + participantItem.getSiteNo() + "."));
    } else {
      participant = new Participant();
      participant.setEnrollmentId(participantItem.getEnrollmentId());
      participant = participantService.getParticipant(participant);

      // Participant does not exist in database yet
      if(participant == null) {
        participant = participantItem;

        // log a warning for appointments scheduled in the past but add them to the system anyway
        if(!isNewAppointmentDateValid(participant)) {
          log.add(new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.WARN, participantId, "Appointment date/time is in the past => adding appointment anyway."));
        }

        // Participant exists already in database
      } else {
        Interview interview = participant.getInterview();

        // Validate interview status
        if(interview != null && (interview.getStatus() == InterviewStatus.COMPLETED || interview.getStatus() == InterviewStatus.CANCELLED)) {
          log.add(new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.WARN, participantId, "Interview " + interview.getStatus().toString().toLowerCase() + " => participant update ignored."));
          return null;
        }

        // Validate appointment date
        if(!isNewAppointmentDateValid(participantItem)) {
          log.add(new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.WARN, participantId, "Appointment date/time is in the past => participant update ignored."));
          return null;
        }

        Appointment appointment = participant.getAppointment();
        if(appointment == null) {
          // Use appointment obtained from the list
          participant.setAppointment(participantItem.getAppointment());
        } else {
          // appointment in database exists
          if(participantItem.getAppointment().getDate().equals(participant.getAppointment().getDate())) {
            log.add(new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.WARN, participantId, "Appointment date for participant " + participantItem.getAppointment().getDate() + " same in database => participant update ignored."));
            return null;
          }
          participant.getAppointment().setDate(participantItem.getAppointment().getDate());
        }
      }
    }

    return participant;
  }

  private boolean validateData(ParticipantAttribute attribute, Data data) {

    // check if the attribute is mandatory and not null
    if(!checkMandatoryCondition(attribute, data)) {
      log.add(new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.ERROR, participantId, "No value for mandatory field " + attribute.getName() + "."));
      return false;
    }

    // For TEXT-type attributes, if the attribute has a list of allowed values, validate that the value
    // is within that list.
    if(attribute.getType().equals(DataType.TEXT) && data != null) {
      if(!checkValueAllowed(attribute, data)) {
        log.add(new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.ERROR, participantId, "Value not allowed for field " + attribute.getName() + ": " + data.getValueAsString() + "."));
        return false;
      }
    }

    // For non-null attribute values, execute the attribute's validators.
    if(data != null && data.getValue() != null) {
      Validatable<Data> validatableData = new Validatable<Data>(data);

      for(IValidator<Data> validator : attribute.getValidators()) {
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
        }
        data.setValue("");
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

  public static void initProcessor() {
    enrollmentIds = new HashSet<String>();
    log = new ArrayList<AppointmentUpdateLog>();
  }

  public void setParticipantMetadata(ParticipantMetadata participantMetadata) {
    this.participantMetadata = participantMetadata;
  }

  public void setParticipantId(String participantId) {
    this.participantId = participantId;
  }

  public void setApplicationConfigurationService(ApplicationConfigurationService applicationConfigurationService) {
    this.applicationConfigurationService = applicationConfigurationService;
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public ExitStatus afterStep(StepExecution stepExecution) {
    for(AppointmentUpdateLog appointmentUpdateLog : log) {
      AppointmentUpdateLog.addErrorLog(stepExecution.getExecutionContext(), appointmentUpdateLog);
    }
    return null;
  }

  public void beforeStep(StepExecution stepExecution) {
    // TODO Auto-generated method stub
  }

}
