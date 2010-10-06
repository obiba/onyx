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

import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

import com.google.common.collect.ImmutableSet;

/**
 * This builder ensures that Questionnaire Element names are unique. If they are not unique a
 * {@link QuestionnaireVariableNameNotUniqueException} is thrown -- nothing is actually built.
 * @see QuestionnaireUniqueVariableNameResolver
 */
public class UniqueQuestionnaireElementNameBuilder {

  private Question question;

  private List<QuestionCategory> categories;

  private final QuestionnaireUniqueVariableNameResolver variableNameResolver;

  private UniqueQuestionnaireElementNameBuilder(Question question, QuestionnaireUniqueVariableNameResolver variableNameResolver) {
    this.variableNameResolver = variableNameResolver;
    this.question = question;
  }

  public UniqueQuestionnaireElementNameBuilder withParentCategories() {
    categories = question.getParentQuestion().getQuestionCategories();
    return this;
  }

  public UniqueQuestionnaireElementNameBuilder withCategories() {
    categories = question.getQuestionCategories();
    return this;
  }

  public void build() {
    if(categories != null) {
      buildCategoricalVariable();
    } else {
      buildParentPlaceholderVariable();
    }
  }

  private void buildParentPlaceholderVariable() {
    variableNameResolver.variableName(question);
  }

  private void buildCategoricalVariable() {
    variableNameResolver.variableName(question);

    for(QuestionCategory questionCategory : categories) {
      buildCategoryVariable(questionCategory);
    }
  }

  private void buildCategoryVariable(final QuestionCategory questionCategory) {
    variableNameResolver.variableName(question, questionCategory);

    if(questionCategory.hasOpenAnswerDefinition()) {
      OpenAnswerDefinition parent = questionCategory.getOpenAnswerDefinition();
      // Make an iterable on its children. If no child present, make an iterable with a single value.
      Iterable<OpenAnswerDefinition> oads = parent.hasChildOpenAnswerDefinitions() ? parent.getOpenAnswerDefinitions() : ImmutableSet.of(parent);
      for(OpenAnswerDefinition oad : oads) {
        buildOpenAnswerVariable(questionCategory, oad);
      }
    }
  }

  protected void buildOpenAnswerVariable(final QuestionCategory questionCategory, final OpenAnswerDefinition oad) {
    variableNameResolver.variableName(question, questionCategory, oad);
  }

  /**
   * @throws QuestionnaireVariableNameNotUniqueException when non unique variable names are encountered.
   * @throws UnsupportedOperationException when joined categories are encountered.
   */
  public static void ensureQuestionnaireVariableNamesAreUnique(Questionnaire questionnaire) {

    final QuestionnaireUniqueVariableNameResolver variableNameResolver = new QuestionnaireUniqueVariableNameResolver();

    QuestionnaireWalker walker = new QuestionnaireWalker(new IWalkerVisitor() {

      @Override
      public void visit(OpenAnswerDefinition openAnswerDefinition) {
      }

      @Override
      public void visit(Category category) {
      }

      @Override
      public void visit(QuestionCategory questionCategory) {
      }

      @Override
      public void visit(Question question) {
        if(question.getParentQuestion() != null) {
          // We've already visited this question since we handle child questions when visiting the parent question
          return;
        }
        // Sub question and category, but sub questions do not have categories
        if(question.isArrayOfSharedCategories()) {
          new UniqueQuestionnaireElementNameBuilder(question, variableNameResolver).build();
          for(Question subQuestion : question.getQuestions()) {
            // Build a variable for each child question, but without comment and using their parent's categories.
            new UniqueQuestionnaireElementNameBuilder(subQuestion, variableNameResolver).withParentCategories().build();
            // variable names of question and category variable name of parent.
          }
        } else if(question.isArrayOfJoinedCategories()) {
          throw new UnsupportedOperationException("Variables for joined categories is not supported.");
        } else if(question.hasSubQuestions()) {
          new UniqueQuestionnaireElementNameBuilder(question, variableNameResolver).build();
          for(Question subQuestion : question.getQuestions()) {
            new UniqueQuestionnaireElementNameBuilder(subQuestion, variableNameResolver).withCategories().build();
          }
        } else {
          new UniqueQuestionnaireElementNameBuilder(question, variableNameResolver).withCategories().build();
        }
      }

      @Override
      public void visit(Page page) {
      }

      @Override
      public void visit(Section section) {
      }

      @Override
      public void visit(Questionnaire questionnaire1) {
      }

      @Override
      public boolean visiteMore() {
        return true;
      }

    });
    walker.walk(questionnaire);
  }
}
