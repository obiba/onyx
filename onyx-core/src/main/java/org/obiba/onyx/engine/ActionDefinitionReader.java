/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

/**
 * Deserialises {@code ActionDefinition} instances from XML files using XStream.
 */
public class ActionDefinitionReader {

  private XStream xstream = new XStream();

  private Resource[] resources;

  public ActionDefinitionReader() {
    // Create an alias for the root node
    xstream.alias("actionDefinitions", ArrayList.class);
    // Create an alias for ActionDefinition nodes
    xstream.alias("actionDefinition", ActionDefinition.class);
  }

  public void setResources(Resource[] resources) {
    this.resources = resources;
  }

  @SuppressWarnings("unchecked")
  public List<ActionDefinition> read() throws IOException {
    List<ActionDefinition> definitions = new ArrayList<ActionDefinition>();
    for(int i = 0; i < this.resources.length; i++) {
      Resource resource = this.resources[i];
      if(resource.exists()) {
        definitions.addAll((List<ActionDefinition>) xstream.fromXML(resource.getInputStream()));
      }
    }
    return definitions;
  }
}
