package org.obiba.onyx.engine;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.user.User;

/**
 * Action is the track of who did what, when on who and why in the Onyx system.
 * 
 * @see ActionType
 * @author Yannick Marcon
 *
 */
@Entity
public class Action extends AbstractEntity {

  private static final long serialVersionUID = -943609521870150739L;

  @ManyToOne
  @JoinColumn(name="user_id")
  private User user;

  @ManyToOne
  @JoinColumn(name="interview_id")
  private Interview interview;

  @Enumerated(EnumType.STRING)
  private ActionType type;

  private String stage;

  @Temporal(TemporalType.TIMESTAMP)
  private Date dateTime;

  private String comment;

  private String eventReason;

  public Action() {
  }

  public Action(ActionDefinition definition) {
    this.type = definition.getType();
    dateTime = new Date();
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
    return type;
  }

  public final void setActionType(ActionType actionType) {
    this.type = actionType;
  }

  public final String getStage() {
    return stage;
  }

  public final void setStage(String stage) {
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
  
  @Override
  public String toString() {
    return "[" + type + " " + dateTime + "]";
  }

}
