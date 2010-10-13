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

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
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

  /**
   * Check that the given name respects the naming pattern.
   * @param name
   * @return
   */
  protected static boolean checkNamePattern(String name) {
    Matcher m = NAME_PATTERN.matcher(name);
    return m.matches();
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
