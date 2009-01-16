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
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDataSet;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

import com.thoughtworks.xstream.XStream;

/**
 * 
 */
public class VariableStreamer {

  private static final Logger log = LoggerFactory.getLogger(VariableStreamer.class);

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
    return setParentInstance((Variable) streamer.xstream.fromXML(is));
  }

  /**
   * Reconstructs the parent instance from a list of children.
   * @param parent
   * @return
   */
  private static Variable setParentInstance(Variable parent) {
    // Relink the parent instance
    if(parent.getVariables() != null) {
      for(Variable child : parent.getVariables()) {
        child.setParent(parent);
        setParentInstance(child);
      }
    }
    return parent;
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

  public static void toCSV(Variable variable, OutputStream os, IVariablePathNamingStrategy variablePathNamingStrategy) {
    csvWrite(new CSVWriter(new OutputStreamWriter(os)), variable, variablePathNamingStrategy);
  }

  public static String toCSV(Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    StringWriter writer = new StringWriter();
    csvWrite(new CSVWriter(writer), variable, variablePathNamingStrategy);
    return writer.toString();
  }

  private static void csvWrite(CSVWriter writer, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    if(variable.getDataType() != null) {
      String[] nextLine = new String[7];
      log.info(variable.toString());
      nextLine[0] = variablePathNamingStrategy.getPath(variable);
      nextLine[1] = variable.getName();
      nextLine[2] = variable.getKey();
      nextLine[3] = variable.getReferences().toString();
      nextLine[4] = variable.getCategories().toString();
      nextLine[5] = variable.getDataType().toString();
      nextLine[6] = variable.getUnit();
      writer.writeNext(nextLine);
    }
    for(Variable child : variable.getVariables()) {
      csvWrite(writer, child, variablePathNamingStrategy);
    }
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
