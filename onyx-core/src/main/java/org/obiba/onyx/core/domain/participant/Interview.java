/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.participant;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;

@Entity
public class Interview extends AbstractEntity {

  private static final long serialVersionUID = -8786498940712113896L;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date startDate;

  @Temporal(TemporalType.TIMESTAMP)
  private Date endDate;

  @OneToOne(optional = false)
  @JoinColumn(name = "participant_id", unique = true)
  private Participant participant;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private InterviewStatus status;

  /**
   * Accumulated time spent in the interview (in seconds).
   */
  private Integer duration;

  public Interview() {
  }

  public Interview(Participant participant) {
    this.participant = participant;
    this.startDate = new Date();
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Participant getParticipant() {
    return participant;
  }

  public void setParticipant(Participant participant) {
    this.participant = participant;
  }

  public InterviewStatus getStatus() {
    return status;
  }

  public void setStatus(InterviewStatus status) {
    this.status = status;
  }

  public Integer getDuration() {
    return duration;
  }

  public void incrementDuration(int seconds) {
    if(duration != null) {
      duration += seconds;
    } else {
      duration = seconds;
    }
  }
}
