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

import java.util.Map;
import java.util.TreeMap;

import org.obiba.magma.Variable;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.IWalkerVisitor;
import org.obiba.onyx.util.data.DataType;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Visit the questionnaire elements and register them by type and name, for caching purpose.
 */
public class QuestionnaireCache implements IWalkerVisitor {

  private Map<String, Section> sectionCache = new TreeMap<String, Section>();

  private Map<String, Page> pageCache = new TreeMap<String, Page>();

  private Map<String, Question> questionCache = new TreeMap<String, Question>();

  private Map<String, QuestionCategory> questionCategoryCache = new TreeMap<String, QuestionCategory>();

  private Map<String, OpenAnswerDefinition> openAnswerDefinitionCache = new TreeMap<String, OpenAnswerDefinition>();

  private Map<String, Variable> variableCache = new TreeMap<String, Variable>();

  private Multimap<DataType, Question> questionByType = HashMultimap.create();

  private Questionnaire questionnaire;

  @Override
  public boolean visiteMore() {
    return true;
  }

  @Override
  public void visit(@SuppressWarnings("hiding") Questionnaire questionnaire) {
    this.questionnaire = questionnaire;
  }

  @Override
  public void visit(Section section) {
    sectionCache.put(section.getName(), section);
  }

  @Override
  public void visit(Page page) {
    pageCache.put(page.getName(), page);
  }

  @Override
  public void visit(Question question) {
    questionCache.put(question.getName(), question);
  }

  @Override
  public void visit(QuestionCategory questionCategory) {
    questionCategoryCache.put(questionCategory.getName(), questionCategory);
    for(OpenAnswerDefinition openAnswer : questionCategory.getCategory().getOpenAnswerDefinitionsByName().values()) {
      questionByType.put(openAnswer.getDataType(), questionCategory.getQuestion());
    }
  }

  @Override
  public void visit(Category category) {

  }

  @Override
  public void visit(OpenAnswerDefinition openAnswerDefinition) {
    openAnswerDefinitionCache.put(openAnswerDefinition.getName(), openAnswerDefinition);
  }

  @Override
  public void visit(Variable variable) {
    variableCache.put(variable.getName(), variable);
  }

  public Questionnaire getQuestionnaire() {
    return questionnaire;
  }

  public Map<String, Section> getSectionCache() {
    return sectionCache;
  }

  public Map<String, Page> getPageCache() {
    return pageCache;
  }

  public Map<String, Question> getQuestionCache() {
    return questionCache;
  }

  public Map<String, QuestionCategory> getQuestionCategoryCache() {
    return questionCategoryCache;
  }

  public Map<String, OpenAnswerDefinition> getOpenAnswerDefinitionCache() {
    return openAnswerDefinitionCache;
  }

  public Map<String, Variable> getVariableCache() {
    return variableCache;
  }

  public Multimap<DataType, Question> getQuestionByType() {
    return questionByType;
  }

}
