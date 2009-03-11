/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.etl.participant.IParticipantReadListener;
import org.obiba.onyx.core.service.ParticipantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class UpdateParticipantListener implements IParticipantReadListener {

  private ParticipantService participantService;

  private String siteCode;

  private User updateIssuedBy;

  private List<Participant> updatedParticipants;

  private List<Participant> createdParticipants;

  private List<Participant> erroneousParticipants;

  private static final Logger appointmentListUpdatelog = LoggerFactory.getLogger("appointmentListUpdate");

  private static final Logger log = LoggerFactory.getLogger(UpdateParticipantListener.class);

  public UpdateParticipantListener(String siteCode, User updateIssuedBy, ParticipantService participantService) {
    super();
    this.siteCode = siteCode;
    this.updateIssuedBy = updateIssuedBy;
    this.participantService = participantService;
  }

  public void onParticipantRead(int line, Participant participant) throws ValidationRuntimeException {
    log.debug("reading participant={} {}", participant.getEnrollmentId(), participant.getFullName());

    if(!getSiteCode().equals(participant.getSiteNo())) {
      // note and ignore
      appointmentListUpdatelog.warn("Line {}: Wrong Participant Site Name", line);
      addErroneousParticipants(participant);
    } else {
      Participant persistedParticipant = new Participant();
      persistedParticipant.setEnrollmentId(participant.getEnrollmentId());
      persistedParticipant = participantService.getParticipant(persistedParticipant);

      if(persistedParticipant == null) {
        if(!isNewAppointmentDateValid(participant, line)) {
          // log a warning for appointments scheduled in the past but add them to the system anyway
          appointmentListUpdatelog.warn("Line {}: Appointment date/time is in the past => adding appointment anyway", line);
        }

        log.debug("adding participant={}", participant.getEnrollmentId());
        participant.setExported(false);
        participantService.updateParticipant(participant);
        addCreatedParticipants(participant);

      } else if(persistedParticipant != null) {
        log.debug("persistedParticipant.interview={}", persistedParticipant.getInterview());
        if(persistedParticipant.getInterview() != null && (persistedParticipant.getInterview().getStatus().equals(InterviewStatus.COMPLETED) || persistedParticipant.getInterview().getStatus().equals(InterviewStatus.CANCELLED))) {
          // note and ignored
          String message = (persistedParticipant.getInterview().getStatus().equals(InterviewStatus.COMPLETED)) ? "completed" : "cancelled";
          appointmentListUpdatelog.warn("Line {}: Interview {} => participant update ignored", line, message);
          addErroneousParticipants(participant);
        } else {
          // not completed
          if(!isNewAppointmentDateValid(participant, line)) {
            appointmentListUpdatelog.warn("Line {}: Appointment date/time is in the past => participant update ignored", line);
            addErroneousParticipants(participant);
            return;
          }

          // update its appointment date
          Appointment appointment = persistedParticipant.getAppointment();
          if(appointment == null) {
            // new appointment
            log.debug("adding participant.appointment={}", participant.getEnrollmentId());
            // Use appointment obtained from the list
            appointment = participant.getAppointment();

            persistedParticipant.setAppointment(appointment);
            participantService.updateParticipant(persistedParticipant);
          } else {
            // appointment in database exists
            if(participant.getAppointment().getDate().equals(persistedParticipant.getAppointment().getDate())) {
              appointmentListUpdatelog.warn("Line {}: Appointment date for participant {} same in database => participant update ignored", line, participant.getAppointment().getDate());
              addErroneousParticipants(participant);
              return;
            }
            // Update the appointment date
            persistedParticipant.getAppointment().setDate(participant.getAppointment().getDate());
            participantService.updateParticipant(persistedParticipant);
          }

          addUpdatedParticipants(participant);
        }
      }
    }
  }

  public void onParticipantReadEnd(int line) throws ValidationRuntimeException {
    appointmentListUpdatelog.info("Update processed by: {}", updateIssuedBy.getFullName());
    appointmentListUpdatelog.info("Number of participants treated: {}", getTotalParticipants());
    appointmentListUpdatelog.info("Number of participants created: {}", getCreatedParticipants().size());
    appointmentListUpdatelog.info("Number of participants updated: {}", getUpdatedParticipants().size());
    appointmentListUpdatelog.info("Number of erroneous participants: {}", getErroneousParticipants().size());
  }

  private boolean isNewAppointmentDateValid(Participant participant, Integer line) {
    Date newAppointmentDate = participant.getAppointment().getDate();
    if(newAppointmentDate != null && newAppointmentDate.compareTo(new Date()) < 0) return false;
    return true;

  }

  public int getTotalParticipants() {
    int totalParticipants = getUpdatedParticipants().size() + getCreatedParticipants().size() + getErroneousParticipants().size();
    return totalParticipants;
  }

  public List<Participant> getUpdatedParticipants() {
    return (updatedParticipants != null) ? updatedParticipants : new ArrayList<Participant>();
  }

  public void addUpdatedParticipants(Participant updatedParticipant) {
    getUpdatedParticipants().add(updatedParticipant);
  }

  public List<Participant> getCreatedParticipants() {
    return (createdParticipants != null) ? createdParticipants : new ArrayList<Participant>();
  }

  public void addCreatedParticipants(Participant createdParticipant) {
    getCreatedParticipants().add(createdParticipant);
  }

  public List<Participant> getErroneousParticipants() {
    return (erroneousParticipants != null) ? erroneousParticipants : new ArrayList<Participant>();
  }

  public void addErroneousParticipants(Participant erroneousParticipant) {
    getErroneousParticipants().add(erroneousParticipant);
  }

  public String getSiteCode() {
    return siteCode;
  }

}
