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
 * Reads the {@link OnyxDataPurge} XML configuration file (purge.xml).
 */
public class OnyxDataPurgeReader {
  private XStream xstream;

  private Resource[] resources;

  public OnyxDataPurgeReader() {
    xstream = new XStream();
    xstream.alias("list", List.class);
    xstream.alias("purge", OnyxDataExportDestination.class);
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
  public List<OnyxDataExportDestination> read() throws IOException {
    List<OnyxDataExportDestination> onyxDestinations = new ArrayList<OnyxDataExportDestination>();
    for(int i = 0; i < this.resources.length; i++) {
      Resource resource = this.resources[i];

      if(resource.exists()) {
        onyxDestinations.addAll((List<OnyxDataExportDestination>) xstream.fromXML(resource.getInputStream()));
      }
    }

    return onyxDestinations;
  }
}
