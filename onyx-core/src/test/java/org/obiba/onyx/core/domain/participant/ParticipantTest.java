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

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;

public class ParticipantTest {

  @Test
  public void testSetAndGetConfiguredAttributeValueDecimal() {
    Participant participant = new Participant();
    String attributeName = "decimalAttribute";

    Data attributeValue = DataBuilder.buildDecimal(1.5);
    setGetAndVerifyConfiguredAttributeValue(participant, attributeName, attributeValue);

    attributeValue = DataBuilder.buildDecimal(2.5);
    setGetAndVerifyConfiguredAttributeValue(participant, attributeName, attributeValue);
  }

  @Test
  public void testSetAndGetConfiguredAttributeValueInteger() {
    Participant participant = new Participant();
    String attributeName = "integerAttribute";

    Data attributeValue = DataBuilder.buildInteger(1l);
    setGetAndVerifyConfiguredAttributeValue(participant, attributeName, attributeValue);

    attributeValue = DataBuilder.buildInteger(2l);
    setGetAndVerifyConfiguredAttributeValue(participant, attributeName, attributeValue);
  }

  @Test
  public void testSetAndGetConfiguredAttributeValueDate() {
    Participant participant = new Participant();
    String attributeName = "dataAttribute";

    Data attributeValue = DataBuilder.buildDate(new Date());
    setGetAndVerifyConfiguredAttributeValue(participant, attributeName, attributeValue);

    attributeValue = DataBuilder.buildDate(new Date());
    setGetAndVerifyConfiguredAttributeValue(participant, attributeName, attributeValue);
  }

  @Test
  public void testSetAndGetConfiguredAttributeValueText() {
    Participant participant = new Participant();
    String attributeName = "textAttribute";

    Data attributeValue = DataBuilder.buildText("514-488-8888");
    setGetAndVerifyConfiguredAttributeValue(participant, attributeName, attributeValue);

    attributeValue = DataBuilder.buildText("450-466-6666");
    setGetAndVerifyConfiguredAttributeValue(participant, attributeName, attributeValue);
  }

  //
  // Helper Methods
  //

  /**
   * Sets, gets and verifies the value of configured participant attribute.
   * 
   * @param participant participant
   * @param attributeName attribute name
   * @param attributeValue attribute value
   */
  private void setGetAndVerifyConfiguredAttributeValue(Participant participant, String attributeName, Data attributeValue) {
    // Set the value of the configured attribute.
    participant.setConfiguredAttributeValue(attributeName, attributeValue);

    // Get the value and verify that it matches the value set.
    Data data = participant.getConfiguredAttributeValue(attributeName);
    Assert.assertNotNull(data);
    Assert.assertEquals(attributeValue.getValue(), data.getValue());
  }
}
