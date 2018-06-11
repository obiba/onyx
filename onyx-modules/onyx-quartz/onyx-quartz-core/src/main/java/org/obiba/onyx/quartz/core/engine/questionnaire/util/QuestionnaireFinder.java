/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.obiba.magma.Variable;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.CategoryFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.OpenAnswerDefinitionFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.PageFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.QuestionFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.QuestionnaireCache;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.SectionFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

/**
 * Find elements in a {@link Questionnaire}.
 * @author Yannick Marcon
 * 
 */
public class QuestionnaireFinder implements Serializable {

  private static final long serialVersionUID = 1L;

  private transient static final Logger log = LoggerFactory.getLogger(QuestionnaireFinder.class);

  private static long total = 0;

  private final Questionnaire questionnaire;

  public QuestionnaireFinder(Questionnaire questionnaire) {
    this.questionnaire = questionnaire;
  }

  public static QuestionnaireFinder getInstance(Questionnaire questionnaire) {
    return new QuestionnaireFinder(questionnaire);
  }

  /**
   * Get the {@link Questionnaire} currently explored.
   * @return
   */
  public Questionnaire getQuestionnaire() {
    return questionnaire;
  }

  /**
   * Find {@link Variable} in the questionnaire.
   * @param name
   * @return null if not found
   */
  public Variable findVariable(String name) {
    long time = getDuration(0);
    Variable variable = null;
    for(Variable var : questionnaire.getVariables()) {
      if(var.getName().equals(name)) {
        variable = var;
        break;
      }
    }
    addTotal("variable", getDuration(time));

    return variable;
  }

  /**
   * Find {@link Section} in the questionnaire.
   * @param name
   * @return null if not found
   */
  public Section findSection(String name) {
    long time = getDuration(0);
    SectionFinder finder = new SectionFinder(name);
    QuestionnaireWalker walker = new QuestionnaireWalker(finder);
    walker.walk(questionnaire);
    addTotal("section", getDuration(time));

    return finder.getFirstElement();
  }

  /**
   * Find {@link Page} in the questionnaire.
   * @param name
   * @return null if not found
   */
  public Page findPage(String name) {
    Page page;

    long time = getDuration(0);
    if(questionnaire.getQuestionnaireCache() != null) {
      page = questionnaire.getQuestionnaireCache().getPageCache().get(name);
    } else {
      PageFinder finder = new PageFinder(name);
      QuestionnaireWalker walker = new QuestionnaireWalker(finder);
      walker.walk(questionnaire);
      page = finder.getFirstElement();
    }
    addTotal("page", getDuration(time));

    return page;
  }

  /**
   * Find {@link Question} with the given name in the questionnaire.
   * @param name
   * @return null if not found
   */
  public Question findQuestion(String name) {
    Question question;

    long time = getDuration(0);
    if(questionnaire.getQuestionnaireCache() != null) {
      question = questionnaire.getQuestionnaireCache().getQuestionCache().get(name);
    } else {
      QuestionFinder finder = new QuestionFinder(name);
      QuestionnaireWalker walker = new QuestionnaireWalker(finder);
      walker.walk(questionnaire);
      question = finder.getFirstElement();
    }
    addTotal("question", getDuration(time));

    return question;
  }

  /**
   * Find a {@link QuestionCategory} given the {@link Question} parent.
   * @param questionName
   * @param name
   * @return
   */
  public QuestionCategory findQuestionCategory(String questionName, String name) {
    Question question = findQuestion(questionName);
    if(question == null) return null;

    QuestionCategory qCategory = null;

    long time = getDuration(0);
    if(questionnaire.getQuestionnaireCache() != null) {
      qCategory = questionnaire.getQuestionnaireCache().getQuestionCategoryCache().get(questionName + "." + name);
    } else {
      String categoryName = name;
      if(!categoryName.startsWith(questionName + ".")) {
        categoryName = questionName + "." + name;
      }

      for(QuestionCategory questionCategory : question.getQuestionCategories()) {
        if(questionCategory.getName().equals(categoryName)) {
          qCategory = questionCategory;
          break;
        }
      }
    }
    addTotal("questionCategory", getDuration(time));

    return qCategory;
  }

