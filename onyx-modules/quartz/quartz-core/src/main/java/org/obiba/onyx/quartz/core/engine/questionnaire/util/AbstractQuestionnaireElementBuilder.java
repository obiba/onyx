package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

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
  private static final Pattern NAME_PATTERN = Pattern.compile("[a-z,A-Z,0-9,_]+");
  
  /**
   * The current questionnaire element.
   */
  protected T element;
  
  /**
   * The questionnaire we are dealing with.
   */
  protected Questionnaire questionnaire;

  /**
   * Constructor with a given questionnaire.
   * @param questionnaire
   */
  public AbstractQuestionnaireElementBuilder(Questionnaire questionnaire) {
    super();
    this.questionnaire = questionnaire;
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
   * Build an exception if an element cannot be found in the questionnaire from its name. 
   * @param elementClass
   * @param name
   * @return
   */
  @SuppressWarnings("unchecked")
  protected static IllegalStateException invalidElementNameException(Class elementClass, String name) {
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


  
}
