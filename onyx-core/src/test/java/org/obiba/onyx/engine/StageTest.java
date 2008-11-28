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

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.springframework.context.MessageSourceResolvable;

public class StageTest {

  private ExtendedApplicationContextMock applicationContextMock;

  @Before
  public void setUp() {
    applicationContextMock = new ExtendedApplicationContextMock();
  }

  /**
   * Tests that the returned stage description is localized.
   */
  @Test
  public void testGetDescription() {
    Stage heightMeasurementStage = new Stage();
    heightMeasurementStage.setName("Height_Measurement");

    MessageSourceResolvable resolvableDescription = heightMeasurementStage.getDescription();

    String expectedEnglishLocalizedStageDescription = "Height Measurement";
    applicationContextMock.setMessage(expectedEnglishLocalizedStageDescription);

    String actualEnglishLocalizedStageDescription = applicationContextMock.getMessage(resolvableDescription, Locale.ENGLISH);
    Assert.assertEquals(expectedEnglishLocalizedStageDescription, actualEnglishLocalizedStageDescription);

    // TODO: Substitute true French translation when it is added to the resource bundle
    String expectedFrenchLocalizedStageDescription = "fr:Height Measurement";
    applicationContextMock.setMessage(expectedFrenchLocalizedStageDescription);
    String actualFrenchLocalizedStageDescription = applicationContextMock.getMessage(resolvableDescription, Locale.FRENCH);

    Assert.assertEquals(expectedFrenchLocalizedStageDescription, actualFrenchLocalizedStageDescription);
  }
}
