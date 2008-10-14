package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.impl;

import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.IPropertyKeyWriter;

/**
 * Write the questionnaire properties into a properties object.
 * @author Yannick Marcon
 * 
 */
public class PropertiesPropertyKeyWriterImpl implements IPropertyKeyWriter {

  private Properties language;

  private Properties properties = new Properties();

  /**
   * Constructor without properties reference.
   */
  public PropertiesPropertyKeyWriterImpl() {
  }

  /**
   * Constructor with a property reference.
   * @param language
   */
  public PropertiesPropertyKeyWriterImpl(Properties language) {
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

  public void writeComment(String... comments) {
  }

}
