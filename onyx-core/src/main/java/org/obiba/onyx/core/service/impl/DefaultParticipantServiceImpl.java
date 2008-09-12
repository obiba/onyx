package org.obiba.onyx.core.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.application.AppConfiguration;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
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

  private IParticipantReader participantReader;

  private boolean participantReaderCallbackInitialized = false;

  public void setParticipantReader(IParticipantReader participantReader) {
    this.participantReader = participantReader;
  }

  public void assignCodeToParticipant(Participant participant, String barcode, String receptionComment, User user) {
    participant.setBarcode(barcode);
    persistenceManager.save(participant);

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
      persistenceManager.save(receptionCommentAction);
    }
  }

  public void updateParticipant(Participant participant) {
    persistenceManager.save(participant);
  }

  public void updateParticipantList() throws ValidationRuntimeException {
    AppConfiguration appConfig = new AppConfiguration();
    appConfig = getPersistenceManager().matchOne(appConfig);

    File dir = new File(appConfig.getParticipantDirectoryPath());
    log.info("participantDirectory={}", dir.getAbsolutePath());
    File lastModifiedFile = null;
    for(File f : dir.listFiles()) {
      if(f.isFile() && (lastModifiedFile == null || (f.lastModified() > lastModifiedFile.lastModified()))) {
        lastModifiedFile = f;
      }
    }

    if(lastModifiedFile == null) {
      throw new ValidationRuntimeException("NoParticipantsListFileFound", "No participants list file found.");
    }

    log.info("participantFile={}", lastModifiedFile.getAbsolutePath());
    try {
      updateParticipants(new FileInputStream(lastModifiedFile));
    } catch(FileNotFoundException e) {
      // should not happen cause we found it by exploring the directory
    }
  }

  public void updateParticipants(InputStream participantsListStream) throws ValidationRuntimeException {

    if(!participantReaderCallbackInitialized) {
      participantReader.addParticipantReadListener(new IParticipantReadListener() {

        public void onParticipantRead(int line, Participant participant) throws ValidationRuntimeException {
          log.debug("reading participant={} {}", participant.getEnrollmentId(), participant.getFullName());
          Participant persistedParticipant = new Participant();
          persistedParticipant.setEnrollmentId(participant.getEnrollmentId());
          persistedParticipant = getPersistenceManager().matchOne(persistedParticipant);
          if(persistedParticipant == null) {
            log.info("adding participant={}", participant.getEnrollmentId());
            getPersistenceManager().save(participant);
            getPersistenceManager().save(participant.getAppointment());
          } else {
            log.debug("persistedParticipant.interview={}", persistedParticipant.getInterview());
            if(persistedParticipant.getInterview() != null && persistedParticipant.getInterview().getStatus().equals(InterviewStatus.COMPLETED)) {
              // error if new appointment date is in the future
              Date newAppointmentDate = participant.getAppointment().getDate();
              if(newAppointmentDate != null && newAppointmentDate.compareTo(new Date()) > 0) {
                throw new ValidationRuntimeException(participant, "ParticipantInterviewCompletedWithAppointmentInTheFuture", new String[] { Integer.toString(line), participant.getEnrollmentId() }, "Participant's interview is completed, but new appointment date is in the future.");
              }
              // else ignore
            } else {
              // update its appointment date
              Appointment appointment = persistedParticipant.getAppointment();
              if(persistedParticipant.getAppointment() == null) {
                log.info("adding participant.appointment={}", participant.getEnrollmentId());
                appointment = participant.getAppointment();
                appointment.setParticipant(persistedParticipant);
                getPersistenceManager().save(appointment);
              } else {
                log.info("updating participant.appointment={}", participant.getEnrollmentId());
                persistedParticipant.getAppointment().setDate(participant.getAppointment().getDate());
                getPersistenceManager().save(persistedParticipant.getAppointment());
              }
            }
          }
        }
      });
      participantReaderCallbackInitialized = true;
    }

    // do some clean up before updating
    for(Participant p : getNotReceivedParticipants()) {
      if(p.getAppointment() != null) {
        log.info("removing appointment.appointmentCode={}", p.getAppointment().getAppointmentCode());
        getPersistenceManager().delete(p.getAppointment());
      }
      log.info("removing participant.enrollmentId={}", p.getEnrollmentId());
      getPersistenceManager().delete(p);
    }

    try {
      participantReader.process(participantsListStream);
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