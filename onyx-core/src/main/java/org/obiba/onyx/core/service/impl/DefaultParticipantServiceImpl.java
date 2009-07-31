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
import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttributeValue;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.util.data.Data;
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

    // Persist the start action
    Action receptionAction = new Action();
    receptionAction.setActionType(ActionType.START);
    receptionAction.setDateTime(new Date());
    if(receptionComment != null && receptionComment.trim().length() != 0) receptionAction.setComment(receptionComment);
    receptionAction.setUser(user);
    receptionAction.setInterview(interview);
    getPersistenceManager().save(receptionAction);

  }

  public void updateParticipant(Participant participant) {
    getPersistenceManager().save(participant);
  }

  /**
   * Delete all appointments that have not been received
   */
  public void cleanUpAppointment() {
    for(Participant p : getNotReceivedParticipants()) {
      log.debug("removing participant.enrollmentId={}", p.getEnrollmentId());
      getPersistenceManager().delete(p);
    }
  }

  /**
   * DB access layer specific implementation for getting {@link Participant} that have not been received.
   * @return
   */
  protected abstract List<Participant> getNotReceivedParticipants();

  public List<Action> getActions(Participant participant) {
    return getActions(participant, null);
  }

  public List<Action> getActions(Participant participant, String stage) {
    Action template = new Action();
    template.setInterview(participant.getInterview());
    template.setStage(stage);
    return getPersistenceManager().match(template);
  }

  public Data getConfiguredAttributeValue(Participant participant, String attributeName) {
    ParticipantAttributeValue value = new ParticipantAttributeValue();
    value.setAttributeName(attributeName);
    value.setParticipant(participant);

    value = getPersistenceManager().matchOne(value);
    if(value != null && value.getData() != null && value.getValue() != null) {
      return value.getData();
    }

    return null;
  }

  public Participant getParticipant(Participant participant) {
    return getPersistenceManager().matchOne(participant);
  }

}
