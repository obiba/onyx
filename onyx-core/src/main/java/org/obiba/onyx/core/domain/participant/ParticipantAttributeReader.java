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
import java.util.concurrent.LinkedBlockingQueue;

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
    xstream.alias("participantAttributes", List.class);

    // Create an alias for ParticipantAttribute nodes
    xstream.alias("attribute", ParticipantAttribute.class);

    xstream.alias("group", Group.class);

    // Put name in an attribute, instead of an element.
    xstream.useAttributeFor(Group.class, "name");

    // Removes the redundant <participantAttributes> child element from <group>.
    xstream.addImplicitCollection(Group.class, "participantAttributes");

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
    List<ParticipantElement> participantElements = new ArrayList<ParticipantElement>();

    for(int i = 0; i < this.resources.length; i++) {
      Resource resource = this.resources[i];

      if(resource.exists()) {
        participantElements.addAll((List<ParticipantElement>) xstream.fromXML(resource.getInputStream()));
      }
    }

    return flatten(participantElements);
  }

  /**
   * Flattens a list of attributes and groups of attributes into a list of attributes. Attributes that were children of
   * a group set a member to refer to the {@code Group} object. {@code Group} objects are set to refer to all their
   * child attributes.
   * @param participantElements List of attributes and groups of attributes.
   * @return List of attributes. Attributes that were part of a group will contain a reference to that group.
   */
  private List<ParticipantAttribute> flatten(List<ParticipantElement> participantElements) {

    Group defaultGroup = new Group(Group.DEFAULT_GROUP_NAME);
    List<ParticipantAttribute> attributeList = new ArrayList<ParticipantAttribute>();
    for(ParticipantElement participantElement : participantElements) {
      doFlatten(participantElement, defaultGroup, attributeList);
    }
    return attributeList;
  }

  private void doFlatten(ParticipantElement participantElement, Group group, List<ParticipantAttribute> attributeList) {
    if(participantElement instanceof ParticipantAttribute) {
      ParticipantAttribute attribute = (ParticipantAttribute) participantElement;
      attribute.setGroup(group);
      if(group.getName().equals(Group.DEFAULT_GROUP_NAME)) {
        // Add attributes without a group to the default group.
        group.addParticipantAttribute(attribute);
      }
      attributeList.add(attribute);
      return;
    } else if(participantElement instanceof Group) {
      Group childGroup = (Group) participantElement;
      LinkedBlockingQueue<ParticipantAttribute> childElements = new LinkedBlockingQueue<ParticipantAttribute>(childGroup.getParticipantAttributes());
      for(ParticipantElement childElement : childElements) {
        doFlatten(childElement, childGroup, attributeList);
      }
    }
  }

}
