package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.impl;

import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.IQuestionnairePropertiesWriter;

/**
 * Write the questionnaire properties into a properties object.
 * @author Yannick Marcon
 * 
 */
public class QuestionnairePropertiesWriter implements IQuestionnairePropertiesWriter {

  private Properties language;

  private Properties properties = new Properties();

  /**
   * Constructor without properties reference.
   */
  public QuestionnairePropertiesWriter() {
  }

  /**
   * Constructor with a property reference.
   * @param language
   */
  public QuestionnairePropertiesWriter(Properties language) {
    this.language = language;
  }

  /**
   * Get the questionnaire properties object.
   * @return
   */
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
