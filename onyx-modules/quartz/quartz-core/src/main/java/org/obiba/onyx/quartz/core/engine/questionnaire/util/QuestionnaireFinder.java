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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.CategoryFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.OpenAnswerDefinitionFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.PageFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.QuestionFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.SectionFinder;

/**
 * Find elements in a {@link Questionnaire}.
 * @author Yannick Marcon
 * 
 */
public class QuestionnaireFinder {

  private Questionnaire questionnaire;

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
   * Find {@link Section} in the questionnaire.
   * @param name
   * @return null if not found
   */
  public Section findSection(String name) {
    SectionFinder finder = new SectionFinder(name);
    QuestionnaireWalker walker = new QuestionnaireWalker(finder);
    walker.walk(questionnaire);

    return finder.getFirstElement();
  }

  /**
   * Find {@link Page} in the questionnaire.
   * @param name
   * @return null if not found
   */
  public Page findPage(String name) {
    PageFinder finder = new PageFinder(name);
    QuestionnaireWalker walker = new QuestionnaireWalker(finder);
    walker.walk(questionnaire);

    return finder.getFirstElement();
  }

  /**
   * Find {@link Question} with the given name in the questionnaire.
   * @param name
   * @return null if not found
   */
  public Question findQuestion(String name) {
    QuestionFinder finder = new QuestionFinder(name);
    QuestionnaireWalker walker = new QuestionnaireWalker(finder);
    walker.walk(questionnaire);

    return finder.getFirstElement();
  }

  /**
   * Find the first {@link OpenAnswerDefinition} with the given name.
   * @param name
   * @return
   */
  public OpenAnswerDefinition findOpenAnswerDefinition(String name) {
    OpenAnswerDefinitionFinder finder = new OpenAnswerDefinitionFinder(name);
    QuestionnaireWalker walker = new QuestionnaireWalker(finder);
    walker.walk(questionnaire);

    return finder.getFirstElement();
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
  public Map<Category, List<Question>> findCategories(String name) {
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

  /**
   * Find among the shared {@link Categories} the set of the ones that are unique by their name.
   * @return
   */
  public List<Category> findGlobalCategories() {
    Map<String, Category> map = new HashMap<String, Category>();
    for(Category category : findSharedCategories()) {
      if(!map.containsKey(category)) {
        map.put(category.getName(), category);
      } else {
        map.remove(category.getName());
      }
    }

    return new LinkedList<Category>(map.values());
  }
}
