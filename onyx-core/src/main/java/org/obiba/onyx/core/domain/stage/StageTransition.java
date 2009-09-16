/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.stage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.state.StageState;

/**
 * Entity representing a stage's state transition.
 */
@Entity
public class StageTransition extends AbstractEntity {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  /**
   * The name of the stage in which the state transition occurred.
   */
  @Column(nullable = false)
  private String stage;

  /**
   * The stage's associated interview.
   */
  @ManyToOne
  @JoinColumn(name = "interview_id", nullable = false)
  private Interview interview;

  /**
   * The transition's associated action (i.e., the action that caused the transition).
   */
  @ManyToOne
  @JoinColumn(name = "action_id", nullable = false)
  private Action action;

  /**
   * The transition's "from" state (i.e., the stage's state before the transition).
   */
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private StageState fromState;

  /**
   * The transition's "to" state (i.e., the stage's state after the transition).
   */
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private StageState toState;

  /**
   * The transition event (i.e., the event triggered by the action and causing the transition).
   */
  @Column(nullable = false)
  private String event;

  //
  // Methods
  //

  public String getStage() {
    return stage;
  }

  public void setStage(String stage) {
    this.stage = stage;
  }

  public Interview getInterview() {
    return interview;
  }

  public void setInterview(Interview interview) {
    this.interview = interview;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public StageState getFromState() {
    return fromState;
  }

  public void setFromState(StageState fromState) {
    this.fromState = fromState;
  }

  public StageState getToState() {
    return toState;
  }

  public void setToState(StageState toState) {
    this.toState = toState;
  }

  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }
}
