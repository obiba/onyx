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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttributeValue;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.etl.participant.IParticipantReadListener;
import org.obiba.onyx.core.etl.participant.IParticipantReader;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation (non hibernate specific) of Participant Service
 * @see#ParticipantServiceHibernateImpl.
 * @author Yannick Marcon
 * 
 */
@Transactional
public abstract class DefaultParticipantServiceImpl extends PersistenceManagerAwareService implements ParticipantService {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultParticipantServiceImpl.class);

  @SuppressWarnings("unused")
  private static final Logger appointmentListUpdatelog = LoggerFactory.getLogger("appointmentListUpdate");

  private IParticipantReader participantReader;

  public void setParticipantReader(IParticipantReader participantReader) {
    this.participantReader = participantReader;
  }

  public void assignCodeToParticipant(Participant participant, String barcode, String receptionComment, User user) {
    participant.setBarcode(barcode);
    getPersistenceManager().save(participant);

    //
    // Create an interview for the participant, in the IN_PROGRESS state.
    //
    // TODO: Revisit whether the interview should be in the IN_PROGRESS state
    // or instead in the NOT_STARTED state a this point.
    //
    Interview interview = new Interview();
    interview.setParticipant(participant);
    interview.setStartDate(new Date());
    interview.setStatus(InterviewStatus.IN_PROGRESS);
    getPersistenceManager().save(interview);

    // Persist the reception comment, if there is one.
    if(receptionComment != null && receptionComment.trim().length() != 0) {
      Action receptionCommentAction = new Action();
      receptionCommentAction.setActionType(ActionType.COMMENT);
      receptionCommentAction.setDateTime(new Date());
      receptionCommentAction.setComment(receptionComment);
      receptionCommentAction.setUser(user);
      receptionCommentAction.setInterview(interview);
      getPersistenceManager().save(receptionCommentAction);
    }
  }

  public void updateParticipant(Participant participant) {
    getPersistenceManager().save(participant);
    getPersistenceManager().save(participant.getAppointment());
    for(ParticipantAttributeValue configuredAttribute : participant.getConfiguredAttributeValues()) {
      getPersistenceManager().save(configuredAttribute);
    }
  }

  public void updateParticipantList(User user) throws ValidationRuntimeException {

    appointmentListUpdatelog.info("Start updating appointments - by {}", user.getFullName());

    ApplicationConfiguration appConfig = new ApplicationConfiguration();
    appConfig = getPersistenceManager().matchOne(appConfig);
    if(appConfig.getParticipantDirectoryPath() == null) {
      appointmentListUpdatelog.error("Abort updating appointments: " + "No participants list repository");

      ValidationRuntimeException vex = new ValidationRuntimeException();
      vex.reject("NoParticipantsListRepository", "No participants list repository.");
      throw vex;
    }

    File dir = new File(appConfig.getParticipantDirectoryPath());
    log.debug("participantDirectory={}", dir.getAbsolutePath());
    File lastModifiedFile = null;
    if(dir.exists() && dir.isDirectory()) {
      File[] dirContent = dir.listFiles();
      if(dirContent != null) {
        for(File f : dirContent) {
          if(f.isFile() && (lastModifiedFile == null || (f.lastModified() > lastModifiedFile.lastModified()))) {
            lastModifiedFile = f;
          }
        }
      }
    } else {
      log.error("Participants update list repository does not exists or is not a directory: {}", dir.getAbsolutePath());
      appointmentListUpdatelog.error("Abort updating appointments: " + "Participants update list repository does not exists or is not a directory (" + dir.getAbsolutePath() + ")");
    }

    if(lastModifiedFile == null) {
      appointmentListUpdatelog.error("Abort updating appointments: No participants list file found in: " + appConfig.getParticipantDirectoryPath());

      ValidationRuntimeException vex = new ValidationRuntimeException();
      vex.reject("NoParticipantsListFileFound", new String[] { appConfig.getParticipantDirectoryPath() }, "No participants list file found in: " + appConfig.getParticipantDirectoryPath());
      throw vex;
    }

    log.info("participantFile={}", lastModifiedFile.getAbsolutePath());
    appointmentListUpdatelog.info("Participant File: {}", lastModifiedFile.getAbsolutePath());

    try {
      updateParticipants(new FileInputStream(lastModifiedFile));
      appointmentListUpdatelog.info("End updating appointments");
    } catch(FileNotFoundException e) {
      // should not happen cause we found it by exploring the directory
      appointmentListUpdatelog.error("Abort updating appointments: No participants list file found in: " + appConfig.getParticipantDirectoryPath());

      ValidationRuntimeException vex = new ValidationRuntimeException();
      vex.reject("NoParticipantsListFileFound", new String[] { appConfig.getParticipantDirectoryPath() }, "No participants list file found in: " + appConfig.getParticipantDirectoryPath());
      throw vex;
    } catch(IllegalArgumentException e) {
      appointmentListUpdatelog.error("Abort updating appointments: Validation error - " + e.getMessage());

      ValidationRuntimeException vex = new ValidationRuntimeException();
      vex.reject("ParticipantsListFileValidationError", new String[] { e.getMessage() }, "Validation error in file: " + e.getMessage());
      throw vex;
    }
  }

  public void updateParticipants(InputStream participantsListStream) throws ValidationRuntimeException {
    final ApplicationConfiguration appConfig = getPersistenceManager().matchOne(new ApplicationConfiguration());

    final ValidationRuntimeException vex = new ValidationRuntimeException();

    IParticipantReadListener listener = null;

    participantReader.addParticipantReadListener(listener = new IParticipantReadListener() {

      public void onParticipantRead(int line, Participant participant) throws ValidationRuntimeException {
        log.debug("reading participant={} {}", participant.getEnrollmentId(), participant.getFullName());
        if(!appConfig.getSiteNo().equals(participant.getSiteNo())) {
          // note and ignore
          appointmentListUpdatelog.warn("Line {}: Wrong Participant Site Name", line);
        } else {
          Participant persistedParticipant = new Participant();
          persistedParticipant.setEnrollmentId(participant.getEnrollmentId());
          persistedParticipant = getPersistenceManager().matchOne(persistedParticipant);

          if(persistedParticipant == null && isNewAppointmentDateValid(participant, line) == true) {
            log.debug("adding participant={}", participant.getEnrollmentId());
            getPersistenceManager().save(participant);
            getPersistenceManager().save(participant.getAppointment());

            for(ParticipantAttributeValue configuredAttribute : participant.getConfiguredAttributeValues()) {
              getPersistenceManager().save(configuredAttribute);
            }

          } else if(persistedParticipant != null) {
            log.debug("persistedParticipant.interview={}", persistedParticipant.getInterview());
            if(persistedParticipant.getInterview() != null && persistedParticipant.getInterview().getStatus().equals(InterviewStatus.COMPLETED)) {
              // note and ignored
              appointmentListUpdatelog.warn("Line {}: Interview completed => participant update ignored", line);
            } else {
              // not completed
              if(isNewAppointmentDateValid(participant, line) == false) return;

              // update its appointment date
              Appointment appointment = persistedParticipant.getAppointment();
              if(persistedParticipant.getAppointment() == null) {
                log.debug("adding participant.appointment={}", participant.getEnrollmentId());
                appointment = participant.getAppointment();
                appointment.setParticipant(persistedParticipant);
                getPersistenceManager().save(appointment);
              } else {

                // appointment in database exists
                if(participant.getAppointment().getDate().equals(persistedParticipant.getAppointment().getDate())) {
                  appointmentListUpdatelog.warn("Line {}: Appointment date for participant {} same in database => participant update ignored", line, participant.getAppointment().getDate());
                  return;
                }

                persistedParticipant.getAppointment().setDate(participant.getAppointment().getDate());
                getPersistenceManager().save(persistedParticipant.getAppointment());

              }
            }
          }
        }
      }

      public void onParticipantReadEnd(int line) throws ValidationRuntimeException {
        if(vex.getAllObjectErrors().size() > 0) throw vex;
      }

      private boolean isNewAppointmentDateValid(Participant participant, Integer line) throws ValidationRuntimeException {
        Date newAppointmentDate = participant.getAppointment().getDate();
        if(newAppointmentDate != null && newAppointmentDate.compareTo(new Date()) < 0) {
          SimpleDateFormat formater = new SimpleDateFormat("yyyy/MM/dd HH:mm");
          appointmentListUpdatelog.error("Abort updating appointments: Error at line {}: Appointment date {} is passed", line, formater.format(participant.getAppointment().getDate()));
          vex.reject(participant, "AppointmentDatePassed", new String[] { Integer.toString(line), participant.getEnrollmentId(), formater.format(participant.getAppointment().getDate()) }, "Participant's appointment date is passed.");
          if(vex.getAllObjectErrors().size() == 10) {
            throw vex;
          }
          return false;
        } else
          return true;
      }
    });

    // do some clean up before updating
    for(Participant p : getNotReceivedParticipants()) {
      if(p.getAppointment() != null) {
        log.debug("removing appointment.appointmentCode={}", p.getAppointment().getAppointmentCode());
        getPersistenceManager().delete(p.getAppointment());
      }

      for(ParticipantAttributeValue configuredAttribute : p.getConfiguredAttributeValues()) {
        getPersistenceManager().delete(configuredAttribute);
      }

      log.debug("removing participant.enrollmentId={}", p.getEnrollmentId());
      getPersistenceManager().delete(p);
    }

    try {
      participantReader.process(participantsListStream);
      if(listener != null) participantReader.removeParticipantReadListener(listener);
    } catch(IOException e) {
      log.error("Update participants list read failed.", e);
      throw new ValidationRuntimeException("UpdateParticipantsListReadFailed", "Update participants list read failed.");
    }
  }

  /**
   * DB access layer specific implementation for getting {@link Participant} that have not been received.
   * @return
   */
  protected abstract List<Participant> getNotReceivedParticipants();
}
