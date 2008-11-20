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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

public class ValidationPanelTest {
  //
  // Instance Variables
  //  

  private ExtendedApplicationContextMock applicationContextMock;

  private ActiveTubeRegistrationService activeTubeRegistrationServiceMock;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() throws Exception {
    initApplicationContext();
  }

  //
  // Test Methods
  //

  @Test
  public void testGetRegisteredTubeCountLabelText() {
    int registeredTubeCount = 3;

    expect(activeTubeRegistrationServiceMock.getRegisteredTubeCount()).andReturn(registeredTubeCount);

    replay(activeTubeRegistrationServiceMock);

    ValidationPanel validationPanel = new ValidationPanel("validation");
    String labelText = validationPanel.getRegisteredTubeCountLabelText();

    verify(activeTubeRegistrationServiceMock);

    // Verify that the label text is correct.
    String expectedLabelText = "Number of tubes collected: " + registeredTubeCount;

    Assert.assertNotNull(labelText);
    Assert.assertEquals(expectedLabelText, labelText);
  }

  @Test
  public void testGetExpectedTubeCountLabelText() {
    int expectedTubeCount = 3;

    expect(activeTubeRegistrationServiceMock.getExpectedTubeCount()).andReturn(expectedTubeCount);

    replay(activeTubeRegistrationServiceMock);

    ValidationPanel validationPanel = new ValidationPanel("validation");
    String labelText = validationPanel.getExpectedTubeCountLabelText();

    verify(activeTubeRegistrationServiceMock);

    // Verify that the label text is correct.
    String expectedLabelText = "Number of tubes expected: " + expectedTubeCount;

    Assert.assertNotNull(labelText);
    Assert.assertEquals(expectedLabelText, labelText);
  }

  //
  // Helper Methods
  //

  private void initApplicationContext() {
    applicationContextMock = new ExtendedApplicationContextMock();

    activeTubeRegistrationServiceMock = createMock(ActiveTubeRegistrationService.class);
    applicationContextMock.putBean("activeTubeRegistrationService", activeTubeRegistrationServiceMock);

    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("config/language-common");
    applicationContextMock.putBean("messageSource", messageSource);

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(applicationContextMock);

    new WicketTester(application);

    // Test in English locale.
    Session.get().setLocale(new Locale("en"));
  }
}
