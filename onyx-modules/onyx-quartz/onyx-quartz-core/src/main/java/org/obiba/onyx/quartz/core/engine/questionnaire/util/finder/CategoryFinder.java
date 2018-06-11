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
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.obiba.magma.Variable;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Class for finding {@link Category}.
 * @author Yannick Marcon
 * 
 */
public class CategoryFinder extends AbstractFinderVisitor<Category> {

  /**
   * A map for storing the {@link Category} / {@link Question} association.
   */
  Multimap<Category, Question> questionCategories = HashMultimap.create();

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
  public Multimap<Category, Question> getQuestionCategories() {
    return questionCategories;
  }

  /**
   * Look for shared {@link Category}: categories referred by more than one question.
   * @return
   */
  public List<Category> getQuestionSharedCategories() {
    List<Category> shared = new ArrayList<Category>();
    for(Entry<Category, Collection<Question>> entry : questionCategories.asMap().entrySet()) {
      if(entry.getValue().size() > 1) {
        shared.add(entry.getKey());
      }
    }
    return shared;
  }

  @Override
  public void visit(Questionnaire questionnaire) {
  }

  @Override
  public void visit(Section section) {
  }

  @Override
  public void visit(Page page) {
  }

  @Override
  public void visit(Question question) {
  }

  @Override
  public void visit(QuestionCategory questionCategory) {
    if(getName() == null || visitElement(questionCategory.getCategory())) {
      questionCategories.put(questionCategory.getCategory(), questionCategory.getQuestion());
    }
  }

  @Override
  public void visit(Category category) {
  }

  @Override
  public void visit(OpenAnswerDefinition openAnswerDefinition) {
  }

  @Override
  public void visit(Variable variable) {
  }

}
