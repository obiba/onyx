/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.wizard;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.domain.parser.IBarcodePartParser;
import org.obiba.onyx.ruby.core.domain.parser.impl.RandomDigitsBarcodePartParser;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;
import org.springframework.context.support.DefaultMessageSourceResolvable;

public class RegisteredParticipantTubeColumnProviderTest {
  //
  // Instance Variables
  //

  private ApplicationContextMock applicationContextMock;

  private TubeRegistrationConfiguration tubeRegistrationConfiguration;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() throws Exception {
    initDomainObjects();
    initApplicationContext();
  }

  //
  // Test Methods
  //

  @Test
  public void testGetFirstBarcodePartColumnIndex() {
    RegisteredParticipantTubeColumnProvider columnProvider = new RegisteredParticipantTubeColumnProvider(tubeRegistrationConfiguration);

    // The index of the first bar code part column should 2 (since the first
    // two columns are the Delete column and the Barcode column).
    Assert.assertEquals(2, columnProvider.getFirstBarcodePartColumnIndex());
  }

  //
  // Helper Methods
  //

  private void initDomainObjects() throws Exception {
    RandomDigitsBarcodePartParser partParser = new RandomDigitsBarcodePartParser();
    partParser.setSize(7);
    partParser.setFormat(".*");
    partParser.setPartTitle(new DefaultMessageSourceResolvable("testPartTitle"));

    List<IBarcodePartParser> partParserList = new ArrayList<IBarcodePartParser>();
    partParserList.add(partParser);

    BarcodeStructure barcodeStructure = new BarcodeStructure();
    barcodeStructure.setParsers(partParserList);

    tubeRegistrationConfiguration = new TubeRegistrationConfiguration();
    tubeRegistrationConfiguration.setBarcodeStructure(barcodeStructure);
  }

  private void initApplicationContext() {
    applicationContextMock = new ExtendedApplicationContextMock();
    applicationContextMock.putBean("tubeRegistrationConfiguration", tubeRegistrationConfiguration);

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(applicationContextMock);

    new WicketTester(application);
  }
}
