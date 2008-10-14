package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.impl;

import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.IQuestionnairePropertiesWriter;

public class QuestionnairePropertiesWriter implements IQuestionnairePropertiesWriter {

  private Properties language;
  
  private Properties properties = new Properties();

  public QuestionnairePropertiesWriter() {
  }
  
  public QuestionnairePropertiesWriter(Properties language) {
    this.language = language;
  }

  public Properties getProperties() {
    return properties;
  }

  public void endBloc() {
  }

  public void write(String key, String value) {
    properties.put(key, value);
  }

  public void end() {
  }

  public Properties getReference() {
    return language;
  }

}
