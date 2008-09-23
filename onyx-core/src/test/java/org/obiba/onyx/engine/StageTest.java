package org.obiba.onyx.engine;

import static org.easymock.EasyMock.*;

import java.util.Locale;

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.service.UserSessionService;

public class StageTest {

  private MessageCapableApplicationContextMock applicationContextMock;
  
  private UserSessionService userSessionServiceMock;
  
  @Before
  public void setUp() {
    applicationContextMock = new MessageCapableApplicationContextMock();
    
    userSessionServiceMock = createMock(UserSessionService.class);
    applicationContextMock.putBean("userSessionService", userSessionServiceMock);
  }
  
  /**
   * Tests that the returned stage description is localized.
   */
  @Test
  public void testGetDescription() {
    Stage heightMeasurementStage = new Stage();
    heightMeasurementStage.setApplicationContext(applicationContextMock);
    heightMeasurementStage.setUserSessionService(userSessionServiceMock);
    
    String unLocalizedStageDescription = "Height_Measurement";
    heightMeasurementStage.setDescription(unLocalizedStageDescription);

    //
    // Test in English locale ("en").
    //
    Locale englishLocale = new Locale("en");
    
    String expectedEnglishLocalizedStageDescription = "Height Measurement";
    applicationContextMock.setMessage(expectedEnglishLocalizedStageDescription);

    expect(userSessionServiceMock.getLocale()).andReturn(englishLocale);
    
    replay(userSessionServiceMock);
    
    String actualEnglishLocalizedStageDescription = heightMeasurementStage.getDescription();
    
    verify(userSessionServiceMock);
    
    Assert.assertEquals(expectedEnglishLocalizedStageDescription, actualEnglishLocalizedStageDescription);
    
    // Reset userSessionServiceMock (otherwise in the next part of the test the calls to getLocale()
    // won't match expectations).
    reset(userSessionServiceMock);
    
    //
    // Test in French locale ("fr").
    //
    Locale frenchLocale = new Locale("fr");
    
    //TODO: Substitute true French translation when it is added to the resource bundle
    String expectedFrenchLocalizedStageDescription = "fr:Height Measurement";
    applicationContextMock.setMessage(expectedFrenchLocalizedStageDescription);

    expect(userSessionServiceMock.getLocale()).andReturn(frenchLocale);
    
    replay(userSessionServiceMock);
    
    String actualFrenchLocalizedStageDescription = heightMeasurementStage.getDescription();
    
    verify(userSessionServiceMock);
    
    Assert.assertEquals(expectedFrenchLocalizedStageDescription, actualFrenchLocalizedStageDescription);    
  }
  
  class MessageCapableApplicationContextMock extends ApplicationContextMock {
    private static final long serialVersionUID = 1L;

    private String message;
    
    // Need to override this method since the default implementation
    // throws an UnsupportedOperationException!
    public String getMessage(String code, Object[] args, Locale locale) {
      return message;
    }
    
    // This method is used to control the return value of getMessage.
    public void setMessage(String message) {
      this.message = message;
    }
  }
}
