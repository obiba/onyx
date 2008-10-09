package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

public abstract class AbstractQuestionnaireElementBuilder<T> {
  
  protected T element;
  
  protected Questionnaire questionnaire;
  
  private static final Pattern NAME_PATTERN = Pattern.compile("[a-z,A-Z,0-9,_,.]+");
  
  protected static boolean checkNamePattern(String name) {
    Matcher m = NAME_PATTERN.matcher(name);
    return m.matches();
  }
  
  protected static IllegalArgumentException invalidNamePatternException(String name) {
    return new IllegalArgumentException("Not a valid questionnaire element name: " + name + ". Expected pattern is " + NAME_PATTERN);
  }
  
  public T getElement() {
    return element;
  }
  
  public Questionnaire getQuestionnaire() {
    return questionnaire;
  }
}
