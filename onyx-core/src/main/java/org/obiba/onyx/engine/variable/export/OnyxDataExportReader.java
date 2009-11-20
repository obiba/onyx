/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.filter.ExcludeAllFilter;
import org.obiba.magma.filter.JavaScriptFilter;
import org.obiba.magma.filter.VariableAttributeFilter;
import org.obiba.magma.filter.VariableNameFilter;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

/**
 * Reads the {@link OnyxDataExport} XML configuration file.
 */
public class OnyxDataExportReader {
  private XStream xstream;

  private Resource[] resources;

  public OnyxDataExportReader() {
    xstream = new XStream();
    xstream.alias("destinations", List.class);
    xstream.alias("destination", OnyxDestination.class);
    xstream.alias("excludeAll", ExcludeAllFilter.class);
    xstream.alias("script", JavaScriptFilter.class);
    xstream.alias("variableName", VariableNameFilter.class);
    xstream.alias("variableAttribute", VariableAttributeFilter.class);
    xstream.autodetectAnnotations(true);
  }

  public void setResources(Resource[] resources) {
    this.resources = resources;
  }

  @SuppressWarnings("unchecked")
  public List<OnyxDestination> read() throws IOException {
    List<OnyxDestination> onyxDestinations = new ArrayList<OnyxDestination>();
    for(int i = 0; i < this.resources.length; i++) {
      Resource resource = this.resources[i];

      if(resource.exists()) {
        onyxDestinations.addAll((List<OnyxDestination>) xstream.fromXML(resource.getInputStream()));
      }
    }

    return onyxDestinations;
  }
}
