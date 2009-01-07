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

import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
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

  public static String toXML(VariableData variableData) {
    VariableStreamer streamer = new VariableStreamer();
    return streamer.xstream.toXML(variableData);
  }

  private void initializeXStream() {
    xstream = new XStream();
    xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
    xstream.autodetectAnnotations(true);

    xstream.alias("data", Data.class);
    xstream.useAttributeFor(Data.class, "type");
  }
}
