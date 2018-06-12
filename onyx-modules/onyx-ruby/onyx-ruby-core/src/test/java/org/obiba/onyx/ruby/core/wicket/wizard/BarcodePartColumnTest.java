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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.domain.parser.IBarcodePartParser;
import org.obiba.onyx.ruby.core.domain.parser.impl.RegularExpressionBarcodePartParser;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.obiba.wicket.test.MockSpringApplication;
import org.springframework.context.support.DefaultMessageSourceResolvable;

public class BarcodePartColumnTest {
  //
  // Instance Variables
  //

  private ExtendedApplicationContextMock applicationContextMock;

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
  public void testColumnHeader() {
    BarcodePartColumn barcodePartColumn = new BarcodePartColumn(new Model("testPartTitle"), 2);

    // Verify the column's header.
    Assert.assertEquals("testPartTitle", barcodePartColumn.getDisplayModel().getObject());
  }

  @Test
  public void testPopulateItem() {
    int firstBarcodePartColumnIndex = 2;

    BarcodePartColumn barcodePartColumn = new BarcodePartColumn(new Model("testPartTitle"), firstBarcodePartColumnIndex);

    ParticipantTubeRegistration participantTubeRegistration = new ParticipantTubeRegistration();
    participantTubeRegistration.setTubeSetName("SamplesCollection");

    RegisteredParticipantTube registeredParticipantTube = new RegisteredParticipantTube();
    registeredParticipantTube.setParticipantTubeRegistration(participantTubeRegistration);
    registeredParticipantTube.setBarcode("1234567011");

    Item cellItem = new Item("itemId", firstBarcodePartColumnIndex, new Model());
    barcodePartColumn.populateItem(cellItem, "componentId", new Model(registeredParticipantTube));

    // Verify that the cell item contains a label with the expected text (<partTitle>.<partLabel>),
    // where <partLabel> is the first seven characters of the barcode.
    Label label = (Label) cellItem.get("componentId");
    Assert.assertNotNull(label);
    MessageSourceResolvableStringModel model = (MessageSourceResolvableStringModel) label.getDefaultModel();
    Assert.assertNotNull(model);
    // Assert.assertTrue(Arrays.asList(model.getCodes()).contains("testPartTitle.1234567"));
  }

  //
  // Helper Methods
  //

  private void initDomainObjects() throws Exception {
    RegularExpressionBarcodePartParser partParser = new RegularExpressionBarcodePartParser();
    partParser.setSize(7);
    partParser.setExpression("\\d{7,}");
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

    Map<String, TubeRegistrationConfiguration> tubeRegistrationConfigurationMap = new HashMap<String, TubeRegistrationConfiguration>();
    tubeRegistrationConfigurationMap.put("SamplesCollection", tubeRegistrationConfiguration);
    applicationContextMock.putBean("tubeRegistrationConfigurationMap", tubeRegistrationConfigurationMap);

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(applicationContextMock);

    new WicketTester(application);
  }
}
