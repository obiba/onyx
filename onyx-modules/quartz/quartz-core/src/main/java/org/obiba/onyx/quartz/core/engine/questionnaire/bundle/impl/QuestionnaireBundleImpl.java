package org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.springframework.context.MessageSource;

public class QuestionnaireBundleImpl implements QuestionnaireBundle {
  //
  // Constants
  //
  
  public static final String RESOURCE_PATH_TEMPLATE =
    "/questionnaires/{questionnaire-name}/{questionnaire-version}/resources/{resource-name}";
  
  //
  // Instance Variables
  //
  
  private Questionnaire questionnaire;

  private MessageSource messageSource;

  private Set<Locale> languages;

  //
  // Constructors
  //

  public QuestionnaireBundleImpl(Questionnaire questionnaire, MessageSource messageSource, Set<Locale> languages) {
    if (questionnaire == null) {
      throw new IllegalArgumentException("Null questionnaire");
    }
    
    if (messageSource == null) {
      throw new IllegalArgumentException("Null messageSource");
    }
    
    if (languages == null || languages.isEmpty()) {
      throw new IllegalArgumentException("Null or empty language set");
    }
    
    this.questionnaire = questionnaire;
    this.messageSource = messageSource;
    this.languages = new HashSet<Locale>();
    this.languages.addAll(languages);
  }

  //
  // QuestionnaireBundle Methods
  //

  public String getName() {
    return questionnaire.getName();
  }

  public Questionnaire getQuestionnaire() {
    return questionnaire;
  }

  public String getResourcePath(String name) {
    StringBuffer resourcePath = new StringBuffer();
    
    resourcePath.append("/questionnaires/");
    resourcePath.append(getName());
    resourcePath.append(getQuestionnaire().getVersion());
    resourcePath.append("/resources/");
    resourcePath.append(name);
    
    return resourcePath.toString(); 
  }

  public MessageSource getMessageSource() {
    return messageSource;
  }

  public Set<Locale> getAvailableLanguages() {
    return Collections.unmodifiableSet(languages);
  }
  
  //
  // Methods
  //
  
  @Override
  public int hashCode() {
    return getName().hashCode();
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof QuestionnaireBundle) {
      return ((QuestionnaireBundle)o).getName().equals(getName());
    }
    
    return false;
  }
}