  /**
   * Find the first {@link OpenAnswerDefinition} with the given name.
   * @param name
   * @return
   */
  public OpenAnswerDefinition findOpenAnswerDefinition(String name) {
    OpenAnswerDefinition open;

    long time = getDuration(0);
    if(questionnaire.getQuestionnaireCache() != null) {
      open = questionnaire.getQuestionnaireCache().getOpenAnswerDefinitionCache().get(name);
    } else {
      OpenAnswerDefinitionFinder finder = new OpenAnswerDefinitionFinder(name);
      QuestionnaireWalker walker = new QuestionnaireWalker(finder);
      walker.walk(questionnaire);
      open = finder.getFirstElement();
    }
    addTotal("openAnswerDefinition", getDuration(time));

    return open;
  }

  /**
   * Find the first {@link Category} with the given name.
   * @param name
   * @return null if not found
   */
  public Category findCategory(String name) {
    CategoryFinder finder = new CategoryFinder(name);
    QuestionnaireWalker walker = new QuestionnaireWalker(finder);
    walker.walk(questionnaire);

    return finder.getFirstElement();
  }

  /**
   * Find all the {@link Category} and associated {@link Question}.
   * @param name
   * @return
   */
  public Multimap<Category, Question> findCategories(String name) {
    CategoryFinder finder = new CategoryFinder(name, false);
    QuestionnaireWalker walker = new QuestionnaireWalker(finder);
    walker.walk(questionnaire);
    return finder.getQuestionCategories();
  }

  /**
   * Look for shared {@link Category}: categories refered by more than one question.
   * @return
   */
  public List<Category> findSharedCategories() {
    CategoryFinder finder = new CategoryFinder();
    QuestionnaireWalker walker = new QuestionnaireWalker(finder);
    walker.walk(questionnaire);
    return finder.getQuestionSharedCategories();
  }

  public List<Category> findCategories() {
    CategoryFinder finder = new CategoryFinder();
    QuestionnaireWalker walker = new QuestionnaireWalker(finder);
    walker.walk(questionnaire);
    return new ArrayList<Category>(finder.getQuestionCategories().keySet());
  }

  public Multimap<Category, Question> findQuestionsByCategory() {
    CategoryFinder finder = new CategoryFinder();
    QuestionnaireWalker walker = new QuestionnaireWalker(finder);
    walker.walk(questionnaire);
    return finder.getQuestionCategories();
  }

  /**
   * Find among the shared {@link Categories} the set of the ones that are unique by their name.
   * @return
   */
  public List<Category> findGlobalCategories() {
    Map<String, Category> map = new HashMap<String, Category>();
    for(Category category : findSharedCategories()) {
      if(!map.containsKey(category.getName())) {
        map.put(category.getName(), category);
      } else {
        map.remove(category.getName());
      }
    }
    return new LinkedList<Category>(map.values());
  }

  private long getDuration(long from) {
    return System.currentTimeMillis() - from;
  }

  private synchronized void addTotal(String msg, long duration) {
    total += duration;
    log.debug("### total={}ms [{}]", total, msg);
  }

  //
  // Caches
  //

  /**
   * Build the questionnaire cache.
   */
  public void buildQuestionnaireCache() {
    long time = getDuration(0);
    QuestionnaireCache questionnaireCache = new QuestionnaireCache();

    QuestionnaireWalker walker = new QuestionnaireWalker(questionnaireCache);
    walker.walk(questionnaire);

    log.debug("pages={}", questionnaireCache.getPageCache().size());
    log.debug("questions={}", questionnaireCache.getQuestionCache().size());
    log.debug("questionCategories={}", questionnaireCache.getQuestionCategoryCache().size());
    log.debug("openAnswerDefinition={}", questionnaireCache.getOpenAnswerDefinitionCache().size());

    questionnaire.setQuestionnaireCache(questionnaireCache);
    addTotal("questionnaireCache", getDuration(time));
  }

}
