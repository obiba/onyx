/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder;

import java.util.List;
import java.util.Map;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.util.data.DataType;

/**
 * {@link Category} builder, given a {@link Questionnaire} and a current {@link Question}.
 * @author Yannick Marcon
 * 
 */
public class CategoryBuilder extends AbstractQuestionnaireElementBuilder<Category> {

  private QuestionCategory questionCategory;

  /**
   * Constructor using {@link QuestionBuilder} to get the {@link Question} it is applied to.
   * @param parent
   * @param category
   */
  private CategoryBuilder(QuestionBuilder parent, Category category) {
    super(parent.getQuestionnaire());
    this.element = category;
    this.questionCategory = createQuestionCategory(parent.getElement());
  }

  /**
   * Add a {@link Category} to current {@link Question}, make it the current category.
   * @param name
   * @param selected if is selected by default
   * @return
   * @throws IllegalArgumentException if name does not respect questionnaire element naming pattern.
   */
  public static CategoryBuilder createQuestionCategory(QuestionBuilder parent, String name) {
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    Category category = new Category(name);
    category.setEscape(false);
    return new CategoryBuilder(parent, category);
  }

  /**
   * Add a {@link Category} to current {@link Question}, make it the current category.
   * @param name
   * @param selected if is selected by default
   * @return
   */
  public static CategoryBuilder createQuestionCategory(QuestionBuilder parent, Category category) {
    return new CategoryBuilder(parent, category);
  }

  /**
   * Set the export name to the current question category ({@link QuestionCategory}).
   * @param exportName
   * @return
   */
  public CategoryBuilder setExportName(String exportName) {
    questionCategory.setExportName(exportName);
    return this;
  }

  /**
   * Set if the current question category ({@link QuestionCategory}) should be selected or not.
   * @param selected
   * @return
   */
  public CategoryBuilder setSelected(boolean selected) {
    questionCategory.setSelected(selected);
    return this;
  }

  /**
   * Set if the current question category ({@link QuestionCategory}) should be repeatable or not.
   * @param selected
   * @return
   */
  public CategoryBuilder setReselectable(boolean reselectable) {
    questionCategory.setReselectable(reselectable);
    return this;
  }

  /**
   * Set if the current category ({@link Category}) allows to escape question answer.
   * @param escape
   * @return
   */
  public CategoryBuilder setEscape(boolean escape) {
    questionCategory.getCategory().setEscape(escape);
    return this;
  }

  /**
   * Set the {@link OpenAnswerDefinition} to the current category.
   * @param name
   * @param dataType
   * @return
   */
  public OpenAnswerDefinitionBuilder withOpenAnswerDefinition(String name, DataType dataType) {
    return OpenAnswerDefinitionBuilder.createOpenAnswerDefinition(this, name, dataType);
  }

  /**
   * Add a {@link Category} to the current {@link Question}.
   * @param name
   * @return
   */
  public CategoryBuilder withCategory(String name) {
    this.element = new Category(name);
    element.setEscape(false);
    questionCategory = createQuestionCategory(questionCategory.getQuestion());
    return this;
  }

  /**
   * Add a set of {@link Category} to the current {@link Question}.
   * @param names
   * @return
   */
  public CategoryBuilder withCategories(String... names) {
    for(String name : names) {
      withCategory(name);
    }
    return this;
  }

  /**
   * Look for a {@link Category} with the same name and given export name in the
   * {@link QuestionnaireBuilder. Create it if not found.
   * @param name
   * @return
   */
  public CategoryBuilder withSharedCategory(String name, String exportName) {
    withSharedCategory(name).setExportName(exportName);
    return this;
  }

  /**
   * Look for a {@link Category} with the same name in the {@link QuestionnaireBuilder. Create it if not found.
   * @param name
   * @return
   */
  public CategoryBuilder withSharedCategory(String name) {
    Map<Category, List<Question>> map = QuestionnaireFinder.getInstance(questionnaire).findCategories(name);
    if(map.keySet().size() > 1) {
      throw invalidSharedCategoryNameUnicityException(name);
    } else if(map.keySet().isEmpty()) {
      return withCategory(name);
    } else {
      this.element = map.keySet().iterator().next();
      questionCategory = createQuestionCategory(questionCategory.getQuestion());
      return this;
    }
  }

  /**
   * Add a set of {@link Category} by looking them from the name in the {@link Questionnaire}. Create them if not
   * found.
   * @param names
   * @return
   */
  public CategoryBuilder withSharedCategories(String... names) {
    for(String name : names) {
      withSharedCategory(name);
    }
    return this;
  }

  /**
   * Create the {@link QuestionCategory} for the current {@link Question} and {@link Category}.
   * @param question
   * @return
   */
  private QuestionCategory createQuestionCategory(Question question) {
    // make sure for is not already a category with same name in this question
    for(Category category : question.getCategories()) {
      if(category.getName().equals(element.getName())) {
        throw new IllegalArgumentException("You cannot have categories with the same name in a question: " + element.getName());
      }
    }
    QuestionCategory questionCategory = new QuestionCategory();
    questionCategory.setSelected(false);
    questionCategory.setCategory(element);
    question.addQuestionCategory(questionCategory);
    return questionCategory;
  }

  private IllegalArgumentException invalidSharedCategoryNameUnicityException(String name) {
    return new IllegalArgumentException("There are several categories with name: " + name);
  }

}
