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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.obiba.core.util.FileUtil;
import org.obiba.onyx.quartz.core.engine.questionnaire.QuestionnaireVariableNameResolver;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl.QuestionnaireBundleManagerImpl;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Utility class for creating and updating questionnaires on file system.
 */
public class QuestionnaireCreator {

  private File bundleRootDirectory = new File("target", "questionnaires");

  private File bundleSourceDirectory = new File("src" + File.separatorChar + "main" + File.separatorChar + "webapp" + File.separatorChar + "WEB-INF" + File.separatorChar + "config" + File.separatorChar + "quartz" + File.separatorChar + "resources", "questionnaires");

  private QuestionnaireVariableNameResolver variableNameResolver;

  public QuestionnaireCreator() throws IOException {
    super();
    initialize();
  }

  public QuestionnaireCreator(File workingDir) throws IOException {
    super();
    if(workingDir != null) {
      bundleRootDirectory = new File(workingDir, bundleRootDirectory.getPath());
      bundleSourceDirectory = new File(workingDir, bundleSourceDirectory.getPath());
    }
    initialize();
  }

  public QuestionnaireCreator(File bundleRootDirectory, File bundleSourceDirectory) throws IOException {
    super();
    this.bundleRootDirectory = bundleRootDirectory;
    this.bundleSourceDirectory = bundleSourceDirectory;
    initialize();
  }

  private void initialize() throws IOException {
    if(bundleSourceDirectory.exists()) {
      FileUtil.copyDirectory(bundleSourceDirectory, bundleRootDirectory);
    }
  }

  /**
   * @throws QuestionnaireVariableNameNotUniqueException when non unique variable names are encountered.
   */
  public void createQuestionnaire(QuestionnaireBuilder builder, Locale... locales) {
    QuestionnaireBundle bundle;

    // Create the bundle manager.
    QuestionnaireBundleManager bundleManager = new QuestionnaireBundleManagerImpl(bundleRootDirectory);
    ((QuestionnaireBundleManagerImpl) bundleManager).setPropertyKeyProvider(builder.getPropertyKeyProvider());
    ((QuestionnaireBundleManagerImpl) bundleManager).setResourceLoader(new PathMatchingResourcePatternResolver());

    // Create the bundle questionnaire.
    Questionnaire questionnaire = builder.getQuestionnaire();
    ensureQuestionnaireVariableNamesAreUnique(questionnaire);
    try {
      bundle = bundleManager.createBundle(questionnaire);
      if(locales != null) {
        for(Locale locale : locales) {
          Properties properties = bundle.getLanguage(locale);

          if(properties != null) {
            bundle.setLanguage(locale, properties);
          } else {
            bundle.setLanguage(locale, new Properties());
          }
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * FIXME (not final) build a questionnaire and values for properties files
   * @param builder
   * @param mapLocaleProperties
   */
  public void createQuestionnaire(QuestionnaireBuilder builder, Map<Locale, Properties> mapLocaleProperties) {
    QuestionnaireBundle bundle;

    // Create the bundle manager.
    QuestionnaireBundleManager bundleManager = new QuestionnaireBundleManagerImpl(bundleRootDirectory);
    ((QuestionnaireBundleManagerImpl) bundleManager).setPropertyKeyProvider(builder.getPropertyKeyProvider());
    ((QuestionnaireBundleManagerImpl) bundleManager).setResourceLoader(new PathMatchingResourcePatternResolver());

    // Create the bundle questionnaire.
    final Questionnaire questionnaire = builder.getQuestionnaire();
    ensureQuestionnaireVariableNamesAreUnique(questionnaire);
    try {
      bundle = bundleManager.createBundle(questionnaire);
      if(mapLocaleProperties != null) {
        Iterable<Locale> localesToDelete = Iterables.filter(bundle.getAvailableLanguages(), new Predicate<Locale>() {

          @Override
          public boolean apply(Locale input) {
            return !questionnaire.getLocales().contains(input);
          }
        });
        for(Locale localeToDelete : localesToDelete) {
          bundle.deleteLanguage(localeToDelete);
        }

        for(Map.Entry<Locale, Properties> entry : mapLocaleProperties.entrySet()) {
          // if(!bundle.getAvailableLanguages().contains(entry.getKey())) {
          bundle.setLanguage(entry.getKey(), entry.getValue());
          // }
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @throws QuestionnaireVariableNameNotUniqueException when non unique variable names are encountered.
   * @throws UnsupportedOperationException when joined categories are encountered.
   */
  private void ensureQuestionnaireVariableNamesAreUnique(Questionnaire questionnaire) {

    variableNameResolver = new QuestionnaireUniqueVariableNameResolver();

    QuestionnaireWalker walker = new QuestionnaireWalker(new IWalkerVisitor() {

      public void visit(OpenAnswerDefinition openAnswerDefinition) {
      }

      public void visit(Category category) {
      }

      public void visit(QuestionCategory questionCategory) {
      }

      public void visit(Question question) {
        if(question.getParentQuestion() != null) {
          // We've already visited this question since we handle child questions when visiting the parent question
          return;
        }
        // Sub question and category, but sub questions do not have categories
        if(question.isArrayOfSharedCategories()) {
          new UniqueQuestionnaireElementNameBuilder(question).build();
          for(Question subQuestion : question.getQuestions()) {
            // Build a variable for each child question, but without comment and using their parent's categories.
            new UniqueQuestionnaireElementNameBuilder(subQuestion).withParentCategories().build();
            // variable names of question and category variable name of parent.
          }
        } else if(question.isArrayOfJoinedCategories()) {
          throw new UnsupportedOperationException("Variables for joined categories is not supported.");
        } else if(question.hasSubQuestions()) {
          new UniqueQuestionnaireElementNameBuilder(question).build();
          for(Question subQuestion : question.getQuestions()) {
            new UniqueQuestionnaireElementNameBuilder(subQuestion).withCategories().build();
          }
        } else {
          new UniqueQuestionnaireElementNameBuilder(question).withCategories().build();
        }
      }

      public void visit(Page page) {
      }

      public void visit(Section section) {
      }

      public void visit(Questionnaire questionnaire) {
      }

      public boolean visiteMore() {
        return true;
      }

    });
    walker.walk(questionnaire);
  }

  /**
   * This builder ensures that Questionnaire Element names are unique. If they are not unique a
   * {@link QuestionnaireVariableNameNotUniqueException} is thrown -- nothing is actually built.
   * @see QuestionnaireUniqueVariableNameResolver
   */
  private class UniqueQuestionnaireElementNameBuilder {

    private Question question;

    private List<QuestionCategory> categories;

    public UniqueQuestionnaireElementNameBuilder(Question question) {
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
  }

}
