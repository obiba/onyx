/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.domain.answer;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.obiba.core.domain.AbstractEntity;

@Entity
public class CategoryAnswer extends AbstractEntity {

  private static final long serialVersionUID = 8308345423791582240L;

  private String categoryName;

  private Integer occurence;

  private Boolean active;

  @ManyToOne
  @JoinColumn(name = "question_answer_id")
  private QuestionAnswer questionAnswer;

  @ManyToOne
  @JoinColumn(name = "parent_category_answer_id")
  private CategoryAnswer parentCategoryAnswer;

  @OneToMany(mappedBy = "parentCategoryAnswer")
  private List<CategoryAnswer> childrenCategoryAnswers;

  @OneToOne(mappedBy = "categoryAnswer")
  private OpenAnswer openAnswer;

  public QuestionAnswer getQuestionAnswer() {
    return questionAnswer;
  }

  public void setQuestionAnswer(QuestionAnswer questionAnswer) {
    this.questionAnswer = questionAnswer;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public Integer getOccurence() {
    return occurence;
  }

  public void setOccurence(Integer occurence) {
    this.occurence = occurence;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public CategoryAnswer getParentCategoryAnswer() {
    return parentCategoryAnswer;
  }

  public void setParentCategoryAnswer(CategoryAnswer parentCategoryAnswer) {
    this.parentCategoryAnswer = parentCategoryAnswer;
  }

  public QuestionAnswer getParentQuestionAnswer() {
    if(getParentCategoryAnswer() == null) return null;

    return getParentCategoryAnswer().getQuestionAnswer();
  }

  public List<CategoryAnswer> getChildrenCategoryAnswers() {
    return childrenCategoryAnswers != null ? childrenCategoryAnswers : (childrenCategoryAnswers = new ArrayList<CategoryAnswer>());
  }

  public OpenAnswer getOpenAnswer() {
    return openAnswer;
  }

  public void setOpenAnswer(OpenAnswer openAnswer) {
    this.openAnswer = openAnswer;
    this.openAnswer.setCategoryAnswer(this);
  }
}
