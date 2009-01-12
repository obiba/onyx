/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.util;

import java.io.InputStream;
import java.io.OutputStream;

import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDataSet;
import org.obiba.onyx.util.data.Data;

import com.thoughtworks.xstream.XStream;

/**
 * 
 */
public class VariableStreamer {

  /**
   * The de-serializer.
   */
  private XStream xstream;

  private VariableStreamer() {
    initializeXStream();
  }

  public static String toXML(Variable variable) {
    VariableStreamer streamer = new VariableStreamer();
    return streamer.xstream.toXML(variable);
  }

  public static Variable fromXML(InputStream is) {
    VariableStreamer streamer = new VariableStreamer();
    return (Variable) streamer.xstream.fromXML(is);
  }

  public static void toXML(Variable variable, OutputStream os) {
    VariableStreamer streamer = new VariableStreamer();
    streamer.xstream.toXML(variable, os);
  }

  public static String toXML(VariableData variableData) {
    VariableStreamer streamer = new VariableStreamer();
    return streamer.xstream.toXML(variableData);
  }

  public static void toXML(VariableData variableData, OutputStream os) {
    VariableStreamer streamer = new VariableStreamer();
    streamer.xstream.toXML(variableData, os);
  }

  public static String toXML(VariableDataSet variableDataSet) {
    VariableStreamer streamer = new VariableStreamer();
    return streamer.xstream.toXML(variableDataSet);
  }

  public static void toXML(VariableDataSet variableDataSet, OutputStream os) {
    VariableStreamer streamer = new VariableStreamer();
    streamer.xstream.toXML(variableDataSet, os);
  }

  private void initializeXStream() {
    xstream = new XStream();
    xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
    xstream.processAnnotations(Variable.class);
    xstream.autodetectAnnotations(true);

    xstream.alias("data", Data.class);
    xstream.useAttributeFor(Data.class, "type");
  }
}
