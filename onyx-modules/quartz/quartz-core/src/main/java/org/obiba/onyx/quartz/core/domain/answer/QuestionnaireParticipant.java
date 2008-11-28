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
import java.util.List;
import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Participant;

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

  @OneToMany(mappedBy = "questionnaireParticipant")
  private List<QuestionAnswer> questionAnswers;

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

  public List<QuestionAnswer> getParticipantAnswers() {
    return questionAnswers != null ? questionAnswers : (questionAnswers = new ArrayList<QuestionAnswer>());
  }

  public void addParticipantAnswer(QuestionAnswer questionAnswers) {
    if(questionAnswers != null) {
      getParticipantAnswers().add(questionAnswers);
      questionAnswers.setQuestionnaireParticipant(this);
    }
  }

}
