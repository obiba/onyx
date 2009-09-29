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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;

@Entity
public class QuestionnaireParticipant extends AbstractEntity {

  private static final long serialVersionUID = -4091599688342582439L;

  @OneToOne
  @JoinColumn(name = "participant_id")
  private Participant participant;

  private String questionnaireName;

  private String questionnaireVersion;

  private Locale locale;

  private String resumePage;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Temporal(TemporalType.TIMESTAMP)
  private Date timeStart;

  @Temporal(TemporalType.TIMESTAMP)
  private Date timeEnd;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionnaireParticipant")
  private List<QuestionAnswer> questionAnswers;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionnaireParticipant")
  private List<QuestionnaireMetric> questionnaireMetrics;

  public Participant getParticipant() {
    return participant;
  }

  public void setParticipant(Participant participant) {
    this.participant = participant;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public String getQuestionnaireName() {
    return questionnaireName;
  }

  public void setQuestionnaireName(String questionnaireName) {
    this.questionnaireName = questionnaireName;
  }

  public String getQuestionnaireVersion() {
    return questionnaireVersion;
  }

  public void setQuestionnaireVersion(String questionnaireVersion) {
    this.questionnaireVersion = questionnaireVersion;
  }

  public void setResumePage(String resumePage) {
    this.resumePage = resumePage;
  }

  public String getResumePage() {
    return resumePage;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Date getTimeStart() {
    return timeStart;
  }

  public void setTimeStart(Date timeStart) {
    this.timeStart = timeStart;
  }

  public Date getTimeEnd() {
    return timeEnd;
  }

  public void setTimeEnd(Date timeEnd) {
    this.timeEnd = timeEnd;
  }

  public List<QuestionAnswer> getParticipantAnswers() {
    return questionAnswers != null ? questionAnswers : (questionAnswers = new ArrayList<QuestionAnswer>());
  }

  public void addParticipantAnswer(QuestionAnswer questionAnswers) {
    if(questionAnswers != null) {
      getParticipantAnswers().add(questionAnswers);
      questionAnswers.setQuestionnaireParticipant(this);
    }
  }

  public List<QuestionnaireMetric> getQuestionnaireMetrics() {
    return questionnaireMetrics != null ? questionnaireMetrics : (questionnaireMetrics = new ArrayList<QuestionnaireMetric>());
  }

  public QuestionnaireMetric getQuestionnaireMetric(String page) {
    // Return the metric for the specific page.
    for(QuestionnaireMetric aQuestionnaireMetric : getQuestionnaireMetrics()) {
      if(aQuestionnaireMetric.getPage().equals(page)) {
        return aQuestionnaireMetric;
      }
    }

    // If not found, create one and return it.
    QuestionnaireMetric questionnaireMetric = new QuestionnaireMetric();
    questionnaireMetric.setQuestionnaireParticipant(this);
    questionnaireMetric.setPage(page);
    getQuestionnaireMetrics().add(questionnaireMetric);

    return questionnaireMetric;
  }
}
