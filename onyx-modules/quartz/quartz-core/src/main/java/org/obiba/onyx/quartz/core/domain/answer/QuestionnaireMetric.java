/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.domain.answer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obiba.core.domain.AbstractEntity;

/**
 * {@link QuestionnaireParticipant} metrics.
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "questionnaire_participant_id", "page" }) })
public class QuestionnaireMetric extends AbstractEntity {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  /**
   * The associated {@link QuestionnaireParticipant}.
   */
  @ManyToOne
  @JoinColumn(name = "questionnaire_participant_id", nullable = false)
  private QuestionnaireParticipant questionnaireParticipant;

  /**
   * The associated page (i.e., page name).
   */
  @Column(nullable = false)
  private String page;

  /**
   * Accumulated time spent on the associated page (in seconds).
   */
  private int duration;

  //
  // Methods
  //

  public QuestionnaireParticipant getQuestionnaireParticipant() {
    return questionnaireParticipant;
  }

  public void setQuestionnaireParticipant(QuestionnaireParticipant questionnaireParticipant) {
    this.questionnaireParticipant = questionnaireParticipant;
  }

  public String getPage() {
    return page;
  }

  public void setPage(String page) {
    this.page = page;
  }

  public int getDuration() {
    return duration;
  }

  public void incrementDuration(int seconds) {
    duration += seconds;
  }
}
