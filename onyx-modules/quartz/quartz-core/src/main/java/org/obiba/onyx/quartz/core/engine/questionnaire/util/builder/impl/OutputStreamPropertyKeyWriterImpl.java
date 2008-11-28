/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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

  public void writeComment(String... comments) {
    for (String comment : comments) {
      printWriter.println("## " + comment);
    }
  }

}
