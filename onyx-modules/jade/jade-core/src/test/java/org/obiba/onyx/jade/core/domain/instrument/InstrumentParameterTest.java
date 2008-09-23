package org.obiba.onyx.jade.core.domain.instrument;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;

public class InstrumentParameterTest {

  private ExtendedApplicationContextMock applicationContextMock;
  
  private UserSessionService userSessionServiceMock;
  
  @Before
  public void setUp() {
    applicationContextMock = new ExtendedApplicationContextMock();
    
    userSessionServiceMock = createMock(UserSessionService.class);
    applicationContextMock.putBean("userSessionService", userSessionServiceMock);
  }
    
  /**
   * Tests that the returned instrument parameter description is localized.
   */
  @Test
  public void testGetDescription() {
    // Will test with an InstrumentOutputParameter object, since
    // InstrumentParameter is abstract. Note: InstrumentInputParameter
    // inherits the same implementation of getDescription() so there is
    // no need to repeat the test for that type of object.
    InstrumentOutputParameter outputParam = new InstrumentOutputParameter();
    
    outputParam.setApplicationContext(applicationContextMock);
    outputParam.setUserSessionService(userSessionServiceMock);
    
    String unLocalizedDescription = "First_Height_Measurement";
    outputParam.setDescription(unLocalizedDescription);

    //
    // Test in English locale ("en").
    //
    Locale englishLocale = new Locale("en");
    
    String expectedEnglishLocalizedDescription = "First Height Measurement";
    applicationContextMock.setMessage(expectedEnglishLocalizedDescription);

    expect(userSessionServiceMock.getLocale()).andReturn(englishLocale);
    
    replay(userSessionServiceMock);
    
    String actualEnglishLocalizedDescription = outputParam.getDescription();
    
    verify(userSessionServiceMock);
    
    Assert.assertEquals(expectedEnglishLocalizedDescription, actualEnglishLocalizedDescription);
    
    // Reset userSessionServiceMock (otherwise in the next part of the test the calls to getLocale()
    // won't match expectations).
    reset(userSessionServiceMock);
    
    //
    // Test in French locale ("fr").
    //
    Locale frenchLocale = new Locale("fr");
    
    //TODO: Substitute true French translation when it is added to the resource bundle
    String expectedFrenchLocalizedDescription = "fr:First Height Measurement";
    applicationContextMock.setMessage(expectedFrenchLocalizedDescription);

    expect(userSessionServiceMock.getLocale()).andReturn(frenchLocale);
    
    replay(userSessionServiceMock);
    
    String actualFrenchLocalizedDescription = outputParam.getDescription();
    
    verify(userSessionServiceMock);
    
    Assert.assertEquals(expectedFrenchLocalizedDescription, actualFrenchLocalizedDescription);    
  }
}
