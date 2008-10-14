package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.impl;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.IPropertyKeyWriter;

/**
 * Write the questionnaire properties into a output stream.
 * @author Yannick Marcon
 * 
 */
public class OutputStreamPropertyKeyWriterImpl implements IPropertyKeyWriter {

  private Properties language;

  private PrintWriter printWriter;

  public OutputStreamPropertyKeyWriterImpl(Properties language, OutputStream outputStream) {
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
