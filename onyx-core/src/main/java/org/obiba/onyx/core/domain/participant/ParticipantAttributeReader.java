/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.participant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.wicket.data.validation.converter.DataValidatorConverter;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

public class ParticipantAttributeReader {
  //
  // Instance Variables
  //

  private XStream xstream = new XStream();

  private Resource[] resources;

  //
  // Constructors
  //

  public ParticipantAttributeReader() {
    // Create an alias for the root node.
    xstream.alias("participantAttributes", ArrayList.class);

    // Create an alias for ParticipantAttribute nodes
    xstream.alias("attribute", ParticipantAttribute.class);

    // Use DataValidatorConverter to allow easier aliases for validator nodes
    xstream.registerConverter(new DataValidatorConverter().createAliases(xstream));
  }

  //
  // Methods
  //

  public void setResources(Resource[] resources) {
    this.resources = resources;
  }

  @SuppressWarnings("unchecked")
  public List<ParticipantAttribute> read() throws IOException {
    List<ParticipantAttribute> attributes = new ArrayList<ParticipantAttribute>();

    for(int i = 0; i < this.resources.length; i++) {
      Resource resource = this.resources[i];

      if(resource.exists()) {
        attributes.addAll((List<ParticipantAttribute>) xstream.fromXML(resource.getInputStream()));
      }
    }

    return attributes;
  }
}
