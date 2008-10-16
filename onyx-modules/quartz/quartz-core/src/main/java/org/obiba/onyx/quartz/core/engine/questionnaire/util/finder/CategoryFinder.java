/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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

/**
 * Class for finding {@link Category}.
 * @author Yannick Marcon
 *
 */
public class CategoryFinder extends AbstractFinderVisitor<Category> {

  /**
   * A map for storing the {@link Category} / {@link Question} association.
   */
  Map<Category, List<Question>> questionCategories = new HashMap<Category, List<Question>>();

  /**
   * Constructor, for searching all categories with their question association.
   * @see #getQuestionCategories()
   * @see #getQuestionSharedCategories()
   */
  public CategoryFinder() {
    super(null, false);
  }
  
  /**
   * Constructor, for searching first {@link Category} with given name.
   * @param name
   */
  public CategoryFinder(String name) {
    super(name);
  }

  /**
   * Constructor, for searching {@link Category} with given name.
   * @param name
   * @param stopAtFirst
   */
  public CategoryFinder(String name, boolean stopAtFirst) {
    super(name, stopAtFirst);
  }

  /**
   * Get the {@link Category} / {@link Question} association.
   * @return
   */
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
