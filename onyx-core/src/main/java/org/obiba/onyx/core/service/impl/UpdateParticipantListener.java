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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Interview;
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

  private static final Logger appointmentListUpdatelog = LoggerFactory.getLogger("appointmentListUpdate");

  private static final Logger log = LoggerFactory.getLogger(UpdateParticipantListener.class);

  private ParticipantService participantService;

  private String siteCode;

  private User updateIssuedBy;

  private List<Participant> updatedParticipants = new LinkedList<Participant>();

  private List<Participant> createdParticipants = new LinkedList<Participant>();

  private List<Participant> ignoredParticipants = new LinkedList<Participant>();

  private List<Participant> erroneousParticipants = new LinkedList<Participant>();

  public UpdateParticipantListener(String siteCode, User updateIssuedBy, ParticipantService participantService) {
    super();
    this.siteCode = siteCode;
    this.updateIssuedBy = updateIssuedBy;
    this.participantService = participantService;
  }

  public void onParticipantRead(int line, Participant participant) throws ValidationRuntimeException {
    log.debug("reading participant={} {}", participant.getEnrollmentId(), participant.getFullName());

    String lineStr = (line != 0) ? "Line " + line + ": " : "";

    if(!getSiteCode().equals(participant.getSiteNo())) {
      // note and ignore
      appointmentListUpdatelog.warn("{}Ignoring participant for site {}.", lineStr, participant.getSiteNo());
      addIgnoredParticipant(participant);
    } else {
      Participant persistedParticipant = new Participant();
      persistedParticipant.setEnrollmentId(participant.getEnrollmentId());
      persistedParticipant = participantService.getParticipant(persistedParticipant);

      if(persistedParticipant == null) {
        if(!isNewAppointmentDateValid(participant)) {
          // log a warning for appointments scheduled in the past but add them to the system anyway
          appointmentListUpdatelog.warn("{}Appointment date/time is in the past => adding appointment anyway.", lineStr);
        }

        log.debug("adding participant={}", participant.getEnrollmentId());
        participant.setExported(false);
        participantService.updateParticipant(participant);
        addCreatedParticipant(participant);

      } else if(persistedParticipant != null) {
        log.debug("persistedParticipant.interview={}", persistedParticipant.getInterview());
        Interview interview = persistedParticipant.getInterview();
        if(interview != null && (interview.getStatus() == InterviewStatus.COMPLETED || interview.getStatus() == InterviewStatus.CANCELLED)) {
          // note and ignored
          appointmentListUpdatelog.warn("{}Interview {} => participant update ignored.", lineStr, interview.getStatus().toString().toLowerCase());
          addIgnoredParticipant(participant);
        } else {
          // not completed
          if(!isNewAppointmentDateValid(participant)) {
            appointmentListUpdatelog.warn("{}Appointment date/time is in the past => participant update ignored.", lineStr);
            addIgnoredParticipant(participant);
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
              appointmentListUpdatelog.warn("{}Appointment date for participant {} same in database => participant update ignored.", lineStr, participant.getAppointment().getDate());
              addIgnoredParticipant(participant);
              return;
            }
            // Update the appointment date
            persistedParticipant.getAppointment().setDate(participant.getAppointment().getDate());
            participantService.updateParticipant(persistedParticipant);
          }

          addUpdatedParticipant(participant);
        }
      }
    }
  }

  public void onParticipantReadEnd() throws ValidationRuntimeException {
    appointmentListUpdatelog.info("Update processed by: {}", updateIssuedBy.getFullName());
    appointmentListUpdatelog.info("Number of participants treated: {}", getTotalParticipants());
    appointmentListUpdatelog.info("Number of participants created: {}", getCreatedParticipants().size());
    appointmentListUpdatelog.info("Number of participants updated: {}", getUpdatedParticipants().size());
    appointmentListUpdatelog.info("Number of ignored participants: {}", getIgnoredParticipants().size());
    appointmentListUpdatelog.info("Number of erroneous participants: {}", getErroneousParticipants().size());
  }

  private boolean isNewAppointmentDateValid(Participant participant) {
    Date newAppointmentDate = participant.getAppointment().getDate();
    if(newAppointmentDate != null && newAppointmentDate.compareTo(new Date()) < 0) return false;
    return true;

  }

  public int getTotalParticipants() {
    int totalParticipants = getUpdatedParticipants().size() + getCreatedParticipants().size() + getIgnoredParticipants().size() + getErroneousParticipants().size();
    return totalParticipants;
  }

  public List<Participant> getUpdatedParticipants() {
    return updatedParticipants;
  }

  public void addUpdatedParticipant(Participant updatedParticipant) {
    getUpdatedParticipants().add(updatedParticipant);
  }

  public List<Participant> getCreatedParticipants() {
    return createdParticipants;
  }

  public void addCreatedParticipant(Participant createdParticipant) {
    getCreatedParticipants().add(createdParticipant);
  }

  public List<Participant> getErroneousParticipants() {
    return erroneousParticipants;
  }

  public void addErroneousParticipant(Participant erroneousParticipant) {
    getErroneousParticipants().add(erroneousParticipant);
  }

  public List<Participant> getIgnoredParticipants() {
    return ignoredParticipants;
  }

  public void addIgnoredParticipant(Participant participant) {
    getIgnoredParticipants().add(participant);
  }

  public String getSiteCode() {
    return siteCode;
  }

}
