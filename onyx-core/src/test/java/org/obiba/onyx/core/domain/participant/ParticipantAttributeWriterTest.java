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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.util.data.DataType;

import com.thoughtworks.xstream.XStream;

/**
 * Test XStream configuration needed to produce the participant-attribute.xml file.
 */
public class ParticipantAttributeWriterTest {

  XStream xstream;

  ParticipantAttribute doctorFirstName;

  ParticipantAttribute doctorLastName;

  ParticipantAttribute specialNeeds;

  ParticipantAttribute maritalStatus;

  ParticipantAttribute familyMemberFirstName;

  ParticipantAttribute familyMemberLastName;

  Group doctorGroup;

  Group familyMemberGroup;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    xstream = new XStream();
    xstream.alias("attribute", ParticipantAttribute.class);
    doctorGroup = new Group("doctor");
    familyMemberGroup = new Group("familyMember");

    doctorFirstName = createParticipantAttribute("Doctor First Name");
    doctorLastName = createParticipantAttribute("Doctor Last Name");
    doctorGroup.addParticipantAttribute(doctorFirstName);
    doctorGroup.addParticipantAttribute(doctorLastName);

    familyMemberFirstName = createParticipantAttribute("Family Member First Name");
    familyMemberLastName = createParticipantAttribute("Family Member Last Name");
    familyMemberGroup.addParticipantAttribute(familyMemberFirstName);
    familyMemberGroup.addParticipantAttribute(familyMemberLastName);

    specialNeeds = createParticipantAttribute("Special Needs");
    maritalStatus = createParticipantAttribute("Marital Status");
  }

  @Test
  public void testWriteXmlGroupsAndAttributesMixed() {
    List<ParticipantElement> attributeList = new ArrayList<ParticipantElement>();
    xstream.alias("participantAttributes", List.class);
    xstream.alias("group", Group.class);
    xstream.useAttributeFor(Group.class, "name");
    xstream.addImplicitCollection(Group.class, "participantAttributes");

    attributeList.add(doctorGroup);
    attributeList.add(specialNeeds);
    attributeList.add(familyMemberGroup);
    attributeList.add(maritalStatus);

    String xml = xstream.toXML(attributeList);
    System.out.println(xml);
  }

  private ParticipantAttribute createParticipantAttribute(String name) {
    ParticipantAttribute participantAttribute = new ParticipantAttribute();
    participantAttribute.setName(name);
    participantAttribute.setType(DataType.TEXT);
    return participantAttribute;
  }

}
