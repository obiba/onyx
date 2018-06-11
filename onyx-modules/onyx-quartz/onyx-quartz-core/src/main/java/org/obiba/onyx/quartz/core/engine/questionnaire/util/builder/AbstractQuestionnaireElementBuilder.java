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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obiba.magma.Variable;
import org.obiba.magma.type.BooleanType;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.data.QuestionnaireDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.QuestionnaireVariableNameResolver;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireUniqueVariableNameResolver;
import org.obiba.onyx.quartz.core.wicket.layout.IPageLayoutFactory;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionPanelFactory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultPageLayoutFactory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultQuestionPanelFactory;

/**
 * Base class for defining {@link Questionnaire} element builders.
 * @author Yannick Marcon
 * 
 * @param <T>
 */
public abstract class AbstractQuestionnaireElementBuilder<T> {

  /**
   * Naming pattern for questionnaire elements to be respected.
   */
  protected static final Pattern NAME_PATTERN = Pattern.compile("[a-z,A-Z,0-9,_]+");

  /**
   * The current questionnaire element.
   */
  protected T element;

  /**
   * The questionnaire we are dealing with.
   */
  protected Questionnaire questionnaire;

  /**
   * The default page layout.
   */
  private Class<? extends IPageLayoutFactory> pageLayoutFactoryClass;

  /**
   * The default question panel.
   */
  private Class<? extends IQuestionPanelFactory> questionPanelFactoryClass;

  /**
   * A utility for resolving variable names.
   */
  protected QuestionnaireVariableNameResolver variableNameResolver;

  /**
   * Constructor with a given questionnaire.
   * @param questionnaire
   */
  public AbstractQuestionnaireElementBuilder(Questionnaire questionnaire, Class<? extends IPageLayoutFactory> pageLayoutFactoryClass, Class<? extends IQuestionPanelFactory> questionPanelFactoryClass) {
    super();
    initialize(questionnaire, pageLayoutFactoryClass, questionPanelFactoryClass);
  }

  public AbstractQuestionnaireElementBuilder(AbstractQuestionnaireElementBuilder<?> parent) {
    super();
    if(parent != null) {
      initialize(parent.getQuestionnaire(), parent.getDefaultPageUI(), parent.getDefaultQuestionUI());
    } else {
      initialize(null, null, null);
    }
  }

  @SuppressWarnings("hiding")
  private void initialize(Questionnaire questionnaire, Class<? extends IPageLayoutFactory> pageLayoutFactoryClass, Class<? extends IQuestionPanelFactory> questionPanelFactoryClass) {
    this.variableNameResolver = new QuestionnaireUniqueVariableNameResolver();
    this.questionnaire = questionnaire;
    if(pageLayoutFactoryClass != null) {
      this.pageLayoutFactoryClass = pageLayoutFactoryClass;
    } else {
      this.pageLayoutFactoryClass = DefaultPageLayoutFactory.class;
    }
    if(questionPanelFactoryClass != null) {
      this.questionPanelFactoryClass = questionPanelFactoryClass;
    } else {
      this.questionPanelFactoryClass = DefaultQuestionPanelFactory.class;
    }
  }

  protected void addVariable(Variable variable) {
    Variable var = QuestionnaireFinder.getInstance(questionnaire).findVariable(variable.getName());
    if(var != null) {
      throw invalidNameUnicityException(Variable.class, variable.getName());
    }
    if(!variable.getEntityType().equals("Participant")) {
      throw new IllegalArgumentException("Wrong variable value type: 'Participant' expected, '" + variable.getEntityType() + "' found in variable " + variable.getName());
    }
    if(!variable.hasAttribute("script")) {
      throw new IllegalArgumentException("Missing 'script' attribute in variable: " + variable.getName());
    }
    String script = variable.getAttributeStringValue("script");
    if(script == null || script.trim().length() == 0) {
      throw new IllegalArgumentException("Missing 'script' attribute in variable: " + variable.getName());
    }
    questionnaire.addVariable(variable);
  }

