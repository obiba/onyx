package org.obiba.onyx.jade.engine.state;

import static org.easymock.EasyMock.*;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.Action;
import org.springframework.context.ApplicationContext;

public class JadeSkippedStateTest {
  
  /**
   * Tests that the state's message includes the name of the state followed by the 
   * reason the state was reached, between parentheses.
   */
  @Test
  public void testMessageIncludesReasonIfThereIsOne() {
    String state = "Jade.Skipped";
    String reasonSkipped = "DEFECTIVE_INSTRUMENT";
    Locale locale = new Locale("en");
    
    JadeSkippedState skippedState = new JadeSkippedState();
    
    UserSessionService userSessionServiceMock = createMock(UserSessionService.class);
    skippedState.setUserSessionService(userSessionServiceMock);
    
    expect(userSessionServiceMock.getLocale()).andReturn(locale);
    
    ApplicationContext applicationContextMock = createMock(ApplicationContext.class);
    skippedState.setApplicationContext(applicationContextMock);
    
    expect(applicationContextMock.getMessage(state, null, locale)).andReturn("Skipped");
    expect(applicationContextMock.getMessage(reasonSkipped, null, locale)).andReturn("Defective instrument");

    Action reasonSkippedAction = new Action();
    reasonSkippedAction.setEventReason(reasonSkipped);
    skippedState.setReason(reasonSkippedAction);
    
    replay(userSessionServiceMock);
    replay(applicationContextMock);
    
    String message = skippedState.getMessage();
    
    verify(userSessionServiceMock);
    verify(applicationContextMock);
    
    String expectedMessage = "Skipped (Defective instrument)";
    Assert.assertEquals(expectedMessage, message);
  }
}