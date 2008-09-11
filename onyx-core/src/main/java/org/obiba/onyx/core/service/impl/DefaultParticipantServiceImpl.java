package org.obiba.onyx.core.service.impl;

import java.io.File;
import java.io.FileDescriptor;
import java.util.Date;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.application.AppConfiguration;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
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

  public Participant createParticipant(Participant model) {
    if(model.getId() != null) throw new IllegalArgumentException("Participant model must not be an already persisted entity.");

    return getPersistenceManager().save(model);
  }

  public Appointment addAppointment(Participant participant, Date date) {
    Appointment appointment = new Appointment();
    appointment.setDate(date);
    appointment.setParticipant(participant);
    appointment = getPersistenceManager().save(appointment);

    getPersistenceManager().save(participant);

    return appointment;
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
    log.info("participant.lastName={}", participant.getLastName());
    persistenceManager.save(participant);
    log.info("participant.lastName={}", participant.getLastName());
  }

  public void updateParticipantList() {
    AppConfiguration appConfig = new AppConfiguration();
    appConfig = getPersistenceManager().matchOne(appConfig);

    File dir = new File(appConfig.getParticipantDirectoryPath());
    log.info("participantDirectory={}", dir.getAbsolutePath());
    File lastModifiedFile = null;
    for(File f : dir.listFiles()) {
     if (lastModifiedFile == null || (f.lastModified()>lastModifiedFile.lastModified())) {
       lastModifiedFile = f;
     }
    }
    
    if (lastModifiedFile == null) {
      throw new ValidationRuntimeException("NoParticipantFileFound", "No particpant file found.");
    }
    
    log.info("participantFile={}", lastModifiedFile.getAbsolutePath());
  }
}