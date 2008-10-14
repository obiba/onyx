package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.impl;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.IQuestionnairePropertiesWriter;

public class QuestionnairePropertiesOutputStreamWriter implements IQuestionnairePropertiesWriter {

  private Properties language;
  
  private PrintWriter printWriter;
  
  public QuestionnairePropertiesOutputStreamWriter(Properties language, OutputStream outputStream) {
    this.language = language;
    this.printWriter = new PrintWriter(outputStream);
  }

  public void endBloc() {
    printWriter.println();
  }

  public void write(String key, String value) {
    printWriter.println(key + "=" + value);
  }

  public void end() {
    printWriter.flush();
    printWriter.close();
  }

  public Properties getReference() {
    return language;
  }

}
