/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.stage.StageTransition;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.engine.state.StageState;

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

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(optional = false)
  @JoinColumn(name = "interview_id")
  private Interview interview;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ActionType type;

  private String stage;

  @OneToMany(cascade = { CascadeType.ALL }, mappedBy = "action")
  private List<StageTransition> stageTransitions;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateTime;

  @Column(length = 2000)
  private String comment;

  private String eventReason;

  @Column(nullable = false)
  private String actionDefinitionCode;

  public Action() {
  }

  public Action(ActionDefinition definition) {
    this(definition.getType(), definition.getCode());
  }

  public Action(ActionType type, String code) {
    this.type = type;
    this.dateTime = new Date();
    this.actionDefinitionCode = code;
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

  public List<StageTransition> getStageTransitions() {
    return stageTransitions != null ? stageTransitions : (stageTransitions = new ArrayList<StageTransition>());
  }

  public void addStageTransition(StageTransition stageTransition) {
    if(stageTransition != null) {
      getStageTransitions().add(stageTransition);
      stageTransition.setAction(this);
    }
  }

  /**
   * Returns the action's "from" state.
   * 
   * This is a <em>derived</em> attribute. The action's "from" state is that of the associated stage transition with
   * the same stage as the action.
   * 
   * @return action's "from" state
   * @see {@link org.obiba.onyx.core.domain.stage.StageTransition StageTransition}
   */
  public StageState getFromState() {
    if(getStage() != null) {
      for(StageTransition transition : getStageTransitions()) {
        if(transition.getStage().equals(getStage())) {
          return transition.getFromState();
        }
      }
    }

    return null;
  }

  /**
   * Returns the action's "to" state.
   * 
   * This is a <em>derived</em> attribute. The action's "to" state is that of the associated stage transition with the
   * same stage as the action.
   * 
   * @return action's "to" state
   * @see {@link org.obiba.onyx.core.domain.stage.StageTransition StageTransition}
   */
  public StageState getToState() {
    if(getStage() != null) {
      for(StageTransition transition : getStageTransitions()) {
        if(transition.getStage().equals(getStage())) {
          return transition.getToState();
        }
      }
    }

    return null;
  }

  public String getActionDefinitionCode() {
    return actionDefinitionCode;
  }

  public final void setActionDefinitionCode(String actionDefinitionCode) {
    this.actionDefinitionCode = actionDefinitionCode;
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