  /**
   * Returns a valid {@link VariableDataSource} for current questionnaire.
   * @param questionName
   * @param categoryName
   * @param openAnswerName
   * @return
   */
  protected VariableDataSource getValidVariableDataSource(String questionName, String categoryName, String openAnswerName) {
    Question question = QuestionnaireFinder.getInstance(questionnaire).findQuestion(questionName);
    QuestionCategory questionCategory = null;
    if(question == null) throw invalidElementNameException(Question.class, questionName);

    if(categoryName != null && !categoryName.equals(QuestionnaireDataSource.ANY_CATEGORY)) {
      if(question.getCategories().size() > 0) {
        questionCategory = question.findQuestionCategory(categoryName);
        if(questionCategory == null) throw invalidElementNameException(Category.class, categoryName);
      } else {
        Question parentQuestion = question.getParentQuestion();
        questionCategory = parentQuestion.findQuestionCategory(categoryName);
        if(questionCategory == null) throw invalidElementNameException(Category.class, categoryName);
      }
    }

    String variableName;
    if(openAnswerName != null) {
      if(questionCategory == null) {
        throw invalidElementNameException(OpenAnswerDefinition.class, openAnswerName);
      }

      OpenAnswerDefinition open = questionCategory.getCategory().findOpenAnswerDefinition(openAnswerName);
      if(open == null) {
        throw invalidElementNameException(OpenAnswerDefinition.class, openAnswerName);
      }
      variableName = variableNameResolver.variableName(question, questionCategory, open);
    } else if(questionCategory != null) {
      variableName = variableNameResolver.variableName(question, questionCategory);
    } else {
      // make a boolean derived variable that represents the fact that an answer was made
      variableName = variableNameResolver.variableName(question);
      String conditionVariableName = variableName + "_answered";
      if(!questionnaire.hasVariable(conditionVariableName)) {
        Variable.Builder varBuilder = new Variable.Builder(conditionVariableName, BooleanType.get(), "Participant");
        varBuilder.addAttribute("script", "$('" + variableName + "').isNull().not()");
        questionnaire.addVariable(varBuilder.build());
      }
      variableName = conditionVariableName;
    }

    return new VariableDataSource(questionnaire.getName() + ":" + variableName);
  }

  /**
   * Check that the given name respects the naming pattern.
   * @param name
   * @return
   */
  protected static boolean checkNamePattern(String name) {
    Matcher m = NAME_PATTERN.matcher(name);
    return m.matches();
  }

  protected static VariableDataSource checkVariablePath(VariableDataSource ds) {
    if(ds.getTableName() == null) {
      throw new IllegalArgumentException("Missing table name in variable path: " + ds.getTableName());
    }
    return ds;
  }

  /**
   * Build an exception about the name pattern.
   * @param name
   * @return
   */
  protected static IllegalArgumentException invalidNamePatternException(String name) {
    return new IllegalArgumentException("Not a valid questionnaire element name: " + name + ". Expected pattern is " + NAME_PATTERN);
  }

  /**
   * Build an exception about the name unicity.
   * @param elementClass
   * @param name
   * @return
   */
  protected static IllegalArgumentException invalidNameUnicityException(Class<?> elementClass, String name) {
    return new IllegalArgumentException(elementClass.getSimpleName() + " name must be unique: " + name + ".");
  }

  /**
   * Build an exception if an element cannot be found in the questionnaire from its name.
   * @param elementClass
   * @param name
   * @return
   */
  protected static IllegalStateException invalidElementNameException(Class<?> elementClass, String name) {
    return new IllegalStateException("Unable to find in questionnaire the " + elementClass.getSimpleName() + " with name: " + name + ". Create it first.");
  }

  /**
   * Get the current questionnaire element.
   * @return
   */
  public T getElement() {
    return element;
  }

  /**
   * Get the questionnaire currently build.
   * @return
   */
  public Questionnaire getQuestionnaire() {
    return questionnaire;
  }

  public void setDefaultPageUI(Class<? extends IPageLayoutFactory> pageLayoutFactoryClass) {
    this.pageLayoutFactoryClass = pageLayoutFactoryClass;
  }

  public Class<? extends IPageLayoutFactory> getDefaultPageUI() {
    return pageLayoutFactoryClass;
  }

  public Class<? extends IQuestionPanelFactory> getDefaultQuestionUI() {
    return questionPanelFactoryClass;
  }

  public void setDefaultQuestionUI(Class<? extends IQuestionPanelFactory> questionPanelFactoryClass) {
    this.questionPanelFactoryClass = questionPanelFactoryClass;
  }

}
