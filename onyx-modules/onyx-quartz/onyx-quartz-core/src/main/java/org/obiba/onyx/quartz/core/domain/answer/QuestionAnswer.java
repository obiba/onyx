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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obiba.core.domain.AbstractEntity;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "questionName", "questionnaire_participant_id" }) })
public class QuestionAnswer extends AbstractEntity {

  private static final long serialVersionUID = 8513736303565362142L;

  @Column(nullable = false)
  private String questionName;

  @Column(length = 2000)
  private String comment;

  @Column(nullable = false)
  private Boolean active;

  @ManyToOne(optional = false)
  @JoinColumn(name = "questionnaire_participant_id")
  private QuestionnaireParticipant questionnaireParticipant;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionAnswer")
  private List<CategoryAnswer> categoryAnswers;

  public String getQuestionName() {
    return questionName;
  }

  public void setQuestionName(String questionName) {
    this.questionName = questionName;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public boolean isActive() {
    return this.active;
  }

  public QuestionnaireParticipant getQuestionnaireParticipant() {
    return questionnaireParticipant;
  }

  public void setQuestionnaireParticipant(QuestionnaireParticipant questionnaireParticipant) {
    this.questionnaireParticipant = questionnaireParticipant;
  }

  public List<CategoryAnswer> getCategoryAnswers() {
    return categoryAnswers != null ? categoryAnswers : (categoryAnswers = new ArrayList<CategoryAnswer>());
  }

  public void addCategoryAnswer(CategoryAnswer categoryAnswer) {
    if(categoryAnswer != null) {
      getCategoryAnswers().add(categoryAnswer);
      categoryAnswer.setQuestionAnswer(this);
    }
  }

  @Override
  public String toString() {
    return "QuestionAnswer=[" + questionName + ", CategoryAnswers.size=" + getCategoryAnswers().size() + "]";
  }

}
