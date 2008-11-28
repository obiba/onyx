/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.Condition;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;

public class Question implements Serializable, ILocalizable {

  private static final long serialVersionUID = -7795909448581432466L;

  private String name;

  private String number;

  private Page page;

  private boolean multiple;

  private Integer minCount;

  private Integer maxCount;

  private String uIFactoryName;

  private ValueMap uIArguments;

  private List<QuestionCategory> questionCategories;

  private Condition condition;

  private Question parentQuestion;

  private List<Question> questions;

  public Question(String name) {
    this.name = name;
  }

  public Page getPage() {
    return page;
  }

  public void setPage(Page page) {
    this.page = page;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public boolean isRequired() {
    return minCount != null && minCount > 0;
  }

  public boolean isMultiple() {
    return multiple;
  }

  public void setMultiple(boolean multiple) {
    this.multiple = multiple;
  }

  public boolean isBoilerPlate() {
    return (getQuestionCategories().size() == 0 && getQuestions().size() == 0);
  }

  public boolean isToBeAnswered(ActiveQuestionnaireAdministrationService service) {
    if(condition == null) return true;
    return condition.isToBeAnswered(service);
  }

  public String getUIFactoryName() {
    return uIFactoryName;
  }

  public void setUIFactoryName(String factoryName) {
    uIFactoryName = factoryName;
  }

  public ValueMap getUIArguments() {
    return uIArguments;
  }

  public ValueMap addUIArgument(String key, String value) {
    if(uIArguments == null) {
      uIArguments = new ValueMap();
    }
    uIArguments.add(key, value);
    return uIArguments;
  }

  public List<QuestionCategory> getQuestionCategories() {
    return questionCategories != null ? questionCategories : (questionCategories = new ArrayList<QuestionCategory>());
  }

  /**
   * Get the underlying {@link Category} list.
   * @return
   */
  public List<Category> getCategories() {
    List<Category> categories = new ArrayList<Category>();

    for(QuestionCategory questionCategory : getQuestionCategories()) {
      categories.add(questionCategory.getCategory());
    }

    return categories;
  }

  public void addQuestionCategory(QuestionCategory questionCategory) {
    if(questionCategory != null) {
      getQuestionCategories().add(questionCategory);
      questionCategory.setQuestion(this);
    }
  }

  public Condition getCondition() {
    return condition;
  }

  public void setCondition(Condition condition) {
    this.condition = condition;
  }

  public Question getParentQuestion() {
    return parentQuestion;
  }

  public void setParentQuestion(Question parentQuestion) {
    this.parentQuestion = parentQuestion;
  }

  public List<Question> getQuestions() {
    return questions != null ? questions : (questions = new ArrayList<Question>());
  }

  public void addQuestion(Question question) {
    if(question != null) {
      getQuestions().add(question);
      question.setParentQuestion(this);
    }
  }

  public Integer getMinCount() {
    return minCount;
  }

  public void setMinCount(Integer minCount) {
    this.minCount = minCount;
  }

  public Integer getMaxCount() {
    return maxCount;
  }

  public void setMaxCount(Integer maxCount) {
    this.maxCount = maxCount;
  }

  //
  // Find methods
  //
  public Category findCategory(String name) {
    for(Category category : getCategories()) {
      if(category.getName().equals(name)) {
        return category;
      }
    }
    return null;
  }

  public QuestionCategory findQuestionCategory(String name) {
    for(QuestionCategory qCategory : getQuestionCategories()) {
      if(qCategory.getCategory().getName().equals(name)) {
        return qCategory;
      }
    }
    return null;
  }

  public void accept(IVisitor visitor) {
    visitor.visit(this);
  }

  public boolean hasDataSource() {

    OpenAnswerDefinition openAnswerDefinition;
    List<Category> categories = getCategories();
    for(Category category : categories) {
      if((openAnswerDefinition = category.getOpenAnswerDefinition()) != null) {
        if(openAnswerDefinition.getDataSource() != null) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return getName();
  }

}
