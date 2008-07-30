package org.obiba.onyx.engine;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.user.User;

@Entity
public class ActionInstance extends AbstractEntity {

  private static final long serialVersionUID = -943609521870150739L;

  private User user;

  private Interview interview;

  @Enumerated(EnumType.STRING)
  private ActionType actionType;

  @Temporal(TemporalType.TIMESTAMP)
  private Date dateTime;

  private String comment;

  private String eventReason;

  public ActionInstance() {
    super();
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Interview getInterview() {
    return interview;
  }

  public void setInterview(Interview interview) {
    this.interview = interview;
  }

  public Date getDateTime() {
    return dateTime;
  }

  public void setDateTime(Date dateTime) {
    this.dateTime = dateTime;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getEventReason() {
    return eventReason;
  }

  public void setEventReason(String eventReason) {
    this.eventReason = eventReason;
  }

  public ActionType getActionType() {
    return actionType;
  }

  public void setActionType(ActionType actionType) {
    this.actionType = actionType;
  }

}
