package org.obiba.onyx.quartz.core.engine.questionnaire.util;

public interface IQuestionnairePropertiesWriter {

  public boolean contains(String key);
  
  public void write(String key, String value);
  
  public void endBloc();
  
}
