package org.obiba.onyx.engine;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.user.User;

@Entity
public class Action extends AbstractEntity {

  private static final long serialVersionUID = -943609521870150739L;

  @ManyToOne
  private User user;

  @ManyToOne
  private Interview interview;

  @Enumerated(EnumType.STRING)
  private ActionType actionType;

  @ManyToOne
  private Stage stage;

  @Temporal(TemporalType.TIMESTAMP)
  private Date dateTime;

  private String comment;

  private String eventReason;

  public Action() {
    super();
  }

  public final User getUser() {
    return user;
  }

  public final void setUser(User user) {
    this.user = user;
  }

  public final Interview getInterview() {
    return interview;
  }

  public final void setInterview(Interview interview) {
    this.interview = interview;
  }

  public final ActionType getActionType() {
    return actionType;
  }

  public final void setActionType(ActionType actionType) {
    this.actionType = actionType;
  }

  public final Stage getStage() {
    return stage;
  }

  public final void setStage(Stage stage) {
    this.stage = stage;
  }

  public final Date getDateTime() {
    return dateTime;
  }

  public final void setDateTime(Date dateTime) {
    this.dateTime = dateTime;
  }

  public final String getComment() {
    return comment;
  }

  public final void setComment(String comment) {
    this.comment = comment;
  }

  public final String getEventReason() {
    return eventReason;
  }

  public final void setEventReason(String eventReason) {
    this.eventReason = eventReason;
  }

}
