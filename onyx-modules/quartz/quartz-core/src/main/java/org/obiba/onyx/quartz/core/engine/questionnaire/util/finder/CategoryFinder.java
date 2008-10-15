package org.obiba.onyx.quartz.core.engine.questionnaire.util.finder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

public class CategoryFinder extends AbstractFinderVisitor<Category> {

  Map<Category, List<Question>> questionCategories = new HashMap<Category, List<Question>>();

  public CategoryFinder() {
    super(null, false);
  }
  
  public CategoryFinder(String name) {
    super(name);
  }

  public CategoryFinder(String name, boolean stopAtFirst) {
    super(name, stopAtFirst);
  }

  public Map<Category, List<Question>> getQuestionCategories() {
    return questionCategories;
  }

  /**
   * Look for shared {@link Category}: categories refered by more than one question.
   * @return
   */
  public List<Category> getQuestionSharedCategories() {
    List<Category> shared = new ArrayList<Category>();
    for (Entry<Category, List<Question>> entry : questionCategories.entrySet()) {
      if (entry.getValue().size() > 1) {
        shared.add(entry.getKey());
      }
    }
    return shared;
  }
  
  public void visit(Questionnaire questionnaire) {
  }

  public void visit(Section section) {
  }

  public void visit(Page page) {
  }

  public void visit(Question question) {
  }

  public void visit(QuestionCategory questionCategory) {
    if(getName() == null || visitElement(questionCategory.getCategory())) {
      if(!questionCategories.containsKey(questionCategory.getCategory())) {
        ArrayList<Question> questions = new ArrayList<Question>();
        questions.add(questionCategory.getQuestion());
        questionCategories.put(questionCategory.getCategory(), questions);
      } else {
        questionCategories.get(questionCategory.getCategory()).add(questionCategory.getQuestion());
      }
    }
  }

  public void visit(Category category) {
  }

  public void visit(OpenAnswerDefinition openAnswerDefinition) {
  }

}
