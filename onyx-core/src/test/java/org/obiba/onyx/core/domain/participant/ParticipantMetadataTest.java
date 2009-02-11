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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.util.data.DataType;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ParticipantMetadataTest {
  //
  // Instance Variables
  //

  private ParticipantMetadata participantMetadata;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("test-participantMetadata.xml");
    participantMetadata = (ParticipantMetadata) context.getBean("participantMetadata");
  }

  //
  // Test Methods
  //

  @Test
  public void testInitConfig() {
    try {
      participantMetadata.initConfig();
    } catch(IOException ex) {
      fail("initConfig threw an exception");
      ex.printStackTrace();
    }

    // Verify that the attribute list is not null and is of the expected size (3).
    List<ParticipantAttribute> attributes = participantMetadata.getConfiguredAttributes();
    assertNotNull(attributes);
    assertEquals(3, attributes.size());

    // Verify attributes.
    verifyAttribute(attributes.get(0), "AllowedValuesAttribute", DataType.TEXT, true, false, true, false, new String[] { "Value1", "Value2", "Value3" });
    verifyAttribute(attributes.get(1), "IntegerAttribute", DataType.INTEGER, false, true, false, true, null);
    verifyAttribute(attributes.get(2), "DateAttribute", DataType.DATE, true, true, true, true, null);
  }

  @Test
  public void testGetAttributeWithAttributeInMetadata() {
    String[] attributeNames = { "AllowedValuesAttribute", "IntegerAttribute", "DateAttribute" };

    for(String attributeName : attributeNames) {
      ParticipantAttribute attribute = participantMetadata.getConfiguredAttribute(attributeName);
      assertNotNull(attribute);
      assertEquals(attributeName, attribute.getName());
    }
  }

  @Test
  public void testGetAttributeWithAttributeNotInMetadata() {
    ParticipantAttribute attribute = participantMetadata.getConfiguredAttribute("NoSuchAttribute");
    assertNull(attribute);
  }

  //
  // Helper Methods
  //

  private void verifyAttribute(ParticipantAttribute attribute, String name, DataType type, boolean mandatoryAtEnrollment, boolean mandatoryAtReception, boolean editableAtReception, boolean editableAfterReception, String[] expectedAllowedValues) {
    // Verify name.
    assertEquals(name, attribute.getName());

    // Verify data type.
    assertEquals(type, attribute.getType());

    // Verify mandatory/editable settings.
    assertEquals(mandatoryAtEnrollment, attribute.isMandatoryAtEnrollment());
    assertEquals(mandatoryAtReception, attribute.isMandatoryAtReception());
    assertEquals(editableAtReception, attribute.isEditableAtReception());
    assertEquals(editableAfterReception, attribute.isEditableAfterReception());

    // Verify allowed values.
    Set<String> allowedValues = attribute.getAllowedValues();
    assertNotNull(allowedValues);

    if(expectedAllowedValues != null) {
      assertEquals(expectedAllowedValues.length, allowedValues.size());

      for(String expectedAllowedValue : expectedAllowedValues) {
        assertTrue(allowedValues.contains(expectedAllowedValue));
      }
    } else if(expectedAllowedValues == null) {
      assertTrue(allowedValues.isEmpty());
    }
  }
}
